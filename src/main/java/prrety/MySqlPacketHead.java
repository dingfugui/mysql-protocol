package prrety;

import java.nio.ByteBuffer;

public class MySqlPacketHead extends Message {

    final ByteBuffer buffer = ByteBuffer.allocate(4);

    public int getLength() {
        return buffer.get(0)
                | ((buffer.get(1) << 8))
                | (buffer.get(2) << 16);
    }

    public int getSequenceId() {
        return buffer.get(3);
    }

}
