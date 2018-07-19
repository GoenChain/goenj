package io.goen.net.p2p.event;

public class FindEvent extends Event{

    byte[] nearDistance;
    long expires;

    public FindEvent(){
        this.setType(new byte[]{3});
    }
    @Override
    public void parse(byte[] data) {
        FindEvent findEvent = new FindEvent();


    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }
}
