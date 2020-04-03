package prrety;

import com.mysql.cj.protocol.Security;
import com.mysql.cj.protocol.a.NativeServerSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MySqlConnection implements AutoCloseable {
    public static final int MAX_PACKET_SIZE = 256 * 256 * 256 - 1;

    private final SocketChannel socket;
    private final MySqlProtocolReader reader;
    private final MySqlProtocolSender sender;
    private final String username;
    private final String password;
    private final String database;

    private int sequence = 0;

    public static void main(String[] args) throws Exception {
        try (MySqlConnection con = new MySqlConnection("127.0.0.1", 3306, "root", "root", "test")) {
            con.handshake();
        }

    }

    public MySqlConnection(String host, int port, String username, String password, String database) throws IOException {
        this.username = username;
        this.password = password;
        this.database = database;
        this.socket = SocketChannel.open(new InetSocketAddress(host, port));
        this.reader = new MySqlProtocolReader(socket);
        this.sender = new MySqlProtocolSender(socket);
    }

    public void handshake() throws IOException {
        MySqlPacketHead head = reader.readHeader();
        sequence = head.getSequenceId();
        MySqlPacketPayload payload = reader.readPayload(head);
        InitialHandshakePayload initial = new InitialHandshakePayload();
        initial.read(payload);

        final byte[] scramble = authenticate(initial);

        MySqlPacketPayload response = buildHandshakeResponse(initial, scramble);

        this.sender.send(response, ++sequence);

        MySqlPacketHead head2 = this.reader.readHeader();
        MySqlPacketPayload payload2 = this.reader.readPayload(head2);

        GenericResponsePacket packet = new GenericResponsePacket();
        packet.parse(payload2);

        System.out.println(packet.isOk());
    }


    private MySqlPacketPayload buildHandshakeResponse(InitialHandshakePayload initial, byte[] scramble) {
        MySqlPacketPayload resp = new MySqlPacketPayload(1024);

        int capabilityFlags = initial.getCapabilityFlags();
        resp.writeFixInt(20881935, 4);
        resp.writeFixInt(MAX_PACKET_SIZE, 4);
        int characterSet = initial.getCharacterSet();
        resp.writeFixInt(characterSet, 1);
        //filler [00]*23
        resp.writeFixInt(0, 23);

        resp.writeNullString(username.getBytes(StandardCharsets.UTF_8));

        final byte[] authResponse = scramble;
        if ((capabilityFlags & NativeServerSession.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) != 0) {
            resp.writeLenencString(authResponse);
        } else {
            resp.writeLenencInt(authResponse.length);
            resp.writeFixString(authResponse);
        }

        if ((capabilityFlags & NativeServerSession.CLIENT_CONNECT_WITH_DB) != 0) {
            resp.writeNullString(database.getBytes(StandardCharsets.UTF_8));
        }

        if ((capabilityFlags & NativeServerSession.CLIENT_PLUGIN_AUTH) != 0) {
            String authPluginName = initial.getAuthPluginName();
            resp.writeNullString(authPluginName.getBytes(StandardCharsets.UTF_8));
        }

        if ((capabilityFlags & NativeServerSession.CLIENT_CONNECT_ATTRS) != 0) {
            Map<String, String> attributes = getConnectionAttributes();
            ByteBuffer attrBuffer = ByteBuffer.allocate(1024);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                resp.writeLenencString(entry.getKey().getBytes(StandardCharsets.UTF_8));
                resp.writeLenencString(entry.getValue().getBytes(StandardCharsets.UTF_8));
            }
            attrBuffer.flip();

            resp.writeLenencInt(attrBuffer.limit());
        }
        return resp;
    }

    private byte[] authenticate(InitialHandshakePayload initial) {
        final String authPluginName = initial.getAuthPluginName();

        if ("mysql_native_password".equals(authPluginName)) {
            String data = initial.getAuthPluginDataPart();
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

    private Map<String, String> getConnectionAttributes() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("_runtime_version", "1.8.0_181");
        attrs.put("_client_version", "8.0.19");
        attrs.put("_client_license", "GPL");
        attrs.put("_runtime_vendor", "Oracle Corporation");
        attrs.put("_client_name", "MySQL Connector/J");
        return attrs;
    }

    @Override
    public void close() throws Exception {
        if (socket != null) {
            socket.close();
        }
    }
}
