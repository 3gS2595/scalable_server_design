package cs455.scaling.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadPoolManager {
    private ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
    private int poolSize = 0;

    public ThreadPoolManager(int poolSize){
        this.poolSize = poolSize;

        //TODO POPULATE THE CORRECT AMOUNT OF WORKER THREADS
    }

    public void addTask(Task task) {
    }

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16);
    }
}