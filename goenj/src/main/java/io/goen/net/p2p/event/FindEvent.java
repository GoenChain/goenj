package io.goen.net.p2p.event;

import io.goen.rlp.RLP;
import io.goen.rlp.RLPList;
import io.goen.util.ByteUtil;

public class FindEvent extends Event{

    byte[] nearDistance;
    long expires;

    public FindEvent(){
        super();
        this.setType(new byte[]{3});
    }

    @Override
    public void parseData(byte[] encodedData) {
        RLPList list = (RLPList) RLP.decode2OneItem(encodedData, 0);
        this.nearDistance = list.get(0).getRLPData();
        this.expires = ByteUtil.bytesToLong(list.get(1).getRLPData());
    }

    @Override
    public byte[] getDataBytes() {
        byte[] tmpExp = ByteUtil.longToBytes(expires);
        byte[] rlpExp = RLP.encodeElement(tmpExp);
        byte[] rlpFromNearDistance = RLP.encodeElement(nearDistance);

        return RLP.encodeList(rlpFromNearDistance, rlpExp);
    }

    public byte[] getNearDistance() {
        return nearDistance;
    }

    public void setNearDistance(byte[] nearDistance) {
        this.nearDistance = nearDistance;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }
}
