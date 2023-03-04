import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

    private int port;
    private ServerSocket server;
    private List<User> clients;

    public Server(int port){
        this.port = port;
        this.clients = new ArrayList<>();
    }
    public static void main(String[] args) throws IOException {
        new Server(5000).run();
    }

    public void run() throws IOException{
        server = new ServerSocket(port);
        System.out.println("Port " + port + " is now open.");

        while(true){
            Socket client = server.accept();

            String nick = ( new Scanner(client.getInputStream() )).nextLine();

            System.out.println("New client: " + nick + "\"\nHost:" + client.getInetAddress().getHostAddress());

            User newUser = new User(client, nick);

            this.clients.add(newUser);

            newUser.getStreamOut().println("<b>User " + newUser +" has connected! </b>");

            new Thread(new UHandle(this, newUser)).start();
        }
    }

    public void remove(User user) {
        this.clients.remove(user);
    }

    public void send(String msg, User sender){
        for(User client: this.clients){
            client.getStreamOut().println(sender.toString() + "<span>: " + msg+"</span>"); // THE SPANS ARE IMPORTANT
        }
    }
}

class UHandle implements Runnable{

    private final Server server;
    private final User user;

    public UHandle(Server server, User user){
        this.server = server;
        this.user = user;
    }

    public void run(){
        String msg;
        Scanner sc = new Scanner(this.user.getStreamIn());
        while(sc.hasNextLine()){ // send message
            msg = sc.nextLine();
            server.send(msg, user);
        }
        server.remove(user); // kill thread
        sc.close();
    }
}

class User{
    private final PrintStream streamOut;
    private InputStream streamIn;
    private final String nick;
    private final String color;

    public User(Socket client, String name) throws IOException{
        this.streamOut = new PrintStream(client.getOutputStream());
        this.streamIn = client.getInputStream();
        this.nick = name;
        this.color = ColorInt.getColor((int) Math.round(Math.random()));
    }
    public PrintStream getStreamOut(){
        return this.streamOut;
    }
    public InputStream getStreamIn(){
        return this.streamIn;
    }
    public String getNick(){
        return this.nick;
    }
    public String toString(){
        return "<u><span style='color:"+ this.color // THE COLORS ARE ESSENTIAL DO NOT
                +"'>" + this.getNick() + "</span></u>";
    }
}

class ColorInt {
    public static String[] mColors = {
            "#3079ab", // dark blue
            "#e15258", // red
            "#f9845b", // orange
            "#7d669e", // purple
            "#53bbb4", // aqua
            "#51b46d", // green
            "#e0ab18", // mustard
            "#f092b0", // pink
            "#e8d174", // yellow
            "#e39e54", // orange
            "#d64d4d", // red
            "#4d7358", // green
    };

    public static String getColor(int i) {
        return mColors[i % mColors.length];
    }
}
