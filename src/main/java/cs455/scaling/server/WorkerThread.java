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
            task = ThreadPoolManager.get();
            task.run();
        }
    }
}
