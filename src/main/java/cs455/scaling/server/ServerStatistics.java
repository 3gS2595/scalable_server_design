package cs455.scaling.server;

class ServerStatistics {
    int processed = 0;

    ServerStatistics(){
    }

    void add(int i ){
        processed+= i;
    }

    int get (){
        return processed;
    }
}
