package cs455.scaling.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager {
    private int poolSize = 0;

    private final int threadCnt;
    private final WorkerThread[] threads;
    public static final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    public ThreadPoolManager(int threadCnt) {
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

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16);
    }
}