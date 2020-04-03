package jdk;

import java.util.StringJoiner;

public class InitialHandshakePayload {

    private int protocolVersion;
    private String serverVersion;
    private int threadId;
    private String authPluginDataPart1;
    private int capabilityFlags;
    private int characterSet;
    private int statusFlags;
    private int authPluginDataLen;
    private String authPluginDataPart2;
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

    public String getAuthPluginDataPart1() {
        return authPluginDataPart1;
    }

    public void setAuthPluginDataPart1(String authPluginDataPart1) {
        this.authPluginDataPart1 = authPluginDataPart1;
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

    public String getAuthPluginDataPart2() {
        return authPluginDataPart2;
    }

    public void setAuthPluginDataPart2(String authPluginDataPart2) {
        this.authPluginDataPart2 = authPluginDataPart2;
    }

    public String getAuthPluginName() {
        return authPluginName;
    }

    public void setAuthPluginName(String authPluginName) {
        this.authPluginName = authPluginName;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("protocolVersion=" + protocolVersion)
                .add("serverVersion='" + serverVersion + "'")
                .add("threadId=" + threadId)
                .add("authPluginDataPart1='" + authPluginDataPart1 + "'")
                .add("capabilityFlags=" + capabilityFlags)
                .add("characterSet=" + characterSet)
                .add("statusFlags=" + statusFlags)
                .add("authPluginDataLen=" + authPluginDataLen)
                .add("authPluginDataPart2='" + authPluginDataPart2 + "'")
                .add("authPluginName='" + authPluginName + "'")
                .toString();
    }

}
