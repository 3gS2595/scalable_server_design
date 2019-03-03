package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client {
    private static SocketChannel client;
    private static ByteBuffer buffer;

    public static void main(String[] args) throws IOException {
        try {
            //connects to server (arg0=host arg1=port)
            client = SocketChannel.open(new InetSocketAddress(args[0], Integer.parseInt(args[1])));
            //creates buffer
            buffer = ByteBuffer.allocate(256);
        } catch (IOException e) {
            e.printStackTrace();
        }

        buffer = ByteBuffer.wrap("please".getBytes());
        String response = null;
        try{
            client.write(buffer);
            buffer.clear();
            client.read(buffer);
            response = new String(buffer.array()).trim();
            System.out.println(response);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16);
    }
}
