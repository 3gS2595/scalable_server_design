package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private int PORT;
    private String HOST;
    private ThreadPoolManager POOL;

    //constructor
    private Server(String[] args) throws IOException {
        //network
        this.PORT = Integer.parseInt(args[0]);
        this.HOST = InetAddress.getLocalHost().getHostName();

        //functionality
        this.POOL = new ThreadPoolManager(
            Integer.parseInt(args[1]),
            Integer.parseInt(args[2]),
            Integer.parseInt(args[3]));
        this.POOL.start();
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
            //System.out.println("start");
            //blocks until there is activity
            selector.selectNow();
            //collects available keys
            Set<SelectionKey> selectedKeys = selector.selectedKeys();

            //iterates through collected keys
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                // New connection on SeverSocket
                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    register(serverSocket, selector);
                }
                if (key.isReadable()) {
                    register(key);
                }

                iter.remove();
            }
            //System.out.println("stop");
            //System.out.println();
        }

    }

    private void register(SelectionKey key) {
        this.POOL.createTask(key);
    }

    private void register(ServerSocketChannel ServerSocketChannel, Selector selector) {
        this.POOL.createTask(ServerSocketChannel, selector);
    }

    public static void main(String[] args) throws IOException {
        //creates server object
        Server server = new Server(args);
        server.run();
    }
}
