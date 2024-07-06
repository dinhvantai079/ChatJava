package ChatJava;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JFrame {
    private JTextArea chatArea; // Vùng hiển thị các tin nhắn chat
    private JTextArea systemArea; // Vùng hiển thị các thông báo hệ thống
    private JTextField inputField; // Ô nhập liệu để người dùng gửi tin nhắn
    private JButton sendButton; // Nút gửi tin nhắn
    private PrintWriter out; // Đối tượng để gửi dữ liệu tới server
    private BufferedReader in; // Đối tượng để nhận dữ liệu từ server
    private String clientName; // Tên của client

 public ChatClient(String serverAddress, int port) {
        setTitle("Chat Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        systemArea = new JTextArea();
        systemArea.setEditable(false);
        systemArea.setPreferredSize(new Dimension(200, 0));
        JScrollPane systemScrollPane = new JScrollPane(systemArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(systemScrollPane, BorderLayout.WEST);
        panel.add(chatScrollPane, BorderLayout.CENTER);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.SOUTH);

        add(panel);

        try {
            Socket socket = new Socket(serverAddress, port); // Tạo socket kết nối tới server với địa chỉ và cổng nhất định
            out = new PrintWriter(socket.getOutputStream(), true); // Khởi tạo PrintWriter để gửi dữ liệu tới server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Khởi tạo BufferedReader để nhận dữ liệu từ server

            clientName = in.readLine(); // Đọc tên của client từ server
            setTitle("Chat Client - " + clientName);

            new Thread(() -> { // Tạo một luồng mới để nhận tin nhắn từ server
                String message;
                try {
                    while ((message = in.readLine()) != null) { // Lặp để nhận tin nhắn từ server
                        if (message.startsWith("[SYSTEM]")) {
                            systemArea.append(message + "\n");
                        } else { // Nếu tin nhắn là tin nhắn chat
                            chatArea.append(message + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start(); // Khởi động luồng nhận tin nhắn từ server
        } catch (IOException e) {
            e.printStackTrace(); // In ra thông báo lỗi
        }

        sendButton.addActionListener(e -> sendMessage()); // nút Send
        inputField.addActionListener(e -> sendMessage()); // nút Enter

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (out != null) {
                    out.println("exit");
                }
            }
        });
    }

     // Phương thức để gửi tin nhắn tới server
    private void sendMessage() {
        String message = inputField.getText().trim(); // Lấy nội dung tin nhắn từ ô nhập liệu
        if (!message.isEmpty()) { // Nếu tin nhắn không rỗng
            out.println(clientName + ": " + message); // Gửi tin nhắn
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClient("127.0.0.1", 12345).setVisible(true);
        });
    }
}
