package prrety;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MySqlProtocolReader {

    private final SocketChannel socket;

    public MySqlProtocolReader(SocketChannel socket) {
        this.socket = socket;
    }

    public MySqlPacketHead readHeader() throws IOException {
        final MySqlPacketHead head = new MySqlPacketHead();

        //读取头
        read(socket, head.buffer, 4);

        return head;
    }

    public MySqlPacketPayload readPayload(MySqlPacketHead head) throws IOException {
        final MySqlPacketPayload payload = new MySqlPacketPayload(head.getLength());
        //读取body
        read(socket, payload.buffer, head.getLength());

        return payload;
    }

    private int read(SocketChannel socket, ByteBuffer buffer, int len) throws IOException {
        int n = 0;
        while (n < len) {
            int count = socket.read(buffer);
            n += count;
        }
        return n;
    }

}
