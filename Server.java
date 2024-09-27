import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Server {
    private static final String DB_URL = "jdbc:mysql://it.vshp.online:3306/db_d5bcc9";
    private static final String DB_USER = "st_d5bcc9";
    private static final String DB_PASSWORD = "bd2009c8f715";

    static int net = 0;

    private static JTextArea chatArea;
    private static JTextField chatInput;

    public static void main(String[] args) {

        JFrame frame = new JFrame("Доставка");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(255, 255, 255));
        tabbedPane.setForeground(Color.black);

        // Основная панель для добавления блюда и списка блюд
        JPanel carPanel = new JPanel(new BorderLayout());
        carPanel.setBackground(new Color(35, 35, 35));

        // Внутренние панели
        JPanel addCarPanel = new JPanel(new GridBagLayout());
        addCarPanel.setBackground(new Color(47, 47, 47));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel foodLabel = new JLabel("Название блюд:");
        JTextField carField = new JTextField(15);
        JLabel countLabel = new JLabel("Количество:");
        JTextField countField = new JTextField(5);
        JButton addButton = new JButton("Добавить блюдо");

        setLabelStyle(foodLabel);
        setLabelStyle(countLabel);
        setFieldStyle(carField);
        setFieldStyle(countField);
        setButtonStyle(addButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        addCarPanel.add(foodLabel, gbc);
        gbc.gridx = 1;
        addCarPanel.add(carField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        addCarPanel.add(countLabel, gbc);
        gbc.gridx = 1;
        addCarPanel.add(countField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        addCarPanel.add(addButton, gbc);

        // Панель списка блюд
        JPanel carListPanel = new JPanel(new BorderLayout());
        carListPanel.setBackground(new Color(40, 40, 40));

        JTextArea foodListArea = new JTextArea();
        foodListArea.setEditable(false);
        foodListArea.setBackground(new Color(33, 33, 33));
        foodListArea.setForeground(Color.WHITE);
        foodListArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton refreshButton = new JButton("Обновить список");
        setButtonStyle(refreshButton);

        carListPanel.add(new JScrollPane(foodListArea), BorderLayout.CENTER);
        carListPanel.add(refreshButton, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> {
            foodListArea.setText(getCarListFromDatabase());
        });

        addButton.addActionListener(e -> {
            String foodName = carField.getText();
            int foodCount = Integer.parseInt(countField.getText());
            addCarToDatabase(foodName, foodCount);
            JOptionPane.showMessageDialog(frame, "Блюдо добавлено на продажу.");
            foodListArea.setText(getCarListFromDatabase()); // Обновляем список после добавления
        });


        carPanel.add(addCarPanel, BorderLayout.NORTH);
        carPanel.add(carListPanel, BorderLayout.CENTER);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(255, 255, 255));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(56, 56, 56));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));

        chatInput = new JTextField();
        chatInput.setBackground(new Color(255, 255, 255));
        chatInput.setForeground(Color.black);
        chatInput.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton sendButton = new JButton("Отправить");
        setButtonStyle(sendButton);


        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> {
            String message = chatInput.getText();
            if (!message.isEmpty()) {
                chatArea.append("Сервер: " + message + "\n");
                sendMessageToClient(message);
                chatInput.setText("");
            }
        });


        tabbedPane.addTab("Блюда", carPanel);  // Теперь добавляем в одну вкладку
        tabbedPane.addTab("Чат с клиентом", chatPanel);

        frame.add(tabbedPane);
        frame.setVisible(true);

        new Thread(() -> {
            startServer();
        }).start();
    }

    private static void setLabelStyle(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private static void setFieldStyle(JTextField field) {
        field.setBackground(new Color(255, 255, 255));
        field.setForeground(Color.black);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255)));
    }

    private static void setButtonStyle(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(38, 38, 38));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));
    }

    private static void addCarToDatabase(String carName, int carCount) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO dost (name, count) VALUES (?, ?)")) {

            statement.setString(1, carName);
            statement.setInt(2, carCount);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "Блюдо успешно добавлено.");
            } else {
                JOptionPane.showMessageDialog(null, "Не удалось добавить блюдо.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка базы данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static String getCarListFromDatabase() {
        StringBuilder carList = new StringBuilder();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT name, count FROM dost")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int count = resultSet.getInt("count");
                carList.append(name).append(": ").append(count).append(" Блюд\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carList.toString();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            chatArea.append("Сервер запущен. Ожидание подключения клиента...\n");

            Socket clientSocket = serverSocket.accept();
            chatArea.append("Клиент подключился.\n");

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {
                        chatArea.append("Клиент: " + clientMessage + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessageToClient(String message) {

    }
}
