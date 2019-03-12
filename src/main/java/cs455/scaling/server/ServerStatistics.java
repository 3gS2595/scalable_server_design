package cs455.scaling.server;

import java.nio.channels.SelectionKey;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

class ServerStatistics {

    //Book Keeping
    private int processed = 0;

    static void print(LinkedBlockingQueue<SelectionKey> keys){
        LinkedList<Integer> values = new LinkedList<>();
        for(SelectionKey i : keys){
            ServerStatistics temp = (ServerStatistics) i.attachment();
            values.add(temp.get());
            ThreadPoolManager.processed+=(temp.get());
            i.attach(new ServerStatistics());
        }

        double serverThroughput = (double)ThreadPoolManager.processed/20;
        double clientThroughputMean = ((double)ThreadPoolManager.processed/(double)keys.size())/(double)20;

        double clientThroughputStdDev;
        double temp = 0;
        for(int a : values)
            temp += (((double)a/20)-clientThroughputMean)*(((double)a/20)-clientThroughputMean);
        clientThroughputStdDev = Math.sqrt(temp / (keys.size() - 1));

        ThreadPoolManager.processed = (0);

        if(keys.size() == 1) {
            clientThroughputStdDev = 0;
        }

        //final statement creation
        DecimalFormat df2 = new DecimalFormat("#.##");
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println("[" + timeStamp + "]"
            + " Server Throughput: " + df2.format(serverThroughput) + " message(s),"
            + " Active Client Connections: " + keys.size()
            + ", Mean Per-client Throughput: " + df2.format(clientThroughputMean) + " message(s)"
            + ", Std. Dev. Of Per-client Throughput: " + df2.format(clientThroughputStdDev));
    }

    ServerStatistics(){
    }

    void add(int i ){
        processed+= i;
    }

    private int get(){ return processed; }
}
