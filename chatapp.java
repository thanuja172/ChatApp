import java.io.*;
import java.net.*;
import java.util.*;

// Combined Server and Client Program
public class ChatApp {

    // ====== SERVER PART ======
    static class ChatServer {
        private static final int PORT = 1234;
        private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

        public static void main(String[] args) throws IOException {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);
                new ClientHandler(clientSocket).start();
            }
        }

        static class ClientHandler extends Thread {
            private Socket socket;
            private PrintWriter out;
            private BufferedReader in;

            public ClientHandler(Socket socket) {
                this.socket = socket;
            }

            public void run() {
                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    clientWriters.add(out);

                    String message;
                    while ((message = in.readLine()) != null) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println("User: " + message);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Client disconnected.");
                } finally {
                    try { socket.close(); } catch (IOException e) {}
                    clientWriters.remove(out);
                }
            }
        }
    }

    // ====== CLIENT PART ======
    static class ChatClient {
        public static void main(String[] args) {
            try {
                Socket socket = new Socket("localhost", 1234);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Thread to read messages from server
                new Thread(() -> {
                    String msg;
                    try {
                        while ((msg = serverInput.readLine()) != null) {
                            System.out.println(msg);
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected from server.");
                    }
                }).start();

                // Main thread sends messages
                String input;
                while ((input = userInput.readLine()) != null) {
                    out.println(input);
                    if (input.equalsIgnoreCase("exit")) break;
                }

                socket.close();
            } catch (IOException e) {
                System.out.println("Unable to connect to server.");
            }
        }
    }

    // ====== SELECT TO RUN SERVER OR CLIENT ======
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("1. Run as Server");
        System.out.println("2. Run as Client");
        System.out.print("Enter choice (1 or 2): ");
        int choice = sc.nextInt();
        sc.nextLine(); // consume newline

        if (choice == 1) {
            try {
                ChatServer.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (choice == 2) {
            ChatClient.main(null);
        } else {
            System.out.println("Invalid choice.");
        }
    }
}

