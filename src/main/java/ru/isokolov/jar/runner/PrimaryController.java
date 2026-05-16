package ru.isokolov.jar.runner;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PrimaryController {

    private JPanel mainPanel;
    private JTextArea outputArea;
    private JTextArea outputParameter;
    private JButton startButton;
    private JButton stopButton;
    private JLabel pidLabel;
    private JTextField pathTextField;
    private JTable jvmFlagsTable;
    private JTable jvmParamsTable;
    private DefaultTableModel flagsTableModel;
    private DefaultTableModel paramsTableModel;

    public JPanel createPanel() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(800, 600));

        // Создаем меню
        JMenuBar menuBar = createMenuBar();
        mainPanel.add(menuBar, BorderLayout.NORTH);

        // Создаем центральную часть с вкладками
        JTabbedPane tabbedPane = new JTabbedPane();

        // Вкладка "Настройки"
        JPanel settingsPanel = createSettingsPanel();
        tabbedPane.addTab("Настройки", settingsPanel);

        // Вкладка "Параметры"
        JPanel paramsPanel = createParamsPanel();
        tabbedPane.addTab("Параметры", paramsPanel);

        // Вкладка "Журнал"
        JPanel logPanel = createLogPanel();
        tabbedPane.addTab("Журнал", logPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Нижняя панель с PID
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pidLabel = new JLabel("");
        bottomPanel.add(pidLabel);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> System.exit(0));
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");
        editMenu.add(deleteItem);
        menuBar.add(editMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label и поле для пути
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Программа"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        pathTextField = new JTextField("/Users/user/project/spring-petclinic-rest/target/spring-petclinic-rest-3.4.3.jar");
        panel.add(pathTextField, gbc);

        // Кнопки
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startButton = new JButton("Пуск");
        startButton.addActionListener(this::runJar);
        stopButton = new JButton("Стоп");
        stopButton.addActionListener(e -> stopJar());
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createParamsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Таблица флагов
        String[] flagColumns = {"Флаг", "Значение"};
        flagsTableModel = new DefaultTableModel(flagColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jvmFlagsTable = new JTable(flagsTableModel);
        JScrollPane flagsScroll = new JScrollPane(jvmFlagsTable);

        TitledBorder flagsBorder = new TitledBorder("Флаги JVM");
        flagsScroll.setBorder(flagsBorder);

        // Таблица параметров
        String[] paramColumns = {"Параметр", "Значение"};
        paramsTableModel = new DefaultTableModel(paramColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jvmParamsTable = new JTable(paramsTableModel);
        JScrollPane paramsScroll = new JScrollPane(jvmParamsTable);

        TitledBorder paramsBorder = new TitledBorder("Параметры JVM");
        paramsScroll.setBorder(paramsBorder);

        // Текстовая область для вывода
        outputParameter = new JTextArea(5, 30);
        outputParameter.setEditable(false);
        JScrollPane outputParameterScroll = new JScrollPane(outputParameter);

        TitledBorder outputBorder = new TitledBorder("Вывод");
        outputParameterScroll.setBorder(outputBorder);

        // Компоновка
        JPanel centerPanel = new JPanel(new GridLayout(3, 1));
        centerPanel.add(flagsScroll);
        centerPanel.add(paramsScroll);
        centerPanel.add(outputParameterScroll);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void runJar(ActionEvent event) {
        try {
            var process = JarApplicationProcessor.run(pathTextField.getText());
            pidLabel.setText("PID: " + process.pid());
            getJInfo(process.pid());
        } catch (IOException error) {
            outputArea.append("Ошибка запуска процесса: " + error.getMessage() + "\n");
        }

        logOutput(JarApplicationProcessor.getProcess());
    }

    private void stopJar() {
        JarApplicationProcessor.stop();
        pidLabel.setText("");
    }

    private void getJInfo(long pid) {
        ProcessBuilder processBuilder = new ProcessBuilder("jinfo", "-all", String.valueOf(pid));

        try {
            Process jinfoProcess = processBuilder.start();
            logJInfoOutput(jinfoProcess);
        } catch (IOException e) {
            outputArea.append("Ошибка при получении информации с jinfo: " + e.getMessage() + "\n");
        }
    }

    private void logJInfoOutput(Process jinfoProcess) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(jinfoProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseJInfoLine(line);
                    updateParameter(line);
                }
            } catch (IOException e) {
                updateParameter("Ошибка чтения вывода jinfo: " + e.getMessage());
            }
        }).start();
    }

    private void logOutput(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    updateOutput(line);
                }
            } catch (IOException error) {
                updateOutput("Ошибка чтения вывода: " + error.getMessage());
            }
        }).start();
    }

    private void updateOutput(String line) {
        SwingUtilities.invokeLater(() -> outputArea.append(line + "\n"));
    }

    private void updateParameter(String line) {
        SwingUtilities.invokeLater(() -> outputParameter.append(line + "\n"));
    }

    private void parseJInfoLine(String line) {
        if (line.startsWith("-XX")) {
            String[] flags = line.split(" ");
            for (String flag : flags) {
                String[] parts = flag.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    flagsTableModel.addRow(new Object[]{key, value});
                }
            }
        } else {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                paramsTableModel.addRow(new Object[]{key, value});
            }
        }
    }

    private void showAlert(String message) {
        JOptionPane.showMessageDialog(mainPanel, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}
