package prrety;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MySqlProtocolSender {

    private final SocketChannel socket;

    public MySqlProtocolSender(SocketChannel socket) {
        this.socket = socket;
    }

    public void send(MySqlPacketPayload payload, int seq) throws IOException {
        ByteBuffer head = ByteBuffer.allocate(4);
        ByteBuffer body = payload.buffer;
        body.flip();
        int length = body.limit();
        head.put((byte) (length & 0xff));
        head.put((byte) (length >>> 8));
        head.put((byte) (length >>> 16));
        head.put((byte) seq);
        head.flip();

        syncWrite(socket, head, 4);
        syncWrite(socket, body, length);
    }

    private void syncWrite(SocketChannel socket, ByteBuffer buffer, int len) throws IOException {
        int i = 0;
        while ((i += socket.write(buffer)) < len) {
        }


    }
}
