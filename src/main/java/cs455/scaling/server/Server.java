package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private int PORT;
    private String HOST;
    private ThreadPoolManager POOL;

    private Server(String[] args) throws IOException {
        this.PORT = Integer.parseInt(args[0]);
        this.HOST = InetAddress.getLocalHost().getHostName();
        this.POOL = new ThreadPoolManager(Integer.parseInt(args[1]));
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

                // previous connection has data to read
                if (key.isReadable()) {
                    readAndRespond(key);
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
        this.POOL.execute(new Task(client));
    }

    private void readAndRespond(SelectionKey key) throws IOException {
        //creates buffer to read into
        ByteBuffer buffer = ByteBuffer.allocate(256);

        //gets socket from key
        SocketChannel client = (SocketChannel) key.channel();

        //intakes data from socket
        int bytesRead = client.read(buffer);

        if (bytesRead == -1) {
            client.close();
            System.out.println("CLIENT DISCONNECTED");
        } else {
            //returning the message to them
            System.out.println("received: " + new String(buffer.array()));
            //flip the buffer to now write
            buffer.flip();
            client.write(buffer);
            //clear the buffer
            buffer.clear();
        }
    }

    public static void main(String[] args) throws IOException {
        //creates server object
        Server server = new Server(args);
        server.run();
    }
}
