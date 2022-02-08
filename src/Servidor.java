import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Servidor{
    public static void main(String[] args) {
        
        Socket socket = null;
        ServerSocket server = null;
        CopyOnWriteArrayList<String> messages = new CopyOnWriteArrayList<String>();

        try {
            server = new ServerSocket(8080);
            System.out.println("Server listening on port 8080.");

            while (true) {
                socket = server.accept();
                ServidorHilo hiloServer = new ServidorHilo(socket, messages);
                hiloServer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
