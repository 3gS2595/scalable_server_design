package cs455.scaling.server;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager {
    private int poolSize = 0;

    private final int threadCnt;
    private final WorkerThread[] threads;
    static final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>();
    static final HashMap<SelectionKey, Integer> batches = new HashMap<>();

    ThreadPoolManager(int threadCnt, int batchSize) {
        this.threadCnt = threadCnt;
        threads = new WorkerThread[threadCnt];

        for (int i = 0; i < threadCnt; i++) {
            threads[i] = new WorkerThread(batchSize);
            threads[i].start();
        }
    }

    void execute(Task task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    public void add(SelectionKey key) {
        synchronized (batches) {
            if(!batches.containsKey(key))
                batches.putIfAbsent(key, 1);
            else{
                int temp = batches.get(key);
                temp = temp +1;
                batches.put(key, temp);
            }
        }
    }
}