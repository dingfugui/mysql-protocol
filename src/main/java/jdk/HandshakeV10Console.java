package jdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HandshakeV10Console {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 3306));
        InputStream in = socket.getInputStream();

        byte[] head = new byte[4];
        while (in.read(head) != 4) {
        }

        final int length = head[0] + (head[1] & 0xff << 8) + (head[2] & 0xff << 16);
        System.out.println("length:" + length);
        final int seq = head[3];
        System.out.println("seq:" + seq);

        byte[] body = new byte[length];
        while ((in.read(body)) != length) {
        }

        final int protocolVersion = body[0];
        System.out.println("protocolVersion:" + protocolVersion);

        int position = 1, p = 0;
        for (; ; ) {
            if (body[position + p] == 0) {
                break;
            }
            p++;
        }
        byte[] ssa = new byte[p];
        System.arraycopy(body, position, ssa, 0, ssa.length);
        final String serverVersion = new String(ssa);
        System.out.println("serverVersion:" + serverVersion);

        position = position + p + 1;

        byte[] cida = new byte[4];
        System.arraycopy(body, position, cida, 0, cida.length);
        final int connectionId = (cida[0] & 0xff) + ((cida[1] & 0xff) << 8) + ((cida[2] & 0xff) << 16) + ((cida[3] & 0xff) << 24);
        System.out.println("connectionId:" + connectionId);

        position += 4;

        // auth-plugin-data-part-1
        byte[] apdpa1 = new byte[8];
        System.arraycopy(body, position, apdpa1, 0, apdpa1.length);
        final String authPluginDataPart1 = new String(apdpa1);
        System.out.println("authPluginDataPart1:" + authPluginDataPart1);

        position += 9; //filler(1) == 0x00


        //capability_flag_1 (2)
        byte[] cfa = new byte[2];
        System.arraycopy(body, position, cfa, 0, cfa.length);
        final int capabilityFlag = (cfa[0] & 0xff) + ((cfa[1] & 0xff) << 8);
        System.out.println("capabilityFlag:" + capabilityFlag);//65535 = ffff

        position += 2;

        //character_set (1)
        final int characterSet = (body[position] & 0xff);
        System.out.println("characterSet:" + characterSet);//33 = utf8_general_ci

        position += 1;

        //status_flags (2)
        final int statusFlags = (body[position] & 0xff) + ((body[position + 1] & 0xff) << 8);
        System.out.println("statusFlags:" + statusFlags);//2 = auto-commit is enabled

        position += 2;

        //capability_flag_2 (2)
        byte[] cfa2 = new byte[2];
        System.arraycopy(body, position, cfa2, 0, cfa.length);
        final int capabilityFlag2 = ((cfa2[0] & 0xff) << 16) + ((cfa2[1] & 0xff) << 24);
        System.out.println("capabilityFlag2:" + capabilityFlag2);//65535 = ffff

        position += 2;

        //auth_plugin_data_len (1)
        final int authPluginDataLen = (body[position] & 0xff);
        System.out.println("authPluginDataLen:" + authPluginDataLen);// 0x00080000

        position += 1;

        position += 10;//reserved (all [00])

        int capabilities = capabilityFlag + capabilityFlag2;
        if ((capabilities & 0x00008000) != 0) {
            // auth-plugin-data-part-2
            int len = Math.max(13, authPluginDataLen - 8);
            System.out.println("auth-plugin-data-part-2 length:" + len);
            byte[] apdpa2 = new byte[len];
            System.arraycopy(body, position, apdpa2, 0, apdpa2.length);
            String authPluginDataPart2 = new String(apdpa2);
            System.out.println("authPluginDataPart2:" + authPluginDataPart2);
            position += len;
        }


        if ((capabilities & 0x00080000) != 0) {
            //auth-plugin name
            int p2 = 0;
            for (; ; ) {
                if (body[position + p2] == 0) {
                    break;
                }
                p2++;
            }
            byte[] apna = new byte[p2];
            System.arraycopy(body, position, apna, 0, apna.length);
            final String authPluginName = new String(apna);
            System.out.println("authPluginName:" + authPluginName);
            position = position + p2 + 1;
        }
        System.out.println(position);


        socket.close();
    }
}
