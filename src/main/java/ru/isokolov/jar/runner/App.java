package ru.isokolov.jar.runner;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class App {

    private static JFrame frame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        frame = new JFrame("JAR Runner");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                JarApplicationProcessor.stop();
                System.exit(0);
            }
        });

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Создаем основную панель
        JPanel primaryPanel = createPrimaryPanel();
        mainPanel.add(primaryPanel, "primary");

        frame.add(mainPanel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel createPrimaryPanel() {
        PrimaryController controller = new PrimaryController();
        return controller.createPanel();
    }

    public static void switchToPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }
}