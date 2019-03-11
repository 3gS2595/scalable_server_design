package cs455.scaling.server;

public class WorkerThread extends Thread {

    WorkerThread() {
    }

    public void run() {
        Task task;
        while (true) {
            task = ThreadPoolManager.get();
            task.run();
        }
    }
}
