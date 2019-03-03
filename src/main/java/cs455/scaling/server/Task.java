package cs455.scaling.server;

import java.nio.channels.SocketChannel;

public class Task implements Runnable {
    private SocketChannel client;
    public Task(SocketChannel client){
        this.client = client;
    }

    public void run() {
        System.out.println("Task " + " is running.");
    }
}

