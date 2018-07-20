package io.goen.net.p2p.event;

import io.goen.net.p2p.Node;

import java.util.List;

public class NodesEvent extends Event{
    List<Node> nodes;

    public NodesEvent(){
        this.setType(new byte[]{4});
    }
    @Override
    public void parse(byte[] data) {

    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
