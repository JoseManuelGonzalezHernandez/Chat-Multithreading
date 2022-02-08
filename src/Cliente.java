import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Cliente extends Thread{
    public static void main(String[] args) {
        
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        Socket socket = null;
        Scanner sc = null;
        try {
            sc = new Scanner(System.in);
            socket = new Socket("127.0.0.1", 8080);

            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            System.out.println((String) ois.readObject());
            
            oos.writeObject(sc.nextLine());

            System.out.println((String) ois.readObject());

            boolean finish = false;
            while (!finish) {
                String newMessages = (String) ois.readObject();
                if (!newMessages.equals("")) {
                    System.out.println(newMessages);
                }
                System.out.println("Introduzca su mensaje...");
                String message = sc.nextLine();
                oos.writeObject(message);

                String messageServer = (String) ois.readObject();
                System.out.println(messageServer);
                if (messageServer.equals("Good bye")) {
                    finish = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeAllObjectStreams(oos, ois, socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void closeAllObjectStreams(ObjectOutputStream oos, ObjectInputStream ois, Socket socket) throws IOException {
        if (oos != null)
            oos.close();
        if (ois != null)
            ois.close();
        if (socket != null)
            socket.close();
    }
}
