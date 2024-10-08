import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private static List<String> foodt = new ArrayList<>(); // Корзина
    private static JTextArea foodArea;


    private static final String DB_URL = "jdbc:mysql://it.vshp.online:3306/db_d5bcc9";
    private static final String DB_USER = "st_d5bcc9";
    private static final String DB_PASSWORD = "bd2009c8f715";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Доставка еды");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);


        JTabbedPane tabbedPane = new JTabbedPane();


        JPanel buyPanel = new JPanel();
        buyPanel.setLayout(new BorderLayout());


        JPanel dishPanel = new JPanel();
        dishPanel.setLayout(new FlowLayout());


        loadDishesFromDatabase(dishPanel);

        buyPanel.add(dishPanel, BorderLayout.CENTER);


        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));


        JPanel foodtPanel = new JPanel();
        foodArea = new JTextArea(5, 40);
        foodArea.setEditable(false);

        JScrollPane foodtScrollPane = new JScrollPane(foodArea);
        foodtScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        foodtPanel.add(foodtScrollPane);
        centerPanel.add(foodtPanel);


        JPanel buttonPanel = new JPanel();
        JButton buyButton = new JButton("Купить");
        JButton clearButton = new JButton("Очистить корзину");


        buyButton.setPreferredSize(new Dimension(120, 30));
        clearButton.setPreferredSize(new Dimension(120, 30));

        buyButton.addActionListener(e -> {
            if (foodt.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Корзина пуста! Добавьте блюдо перед покупкой.");
            } else {
                String message = "Блюдо(а) готовятся:\n" + String.join(", ", foodt);


                sendToServer(message);


                JOptionPane.showMessageDialog(null, "Вы купили:\n" + String.join("\n", foodt));
                foodt.clear();

                updatefoodtDisplay();
            }
        });

        clearButton.addActionListener(e -> {
            foodt.clear();
            updatefoodtDisplay();
            JOptionPane.showMessageDialog(null, "Корзина очищена.");
        });

        buttonPanel.add(buyButton);
        buttonPanel.add(clearButton);

        centerPanel.add(buttonPanel);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // 10 пикселей отступа сверху и снизу


        buyPanel.add(centerPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Купить", buyPanel);


        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private static void loadDishesFromDatabase(JPanel dishPanel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM dost")) {

            while (rs.next()) {
                String dishName = rs.getString("name");
                JButton dishButton = new JButton(dishName);
                dishButton.addActionListener(e -> {
                    foodt.add(dishName);
                    updatefoodtDisplay();
                });
                dishPanel.add(dishButton);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ошибка загрузки данных: " + e.getMessage());
        }
    }

    private static void updatefoodtDisplay() {
        // Обновление отображения корзины в текстовой области
        foodArea.setText(String.join("\n", foodt));
    }

    private static void sendToServer(String message) {
        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }
}

