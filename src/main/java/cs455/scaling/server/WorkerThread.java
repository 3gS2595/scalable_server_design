package cs455.scaling.server;

import java.nio.ByteBuffer;


public class WorkerThread extends Thread {
    private int batchSize = 0;

    public WorkerThread(int batchSize) {
        this.batchSize = batchSize;
    }

    public void run() {
        Task task;
        ByteBuffer buffer = ByteBuffer.allocate(8*batchSize);
        while (true) {
            synchronized (ThreadPoolManager.queue) {
                while (ThreadPoolManager.queue.isEmpty()) {
                    try {
                        ThreadPoolManager.queue.wait();
                    } catch (InterruptedException e) {
                        System.out.println("An error occurred while queue is waiting: " + e.getMessage());
                    }
                }
                //TODO WE NOW HAVE THE NEXT THREAD
                //TODO WAIT TILL BATCH SIZE
                //TODO OR WAIT TILL TIME
                //start building a packet
                //flush it when reach time or size
                task = ThreadPoolManager.queue.poll();
            }
            int size = 0;
            while (size/8 <= batchSize) {
                size += task.size().array().length;
                System.out.println(size);
            }
            size =0;
            System.out.println("WEMADEIT");

        }
    }
}
