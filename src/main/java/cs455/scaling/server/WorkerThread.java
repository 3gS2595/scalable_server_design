package cs455.scaling.server;


public class WorkerThread extends Thread {
    public void run() {
        Runnable task;

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
            }

            // If we don't catch RuntimeException,
            // the pool could leak threads
            try {
                System.out.println("ITS WORKING");
                task.run();
            } catch (RuntimeException e) {
                System.out.println("Thread pool is interrupted due to an issue: " + e.getMessage());
            }
        }
    }
}
