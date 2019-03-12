package cs455.scaling.hash;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static String SHA1FromBytes(byte[] data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        }

        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        String res = hashInt.toString(16);
        String s = "0000000000000000000000000000000000000000".substring(res.length()) + res;
        return s;
    }
}
