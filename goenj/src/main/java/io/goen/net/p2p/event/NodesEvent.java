package io.goen.net.p2p.event;

import io.goen.net.p2p.Node;
import io.goen.rlp.RLP;
import io.goen.rlp.RLPList;
import io.goen.util.ByteUtil;

import java.util.ArrayList;
import java.util.List;

public class NodesEvent extends Event{
    List<Node> nodes;
    long expires;

    public NodesEvent(){
        super();
        this.setType(new byte[]{4});
    }

    @Override
    public void parseData(byte[] encodedData) {
        RLPList list = (RLPList) RLP.decode2OneItem(encodedData, 0);
        RLPList nodesRLP = (RLPList) list.get(0);

        nodes = new ArrayList<>();

        for (int i = 0; i < nodesRLP.size(); ++i) {
            RLPList nodeRLP = (RLPList) nodesRLP.get(i);
            Node node = new Node(nodeRLP.getRLPData());
            nodes.add(node);
        }
        this.expires = ByteUtil.bytesToLong(list.get(1).getRLPData());
    }

    @Override
    public byte[] getDataBytes() {
        byte[][] nodeRLPs = null;

        if (nodes != null) {
            nodeRLPs = new byte[nodes.size()][];
            for(int i = 0 ;i< nodes.size();i++){
                nodeRLPs[i] = nodes.get(i).getBytes();
            }
        }

        byte[] rlpListNodes = RLP.encodeList(nodeRLPs);
        byte[] tmpExp = ByteUtil.longToBytes(expires);
        byte[] rlpExp = RLP.encodeElement(tmpExp);

        return RLP.encodeList(rlpListNodes, rlpExp);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }
}
