package cs455.scaling.server;

public class WorkerThread extends Thread {
    private final int batchSize;
    private final int batchTime;

    WorkerThread(int batchSize, int batchTime) {
        this.batchTime = batchTime;
        this.batchSize = batchSize;
    }

    public void run() {
        Task task;
        while (true) {
            synchronized (ThreadPoolManager.queue) {
                while (ThreadPoolManager.queue.isEmpty()) {
                    try {
                        ThreadPoolManager.queue.wait();
                    } catch (InterruptedException e) {
                        System.out.println("An error occurred while queue is waiting: " + e.getMessage());
                    }
                }
                long activatedAt = System.currentTimeMillis() / 1000;
                task = ThreadPoolManager.queue.poll();

                //wait for batchSize or batchTime
                long activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
                while ((task.get().size() < batchSize) && activeFor != batchTime) {
                    activeFor = (System.currentTimeMillis() / 1000) - activatedAt;
                }
            }
            task.run();

        }
    }
}
