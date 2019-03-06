package cs455.scaling.server;

public class WorkerThread extends Thread {
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
                task = ThreadPoolManager.queue.poll();
            }

            while(true) {
                if(ThreadPoolManager.batches.get(task.selectionKey) != 1)
                System.out.println(ThreadPoolManager.batches.get(task.selectionKey));
            }
            // If we don't catch RuntimeException,
            // the pool could leak threads

        }
    }
}
