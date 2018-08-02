package io.goen.core;

import io.goen.net.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.net.InetAddress;

public class GoenConfig {

    public static GoenConfig system = new GoenConfig();

    private boolean P2PStart;

    private InetAddress host;
    private int port;

    public final ECKey systemKey = Loader(ECKey.class);

    private final byte[] publicKey = systemKey.getPubKey();

    private String[] peers = new String[] { "gnode://029c22429ce7570b0a8a6f3861430c879298f3255223406f3651bf465f9cc33bab@127.0.0.1:1234" };


    public boolean isP2PStart() {
        return P2PStart;
    }

    public void setP2PStart(boolean p2PStart) {
        P2PStart = p2PStart;
    }

    private ECKey Loader(Class<ECKey> clazz){
        ECKey systemKey = ECKey.fromPrivate(Hex.decode("5f844b9b9a147e2537fb091e3791bc37a75fd06c8ac76a2bdef13322b8e3b67a"));
        return systemKey;
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

}
