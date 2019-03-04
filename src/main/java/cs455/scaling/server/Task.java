package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Task implements Runnable {
    private SocketChannel client;
    private int batchSize = 0;

    //constructor
    Task(SocketChannel client, int batchSize){
        this.client = client;
        this.batchSize = batchSize;
    }

    public void run() {
        try {
            byte[] payload = new byte[8];
            System.out.println(" ");
            ByteBuffer buffer = ByteBuffer.allocate(8);
            int bytesRead = client.read(buffer);
            payload = buffer.array();

            if (bytesRead == -1) {
                client.close();
                System.out.println("CLIENT DISCONNECTED");
            } else {
                //returning the message to them
                System.out.println(buffer.array());
                //flip the buffer to now write
                buffer.flip();
                client.write(buffer);
                //clear the buffer
                buffer.clear();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        System.out.println(" ");

    }
}

