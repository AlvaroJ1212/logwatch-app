package com.logwatch.desktop.view;

import com.logwatch.desktop.api.ApiClient;
import com.logwatch.desktop.model.RuleModel;
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
import java.util.List;
import java.util.Optional;

public class RulesView {

    private final ApiClient apiClient;
    private final BorderPane root;
    private TableView<RuleModel> table;
    private Label statusLabel;

    public RulesView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.root = new BorderPane();
        buildUI();
    }

    public BorderPane getRoot() { return root; }

    private void buildUI() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

        Button btnNew = new Button("Nueva Regla");
        btnNew.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        btnNew.setOnAction(e -> showRuleEditor(null));

        Button btnEdit = new Button("Editar");
        btnEdit.setOnAction(e -> {
            RuleModel selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showRuleEditor(selected);
        });

        Button btnToggle = new Button("Activar/Desactivar");
        btnToggle.setOnAction(e -> toggleSelected());

        Button btnRefresh = new Button("Refrescar");
        btnRefresh.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(btnNew, btnEdit, btnToggle, btnRefresh);
        root.setTop(toolbar);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        addColumn("ID", "id", 50);
        addColumn("Nombre", "name", 200);
        addColumn("Severidad", "severity", 100);

        TableColumn<RuleModel, Boolean> enabledCol = new TableColumn<>("Activa");
        enabledCol.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        enabledCol.setPrefWidth(80);
        enabledCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item ? "SI" : "NO");
                    setStyle(item ? "-fx-text-fill: green; -fx-font-weight: bold;"
                            : "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
        table.getColumns().add(enabledCol);

        addColumn("Ultima evaluacion", "lastEvaluatedAt", 170);
        addColumn("Actualizada", "updatedAt", 170);

        table.setRowFactory(tv -> {
            TableRow<RuleModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showRuleDetail(row.getItem());
                }
            });
            return row;
        });

        root.setCenter(table);

        statusLabel = new Label("Listo");
        HBox bottomBar = new HBox(statusLabel);
        bottomBar.setPadding(new Insets(8));
        bottomBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");
        root.setBottom(bottomBar);
    }

    private void addColumn(String title, String property, double width) {
        TableColumn<RuleModel, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        table.getColumns().add(col);
    }

    public void loadData() {
        statusLabel.setText("Cargando reglas...");
        new Thread(() -> {
            try {
                List<RuleModel> rules = apiClient.getRules();
                Platform.runLater(() -> {
                    table.setItems(FXCollections.observableArrayList(rules));
                    statusLabel.setText(rules.size() + " regla(s) encontrada(s)");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Error: " + ex.getMessage()));
            }
        }).start();
    }

    private void showRuleEditor(RuleModel existing) {
        Dialog<RuleModel> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nueva Regla" : "Editar Regla: " + existing.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        nameField.setPromptText("Nombre de la regla");
        nameField.setPrefWidth(300);

        ComboBox<String> severityBox = new ComboBox<>();
        severityBox.getItems().addAll("LOW", "MEDIUM", "HIGH", "CRITICAL");
        severityBox.setValue(existing != null ? existing.getSeverity() : "MEDIUM");

        TextArea yamlArea = new TextArea(existing != null ? existing.getDefinitionYaml() : getDefaultYaml());
        yamlArea.setPrefRowCount(12);
        yamlArea.setPrefColumnCount(50);
        yamlArea.setStyle("-fx-font-family: 'Consolas', monospace;");

        CheckBox enabledBox = new CheckBox("Activa");
        enabledBox.setSelected(existing == null || Boolean.TRUE.equals(existing.getEnabled()));

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Severidad:"), 0, 1);
        grid.add(severityBox, 1, 1);
        grid.add(enabledBox, 1, 2);
        grid.add(new Label("Definicion YAML:"), 0, 3);
        grid.add(yamlArea, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(600);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                RuleModel model = new RuleModel();
                model.setName(nameField.getText().trim());
                model.setSeverity(severityBox.getValue());
                model.setDefinitionYaml(yamlArea.getText());
                model.setEnabled(enabledBox.isSelected());
                return model;
            }
            return null;
        });

        Optional<RuleModel> result = dialog.showAndWait();
        result.ifPresent(model -> {
            new Thread(() -> {
                try {
                    if (existing == null) {
                        apiClient.createRule(model);
                    } else {
                        apiClient.updateRule(existing.getId(), model);
                    }
                    Platform.runLater(() -> {
                        statusLabel.setText("Regla guardada correctamente");
                        loadData();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> statusLabel.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });
    }

    private void showRuleDetail(RuleModel rule) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de Regla: " + rule.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        content.getChildren().addAll(
                new Label("ID: " + rule.getId()),
                new Label("Nombre: " + rule.getName()),
                new Label("Severidad: " + rule.getSeverity()),
                new Label("Activa: " + (Boolean.TRUE.equals(rule.getEnabled()) ? "Si" : "No")),
                new Label("Ultima evaluacion: " + formatInstant(rule.getLastEvaluatedAt())),
                new Label("Actualizada: " + formatInstant(rule.getUpdatedAt())),
                new Separator(),
                new Label("Definicion YAML:")
        );

        TextArea yamlArea = new TextArea(rule.getDefinitionYaml());
        yamlArea.setEditable(false);
        yamlArea.setPrefRowCount(10);
        yamlArea.setStyle("-fx-font-family: 'Consolas', monospace;");
        content.getChildren().add(yamlArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.showAndWait();
    }

    private void toggleSelected() {
        RuleModel selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        new Thread(() -> {
            try {
                apiClient.toggleRule(selected.getId());
                Platform.runLater(() -> {
                    statusLabel.setText("Regla '" + selected.getName() + "' conmutada");
                    loadData();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Error: " + ex.getMessage()));
            }
        }).start();
    }

    private String getDefaultYaml() {
        return """
                match:
                  eventType: AUTH_FAILURE
                  httpStatus: [401, 403]
                groupBy: ip
                windowSeconds: 300
                threshold: 10
                cooldownSeconds: 600
                description: "Descripcion de la regla"
                """;
    }

    private String formatInstant(Instant instant) {
        if (instant == null) return "-";
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}
