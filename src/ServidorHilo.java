import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ServidorHilo
 */
public class ServidorHilo extends Thread {
    private Socket socket;
    private CopyOnWriteArrayList<String> messages;

    public ServidorHilo(Socket socket, CopyOnWriteArrayList<String> messages) {
        this.socket = socket;
        this.messages = messages;
    }

    public void run() {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());

            System.out.println("Conexión recibida desde " + socket.getInetAddress());
            oos.writeObject("¿Cual es su usuario?");
            String username = (String) ois.readObject();
            this.saludo(username, oos, ois);

            String currentLocalTime = obtenerTiempoActual();

            boolean finish = false;

            int lastMessage = messages.size() - 1;
            while (!finish) {
                String newMessages = "";
                boolean haveMessages = true;
                while (haveMessages) {
                    if (lastMessage == messages.size() - 1) {
                        haveMessages = false;
                    } else {
                        lastMessage++;
                        newMessages += messages.get(lastMessage);
                    }
                }
                oos.writeObject("Mensaje nuevo: \n" + newMessages);
                String message = (String) ois.readObject();
                if (message.startsWith("message:")) {
                    this.newMessage(message, username, currentLocalTime);
                    oos.writeObject("Mensaje enviado correctamente.");
                } else if (message.equals("bye")) {
                    finish = true;
                    oos.writeObject("Good bye");
                } else {
                    oos.writeObject(
                            "ERROR: Opción inexistente.\n Para enviar un mensaje, siempre debe ir precedido de la cadena \"message:\", en cambio,\n si quiere desconectarse puede escribir \"bye\".");
                }
            }
        } catch (ClassNotFoundException | IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                closeAllObjectStreams(oos, ois, socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Conexión cerrada.");
        }
    }

    public synchronized void newMessage(String message, String username, String currentTimeClient)
            throws InterruptedException {
        messages.add("<" + username + ">" + " [" + currentTimeClient + "] " + "<" + message.substring(8) + ">");
        notifyAll();
    }

    public synchronized String returnMessages() throws InterruptedException {
        String output = "";
        for (String message : messages) {
            output += message + "\n";
        }
        notifyAll();
        if (output == "") {
            return null;
        } else {
            return output;
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

    private void saludo(String username, ObjectOutputStream oos, ObjectInputStream ois) {
        try {
            if (this.returnMessages() == null) {
                oos.writeObject("Welcome, " + username + ".\nNo hay mensajes nuevos...");
            } else {
                oos.writeObject("Welcome, " + username + ".\nChat:\n" + this.returnMessages());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String obtenerTiempoActual() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}