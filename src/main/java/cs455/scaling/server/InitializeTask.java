package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class InitializeTask implements Task {
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    SocketChannel clientSocket;
    SelectionKey thisKey;

    InitializeTask(ServerSocketChannel serverSocketChannel, Selector selector){
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
        try {
            clientSocket = serverSocketChannel.accept();
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
        System.out.println("4");
    }

    @Override
    public int getType() {
        return 1;
    }
}
