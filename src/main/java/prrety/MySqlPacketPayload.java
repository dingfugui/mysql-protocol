package prrety;

import java.nio.ByteBuffer;

public class MySqlPacketPayload {

    final ByteBuffer buffer;

    public MySqlPacketPayload(int length) {
        buffer = ByteBuffer.allocate(length);
    }


    public void read() {
        buffer.flip();
    }

    public void reset() {
        buffer.rewind();
    }


    /* read method*/

    public String readFixString(int len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = buffer.get();
        }
        return new String(data);
    }

    public String readNullString() {
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

    public String readEOFString() {
        int remaining = buffer.remaining();
        byte[] data = new byte[remaining];
        for (int i = 0; i < remaining; i++) {
            data[i] = buffer.get();
        }
        return new String(data);
    }

    public int readFixInt(int len) {
        int data = 0;
        for (int i = 0; i < len; i++) {
            data |= (buffer.get() << (i * 8));
        }
        return data;
    }

    public void skip() {
        skip(1);
    }

    public void skip(int step) {
        for (int i = 0; i < step; i++) {
            buffer.get();
        }
    }

    /* write method*/

    public void writeFixString(byte[] data) {
        grow(buffer, data.length);
        for (byte b : data) {
            buffer.put(b);
        }
    }

    public void writeLenencString(byte[] data) {
        grow(buffer, data.length + 9);
        writeLenencInt(data.length);
        writeFixString(data);
    }

    /**
     * If the value is < 251, it is stored as a 1-byte integer.
     * If the value is ≥ 251 and < (2^16), it is stored as fc + 2-byte integer.
     * If the value is ≥ (2^16) and < (2^24), it is stored as fd + 3-byte integer.
     * If the value is ≥ (2^24) and < (2^64) it is stored as fe + 8-byte integer.
     */
    public void writeLenencInt(int v) {
        if (v < 251) {
            grow(buffer, 1);
            writeFixInt(v, 1);
        } else if (v < 65536L) {
            grow(buffer, 3);
            writeFixInt(0xfc, 1);
            writeFixInt(v, 2);
        } else if (v < 16777216L) {
            grow(buffer, 4);
            writeFixInt(0xfd, 1);
            writeFixInt(v, 3);

        } else {
            grow(buffer, 9);
            writeFixInt(0xfe, 1);
            writeFixInt(v, 8);
        }
    }

    public void writeNullString(byte[] data) {
        grow(buffer, data.length + 1);
        for (byte b : data) {
            buffer.put(b);
        }
        buffer.put((byte) 0);
    }

    public void writeFixInt(int v, int len) {
        grow(buffer, len);
        for (int i = 0; i < len; i++) {
            buffer.put((byte) (v >>> (i * 8)));
        }
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
