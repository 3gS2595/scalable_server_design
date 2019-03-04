package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Random;

public class Client {
    private String SERVER_HOST = null;
    private int SERVER_PORT = 0;
    private int MESSAGE_RATE = 0;
    private static SocketChannel client;
    private static ByteBuffer buffer;
    private LinkedList<byte[]> sent = new LinkedList<>();

    private Client(String[] args){
        this.SERVER_HOST = args[0];
        this.SERVER_PORT = Integer.parseInt(args[1]);
        this.MESSAGE_RATE = Integer.parseInt(args[2]);
    }

    private void run() throws InterruptedException{
        while(true) {
            try {
                //connects to server (arg0=host arg1=port)
                client = SocketChannel.open(new InetSocketAddress(this.SERVER_HOST, this.SERVER_PORT));
                //creates buffer
                buffer = ByteBuffer.allocate(256);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] payload = new byte[8];
            new Random().nextBytes(payload);
            buffer = ByteBuffer.wrap(payload);
            System.out.println(buffer.array());

            byte[] response = null;
            try {
                client.write(buffer);
                buffer.clear();
                client.read(buffer);
                response = buffer.array();
                System.out.println(response);
                buffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //handles sending messages by the desired rate
            Thread.sleep(1000/ MESSAGE_RATE);
        }
    }

    private String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16);
    }

    public static void main(String[] args) throws InterruptedException {
        Client thisClient = new Client(args);
        thisClient.run();
    }
}
