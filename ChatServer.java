package ChatJava;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Set<PrintWriter> clientWriters = new HashSet<>(); // Set chứa các PrintWriter của các client để gửi tin nhắn tới từng client
    private static List<String> clientNames = new ArrayList<>(); // Danh sách chứa tên của các client đã kết nối
    private static int nextClientId = 1; // Id của client
    private static Set<Integer> availableIds = new TreeSet<>(); // Set chứa các ID của client

    public static void main(String[] args) {
        System.out.println("Chat server is running...");
        
        int port = 12345;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start(); // Chấp nhận kết nối từ client và khởi tạo một luồng xử lý client mới
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket; // Socket để kết nối với client
        private PrintWriter out; // PrintWriter để gửi dữ liệu tới client
        private BufferedReader in; // BufferedReader để nhận dữ liệu từ client
        private String clientName; // Tên của client

        public ClientHandler(Socket socket) {
            this.socket = socket; // Khởi tạo socket
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Khởi tạo BufferedReader để nhận dữ liệu từ client
                out = new PrintWriter(socket.getOutputStream(), true); // Khởi tạo PrintWriter để gửi dữ liệu tới client

                synchronized (clientWriters) {
                    if (!availableIds.isEmpty()) { // Nếu có ID client đã ngắt kết nối và có thể tái sử dụng
                        int id = availableIds.iterator().next(); // Lấy ID đầu tiên trong danh sách
                        availableIds.remove(id); // Xóa ID đã được sử dụng
                        clientName = "client" + id;
                    } else { // Nếu không có ID tái sử dụng
                        clientName = "client" + nextClientId++; // Đặt tên cho client với ID tiếp theo
                    }
                    clientNames.add(clientName); // Thêm tên của client vào danh sách
                    clientWriters.add(out); // Thêm PrintWriter của client vào Set để gửi tin nhắn tới từng client

                    out.println(clientName); // Gửi tên của client tới client

                    // Gửi thông báo hệ thống tới tất cả các client
                    sendSystemMessage(clientName + " has connected.");
                }

                String message;
                while ((message = in.readLine()) != null) { // Lặp để nhận tin nhắn từ client
                    System.out.println(message); // In tin nhắn từ client ra console
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) { // Duyệt qua từng PrintWriter của client trong Set
                            writer.println(message); // Gửi tin nhắn từ client tới tất cả các client khác
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close(); // Đóng kết nối với client
                } catch (IOException e) {
                    e.printStackTrace(); // In ra thông báo lỗi nếu có lỗi khi đóng kết nối
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out); // Xóa PrintWriter của client khỏi Set
                    clientNames.remove(clientName); // Xóa tên của client khỏi danh sách

                    int id = Integer.parseInt(clientName.replace("client", "")); // Lấy ID của client từ tên
                    availableIds.add(id); // Thêm ID vào danh sách có thể tái sử dụng

                    // Gửi thông báo hệ thống tới tất cả các client
                    sendSystemMessage(clientName + " has disconnected.");
                }
            }
        }

        private void sendSystemMessage(String message) {
            for (PrintWriter writer : clientWriters) { // Duyệt qua từng PrintWriter của client trong Set
                writer.println("[SYSTEM] " + message); // Gửi thông báo hệ thống tới tất cả các client
            }
        }
    }
}
