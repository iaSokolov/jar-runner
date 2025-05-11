package ru.isokolov.jar.runner;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;

public class PrimaryController {

    @FXML
    private TextArea outputArea;

    @FXML
    private TextArea outputParamter;

    @FXML
    private Button startButton;

    @FXML
    private Button stopButton;

    @FXML
    private Text pidText;
    
    @FXML
    private TextField pathTextField;

    @FXML
    private TableView<JInfoData> jvmFlagsTableView;
    
    @FXML
    private TableView<JInfoData> jvmParamsTableView;
    
    @FXML
    private TableColumn<JInfoData, String> paramNameColumn; 
    
    @FXML
    private TableColumn<JInfoData, String> paramValueColumn;
    
    @FXML
    private TableColumn<JInfoData, String> flagNameColumn;
    
    @FXML
    private TableColumn<JInfoData, String> flagValueColumn;
    
    private ObservableList<JInfoData> jvmParams;
    private ObservableList<JInfoData> jvmFlags;
    
    @FXML
    public void initialize() {
        jvmParams = FXCollections.observableArrayList(); 
        paramNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        paramValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        jvmParamsTableView.setItems(jvmParams);
        
        jvmFlags = FXCollections.observableArrayList();        
        flagNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        flagValueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());        
        jvmFlagsTableView.setItems(jvmFlags);
    }    

    @FXML
    private void runJar(ActionEvent event) {
        try {
            var process = JarApplicationProcessor.run(pathTextField.getText());
            pidText.setText(String.valueOf(process.pid()));
            getJInfo(process.pid());
        } catch (IOException error) {
            outputArea.appendText("Ошибка запуска процесса: " + error.getMessage() + "\n");
        }

        logOutput(JarApplicationProcessor.getProcess());
    }

    @FXML
    private void stopJar() {
        JarApplicationProcessor.stop();
        pidText.setText("");
    }

    private void getJInfo(long pid) {
        ProcessBuilder processBuilder = new ProcessBuilder("jinfo", "-all", String.valueOf(pid));

        try {
            Process jinfoProcess = processBuilder.start();
            logJInfoOutput(jinfoProcess);
        } catch (IOException e) {
            outputArea.appendText("Ошибка при получении информации с jinfo: " + e.getMessage() + "\n");
        }
    }

    private void logJInfoOutput(Process jinfoProcess) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(jinfoProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseJInfoLine(line);
                    updateParamter(line);
                }
            } catch (IOException e) {
                updateParamter("Ошибка чтения вывода jinfo: " + e.getMessage());
            }
        }).start();
    }

    private void logOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Обновляем TextArea на JavaFX потоке
                    updateOutput(line);
                }
            } catch (IOException error) {
                updateOutput("Ошибка чтения вывода: " + error.getMessage());
            }
        }).start();
    }

    private void updateOutput(String line) {
        javafx.application.Platform.runLater(() -> outputArea.appendText(line + "\n"));
    }

    private void updateParamter(String line) {
        javafx.application.Platform.runLater(() -> outputParamter.appendText(line + "\n"));
    }
    
    private void parseJInfoLine(String line) {
        // Проверяем, содержит ли строка информацию о флагах
        if (line.startsWith("-XX")) {            
            String[] flags = line.split(" ");
            for (int i = 0; i< flags.length; i++) {
                String[] parts = flags[i].split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    jvmFlags.add(new JInfoData(key, value)); // Добавляем данные в таблицу
                }
            }            
            
            // Обновляем таблицу на JavaFX потоке
            javafx.application.Platform.runLater(() -> jvmFlagsTableView.refresh());            
        } else {
            // Обрабатываем обычные строки с ключами и значениями
            String[] parts = line.split("=");
            if (parts.length == 2) { // Если строка содержит ключ и значение
                String key = parts[0].trim();
                String value = parts[1].trim();
                jvmParams.add(new JInfoData(key, value)); // Добавляем данные в таблицу

                // Обновляем таблицу на JavaFX потоке
                javafx.application.Platform.runLater(() -> jvmParamsTableView.refresh());
            }
        }
    }
    
    private void checkView() {
        assert pathTextField != null : "fx:id=\"pathTextField\" was not injected: check your FXML file 'primary.fxml'.";
        assert startButton != null : "fx:id=\"startButton\" was not injected: check your FXML file 'primary.fxml'.";
        assert stopButton != null : "fx:id=\"stopButton\" was not injected: check your FXML file 'primary.fxml'.";
        assert jvmFlagsTableView != null : "fx:id=\"jvmFlagsTableView\" was not injected: check your FXML file 'primary.fxml'.";
        assert flagNameColumn != null : "fx:id=\"flagNameColumn\" was not injected: check your FXML file 'primary.fxml'.";
        assert flagValueColumn != null : "fx:id=\"flagValueColumn\" was not injected: check your FXML file 'primary.fxml'.";
        assert jvmParamsTableView != null : "fx:id=\"jvmParamTableView\" was not injected: check your FXML file 'primary.fxml'.";
        assert paramNameColumn != null : "fx:id=\"paramNameColumn\" was not injected: check your FXML file 'primary.fxml'.";
        assert paramValueColumn != null : "fx:id=\"paramValueColumn\" was not injected: check your FXML file 'primary.fxml'.";
        assert outputParamter != null : "fx:id=\"outputParamter\" was not injected: check your FXML file 'primary.fxml'.";
        assert outputArea != null : "fx:id=\"outputArea\" was not injected: check your FXML file 'primary.fxml'.";
        assert pidText != null : "fx:id=\"pidText\" was not injected: check your FXML file 'primary.fxml'.";
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
