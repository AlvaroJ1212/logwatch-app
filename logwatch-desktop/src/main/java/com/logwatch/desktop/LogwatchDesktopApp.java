package com.logwatch.desktop;

import com.logwatch.desktop.api.ApiClient;
import com.logwatch.desktop.view.AlertsView;
import com.logwatch.desktop.view.EventsView;
import com.logwatch.desktop.view.RulesView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LogwatchDesktopApp extends Application {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_API_KEY = "logwatch-dev-key";

    private ApiClient apiClient;
    private EventsView eventsView;
    private RulesView rulesView;
    private AlertsView alertsView;

    @Override
    public void start(Stage primaryStage) {
        apiClient = new ApiClient(DEFAULT_BASE_URL, DEFAULT_API_KEY);

        eventsView = new EventsView(apiClient);
        rulesView = new RulesView(apiClient);
        alertsView = new AlertsView(apiClient);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab eventsTab = new Tab("Eventos", eventsView.getRoot());
        Tab rulesTab = new Tab("Reglas", rulesView.getRoot());
        Tab alertsTab = new Tab("Alertas", alertsView.getRoot());

        tabPane.getTabs().addAll(eventsTab, rulesTab, alertsTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == eventsTab) eventsView.loadData();
            else if (newTab == rulesTab) rulesView.loadData();
            else if (newTab == alertsTab) alertsView.loadData();
        });

        VBox header = buildHeader();
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(header);
        mainLayout.setCenter(tabPane);

        Scene scene = new Scene(mainLayout, 1100, 700);
        primaryStage.setTitle("LogWatch - Analisis de Logs y Alertas");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(500);
        primaryStage.show();

        eventsView.loadData();
    }

    private VBox buildHeader() {
        Label title = new Label("LogWatch");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1565C0;");

        Label subtitle = new Label("Sistema de Analisis de Logs y Generacion de Alertas");
        subtitle.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        VBox titleBox = new VBox(2, title, subtitle);

        Button btnConfig = new Button("Configuracion");
        btnConfig.setOnAction(e -> showConfigDialog());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, titleBox, spacer, btnConfig);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #1565C0; -fx-border-width: 0 0 3 0;");

        return new VBox(header);
    }

    private void showConfigDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Configuracion de conexion");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        TextField urlField = new TextField(DEFAULT_BASE_URL);
        urlField.setPrefWidth(300);
        TextField keyField = new TextField(DEFAULT_API_KEY);
        keyField.setPrefWidth(300);

        grid.add(new Label("URL del servidor:"), 0, 0);
        grid.add(urlField, 1, 0);
        grid.add(new Label("API Key:"), 0, 1);
        grid.add(keyField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new String[]{urlField.getText().trim(), keyField.getText().trim()};
            }
            return null;
        });

        dialog.showAndWait().ifPresent(config -> {
            apiClient = new ApiClient(config[0], config[1]);
            eventsView = new EventsView(apiClient);
            rulesView = new RulesView(apiClient);
            alertsView = new AlertsView(apiClient);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
