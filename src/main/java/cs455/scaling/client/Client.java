package cs455.scaling.client;

import cs455.scaling.hash.Hash;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Client {
    private String SERVER_HOST;
    private int SERVER_PORT;
    private int MESSAGE_RATE;
    private static ByteBuffer buffer;
    private SocketChannel socketChannel;
    private Selector selector;
    private LinkedList<String> sent = new LinkedList<>();

    private Client(String[] args){
        this.SERVER_HOST = args[0];
        this.SERVER_PORT = Integer.parseInt(args[1]);
        this.MESSAGE_RATE = Integer.parseInt(args[2]);
        buffer = ByteBuffer.allocate(256);
    }

    private void run() {
        try {
            //opens the selector
            //creates the input channel
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(this.SERVER_HOST, this.SERVER_PORT));
            registerChannel(socketChannel, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);

            //connects to server (arg0=host arg1=port)
            System.out.println("connected");

            //operates functions
            while (true) {
                //blocks until there is activity
                selector.selectNow();
                //collects available keys
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                //iterates through collected keys
                //replace with iterator and while has next if concurrency issues
                for (SelectionKey key : selectedKeys) {
                    confirmConnection(key);

                    if (key.isValid()) {
                        if (key.isReadable()) {
                            System.out.println("reading");
                            socketChannel.read(buffer);
                            buffer.clear();
                            System.out.println(Arrays.toString(buffer.array()));
                            buffer.clear();
                        }
                        byte[] payload = new byte[8];
                        new Random().nextBytes(payload);
                        buffer = ByteBuffer.wrap(payload);
                        sent.add(Hash.SHA1FromBytes(buffer.array()));
                        socketChannel.write(buffer);
                        buffer.clear();

                        Thread.sleep(1000 / MESSAGE_RATE);
                    }
                }
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
    private void confirmConnection(SelectionKey key){
        try {
            if (key.isAcceptable()) {
            } else if (key.isConnectable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                if (channel.isConnectionPending()) {
                    channel.finishConnect();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerChannel(SelectableChannel channel, int ops) {
        try {
            channel.configureBlocking(false);
            SelectionKey key = channel.register(selector, ops);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client thisClient = new Client(args);
        thisClient.run();
    }
}
