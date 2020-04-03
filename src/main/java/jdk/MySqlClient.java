package jdk;

import com.mysql.cj.protocol.Security;
import com.mysql.cj.protocol.a.NativeConstants;
import com.mysql.cj.protocol.a.NativeServerSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MySqlClient {

    private String username = "root";
    private String password = "root";
    private String database = "test";

    public static void main(String[] args) throws Exception {
        MySqlClient client = new MySqlClient();
        client.run();
    }

    public void run() {
        try (SocketChannel socket = SocketChannel.open(new InetSocketAddress("127.0.0.1", 3306))) {
            InitialHandshakePayload packet = init(socket);

            HandshakeResponse response = new HandshakeResponse();
            response.setCapabilityFlags(packet.getCapabilityFlags());
            response.setMaxPacketSize(NativeConstants.MAX_PACKET_SIZE);
            response.setCharacterSet(packet.getCharacterSet());
            response.setUsername(username);
            //混淆
            response.setAuthResponse(auth(packet, password));
            response.setDatabase(database);
            response.setClientPluginName(packet.getAuthPluginName());

            Map<String, String> attrs = new HashMap<>();
//            attrs.put("_runtime_version", "1.8.0_181");
//            attrs.put("_client_version", "8.0.19");
//            attrs.put("_client_license", "GPL");
//            attrs.put("_runtime_vendor", "Oracle Corporation");
//            attrs.put("_client_name", "MySQL Connector/J");
            response.setAttributes(attrs);

            //客户端响应
            response(socket, response);

            //服务端响应
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            int n = 0;
            while (n < 4) {
                int count = socket.read(buffer);
                n += count;
            }
            int bodyLength = readFixInt(buffer, 3);
            //读取剩下的
            int len = bodyLength - n;
            n = 0;
            while (n < len) {
                int count = socket.read(buffer);
                n += count;
            }

            byte type = buffer.get(4);
            if (type == 0) {
                //ok
                System.out.println("ok");
            } else if (type == (byte) 0xff) {
                //err
                int code = (buffer.get(5) & 0xff) | ((buffer.get(6) & 0xff) << 8);
                System.out.println("error code:" + code);
                System.out.println("marker:" + (buffer.get(7) & 0xff));
                byte[] ssa = new byte[5];
                ssa[0] = buffer.get(8);
                ssa[1] = buffer.get(9);
                ssa[2] = buffer.get(10);
                ssa[3] = buffer.get(11);
                ssa[4] = buffer.get(12);
                System.out.println("code:" + new String(ssa));

                int s = 13;
                while (true) {
                    if (buffer.get(s) == 0) {
                        break;
                    }
                    s++;
                }
                byte[] msga = new byte[s - 13];
                for (int i = 0; i < msga.length; i++) {
                    msga[i] = buffer.get(13 + i);
                }
                System.out.println("error:" + new String(msga));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void response(SocketChannel socket, HandshakeResponse response) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int capabilityFlags = response.getCapabilityFlags();
        writeFixInt(buffer, 20881935, 4);
        int maxPacketSize = response.getMaxPacketSize();
        writeFixInt(buffer, maxPacketSize, 4);
        int characterSet = response.getCharacterSet();
        writeFixInt(buffer, characterSet, 1);
        //filler [00]*23
        writeFixInt(buffer, 0, 23);
        String username = response.getUsername();
        writeNullString(buffer, username.getBytes(StandardCharsets.UTF_8));

        if ((capabilityFlags & NativeServerSession.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0) {
            byte[] authResponse = response.getAuthResponse();
            writeLengthString(buffer, authResponse);
        } else {
            byte[] authResponse = response.getAuthResponse();
            writeLengthInt(buffer, authResponse.length);
            writeFixString(buffer, authResponse);
        }

        if ((capabilityFlags & NativeServerSession.CLIENT_CONNECT_WITH_DB) != 0) {
            String database = response.getDatabase();
            writeNullString(buffer, database.getBytes(StandardCharsets.UTF_8));
        }

        if ((capabilityFlags & NativeServerSession.CLIENT_PLUGIN_AUTH) != 0) {
            String clientPluginName = response.getClientPluginName();
            writeNullString(buffer, clientPluginName.getBytes(StandardCharsets.UTF_8));
        }

        if ((capabilityFlags & NativeServerSession.CLIENT_CONNECT_ATTRS) != 0) {
            Map<String, String> attributes = response.getAttributes();
            ByteBuffer attrBuffer = ByteBuffer.allocate(1024);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                writeLengthString(attrBuffer, entry.getKey().getBytes(StandardCharsets.UTF_8));
                writeLengthString(attrBuffer, entry.getValue().getBytes(StandardCharsets.UTF_8));
            }
            attrBuffer.flip();

            writeLengthInt(buffer, attrBuffer.limit());
            grow(buffer, attrBuffer.limit());
            buffer.put(attrBuffer);
        }

        buffer.flip();

        int bodySize = buffer.limit();
        ByteBuffer packet = ByteBuffer.allocate(bodySize + 4);
        writeFixInt(packet, bodySize, 3);
        packet.put((byte) 1);
        packet.put(buffer);
        packet.flip();
        socket.write(packet);

    }

    private void writeFixString(ByteBuffer buffer, byte[] data) {
        grow(buffer, data.length);
        for (byte b : data) {
            buffer.put(b);
        }
    }

    private void writeLengthString(ByteBuffer buffer, byte[] data) {
        grow(buffer, data.length + 9);
        writeLengthInt(buffer, data.length);
        writeFixString(buffer, data);
    }

    /**
     * If the value is < 251, it is stored as a 1-byte integer.
     * If the value is ≥ 251 and < (2^16), it is stored as fc + 2-byte integer.
     * If the value is ≥ (2^16) and < (2^24), it is stored as fd + 3-byte integer.
     * If the value is ≥ (2^24) and < (2^64) it is stored as fe + 8-byte integer.
     */
    private void writeLengthInt(ByteBuffer buffer, int v) {
        if (v < 251) {
            grow(buffer, 1);
            writeFixInt(buffer, v, 1);
        } else if (v < 65536L) {
            grow(buffer, 3);
            writeFixInt(buffer, 0xfc, 1);
            writeFixInt(buffer, v, 2);
        } else if (v < 16777216L) {
            grow(buffer, 4);
            writeFixInt(buffer, 0xfd, 1);
            writeFixInt(buffer, v, 3);

        } else {
            grow(buffer, 9);
            writeFixInt(buffer, 0xfe, 1);
            writeFixInt(buffer, v, 8);
        }
    }

    private void writeNullString(ByteBuffer buffer, byte[] data) {
        grow(buffer, data.length + 1);
        for (byte b : data) {
            buffer.put(b);
        }
        buffer.put((byte) 0);
    }

    public void writeFixInt(ByteBuffer buffer, int v, int len) {
        grow(buffer, len);
        for (int i = 0; i < len; i++) {
            buffer.put((byte) (v >>> (i * 8)));
        }
    }


    private int readFixInt(ByteBuffer buffer, int len) {
        int data = 0;
        for (int i = 0; i < len; i++) {
            data |= (buffer.get() << (i * 8));
        }
        return data;
    }

    public InitialHandshakePayload init(SocketChannel socket) throws IOException {
        HandshakeV10Parser parser = new HandshakeV10Parser();
        InitialHandshakePayload packet = parser.parse(socket);
        System.out.println(packet);
        return packet;
    }


    public byte[] auth(InitialHandshakePayload packet, String password) {
        final String authPluginName = packet.getAuthPluginName();

        if ("mysql_native_password".equals(authPluginName)) {
            String data = packet.getAuthPluginDataPart1() + packet.getAuthPluginDataPart2();
            byte[] bytes = data.getBytes();
            byte[] seed = new byte[20];
            //去掉最后的0
            System.arraycopy(bytes, 0, seed, 0, 20);
            return Security.scramble411(password.getBytes(StandardCharsets.UTF_8), seed);
        } else {
            //省略
            return new byte[0];
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
