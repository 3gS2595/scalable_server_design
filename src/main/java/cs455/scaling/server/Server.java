package cs455.scaling.server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private int PORT;
    private String HOST;
    private ThreadPoolManager POOL;
    private int BATCH_SIZE;
    private int BATCH_TIME;


    //constructor
    private Server(String[] args) throws IOException {
        //network
        this.PORT = Integer.parseInt(args[0]);
        this.HOST = InetAddress.getLocalHost().getHostName();

        //functionality
        this.POOL = new ThreadPoolManager(Integer.parseInt(args[1]));
        this.BATCH_SIZE = Integer.parseInt(args[2]);
        this.BATCH_TIME = Integer.parseInt(args[3]);
    }

    private void run() throws IOException {
        //opens the selector
        Selector selector = Selector.open();
        //creates the input channel
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(this.HOST, this.PORT));
        serverSocket.configureBlocking(false);
        //Register our channel to the selector
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        //loop on selector
        while (true) {

            //blocks until there is activity
            selector.select();

            //collects available keys
            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            //iterates through collected keys
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                // New connection on SeverSocket
                if (key.isAcceptable()) {
                    register(selector, serverSocket);
                }

                //remove it from our set
                iter.remove();
            }
        }
    }

    private void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        //intake incoming socket from serverSocket
        SocketChannel client = serverSocket.accept();

        //configure new channel and new key for the selector to monitor
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        this.POOL.execute(new Task(client, BATCH_SIZE));
    }

    public static void main(String[] args) throws IOException {
        //creates server object
        Server server = new Server(args);
        server.run();
    }
}
