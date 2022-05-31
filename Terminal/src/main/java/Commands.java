import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.jpcsclite.APDU;

public class Commands {

    public Commands() {}

    public static Apdu createApplet(){
        Apdu apdu = new Apdu();
        apdu.command[APDU.CLA] = (byte) Integer.parseInt("80", 16);
        apdu.command[APDU.INS] = (byte) Integer.parseInt("B8", 16);
        apdu.command[APDU.P1] = (byte) Integer.parseInt("00", 16);
        apdu.command[APDU.P2] = (byte) Integer.parseInt("00", 16);
        apdu.setLc(20);
        apdu.setLe(127);
        apdu.setDataIn(new byte[]{(byte)0x0a, (byte)0xa0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x62, (byte)0x3,
                (byte)0x1, (byte)0xc, (byte)0x6, (byte)0x1, (byte)0x08, (byte)0x0, (byte)0x0, (byte)0x05, (byte)0x01,
                (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05});
        return apdu;
    }

    public static Apdu selectApplet(){
        Apdu apdu = new Apdu();
        apdu.command[APDU.CLA] = (byte) Integer.parseInt("00", 16);
        apdu.command[APDU.INS] = (byte) Integer.parseInt("A4", 16);
        apdu.command[APDU.P1] = (byte) Integer.parseInt("04", 16);
        apdu.command[APDU.P2] = (byte) Integer.parseInt("00", 16);
        apdu.setLc(10);
        apdu.setDataIn(new byte[] {(byte)0xa0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x62, (byte)0x3, (byte)0x1, (byte)0xc, (byte)0x6, (byte)0x1});
        apdu.setLe(127);
        return apdu;
    }

    public static Apdu verifyPin(int pin){
        Apdu apdu = new Apdu();
        apdu.command[APDU.CLA] = (byte) Integer.parseInt("80", 16);
        apdu.command[APDU.INS] = (byte) Integer.parseInt("20", 16);
        apdu.command[APDU.P1] = (byte) Integer.parseInt("00", 16);
        apdu.command[APDU.P2] = (byte) Integer.parseInt("00", 16);
        apdu.setLc(5);
        apdu.setDataIn(Utils.intToByte(pin));
        apdu.setLe(127);
        return apdu;
    }

    public static Apdu gradeStudent(int courseId, int grade){
        Apdu apdu = new Apdu();
        apdu.command[APDU.CLA] = (byte) Integer.parseInt("80", 16);
        apdu.command[APDU.INS] = (byte) Integer.parseInt("70", 16);
        apdu.command[APDU.P1] = (byte) courseId;
        apdu.command[APDU.P2] = (byte) Integer.parseInt("00", 16);
        apdu.setLc(2);
        apdu.setDataIn(Utils.intToByte(grade));
        apdu.setLe(2);
        //System.out.println(apdu);
        return apdu;
    }

    public static Apdu getGrades(){
        Apdu apdu = new Apdu();
        apdu.command[APDU.CLA] = (byte) Integer.parseInt("80", 16);
        apdu.command[APDU.INS] = (byte) Integer.parseInt("80", 16);
        apdu.command[APDU.P1] = (byte) Integer.parseInt("00", 16);
        apdu.command[APDU.P2] = (byte) Integer.parseInt("00", 16);
        apdu.setLc(0);
        apdu.setLe(127);
        return apdu;
    }
}
