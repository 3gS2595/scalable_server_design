package cs455.scaling.server;

import java.nio.channels.SocketChannel;

public class Task {
    private SocketChannel client;
    public Task(SocketChannel client){
        this.client = client;
    }
}
