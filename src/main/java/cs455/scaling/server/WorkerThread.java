package cs455.scaling.server;

import java.util.LinkedList;

public class WorkerThread extends Thread {
    private int batchSize;

    WorkerThread(int batchSize, int batchTime) {
        this.batchSize = batchSize;
    }

    public void run() {
        Task task;
        LinkedList<byte[]> batch;
        while (true) {
            synchronized (ThreadPoolManager.queue) {
                while (ThreadPoolManager.queue.isEmpty()) {
                    try {
                        ThreadPoolManager.queue.wait();
                    } catch (InterruptedException e) {
                        System.out.println("An error occurred while queue is waiting: " + e.getMessage());
                    }
                }
                task = ThreadPoolManager.queue.poll();
                while (task.get().size() < batchSize - 1){
                }
                batch = task.get();
            }
            task.run();
            //TODO WE NOW HAVE THE NEXT THREAD
            //TODO WAIT TILL BATCH SIZE
            //TODO OR WAIT TILL TIME
            //start building a packet
            //flush it when reach time or size

            System.out.println();
            System.out.println("WEMADEIT " + batch.size());
            System.out.println();

        }
    }
}
