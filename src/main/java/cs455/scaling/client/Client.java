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
    private int outCNT = 0;
    private int inCNT = 0;

    private Client(String[] args){
        this.SERVER_HOST = args[0];
        this.SERVER_PORT = Integer.parseInt(args[1]);
        this.MESSAGE_RATE = Integer.parseInt(args[2]);
        this.buffer = ByteBuffer.allocate(1024);
    }

    private void run() {
        try {
            //opens the selector
            //creates the input channel
            selector = Selector.open();
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(this.SERVER_HOST, this.SERVER_PORT));
            registerChannel(socketChannel);

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
                            checkMatch(response);
                            response = "";
                        }

                        byte[] payload = new byte[8];
                        new Random().nextBytes(payload);
                        buffer = ByteBuffer.wrap(payload);
                        sent.add(Hash.SHA1FromBytes(buffer.array()));
                        outCNT++;
                        socketChannel.write(buffer);
                        buffer.clear();

                        //wait for batchSize or batchTime
                        long activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
                        if (activeFor == 20) {
                            System.out.println("Total Sent Count: " + outCNT + ", Total Received Count: " + inCNT);
                            activatedAt = System.currentTimeMillis() / 1000;
                            outCNT = 0;
                            inCNT = 0;
                        }
                        Thread.sleep(1000 / MESSAGE_RATE);
                    }
                }
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void checkMatch(String response) {
        if (sent.contains(response)){
            sent.remove(response);
            inCNT++;
        }
    }

    private void confirmConnection(SelectionKey key) throws IOException{
        if (key.isAcceptable()) {
        } else if (key.isConnectable()) {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.isConnectionPending())
                channel.finishConnect();
        }
    }

    private void registerChannel(SelectableChannel channel) throws IOException{
        channel.configureBlocking(false);
        channel.register(selector, 9);
    }

    public static void main(String[] args) {
        Client thisClient = new Client(args);
        thisClient.run();
    }
}
