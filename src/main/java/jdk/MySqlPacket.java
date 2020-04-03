package jdk;

import prrety.MySqlPacketHead;
import prrety.MySqlPacketPayload;

public class MySqlPacket {

    private MySqlPacketHead head;
    private MySqlPacketPayload payload;


    public MySqlPacketHead getHead() {
        return head;
    }

    public void setHead(MySqlPacketHead head) {
        this.head = head;
    }

    public MySqlPacketPayload getPayload() {
        return payload;
    }

    public void setPayload(MySqlPacketPayload payload) {
        this.payload = payload;
    }
}
