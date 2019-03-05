package cs455.scaling.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Task implements Runnable {
    private SelectionKey key;
    private ByteBuffer buffer = ByteBuffer.allocate(8);
    private SocketChannel socketChannel;



    //constructor
    Task(SelectionKey key) {
            this.key = key;
    }

    public void run() {
        try {
            socketChannel = (SocketChannel)key.channel();
            if ( socketChannel.isOpen()) {
                byte[] payload = new byte[8];
                int bytesRead = socketChannel.read(buffer);
                while(bytesRead != -1 && buffer.hasRemaining()) {
                    bytesRead = socketChannel.read(buffer);
                }
                payload = buffer.array();


                if (bytesRead == -1) {
                    socketChannel.close();
                    System.out.println("CLIENT DISCONNECTED");
                } else {
                    //returning the message to them
                    //flip the buffer to now write

                    buffer.flip();
                    socketChannel.write(buffer);

                    //clear the buffer
                    buffer.clear();
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}

