import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;


public class Client implements Runnable {

    private final String host;
    private final int port;
    private final String[] ciphersuites = // ALL THE CIPHER SUITES MINIMUM 256 BIT WE BALLLLLL
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

    public static void main(String[] args) throws IOException {
        new Client("127.0.0.1", 5000).run();
    }

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        // connect client to server
        SSLSocket client = null;
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            client = (SSLSocket) factory.createSocket(host, port);
            client.setEnabledCipherSuites(ciphersuites); // only 256-bit ciphers and above
            client.setUseClientMode(true); // force it to client mode
            System.out.println("Starting handshake...");
            client.startHandshake();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("Client successfully connected to server!");
        // Get Socket output stream (where the client send her msg)
        PrintStream output = null;
        try {
            output = new PrintStream(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // ask for a nickname
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a nickname: ");
        String nickname = sc.nextLine();

        // send nickname to server
        output.println(nickname);

        // create a new thread for server messages handling
        try {
            new Thread(new MHandle(client.getInputStream())).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // read messages from keyboard and send to server
        System.out.println("Messages: \n");

        // while new messages
        while (sc.hasNextLine()) {
            output.println(sc.nextLine());
        }

        // end ctrl D
        output.close();
        sc.close();
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}

class MHandle implements Runnable {

    private InputStream server;

    public MHandle(InputStream server) {
        this.server = server;
    }

    public void run() {
        // receive server messages and print out to screen
        Scanner s = new Scanner(server);
        String tmp = "";
        while (s.hasNextLine()) {
            tmp = s.nextLine();
            if (tmp.charAt(0) == '[') {
                tmp = tmp.substring(1, tmp.length()-1);
                System.out.println(
                        "\nUSERS LIST: " +
                                new ArrayList<String>(Arrays.asList(tmp.split(","))) + "\n"
                );
            }else{
                try {
                    System.out.println("\n" + getTagValue(tmp));
                    // System.out.println(tmp);
                } catch(Exception ignore){}
            }
        }
        s.close();
    }

    // I could use a javax.xml.parsers but the goal of Client.java is to keep everything tight and simple
    public static String getTagValue(String xml){
        return  xml.split(">")[2].split("<")[0] + xml.split("<span>")[1].split("</span>")[0];
    }

}
