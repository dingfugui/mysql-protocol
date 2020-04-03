package prrety;

import com.mysql.cj.protocol.a.NativeServerSession;

public class InitialHandshakePayload {

    private int protocolVersion;
    private String serverVersion;
    private int threadId;
    private String authPluginDataPart;
    private int capabilityFlags;
    private int characterSet;
    private int statusFlags;
    private int authPluginDataLen;
    private String authPluginName;


    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getAuthPluginDataPart() {
        return authPluginDataPart;
    }

    public void setAuthPluginDataPart(String authPluginDataPart) {
        this.authPluginDataPart = authPluginDataPart;
    }

    public int getCapabilityFlags() {
        return capabilityFlags;
    }

    public void setCapabilityFlags(int capabilityFlags) {
        this.capabilityFlags = capabilityFlags;
    }

    public int getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(int characterSet) {
        this.characterSet = characterSet;
    }

    public int getStatusFlags() {
        return statusFlags;
    }

    public void setStatusFlags(int statusFlags) {
        this.statusFlags = statusFlags;
    }

    public int getAuthPluginDataLen() {
        return authPluginDataLen;
    }

    public void setAuthPluginDataLen(int authPluginDataLen) {
        this.authPluginDataLen = authPluginDataLen;
    }

    public String getAuthPluginName() {
        return authPluginName;
    }

    public void setAuthPluginName(String authPluginName) {
        this.authPluginName = authPluginName;
    }


    public void read(MySqlPacketPayload payload) {
        payload.read();
        this.setProtocolVersion(payload.readFixInt(1));
        this.setServerVersion(payload.readNullString());
        this.setThreadId(payload.readFixInt(4));
        this.setAuthPluginDataPart(payload.readFixString(8));
        payload.skip(); //filler
        this.setCapabilityFlags(payload.readFixInt(2));
        this.setCharacterSet(payload.readFixInt(1));
        this.setStatusFlags(payload.readFixInt(2));

        int capabilities = this.getCapabilityFlags() | (payload.readFixInt(2) << 16);
        this.setCapabilityFlags(capabilities);
        if ((capabilities & NativeServerSession.CLIENT_PLUGIN_AUTH) != 0) {
            int i = payload.readFixInt(1);
            this.setAuthPluginDataLen(i);
        } else {
            payload.skip();
            this.setAuthPluginDataLen(0);
        }
        payload.skip(10);//reserved 10
        int apdp2len = Math.max(13, this.getAuthPluginDataLen() - 8);
        this.setAuthPluginDataPart(this.getAuthPluginDataPart() + payload.readFixString(apdp2len));

        if ((capabilities & NativeServerSession.CLIENT_PLUGIN_AUTH) != 0) {
            this.setAuthPluginName(payload.readNullString());
        }
        payload.reset();
    }
}
