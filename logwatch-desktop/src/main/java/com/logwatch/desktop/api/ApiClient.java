package com.logwatch.desktop.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.logwatch.desktop.model.AlertModel;
import com.logwatch.desktop.model.EventModel;
import com.logwatch.desktop.model.PageResponse;
import com.logwatch.desktop.model.RuleModel;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private final String baseUrl;
    private final String apiKey;
    private final ObjectMapper mapper;

    public ApiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // --- Events ---

    public PageResponse<EventModel> getEvents(Map<String, String> params) throws Exception {
        String query = buildQuery(params);
        String json = doGet("/api/events" + query);
        return parsePageResponse(json, EventModel.class);
    }

    public EventModel getEvent(Long id) throws Exception {
        String json = doGet("/api/events/" + id);
        return mapper.readValue(json, EventModel.class);
    }

    public void exportEventsCsv(Map<String, String> params, File target) throws Exception {
        String query = buildQuery(params);
        downloadFile("/api/events/export/csv" + query, target);
    }

    public void exportEventsJson(Map<String, String> params, File target) throws Exception {
        String query = buildQuery(params);
        downloadFile("/api/events/export/json" + query, target);
    }

    // --- Rules ---

    public List<RuleModel> getRules() throws Exception {
        String json = doGet("/api/rules");
        return mapper.readValue(json, new TypeReference<>() {});
    }

    public RuleModel createRule(RuleModel rule) throws Exception {
        String body = mapper.writeValueAsString(rule);
        String json = doPost("/api/rules", body);
        return mapper.readValue(json, RuleModel.class);
    }

    public RuleModel updateRule(Long id, RuleModel rule) throws Exception {
        String body = mapper.writeValueAsString(rule);
        String json = doPut("/api/rules/" + id, body);
        return mapper.readValue(json, RuleModel.class);
    }

    public RuleModel toggleRule(Long id) throws Exception {
        String json = doPatch("/api/rules/" + id + "/toggle");
        return mapper.readValue(json, RuleModel.class);
    }

    // --- Alerts ---

    public PageResponse<AlertModel> getAlerts(Map<String, String> params) throws Exception {
        String query = buildQuery(params);
        String json = doGet("/api/alerts" + query);
        return parsePageResponse(json, AlertModel.class);
    }

    public AlertModel getAlertWithEvidence(Long id) throws Exception {
        String json = doGet("/api/alerts/" + id);
        return mapper.readValue(json, AlertModel.class);
    }

    // --- Import ---

    public String importFile(File file, String format) throws Exception {
        String endpoint = "/api/import/" + format;
        return doMultipartPost(endpoint, file);
    }

    // --- HTTP helpers ---

    private String doGet(String path) throws Exception {
        HttpURLConnection conn = openConnection(path, "GET");
        return readResponse(conn);
    }

    private String doPost(String path, String body) throws Exception {
        HttpURLConnection conn = openConnection(path, "POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private String doPut(String path, String body) throws Exception {
        HttpURLConnection conn = openConnection(path, "PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private String doPatch(String path) throws Exception {
        HttpURLConnection conn = openConnection(path, "POST");
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write("{}".getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private String doMultipartPost(String path, File file) throws Exception {
        String boundary = "----LogWatchBoundary" + System.currentTimeMillis();
        HttpURLConnection conn = openConnection(path, "POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {

            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: application/octet-stream\r\n\r\n");
            writer.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                fis.transferTo(os);
            }
            os.flush();

            writer.append("\r\n--").append(boundary).append("--\r\n");
            writer.flush();
        }

        return readResponse(conn);
    }

    private void downloadFile(String path, File target) throws Exception {
        HttpURLConnection conn = openConnection(path, "GET");
        try (InputStream is = conn.getInputStream();
             FileOutputStream fos = new FileOutputStream(target)) {
            is.transferTo(fos);
        }
    }

    private HttpURLConnection openConnection(String path, String method) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method.equals("PATCH") ? "POST" : method);
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();
        if (is == null) throw new IOException("Sin respuesta del servidor (HTTP " + status + ")");

        String body;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            body = sb.toString();
        }

        if (status >= 400) {
            throw new IOException("HTTP " + status + ": " + body);
        }
        return body;
    }

    private String buildQuery(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("?");
        params.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                if (sb.length() > 1) sb.append("&");
                sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            }
        });
        return sb.length() > 1 ? sb.toString() : "";
    }

    @SuppressWarnings("unchecked")
    private <T> PageResponse<T> parsePageResponse(String json, Class<T> elementType) throws Exception {
        JsonNode root = mapper.readTree(json);
        PageResponse<T> page = new PageResponse<>();
        page.setTotalElements(root.path("totalElements").asLong(0));
        page.setTotalPages(root.path("totalPages").asInt(0));
        page.setNumber(root.path("number").asInt(0));
        page.setSize(root.path("size").asInt(20));

        JsonNode contentNode = root.path("content");
        List<T> content = mapper.readValue(
                contentNode.traverse(),
                mapper.getTypeFactory().constructCollectionType(List.class, elementType));
        page.setContent(content);
        return page;
    }
}
