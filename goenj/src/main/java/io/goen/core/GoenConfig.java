package io.goen.core;

import io.goen.net.crypto.ECKey;

import java.net.InetAddress;

public class GoenConfig {

    GoenConfig config = new GoenConfig();

    public static GoenConfig system = new GoenConfig();

    private boolean P2PStart;

    private InetAddress host;
    private int port;

    private String[] peers;


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
}
