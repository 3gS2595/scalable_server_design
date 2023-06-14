package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class InitializeTask implements Task {

    //networking
    private SelectionKey thisKey;

    InitializeTask(ServerSocketChannel serverSocketChannel, Selector selector){
        try {
            SocketChannel clientSocket = serverSocketChannel.accept();
                clientSocket.configureBlocking(false);
                this.thisKey = clientSocket.register(selector, SelectionKey.OP_READ);
                this.thisKey.attach(new ServerStatistics());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        ThreadPoolManager.createTask(thisKey);
        ThreadPoolManager.keys.add(thisKey);
    }

    @Override
    public int getType() {
        return 1;
    }
}
