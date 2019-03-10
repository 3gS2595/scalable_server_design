package cs455.scaling.server;

import cs455.scaling.hash.Hash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Objects;

public class Task implements Runnable{

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
        if (key.isValid()) {
            socketChannel = (SocketChannel) key.channel();
        }
    }

    LinkedList get() {
        try {
            if (key.isValid()) {
                if (socketChannel.isOpen()) {

                    //reads incoming from client
                    ByteBuffer load = ByteBuffer.allocate(8);
                    int read = 0;
                    while (read != -1 && load.hasRemaining()) {
                        read = socketChannel.read(load);
                    }

                    //adds message to batch
                    batch.add(load.array());
                }
                return batch;
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public void run() {
        //returning the message to client
        //System.out.println("Executing (size): " + batch.size());
        buffer.flip();
        ServerStatistics temp = (ServerStatistics) this.key.attachment();
        temp.add(batch.size());
        this.key.attach(temp);
        try {
            for (byte[] batch1 : batch) {
                socketChannel.write(ByteBuffer.wrap(Objects.requireNonNull(Hash.SHA1FromBytes(batch1)).getBytes()));
            }
        } catch (IOException e) { e.printStackTrace(); }

        //clear the buffer
        buffer.clear();
    }
}

