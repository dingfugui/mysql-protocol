package prrety;

public class GenericResponsePacket {

    private int header;

    public void parse(MySqlPacketPayload payload) {
        payload.read();

        int type = payload.readFixInt(1);
        header = type;
        if (type == 0) {
            //ok;
        } else if (type == (byte) 0xff) {
            //err
            int code = payload.readFixInt(2);
            System.out.println("error code:" + code);
            //marker
            String marker = payload.readFixString(1);
            System.out.println("marker:" + marker);
            //state
            String state = payload.readFixString(5);
            System.out.println("code:" + state);

            String msg = payload.readEOFString();
            System.out.println("error:" + msg);
        }
    }

    public boolean isOk() {
        return ((byte) header) == 0x00 || ((byte) header) == (byte) 0xFE;
    }

    public int getHeader() {
        return header;
    }
}
