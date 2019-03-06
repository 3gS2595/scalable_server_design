package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Task implements Runnable {
    private SelectionKey key;
    private ByteBuffer buffer = ByteBuffer.allocate(8);
    private SocketChannel socketChannel;


    //constructor
    Task( SelectionKey key) {
        this.key = key;
    }

    public ByteBuffer size(){
        try {
            socketChannel = (SocketChannel)key.channel();
            if ( socketChannel.isOpen()) {
                byte[] payload = new byte[8];
                int bytesRead = socketChannel.read(buffer);
                while(bytesRead != -1 && buffer.hasRemaining()) {
                    bytesRead = socketChannel.read(buffer);
                }
            }
            return buffer;
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
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //clear the buffer
        buffer.clear();


    }
}

