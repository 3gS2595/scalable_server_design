package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class Task implements Runnable {

    //Networking
    private SocketChannel socketChannel;
    private ByteBuffer buffer;
    private SelectionKey key;

    //Book keeping
    private LinkedList<byte[]> batch;


    Task(SelectionKey key) {
        this.key = key;
        this.batch = new LinkedList<>();
        this.buffer = ByteBuffer.allocate(8);
    }

    LinkedList get() {
        try {
            if (key.isValid()) {
                socketChannel = (SocketChannel) key.channel();
                if (socketChannel.isOpen()) {
                    ByteBuffer load = ByteBuffer.allocate(8);
                    int read = 0;
                    while (read != -1 && load.hasRemaining()) {
                        read = socketChannel.read(load);
                    }
                    batch.add(load.array());
                }
                return batch;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void run() {
        //returning the message to them
        //flip the buffer to now write
        System.out.println("Executing (size): " + batch.size());
        buffer.flip();
        try {
            ByteBuffer fullBatch = ByteBuffer.allocate(batch.size() * 8);
            for (byte[] message : batch) {
                fullBatch.put(message);
                System.out.println(message);
            }
            socketChannel.write(ByteBuffer.wrap(batch.get(0)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //clear the buffer
        buffer.clear();
    }
}

