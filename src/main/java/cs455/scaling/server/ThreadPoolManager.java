package cs455.scaling.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

class ThreadPoolManager extends Thread{
    //args[]
    private static int BATCH_SIZE;
    private static int BATCH_TIME;
    private static int THREAD_CNT;

    //Book keeping
    static LinkedBlockingQueue<SelectionKey> keys = new LinkedBlockingQueue<>();
    private static final LinkedList<Task>  priority = new LinkedList<>();
    private static final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    static int processed;
    private long activatedAt = System.currentTimeMillis() / 1000;

    ThreadPoolManager(int THREAD_CNT, int BATCH_SIZE, int BATCH_TIME) {
        ThreadPoolManager.BATCH_TIME = BATCH_TIME;
        ThreadPoolManager.BATCH_SIZE = BATCH_SIZE;
        ThreadPoolManager.THREAD_CNT = THREAD_CNT;
        processed = (0);
    }

    public void run(){
        //initializes workerThreads
        WorkerThread[] threads = new WorkerThread[THREAD_CNT];
        for (int i = 0; i < THREAD_CNT; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }

        //collects data in intervals
        while(true) {
            //wait for batchSize or batchTime
            long activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
            if (activeFor == 20) {
                activatedAt = System.currentTimeMillis() / 1000;
                ServerStatistics.print(keys);
            }
        }
    }

    static void execute(Task task) {
        synchronized (queue) {
                queue.add(task);
            queue.notify();
        }
    }

    static Task get(){
        Task task;
        if ( priority.size() != 0){
            System.out.println(priority.size() + "SIZE");
            return priority.poll();
        }
            synchronized (ThreadPoolManager.queue) {
                while (ThreadPoolManager.queue.isEmpty()) {
                    try {
                        ThreadPoolManager.queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                long activatedAt = System.currentTimeMillis() / 1000;
                task = ThreadPoolManager.queue.poll();
                if(task.getType() == 1 ){
                    return task;
                }
                //wait for batchSize or batchTime
                long activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
                while ((((BatchTask) task).get().size() < BATCH_SIZE) && activeFor != BATCH_TIME) {
                    activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
                }
            }
        return task;

    }

    static void createTask(SelectionKey key) {
        execute(new BatchTask(key));
    }

    void createTask(ServerSocketChannel ServerSocketChannel, Selector selector) {
        execute(new InitializeTask(ServerSocketChannel, selector));
    }
}