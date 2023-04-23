import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor {

    private static boolean isServerRunning = true;

    public static void main(String[] args) {
        final var recursos = new HashMap<String, String>();

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor rodando na porta 12345...");

            while (isServerRunning) {
                System.out.println("Aguardando conexão do cliente...");
                final var socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket.getInetAddress().getHostAddress());
                final var thread = new Thread(new ClienteHandler(socket, recursos));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            pararServidor();
        }
    }

    public static void pararServidor() {
        isServerRunning = false;
    }
}

class ClienteHandler implements Runnable {
    private final Socket socket;
    private final Map<String, String> recursos;

    public ClienteHandler(Socket socket, Map<String, String> recursos) {
        this.socket = socket;
        this.recursos = recursos;
    }

    @Override
    public void run() {
        try {
            final var objectInputStream = new ObjectInputStream(socket.getInputStream());
            final var recursosRecebidos = (Map<String, String>) objectInputStream.readObject();
            System.out.println("Recursos recebidos do cliente " + socket.getInetAddress().getHostAddress() + ":");
            recursosRecebidos.forEach((key, value) -> System.out.println(key + ": " + value));

            synchronized (recursos) {
                recursos.putAll(recursosRecebidos);
            }

            objectInputStream.close();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
