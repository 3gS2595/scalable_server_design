package cs455.scaling.client;

import cs455.scaling.hash.Hash;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Random;

public class Client {
    private String SERVER_HOST;
    private int SERVER_PORT;
    private int MESSAGE_RATE;
    private static SocketChannel client;
    private LinkedList<String> sent = new LinkedList<>();

    private Client(String[] args){
        this.SERVER_HOST = args[0];
        this.SERVER_PORT = Integer.parseInt(args[1]);
        this.MESSAGE_RATE = Integer.parseInt(args[2]);
    }

    private void run() {
        ByteBuffer read = ByteBuffer.allocate(256);
        System.out.println("HEY");
        try {
            //connects to server (arg0=host arg1=port)
            client = SocketChannel.open(new InetSocketAddress(this.SERVER_HOST, this.SERVER_PORT));
            //creates buffer
            this.send(client);
            read = ByteBuffer.allocate(256);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                read = ByteBuffer.allocate(256);
                read.clear();
                client.read(read);
                System.out.println(read.array());
                read.put(new byte[1024]);
                read.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void send(SocketChannel client){
        ByteBuffer write;
        try {
            while(true) {
                byte[] payload = new byte[8];
                new Random().nextBytes(payload);
                write = ByteBuffer.wrap(payload);
                sent.add(Hash.SHA1FromBytes(write.array()));
                client.write(write);
                write.clear();

                //handles sending messages by the desired rate
                Thread.sleep(1000/ MESSAGE_RATE);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        Client thisClient = new Client(args);
        thisClient.run();
    }
}
