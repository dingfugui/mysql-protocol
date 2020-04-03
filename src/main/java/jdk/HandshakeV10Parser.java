package jdk;

import com.mysql.cj.protocol.a.NativeServerSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class HandshakeV10Parser {


    public HandshakeV10Parser() {
    }

    public InitialHandshakePayload parse(SocketChannel socket) throws IOException {
        InitialHandshakePayload packet = new InitialHandshakePayload();
        ByteBuffer buffer = ByteBuffer.allocate(2048);

        //读取头
        int read = read(socket, buffer, 4);
        //计算包长
        int bodyLength = readFixInt(buffer, 3);
        //读取剩下的
        read(socket, buffer, bodyLength - read);
        buffer.flip();
        buffer.position(4);//跳过头

        packet.setProtocolVersion(readFixInt(buffer, 1));
        packet.setServerVersion(readNullString(buffer));
        packet.setThreadId(readFixInt(buffer, 4));
        packet.setAuthPluginDataPart1(readFixString(buffer, 8));
        buffer.get();//filler
        packet.setCapabilityFlags(readFixInt(buffer, 2));
        packet.setCharacterSet(readFixInt(buffer, 1));
        packet.setStatusFlags(readFixInt(buffer, 2));
        int capabilities = packet.getCapabilityFlags() | (readFixInt(buffer, 2) << 16);
        packet.setCapabilityFlags(capabilities);
        if ((capabilities & NativeServerSession.CLIENT_PLUGIN_AUTH) != 0) {
            int i = readFixInt(buffer, 1);
            packet.setAuthPluginDataLen(i);
        } else {
            buffer.get();
            packet.setAuthPluginDataLen(0);
        }
        buffer.position(buffer.position() + 10);//reserved
        int apdp2len = Math.max(13, packet.getAuthPluginDataLen() - 8);
        packet.setAuthPluginDataPart2(readFixString(buffer, apdp2len));

        if ((capabilities & NativeServerSession.CLIENT_PLUGIN_AUTH) != 0) {
            packet.setAuthPluginName(readNullString(buffer));
        }
        return packet;
    }

    private String readFixString(ByteBuffer buffer, int len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = buffer.get();
        }
        return new String(data);
    }

    private String readNullString(ByteBuffer buffer) {
        int position = buffer.position();
        int end = position;
        while (buffer.get() != 0) {
            end++;
        }
        buffer.position(position);
        byte[] data = new byte[end - position];
        buffer.get(data);
        buffer.get();//skip 00
        return new String(data);
    }

    private int readFixInt(ByteBuffer buffer, int len) {
        int data = 0;
        for (int i = 0; i < len; i++) {
            data |= (buffer.get() << (i * 8));
        }
        return data;
    }

    public int read(SocketChannel socket, ByteBuffer buffer, int len) throws IOException {
        grow(buffer, len);
        int n = 0;
        while (n < len) {
            int count = socket.read(buffer);
            n += count;
        }
        return n;
    }

    public ByteBuffer grow(ByteBuffer buffer, int len) {
        if (buffer.remaining() < len) {
            ByteBuffer nb = ByteBuffer.allocate(buffer.capacity() << 1);
            buffer.flip();
            nb.put(buffer);
            return nb;
        } else {
            return buffer;
        }
    }
}
