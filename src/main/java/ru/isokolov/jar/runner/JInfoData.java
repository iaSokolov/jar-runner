package ru.isokolov.jar.runner;

import javafx.beans.property.SimpleStringProperty;

public class JInfoData {

    private final SimpleStringProperty name;
    private final SimpleStringProperty value;

    public JInfoData(String name, String value) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }
}
