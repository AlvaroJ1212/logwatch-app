module com.logwatch.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.logwatch.desktop to javafx.fxml;
    opens com.logwatch.desktop.model to com.fasterxml.jackson.databind, javafx.base;
    opens com.logwatch.desktop.view to javafx.fxml;

    exports com.logwatch.desktop;
}
