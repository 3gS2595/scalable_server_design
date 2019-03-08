package cs455.scaling.server;

import cs455.scaling.hash.Hash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class Task implements Runnable {
    private SelectionKey key;
    LinkedList<byte[]> batch;
    ByteBuffer buffer;
    private SocketChannel socketChannel;

    public Task(SelectionKey key) {
        this.key = key;
        this.batch = new LinkedList<>();
        this.buffer  = ByteBuffer.allocate(8);
    }

    LinkedList get(){
        try {
            if(key.isValid()) {
                socketChannel = (SocketChannel) key.channel();
                if (socketChannel.isOpen()) {
                    ByteBuffer load = ByteBuffer.allocate(8);
                    int read = 0;
                    while (read != -1 && load.hasRemaining()) {
                        read = socketChannel.read(load);
                    }
                    batch.add(load.array());
                    System.out.print(Hash.SHA1FromBytes(load.array()));

                }
                return batch;
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public void run() {
        //returning the message to them
        //flip the buffer to now write
        buffer.flip();
        try {
            ByteBuffer fullBatch = ByteBuffer.allocate(batch.size()*8);
            for (byte[] message : batch){
                fullBatch.put(message);
            }
            socketChannel.write(fullBatch);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //clear the buffer
        buffer.clear();
    }
}

