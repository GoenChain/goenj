package io.goen.net.p2p.event;

public class FindEvent extends Event{

    byte[] nearDistance;
    long expires;

    public FindEvent(){
        super();
        this.setType(new byte[]{3});
    }

    @Override
    public void parseData(byte[] encodedData) {

    }

    @Override
    public byte[] getDataBytes() {
        return new byte[0];
    }
}
