package jdk;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class HandshakeResponse {

    private int capabilityFlags;
    private int maxPacketSize;
    private int characterSet;
    private String username;
    private int authResponseLength;
    private byte[] authResponse;
    private String database;
    private String clientPluginName;
    private Map<String, String> attributes = new HashMap<>();


    public int getCapabilityFlags() {
        return capabilityFlags;
    }

    public void setCapabilityFlags(int capabilityFlags) {
        this.capabilityFlags = capabilityFlags;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    public int getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(int characterSet) {
        this.characterSet = characterSet;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAuthResponseLength() {
        return authResponseLength;
    }

    public void setAuthResponseLength(int authResponseLength) {
        this.authResponseLength = authResponseLength;
    }

    public byte[] getAuthResponse() {
        return authResponse;
    }

    public void setAuthResponse(byte[] authResponse) {
        this.authResponse = authResponse;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getClientPluginName() {
        return clientPluginName;
    }

    public void setClientPluginName(String clientPluginName) {
        this.clientPluginName = clientPluginName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("capabilityFlags=" + capabilityFlags)
                .add("maxMacketSize=" + maxPacketSize)
                .add("characterSet=" + characterSet)
                .add("username='" + username + "'")
                .add("authResponseLength=" + authResponseLength)
                .add("authResponse='" + new String(authResponse) + "'")
                .add("database='" + database + "'")
                .add("clientPluginName='" + clientPluginName + "'")
                .add("attributes=" + attributes)
                .toString();
    }
}
