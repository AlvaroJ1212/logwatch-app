package com.logwatch.desktop.view;

import com.logwatch.desktop.api.ApiClient;
import com.logwatch.desktop.model.EventModel;
import com.logwatch.desktop.model.PageResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventsView {

    private final ApiClient apiClient;
    private final BorderPane root;
    private TableView<EventModel> table;
    private TextField filterUser, filterIp, filterSource, filterEventType, filterSeverity, filterStatus;
    private DatePicker dateFrom, dateTo;
    private Label statusLabel;
    private int currentPage = 0;
    private int totalPages = 0;

    public EventsView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.root = new BorderPane();
        buildUI();
    }

    public BorderPane getRoot() { return root; }

    private void buildUI() {
        VBox filterBox = buildFilters();
        root.setTop(filterBox);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        addColumn("ID", "id", 60);
        addColumn("Timestamp", "timestamp", 170);
        addColumn("Origen", "source", 100);
        addColumn("Tipo", "eventType", 100);
        addColumn("Severidad", "severity", 80);
        addColumn("Usuario", "userName", 100);
        addColumn("IP", "sourceIp", 120);
        addColumn("HTTP", "httpStatus", 60);
        addColumn("Mensaje", "message", 250);

        table.setRowFactory(tv -> {
            TableRow<EventModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEventDetail(row.getItem());
                }
            });
            return row;
        });

        root.setCenter(table);

        HBox bottomBar = buildBottomBar();
        root.setBottom(bottomBar);
    }

    private VBox buildFilters() {
        filterUser = new TextField();
        filterUser.setPromptText("Usuario");
        filterIp = new TextField();
        filterIp.setPromptText("IP");
        filterSource = new TextField();
        filterSource.setPromptText("Origen");
        filterEventType = new TextField();
        filterEventType.setPromptText("Tipo evento");
        filterSeverity = new TextField();
        filterSeverity.setPromptText("Severidad");
        filterStatus = new TextField();
        filterStatus.setPromptText("HTTP Status");

        dateFrom = new DatePicker();
        dateFrom.setPromptText("Desde");
        dateFrom.setPrefWidth(130);
        dateTo = new DatePicker();
        dateTo.setPromptText("Hasta");
        dateTo.setPrefWidth(130);

        Button btnSearch = new Button("Buscar");
        btnSearch.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSearch.setOnAction(e -> { currentPage = 0; loadData(); });

        Button btnClear = new Button("Limpiar");
        btnClear.setOnAction(e -> clearFilters());

        HBox row1 = new HBox(8, new Label("Desde:"), dateFrom, new Label("Hasta:"), dateTo,
                new Label("Usuario:"), filterUser, new Label("IP:"), filterIp);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(8, new Label("Origen:"), filterSource, new Label("Tipo:"), filterEventType,
                new Label("Severidad:"), filterSeverity, new Label("HTTP:"), filterStatus,
                btnSearch, btnClear);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox filterBox = new VBox(6, row1, row2);
        filterBox.setPadding(new Insets(10));
        filterBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        return filterBox;
    }

    private HBox buildBottomBar() {
        statusLabel = new Label("Listo");

        Button btnPrev = new Button("<< Anterior");
        btnPrev.setOnAction(e -> { if (currentPage > 0) { currentPage--; loadData(); } });

        Button btnNext = new Button("Siguiente >>");
        btnNext.setOnAction(e -> { if (currentPage < totalPages - 1) { currentPage++; loadData(); } });

        Button btnExportCsv = new Button("Exportar CSV");
        btnExportCsv.setOnAction(e -> exportData("csv"));

        Button btnExportJson = new Button("Exportar JSON");
        btnExportJson.setOnAction(e -> exportData("json"));

        Button btnImport = new Button("Importar");
        btnImport.setOnAction(e -> importData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(10, statusLabel, spacer, btnImport, btnExportCsv, btnExportJson,
                btnPrev, btnNext);
        bar.setPadding(new Insets(8));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    private void addColumn(String title, String property, double width) {
        TableColumn<EventModel, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        table.getColumns().add(col);
    }

    public void loadData() {
        statusLabel.setText("Cargando...");
        new Thread(() -> {
            try {
                Map<String, String> params = buildParams();
                params.put("page", String.valueOf(currentPage));
                params.put("size", "50");

                PageResponse<EventModel> response = apiClient.getEvents(params);
                totalPages = response.getTotalPages();

                Platform.runLater(() -> {
                    table.setItems(FXCollections.observableArrayList(response.getContent()));
                    statusLabel.setText(String.format("Página %d de %d (%d eventos totales)",
                            currentPage + 1, Math.max(totalPages, 1), response.getTotalElements()));
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        statusLabel.setText("Error: " + ex.getMessage()));
            }
        }).start();
    }

    private void showEventDetail(EventModel event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle del Evento #" + event.getId());
        dialog.setHeaderText("Evento: " + event.getEventType() + " - " + event.getSeverity());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));

        int row = 0;
        addDetailRow(grid, row++, "ID:", String.valueOf(event.getId()));
        addDetailRow(grid, row++, "Timestamp:", formatInstant(event.getTimestamp()));
        addDetailRow(grid, row++, "Origen:", event.getSource());
        addDetailRow(grid, row++, "Tipo:", event.getEventType());
        addDetailRow(grid, row++, "Severidad:", event.getSeverity());
        addDetailRow(grid, row++, "Usuario:", event.getUserName());
        addDetailRow(grid, row++, "IP:", event.getSourceIp());
        addDetailRow(grid, row++, "HTTP Status:", event.getHttpStatus() != null ? String.valueOf(event.getHttpStatus()) : "-");
        addDetailRow(grid, row++, "Mensaje:", event.getMessage());

        if (event.getRawPayload() != null) {
            TextArea rawArea = new TextArea(event.getRawPayload());
            rawArea.setEditable(false);
            rawArea.setPrefRowCount(8);
            rawArea.setPrefColumnCount(50);
            grid.add(new Label("Raw Payload:"), 0, row);
            grid.add(rawArea, 1, row);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold;");
        grid.add(lbl, 0, row);
        grid.add(new Label(value != null ? value : "-"), 1, row);
    }

    private void exportData(String format) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar eventos");
        chooser.setInitialFileName("events." + format);
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;

        new Thread(() -> {
            try {
                Map<String, String> params = buildParams();
                if ("csv".equals(format)) {
                    apiClient.exportEventsCsv(params, file);
                } else {
                    apiClient.exportEventsJson(params, file);
                }
                Platform.runLater(() -> statusLabel.setText("Exportado a: " + file.getName()));
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Error exportando: " + ex.getMessage()));
            }
        }).start();
    }

    private void importData() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importar eventos");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = chooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;

        String format = file.getName().endsWith(".json") ? "json" : "csv";
        new Thread(() -> {
            try {
                String result = apiClient.importFile(file, format);
                Platform.runLater(() -> {
                    statusLabel.setText("Importación completada");
                    showAlert("Resultado de importación", result);
                    loadData();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Error importando: " + ex.getMessage()));
            }
        }).start();
    }

    private Map<String, String> buildParams() {
        Map<String, String> params = new LinkedHashMap<>();
        if (dateFrom.getValue() != null) {
            params.put("from", dateFrom.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant().toString());
        }
        if (dateTo.getValue() != null) {
            params.put("to", dateTo.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toString());
        }
        putIfNotEmpty(params, "user", filterUser.getText());
        putIfNotEmpty(params, "ip", filterIp.getText());
        putIfNotEmpty(params, "source", filterSource.getText());
        putIfNotEmpty(params, "eventType", filterEventType.getText());
        putIfNotEmpty(params, "severity", filterSeverity.getText());
        putIfNotEmpty(params, "status", filterStatus.getText());
        return params;
    }

    private void putIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value.trim());
    }

    private void clearFilters() {
        filterUser.clear(); filterIp.clear(); filterSource.clear();
        filterEventType.clear(); filterSeverity.clear(); filterStatus.clear();
        dateFrom.setValue(null); dateTo.setValue(null);
        currentPage = 0;
        loadData();
    }

    private String formatInstant(Instant instant) {
        if (instant == null) return "-";
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
