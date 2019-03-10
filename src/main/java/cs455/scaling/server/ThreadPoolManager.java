package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
    private static final LinkedBlockingQueue<SelectionKey> keys = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>();
    private int processed;
    private long activatedAt = System.currentTimeMillis() / 1000;

    private static DecimalFormat df2 = new DecimalFormat(".##");

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
            threads[i] = new WorkerThread(BATCH_SIZE, BATCH_TIME);
            threads[i].start();
        }

        //collects data in intervals
        while(true) {
            //wait for batchSize or batchTime
            long activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
            if (activeFor == 20) {
                activatedAt = System.currentTimeMillis() / 1000;

                LinkedList<Integer> values = new LinkedList<>();
                for(SelectionKey i : keys){
                    ServerStatistics temp = (ServerStatistics) i.attachment();
                    values.add(temp.get());
                    processed+=(temp.get());
                    i.attach(new ServerStatistics());
                }
                double serverThroughput = processed/20;
                double clientThroughputMean = (processed/keys.size())/20;

                double clientThroughputStdDev = 0;
                double temp = 0;
                for(int a : values)
                    temp += ((a/20)-clientThroughputMean)*((a/20)-clientThroughputMean);
                clientThroughputStdDev = Math.sqrt(temp/(keys.size()-1));

                processed = (0);

                String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                System.out.println("[" + timeStamp + "]"
                    + " Server Throughput: " + df2.format(serverThroughput)  + " message(s),"
                    + " Active Client Connections: " + keys.size()
                    + ", Mean Per-client Throughput: " + df2.format(clientThroughputMean) + " message(s)"
                    + ", Std. Dev. Of Per-client Throughput: " + df2.format(clientThroughputStdDev));

            }
        }
    }

    private void execute(Task task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    static Task get(){
        Task task;
        synchronized (ThreadPoolManager.queue) {
            while (ThreadPoolManager.queue.isEmpty()) {
                try {
                    ThreadPoolManager.queue.wait();
                } catch (InterruptedException e) { e.printStackTrace(); }
            }
            long activatedAt = System.currentTimeMillis() / 1000;
            task = ThreadPoolManager.queue.poll();


            //wait for batchSize or batchTime
            long activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
            while ((task.get().size() < BATCH_SIZE) && activeFor != BATCH_TIME) {
                activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
            }
        }
        return task;
    }

    void createTask(SelectionKey key) {
        execute(new Task(key));
    }

    void createTask(ServerSocketChannel ServerSocketChannel, Selector selector) {
        try {
            SocketChannel clientSocket = ServerSocketChannel.accept();
            clientSocket.configureBlocking(false);
            SelectionKey thisKey = clientSocket.register(selector, SelectionKey.OP_READ);
            thisKey.attach(new ServerStatistics());
            execute(new Task(thisKey));

            keys.add(thisKey);

        } catch (IOException e) { e.printStackTrace(); }
    }
}