package com.logwatch.desktop.view;

import com.logwatch.desktop.api.ApiClient;
import com.logwatch.desktop.model.AlertModel;
import com.logwatch.desktop.model.EventModel;
import com.logwatch.desktop.model.PageResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlertsView {

    private final ApiClient apiClient;
    private final BorderPane root;
    private TableView<AlertModel> table;
    private ComboBox<String> filterSeverity;
    private DatePicker dateFrom, dateTo;
    private Label statusLabel;
    private int currentPage = 0;
    private int totalPages = 0;

    public AlertsView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.root = new BorderPane();
        buildUI();
    }

    public BorderPane getRoot() { return root; }

    private void buildUI() {
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(10));
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

        dateFrom = new DatePicker();
        dateFrom.setPromptText("Desde");
        dateFrom.setPrefWidth(130);
        dateTo = new DatePicker();
        dateTo.setPromptText("Hasta");
        dateTo.setPrefWidth(130);

        filterSeverity = new ComboBox<>();
        filterSeverity.getItems().addAll("", "LOW", "MEDIUM", "HIGH", "CRITICAL");
        filterSeverity.setValue("");
        filterSeverity.setPromptText("Severidad");

        Button btnSearch = new Button("Buscar");
        btnSearch.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSearch.setOnAction(e -> { currentPage = 0; loadData(); });

        Button btnRefresh = new Button("Refrescar");
        btnRefresh.setOnAction(e -> loadData());

        filterBar.getChildren().addAll(
                new Label("Desde:"), dateFrom, new Label("Hasta:"), dateTo,
                new Label("Severidad:"), filterSeverity, btnSearch, btnRefresh);
        root.setTop(filterBar);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        addColumn("ID", "id", 50);
        addColumn("Regla", "ruleName", 180);

        TableColumn<AlertModel, String> sevCol = new TableColumn<>("Severidad");
        sevCol.setCellValueFactory(new PropertyValueFactory<>("severity"));
        sevCol.setPrefWidth(90);
        sevCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    String color = switch (item) {
                        case "CRITICAL" -> "#d32f2f";
                        case "HIGH" -> "#f57c00";
                        case "MEDIUM" -> "#fbc02d";
                        case "LOW" -> "#388e3c";
                        default -> "#000";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        table.getColumns().add(sevCol);

        addColumn("Grupo", "groupValue", 120);
        addColumn("Eventos", "eventCount", 70);
        addColumn("Descripcion", "description", 250);
        addColumn("Creada", "createdAt", 170);

        table.setRowFactory(tv -> {
            TableRow<AlertModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showAlertDetail(row.getItem());
                }
            });
            return row;
        });

        root.setCenter(table);

        statusLabel = new Label("Listo");

        Button btnPrev = new Button("<< Anterior");
        btnPrev.setOnAction(e -> { if (currentPage > 0) { currentPage--; loadData(); } });

        Button btnNext = new Button("Siguiente >>");
        btnNext.setOnAction(e -> { if (currentPage < totalPages - 1) { currentPage++; loadData(); } });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottomBar = new HBox(10, statusLabel, spacer, btnPrev, btnNext);
        bottomBar.setPadding(new Insets(8));
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        root.setBottom(bottomBar);
    }

    private void addColumn(String title, String property, double width) {
        TableColumn<AlertModel, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        table.getColumns().add(col);
    }

    public void loadData() {
        statusLabel.setText("Cargando alertas...");
        new Thread(() -> {
            try {
                Map<String, String> params = new LinkedHashMap<>();
                if (dateFrom.getValue() != null) {
                    params.put("from", dateFrom.getValue().atStartOfDay(ZoneId.systemDefault())
                            .toInstant().toString());
                }
                if (dateTo.getValue() != null) {
                    params.put("to", dateTo.getValue().plusDays(1).atStartOfDay(ZoneId.systemDefault())
                            .toInstant().toString());
                }
                if (filterSeverity.getValue() != null && !filterSeverity.getValue().isBlank()) {
                    params.put("severity", filterSeverity.getValue());
                }
                params.put("page", String.valueOf(currentPage));
                params.put("size", "50");

                PageResponse<AlertModel> response = apiClient.getAlerts(params);
                totalPages = response.getTotalPages();

                Platform.runLater(() -> {
                    table.setItems(FXCollections.observableArrayList(response.getContent()));
                    statusLabel.setText(String.format("Página %d de %d (%d alertas totales)",
                            currentPage + 1, Math.max(totalPages, 1), response.getTotalElements()));
                });
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Error: " + ex.getMessage()));
            }
        }).start();
    }

    private void showAlertDetail(AlertModel alert) {
        statusLabel.setText("Cargando detalle...");
        new Thread(() -> {
            try {
                AlertModel detail = apiClient.getAlertWithEvidence(alert.getId());
                Platform.runLater(() -> {
                    statusLabel.setText("Listo");
                    showAlertDialog(detail);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Error: " + ex.getMessage()));
            }
        }).start();
    }

    private void showAlertDialog(AlertModel alert) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de Alerta #" + alert.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(8);
        content.setPadding(new Insets(15));

        content.getChildren().addAll(
                styledLabel("Regla: " + alert.getRuleName(), "-fx-font-size: 14; -fx-font-weight: bold;"),
                new Label("Severidad: " + alert.getSeverity()),
                new Label("Grupo: " + alert.getGroupBy() + " = " + alert.getGroupValue()),
                new Label("Eventos detectados: " + alert.getEventCount()),
                new Label("Ventana: " + formatInstant(alert.getWindowStart())
                        + " - " + formatInstant(alert.getWindowEnd())),
                new Label("Creada: " + formatInstant(alert.getCreatedAt())),
                new Label("Descripcion: " + alert.getDescription())
        );

        if (alert.getEvidenceEvents() != null && !alert.getEvidenceEvents().isEmpty()) {
            content.getChildren().add(new Separator());
            content.getChildren().add(styledLabel("Evidencias (" + alert.getEvidenceEvents().size() + " eventos):",
                    "-fx-font-weight: bold;"));

            TableView<EventModel> evidenceTable = new TableView<>();
            evidenceTable.setPrefHeight(200);

            TableColumn<EventModel, Long> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<EventModel, Instant> tsCol = new TableColumn<>("Timestamp");
            tsCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

            TableColumn<EventModel, String> srcCol = new TableColumn<>("Origen");
            srcCol.setCellValueFactory(new PropertyValueFactory<>("source"));

            TableColumn<EventModel, String> ipCol = new TableColumn<>("IP");
            ipCol.setCellValueFactory(new PropertyValueFactory<>("sourceIp"));

            TableColumn<EventModel, Integer> httpCol = new TableColumn<>("HTTP");
            httpCol.setCellValueFactory(new PropertyValueFactory<>("httpStatus"));

            TableColumn<EventModel, String> msgCol = new TableColumn<>("Mensaje");
            msgCol.setCellValueFactory(new PropertyValueFactory<>("message"));

            evidenceTable.getColumns().addAll(List.of(idCol, tsCol, srcCol, ipCol, httpCol, msgCol));
            evidenceTable.setItems(FXCollections.observableArrayList(alert.getEvidenceEvents()));
            evidenceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

            content.getChildren().add(evidenceTable);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefSize(700, 500);
        dialog.showAndWait();
    }

    private Label styledLabel(String text, String style) {
        Label lbl = new Label(text);
        lbl.setStyle(style);
        return lbl;
    }

    private String formatInstant(Instant instant) {
        if (instant == null) return "-";
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}
