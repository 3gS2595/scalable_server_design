package cs455.scaling.server;

import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager {
    private int poolSize = 0;

    private final int threadCnt;
    private final WorkerThread[] threads;
    static final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    ThreadPoolManager(int threadCnt) {
        this.threadCnt = threadCnt;
        threads = new WorkerThread[threadCnt];

        for (int i = 0; i < threadCnt; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }
    }

    void execute(Task task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }
}