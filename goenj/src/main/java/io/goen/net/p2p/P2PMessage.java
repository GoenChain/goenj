package io.goen.net.p2p;

import io.goen.net.p2p.event.Event;

import java.net.InetSocketAddress;

public class P2PMessage {
    private Event event;
    private InetSocketAddress inetSocketAddress;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }
}
