package cs455.scaling.server;

import cs455.scaling.server.WorkerThread;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager {
    private int poolSize = 0;

    private final int nThreads;
    private final WorkerThread[] threads;
    private final LinkedBlockingQueue queue;

    public ThreadPoolManager(int nThreads) {
        this.nThreads = nThreads;
        queue = new LinkedBlockingQueue();
        threads = new WorkerThread[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new WorkerThread();
            threads[i].start();
        }
    }

    public void execute(Task task) {
        synchronized (queue) {
            queue.add(task);
            queue.notify();
        }
    }

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16);
    }
}