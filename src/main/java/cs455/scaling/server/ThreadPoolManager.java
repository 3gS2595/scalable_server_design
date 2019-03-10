package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

class ThreadPoolManager extends Thread{
    //args[]
    private final int BATCH_SIZE;
    private final int BATCH_TIME;
    private final int THREAD_CNT;

    //Book keeping
    static final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>();
    private int cnt = 0;

    ThreadPoolManager(int THREAD_CNT, int BATCH_SIZE, int BATCH_TIME) {
        this.BATCH_TIME = BATCH_TIME;
        this.BATCH_SIZE = BATCH_SIZE;
        this.THREAD_CNT = THREAD_CNT;
    }

    public void run(){
        //initializes workerThreads
        WorkerThread[] threads = new WorkerThread[THREAD_CNT];
        for (int i = 0; i < THREAD_CNT; i++) {
            threads[i] = new WorkerThread(BATCH_SIZE, BATCH_TIME);
            threads[i].start();
        }
    }

    private void execute(Task task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    void createTask(SelectionKey key) {
        execute(new Task(key));
    }

    void createTask(ServerSocketChannel ServerSocketChannel, Selector selector) {
        try {
            SocketChannel clientSocket = ServerSocketChannel.accept();
            cnt++;
            System.out.println(cnt);
            clientSocket.configureBlocking(false);
            SelectionKey thisKey = clientSocket.register(selector, SelectionKey.OP_READ);
            execute(new Task(thisKey));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}