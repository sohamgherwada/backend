import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocketFactory;


public class Server implements Runnable {
    private int port;
    private List<User> clients;
    private SSLServerSocket server;
    private final ServerSocketFactory sslSocketFactory = SSLServerSocketFactory.getDefault();
    private String[] ciphersuites = // ALL THE CIPHER SUITES MINIMUM 256 BIT WE BALLLLLL
            {"TLS_AES_256_GCM_SHA384",
                    "TLS_CHACHA20_POLY1305_SHA256",
                    "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
                    "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
                    "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
                    "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
                    "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                    "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                    "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
                    "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
                    "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
                    "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
                    "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
                    "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
                    "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
                    "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_RSA_WITH_AES_256_GCM_SHA384",
                    "TLS_RSA_WITH_AES_256_CBC_SHA256",
                    "TLS_RSA_WITH_AES_256_CBC_SHA",
                    "TLS_EMPTY_RENEGOTIATION_INFO_SCSV"};

    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.keyStore", "serverkeystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "clienttruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","password");
        new Server(5000).run();
    }

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList<>();

    }

    public void run() {
        System.setProperty("javax.net.ssl.keyStore", "serverkeystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "clienttruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","password");
        try{
            server = (SSLServerSocket) sslSocketFactory.createServerSocket(port);
            server.setEnabledCipherSuites(ciphersuites);
            server.setUseClientMode(false);
        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println("Port " + port + " is now open.");

        while (true) {
            // accepts a new client
            SSLSocket client = null;
            try {
                client = (SSLSocket) server.accept();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
            // get nickname of newUser
            String nickname = null;
            try {
                nickname = (new Scanner( client.getInputStream() )).nextLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("New Client: \"" + nickname + "\"\nHost:" + client.getInetAddress().getHostAddress());

            // create new User
            User newUser = null;
            try {
                newUser = new User(client, nickname);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }

            // add newUser message to list
            this.clients.add(newUser);

            // create a new thread for newUser incoming messages handling
            new Thread(new UserHandler(this, newUser)).start();
        }
    }

    // delete a user from the list
    public void removeUser(User user){
        this.clients.remove(user);
    }

    // send incoming msg to all Users
    public void broadcastMessages(String msg, User userSender) {
        for (User client : this.clients) {
            client.getOutStream().println(
                    userSender.toString() + "<span>: " + msg+"</span>");
        }
    }

    // send list of clients to all Users
    public void broadcastAllUsers(){
        for (User client : this.clients) {
            client.getOutStream().println(this.clients);
        }
    }
}

class UserHandler implements Runnable {

    private Server server;
    private User user;

    public UserHandler(Server server, User user) {
        this.server = server;
        this.user = user;
    }

    public void run() {
        String message;

        // when there is a new message, broadcast to all
        Scanner sc = new Scanner(this.user.getInputStream());
        while (sc.hasNextLine()) {
            long start = System.currentTimeMillis();
            message = sc.nextLine();
            // broadcast the message to all users
            server.broadcastMessages(message+" ("+(System.currentTimeMillis()-start)+"ms)", user);
        }
        // end of Thread
        server.removeUser(user);
        sc.close();
    }
}

class User {
    private PrintStream streamOut;
    private InputStream streamIn;
    private String nickname;
    private String color;

    // constructor
    public User(SSLSocket client, String name) throws IOException {
        this.streamOut = new PrintStream(client.getOutputStream());
        this.streamIn = client.getInputStream();
        this.nickname = name;
        this.color = ColorInt.getColor((int) Math.round(Math.random()));
    }


    // getteur
    public PrintStream getOutStream(){
        return this.streamOut;
    }

    public InputStream getInputStream(){
        return this.streamIn;
    }

    public String getNickname(){
        return this.nickname;
    }

    // print user with his color
    public String toString(){

        return "<u><span style='color:" + this.color
                +"'>" + this.getNickname() + "</span></u>";

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
