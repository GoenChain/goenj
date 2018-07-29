package io.goen.core;

import io.goen.net.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;

public class GoenConfig {

    public static GoenConfig system = new GoenConfig();

    private boolean P2PStart;

    private InetAddress host;
    private int port;

    private byte[] publicKey = Hex.decode("029c22429ce7570b0a8a6f3861430c879298f3255223406f3651bf465f9cc33bab");

    private String[] peers = new String[] { "gnode://abde@127.0.0.1:1234" };

    public final ECKey systemKey = Loader(ECKey.class);


    public boolean isP2PStart() {
        return P2PStart;
    }

    public void setP2PStart(boolean p2PStart) {
        P2PStart = p2PStart;
    }

    private ECKey Loader(Class<ECKey> clazz){
        return null;
    }

    public InetAddress getHost() {
        return host;
    }

    public void setHost(InetAddress host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String[] getPeers() {
        return peers;
    }

    public void setPeers(String[] peers) {
        this.peers = peers;
    }

    public ECKey getSystemKey() {
        return systemKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
}
