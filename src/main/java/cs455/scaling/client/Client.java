package cs455.scaling.client;

import cs455.scaling.hash.Hash;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class Client {
    //args
    private final String SERVER_HOST;
    private final int SERVER_PORT;
    private final int MESSAGE_RATE;

    //networking
    private ByteBuffer buffer;
    private Selector selector;

    //book keeping
    private final LinkedList<String> sent = new LinkedList<>();
    private int sCNT = 0;
    private int rCNT = 0;

    private Client(String[] args){
        this.SERVER_HOST = args[0];
        this.SERVER_PORT = Integer.parseInt(args[1]);
        this.MESSAGE_RATE = Integer.parseInt(args[2]);
        buffer = ByteBuffer.allocate(1024);
    }

    private void run() {
        try {
            //opens the selector
            //creates the input channel
            selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(this.SERVER_HOST, this.SERVER_PORT));
            registerChannel(socketChannel, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);

            //connects to server (arg0=host arg1=port)
            System.out.println("connected");

            //operates functions
            String response = "";
            long activatedAt = System.currentTimeMillis() / 1000;
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
                            while (response.length() != 40) {
                                socketChannel.read(buffer);
                                response += new String(buffer.array());
                                buffer.clear();
                            }
                            checkIfSent(response);
                            response = "";
                        }

                        byte[] payload = new byte[8];
                        new Random().nextBytes(payload);
                        buffer = ByteBuffer.wrap(payload);
                        sent.add(Hash.SHA1FromBytes(buffer.array()));
                        sCNT++;
                        socketChannel.write(buffer);
                        buffer.clear();

                        //wait for batchSize or batchTime
                        long activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
                        if (activeFor == 20) {
                            System.out.println("Total Sent Count: " + sCNT + ", Total Received Count: " + rCNT);
                            activatedAt = System.currentTimeMillis() / 1000;
                            rCNT = 0;
                            sCNT = 0;
                        }
                        Thread.sleep(1000 / MESSAGE_RATE);
                    }
                }
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void checkIfSent(String response) {
        if (sent.contains(response)){
            sent.remove(response);
            rCNT++;
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
            channel.register(selector, ops);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client thisClient = new Client(args);
        thisClient.run();
    }
}
