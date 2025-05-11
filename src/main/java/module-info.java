module ru.isokolov.jar.runner {
    requires javafx.controls;
    requires javafx.fxml;

    opens ru.isokolov.jar.runner to javafx.fxml;
    exports ru.isokolov.jar.runner;
}
