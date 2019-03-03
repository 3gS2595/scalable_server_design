package cs455.scaling.client;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SenderThread {
    private Socket socket;
    private DataOutputStream dout;

    public SenderThread(Socket socket) throws IOException {
        this.socket = socket;
        dout = new DataOutputStream(socket.getOutputStream());
    }
    public void sendData(byte[] dataToSend) throws IOException {
        int dataLength = dataToSend.length;
        dout.writeInt(dataLength);
        dout.write(dataToSend, 0, dataLength);
        dout.flush();
    }

    public static void sendMessage(String key, int type, int numEntries, byte[][] message) throws IOException{
        String[] splitKey = key.split(":");
        String Address = splitKey[0];
        int Port = (Integer.parseInt(splitKey[1]));
        Socket REG_SOCKET = new Socket(Address, Port);
        SenderThread sender = new SenderThread(REG_SOCKET);
        ///creates Request message byte array
        byte[] marshaledBytes;

        //Initialize used streams
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout =
            new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //insert the deregister request protocol
        dout.writeByte(type);

        if(numEntries == -5){
            dout.write(message[0]);
            dout.writeInt((message[1].length));
            dout.write(message[1]);
        } else {
            if (numEntries > 1)
                dout.writeInt(numEntries);
            for (int i = 0; i < message.length; i++) {
                if (message[i] != null) {
                    dout.writeInt(message[i].length);
                    dout.write(message[i]);
                }
            }
        }

        dout.flush();
        marshaledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();

        //sends request
        sender.sendData(marshaledBytes);
        REG_SOCKET.close();
    }
}

