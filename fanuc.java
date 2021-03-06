import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Main {
    private final static int FRAMEHEADER=0xa0a0a0a0;
    private final static short FTYPE_OPN_REQU=0x0101,FTYPE_OPN_RESP=0x0102;
    private final static short FTYPE_VAR_REQU=0x2101,FTYPE_VAR_RESP=0x2102;
    private final static short FTYPE_CLS_REQU=0x0201,FTYPE_CLS_RESP=0x0202;

    public static void main(String[] args) {
        byte[] n1=new byte[]{1,2,3,4,5,6,7,8};
        byte[] n2=new byte[]{9,10,11,12,13,14,15};
        byte[] h=encap(new byte[][] {n1,n2},FTYPE_VAR_REQU);
        for(byte b:h) {
            System.out.printf("0x%02x ",b);
        }
        System.out.println();
        h[7]=2;
        try {
            byte[][] k = decap(h);
        }
        catch(IllegalAccessException e) {

        }
    }

    static byte[] encap(byte[] payload, short type) {
        ByteBuffer b = ByteBuffer.allocate(1500);
        b.order(ByteOrder.BIG_ENDIAN);
        b.putInt(FRAMEHEADER); //Header
        b.putShort((short) 1); //Version
        b.putShort(type); //Request
        b.putShort((short) 0); //Placeholder Length
        b.putShort((short) 1);

        b.putShort((short) (payload.length + 2));
        b.put(payload);

        b.putShort(8, (short) (b.position() - 10));

        byte[] ret = new byte[b.position()];
        b.position(0);
        b.get(ret);
        return ret;
    }

    static byte[] encap(byte[][] payload, short type) {
        ByteBuffer b=ByteBuffer.allocate(1500);
        b.order(ByteOrder.BIG_ENDIAN);
        b.putInt(FRAMEHEADER); //Header
        b.putShort((short) 1); //Version
        b.putShort(type); //Request
        b.putShort((short)0); //Placeholder Length
        b.putShort((short)payload.length);

        for(byte[] t : payload) {
            b.putShort((short)(t.length+2));
            b.put(t);
        }

        b.putShort(8,(short)(b.position()-10));

        byte[] ret=new byte[b.position()];
        b.position(0);
        b.get(ret);
        return ret;
    }

    static byte[][] decap(byte[] packetdata) throws IllegalAccessException{
         if(packetdata.length>=10) {
            if(packetdata[0] == (byte)0xa0 && packetdata[1] == (byte)0xa0 && packetdata[2] == (byte)0xa0 && packetdata[3] == (byte)0xa0) {
                int fvers = packetdata[4] << 8 | packetdata[5], ftype = packetdata[6] << 8 | packetdata[7];
                int len1 = packetdata[8] << 8 | packetdata[9];
                if(len1 + 10 == packetdata.length) {
                    if(len1==0) return null;
                    if(ftype==FTYPE_VAR_RESP) { //Response
                        int qu=packetdata[10] << 8 | packetdata[11];
                        if(qu>((packetdata.length-12)/2))
                            throw new IllegalAccessException("PROTOCOL decode error");
                        byte[][] re=new byte[qu][];
                        int count=0,n=12;
                        try {
                            while (n < packetdata.length) {
                                int le = packetdata[n] << 8 | packetdata[n + 1];
                                byte[] data = new byte[le - 2];
                                n += 2;
                                for (int n2 = 0; n2 < (le - 2); n2++)
                                    data[n2] = packetdata[n++];
                                re[count++] = data;
                            }
                        }
                        catch(ArrayIndexOutOfBoundsException e) {
                            throw new IllegalAccessException("PROTOCOL decode error");
                        }
                        if(count != re.length) throw new IllegalAccessException("PROTOCOL decode error");
                        return re;
                    }
                }
            }
        }
        throw new IllegalAccessException("PROTOCOL decode error");
    }
}
