package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Task implements Runnable {
    private SocketChannel client;
    public Task(SocketChannel client){
        this.client = client;
    }

    public void run() {
        try {
            System.out.println("Task " + " is running.");
            ByteBuffer buffer = ByteBuffer.allocate(256);
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
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}

