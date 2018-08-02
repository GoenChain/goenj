package io.goen.net.p2p.event;

import io.goen.net.p2p.Node;

import java.util.List;

public class NodesEvent extends Event{
    List<Node> nodes;

    public NodesEvent(){
        super();
        this.setType(new byte[]{4});
    }

    @Override
    public void parseData(byte[] encodedData) {

    }

    @Override
    public byte[] getDataBytes() {
        return new byte[0];
    }
}
