import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import com.sun.javacard.apduio.CadTransportException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class Connection {
    InputStream is;
    OutputStream os;
    CadClientInterface cad;
    public Connection() throws IOException, CadTransportException {
        String crefFilePath = "C:\\Program Files (x86)\\Oracle\\Java Card Development Kit Simulator 3.1.0\\bin\\cref.bat";
        Process process;
        process = Runtime.getRuntime().exec(crefFilePath);
        Socket sock;
        sock = new Socket("localhost", 9025);
        InputStream is = sock.getInputStream();
        OutputStream os = sock.getOutputStream();
        cad=CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T0, is, os);

        cad.powerUp();

        //cap-wallet
        CapScript capScript = new CapScript();
        ArrayList<Apdu> commands = capScript.getCommands();
        for(Apdu command: commands){
            cad.exchangeApdu(command);
        }

        //initialize wallet
        Apdu apdu = Commands.createApplet();
        cad.exchangeApdu(apdu);
        apdu = Commands.selectApplet();
        cad.exchangeApdu(apdu);


        //send pin to smartcard
        Scanner keyboard = new Scanner(System.in);
        System.out.println("STUDENT: enter pin");
        int pin = keyboard.nextInt();

        //verify pin
        apdu = Commands.verifyPin(pin);
        cad.exchangeApdu(apdu);


        if(Arrays.toString(apdu.getSw1Sw2()).equals("[-112, 0]")){
            System.out.println("Pin verified");
            Map<Integer, String> courses = Utils.getCourses();
            System.out.println();
            System.out.println("PROFESSOR: Choose option or course id");
            System.out.println("0: exit");
            for(Integer i: courses.keySet()){
                System.out.println(i + ": " + courses.get(i));
            }
            int courseId = keyboard.nextInt();
            while(courseId != 0){
                System.out.println("PROFESSOR: Enter grade:");
                int grade = keyboard.nextInt();
                //add grade in smart card
                apdu = Commands.gradeStudent(courseId, grade);
                cad.exchangeApdu(apdu);
                //System.out.println(apdu);

                int studentID = Utils.getStudentId(apdu);
                //add in database
                int givenGrade = Utils.addGradeToDb(courseId, studentID, grade);
                System.out.println("Given grade: " + givenGrade);


                System.out.println();
                System.out.println("PROFESSOR: Choose option or course id");
                System.out.println("0: exit");
                for(Integer i: courses.keySet()){
                    System.out.println(i + ": " + courses.get(i));
                }
                courseId = keyboard.nextInt();
            }

            System.out.println("The student has the following grades:");
            apdu = Commands.getGrades();
            cad.exchangeApdu(apdu);
            System.out.println(apdu);
        }
        else{
            System.out.println("Incorrect pin");
        }









        cad.powerDown();
    }

    private void cap() throws CadTransportException, IOException {
        Apdu apdu = new Apdu(new byte[]{(byte)0x00, (byte)(byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x09, (byte)(byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x03, (byte)0x01, (byte)0x08, (byte)0x01, (byte)0x7F});
        System.out.println(apdu);
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        //header
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x7f});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)0x01, (byte)0x00, (byte)0x17, (byte)0x01, (byte)0x00, (byte)0x14, (byte)(byte)0xDE, (byte)(byte)0xCA, (byte)(byte)0xFF, (byte)(byte)0xED, (byte)0x03, (byte)0x02, (byte)0x04, (byte)0x00, (byte)0x01, (byte)0x09, (byte)(byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62, (byte)0x03, (byte)0x01, (byte)0x0C, (byte)0x06, (byte)0x00, (byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x01, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        // com/oracle/jcclassic/samples/wallet/javacard/Directory.cap
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x25, (byte)(byte)0x00, (byte)(byte)0x14, (byte)(byte)0x00, (byte)(byte)0x25, (byte)(byte)0x00, (byte)(byte)0x0E, (byte)(byte)0x00, (byte)(byte)0x15, (byte)(byte)0x00, (byte)(byte)0x62, (byte)(byte)0x00, (byte)(byte)0x1D, (byte)(byte)0x01, (byte)(byte)0xC9, (byte)(byte)0x00, (byte)(byte)0x0A, (byte)(byte)0x00, (byte)(byte)0x3C, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0xD6, (byte)(byte)0x07, (byte)(byte)0x28, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x08, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x02, (byte)(byte)0x01, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        // com/oracle/jcclassic/samples/wallet/javacard/Import.cap
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)(byte)0x04, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x04, (byte)(byte)0x00, (byte)(byte)0x18, (byte)(byte)0x04, (byte)(byte)0x00, (byte)(byte)0x15, (byte)(byte)0x02, (byte)(byte)0x08, (byte)(byte)0x01, (byte)(byte)0x07, (byte)(byte)0xA0, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x62, (byte)(byte)0x01, (byte)(byte)0x01, (byte)(byte)0x00, (byte)(byte)0x01, (byte)(byte)0x07, (byte)(byte)0xA0, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x62, (byte)(byte)0x00, (byte)(byte)0x01, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x04, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        // com/oracle/jcclassic/samples/wallet/javacard/Applet.cap
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)(byte)0x03, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x03, (byte)(byte)0x00, (byte)(byte)0x11, (byte)(byte)0x03, (byte)(byte)0x00, (byte)(byte)0x0E, (byte)(byte)0x01, (byte)(byte)0x0A, (byte)(byte)0xA0, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x62, (byte)(byte)0x03, (byte)(byte)0x01, (byte)(byte)0x0C, (byte)(byte)0x06, (byte)(byte)0x01, (byte)(byte)0x00, (byte)(byte)0x41, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x03, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        // com/oracle/jcclassic/samples/wallet/javacard/Class.cap
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x1D, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x80, (byte)(byte)0x03, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x01, (byte)(byte)0x04, (byte)(byte)0x04, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x5A, (byte)(byte)0xFF, (byte)(byte)0xFF, (byte)(byte)0x00, (byte)(byte)0x4D, (byte)(byte)0x00, (byte)(byte)0x62, (byte)(byte)0x00, (byte)(byte)0x01, (byte)(byte)0x02, (byte)(byte)0x03, (byte)(byte)0x04, (byte)(byte)0x05, (byte)(byte)0x06, (byte)(byte)0x07, (byte)(byte)0x08, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        // com/oracle/jcclassic/samples/wallet/javacard/Method.cap
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x07, (byte)(byte)0x01, (byte)(byte)0xC9, (byte)(byte)0x00, (byte)(byte)0x05, (byte)(byte)0x43, (byte)(byte)0x18, (byte)(byte)0x8C, (byte)(byte)0x00, (byte)(byte)0x03, (byte)(byte)0x18, (byte)(byte)0x8F, (byte)(byte)0x00, (byte)(byte)0x13, (byte)(byte)0x3D, (byte)(byte)0x06, (byte)(byte)0x10, (byte)(byte)0x08, (byte)(byte)0x8C, (byte)(byte)0x00, (byte)(byte)0x02, (byte)(byte)0x87, (byte)(byte)0x00, (byte)(byte)0x19, (byte)(byte)0x1E, (byte)(byte)0x25, (byte)(byte)0x29, (byte)(byte)0x04, (byte)(byte)0x1E, (byte)(byte)0x16, (byte)(byte)0x04, (byte)(byte)0x41, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x04, (byte)(byte)0x41, (byte)(byte)0x31, (byte)(byte)0x19, (byte)(byte)0x1E, (byte)(byte)0x25, (byte)(byte)0x29, (byte)(byte)0x05, (byte)(byte)0x1E, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x41, (byte)(byte)0x04, (byte)(byte)0x41, (byte)(byte)0x31, (byte)(byte)0x19, (byte)(byte)0x1E, (byte)(byte)0x25, (byte)(byte)0x29, (byte)(byte)0x06, (byte)(byte)0xAD, (byte)(byte)0x00, (byte)(byte)0x19, (byte)(byte)0x1E, (byte)(byte)0x04, (byte)(byte)0x41, (byte)(byte)0x16, (byte)(byte)0x06, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x04, (byte)(byte)0x18, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x05, (byte)(byte)0x7A, (byte)(byte)0x04, (byte)(byte)0x30, (byte)(byte)0x8F, (byte)(byte)0x00, (byte)(byte)0x06, (byte)(byte)0x18, (byte)(byte)0x1D, (byte)(byte)0x1E, (byte)(byte)0x8C, (byte)(byte)0x00, (byte)(byte)0x07, (byte)(byte)0x7A, (byte)(byte)0x01, (byte)(byte)0x10, (byte)(byte)0xAD, (byte)(byte)0x00, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x08, (byte)(byte)0x61, (byte)(byte)0x04, (byte)(byte)0x03, (byte)(byte)0x78, (byte)(byte)0x04, (byte)(byte)0x78, (byte)(byte)0x01, (byte)(byte)0x10, (byte)(byte)0xAD, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x00, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x09, (byte)(byte)0x7A, (byte)(byte)0x02, (byte)(byte)0x21, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x0A, (byte)(byte)0x2D, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x0B, (byte)(byte)0x60, (byte)(byte)0x10, (byte)(byte)0x1A, (byte)(byte)0x04, (byte)(byte)0x25, (byte)(byte)0x10, (byte)(byte)0xA4, (byte)(byte)0x6B, (byte)(byte)0x03, (byte)(byte)0x7A, (byte)(byte)0x11, (byte)(byte)0x6E, (byte)(byte)0x00, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x1A, (byte)(byte)0x03, (byte)(byte)0x25, (byte)(byte)0x10, (byte)(byte)0x80, (byte)(byte)0x6A, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x6E, (byte)(byte)0x00, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x1A, (byte)(byte)0x04, (byte)(byte)0x25, (byte)(byte)0x75, (byte)(byte)0x00, (byte)(byte)0x2D, (byte)(byte)0x00, (byte)(byte)0x04, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x00, (byte)(byte)0x27, (byte)(byte)0x00, (byte)(byte)0x30, (byte)(byte)0x00, (byte)(byte)0x21, (byte)(byte)0x00, (byte)(byte)0x40, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x1B, (byte)(byte)0x00, (byte)(byte)0x50, (byte)(byte)0x00, (byte)(byte)0x15, (byte)(byte)0x18, (byte)(byte)0x19, (byte)(byte)0x8C, (byte)(byte)0x00, (byte)(byte)0x0D, (byte)(byte)0x7A, (byte)(byte)0x18, (byte)(byte)0x19, (byte)(byte)0x8C, (byte)(byte)0x00, (byte)(byte)0x0E, (byte)(byte)0x7A, (byte)(byte)0x18, (byte)(byte)0x19, (byte)(byte)0x8C, (byte)(byte)0x00, (byte)(byte)0x0F, (byte)(byte)0x7A, (byte)(byte)0x18, (byte)(byte)0x19, (byte)(byte)0x8C, (byte)(byte)0x00, (byte)(byte)0x10, (byte)(byte)0x7A, (byte)(byte)0x11, (byte)(byte)0x6D, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x7A, (byte)(byte)0x03, (byte)(byte)0x24, (byte)(byte)0xAD, (byte)(byte)0x00, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x11, (byte)(byte)0x61, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x63, (byte)(byte)0x01, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x0A, (byte)(byte)0x2D, (byte)(byte)0x1A, (byte)(byte)0x07, (byte)(byte)0x25, (byte)(byte)0x32, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x12, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x5B, (byte)(byte)0x29, (byte)(byte)0x04, (byte)(byte)0x1F, (byte)(byte)0x04, (byte)(byte)0x6B, (byte)(byte)0x07, (byte)(byte)0x16, (byte)(byte)0x04, (byte)(byte)0x04, (byte)(byte)0x6A, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x67, (byte)(byte)0x00, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x1A, (byte)(byte)0x08, (byte)(byte)0x25, (byte)(byte)0x29, (byte)(byte)0x05, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x10, (byte)(byte)0x7F, (byte)(byte)0x6E, (byte)(byte)0x06, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x63, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x6A, (byte)(byte)0x83, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0xAF, (byte)(byte)0x01, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x41, (byte)(byte)0x11, (byte)(byte)0x7F, (byte)(byte)0xFF, (byte)(byte)0x6F, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x6A, (byte)(byte)0x84, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x18, (byte)(byte)0xAF, (byte)(byte)0x01, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x41, (byte)(byte)0x89, (byte)(byte)0x01, (byte)(byte)0x7A, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x03, (byte)(byte)0x24, (byte)(byte)0xAD, (byte)(byte)0x00, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x11, (byte)(byte)0x61, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x63, (byte)(byte)0x01, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x0A, (byte)(byte)0x2D, (byte)(byte)0x1A, (byte)(byte)0x07, (byte)(byte)0x25, (byte)(byte)0x32, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x12, (byte)(byte)0x5B, (byte)(byte)0x29, (byte)(byte)0x04, (byte)(byte)0x1F, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x04, (byte)(byte)0x6B, (byte)(byte)0x07, (byte)(byte)0x16, (byte)(byte)0x04, (byte)(byte)0x04, (byte)(byte)0x6A, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x67, (byte)(byte)0x00, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x1A, (byte)(byte)0x08, (byte)(byte)0x25, (byte)(byte)0x29, (byte)(byte)0x05, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x10, (byte)(byte)0x7F, (byte)(byte)0x6E, (byte)(byte)0x06, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x63, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x6A, (byte)(byte)0x83, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0xAF, (byte)(byte)0x01, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x43, (byte)(byte)0x63, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x6A, (byte)(byte)0x85, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x18, (byte)(byte)0xAF, (byte)(byte)0x01, (byte)(byte)0x16, (byte)(byte)0x05, (byte)(byte)0x43, (byte)(byte)0x89, (byte)(byte)0x01, (byte)(byte)0x7A, (byte)(byte)0x04, (byte)(byte)0x22, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x0A, (byte)(byte)0x2D, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x14, (byte)(byte)0x32, (byte)(byte)0x1F, (byte)(byte)0x05, (byte)(byte)0x6D, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x67, (byte)(byte)0x00, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x19, (byte)(byte)0x05, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x15, (byte)(byte)0x1A, (byte)(byte)0x03, (byte)(byte)0xAF, (byte)(byte)0x01, (byte)(byte)0x10, (byte)(byte)0x08, (byte)(byte)0x4F, (byte)(byte)0x5B, (byte)(byte)0x38, (byte)(byte)0x1A, (byte)(byte)0x04, (byte)(byte)0xAF, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x01, (byte)(byte)0x11, (byte)(byte)0x00, (byte)(byte)0xFF, (byte)(byte)0x53, (byte)(byte)0x5B, (byte)(byte)0x38, (byte)(byte)0x19, (byte)(byte)0x03, (byte)(byte)0x05, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x16, (byte)(byte)0x7A, (byte)(byte)0x04, (byte)(byte)0x22, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x0A, (byte)(byte)0x2D, (byte)(byte)0x19, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x12, (byte)(byte)0x5B, (byte)(byte)0x32, (byte)(byte)0xAD, (byte)(byte)0x00, (byte)(byte)0x1A, (byte)(byte)0x08, (byte)(byte)0x1F, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x8B, (byte)(byte)0x00, (byte)(byte)0x17, (byte)(byte)0x61, (byte)(byte)0x08, (byte)(byte)0x11, (byte)(byte)0x63, (byte)(byte)0x00, (byte)(byte)0x8D, (byte)(byte)0x00, (byte)(byte)0x0C, (byte)(byte)0x7A, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x07, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        // com/oracle/jcclassic/samples/wallet/javacard/StaticField.cap
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)(byte)0x08, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x08, (byte)(byte)0x00, (byte)(byte)0x0D, (byte)(byte)0x08, (byte)(byte)0x00, (byte)(byte)0x0A, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x08, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);


        // com/oracle/jcclassic/samples/wallet/javacard/ConstantPool.cap
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)(byte)0x05, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x05, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x05, (byte)(byte)0x00, (byte)(byte)0x62, (byte)(byte)0x00, (byte)(byte)0x18, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x02, (byte)(byte)0x00, (byte)(byte)0x02, (byte)(byte)0x01, (byte)(byte)0x06, (byte)(byte)0x80, (byte)(byte)0x09, (byte)(byte)0x00, (byte)(byte)0x06, (byte)(byte)0x80, (byte)(byte)0x03, (byte)(byte)0x00, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x09, (byte)(byte)0x08, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x03, (byte)(byte)0x01, (byte)(byte)0x01, (byte)(byte)0x00, (byte)(byte)0x02, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x05, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x00, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x01, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x09, (byte)(byte)0x02, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x09, (byte)(byte)0x05, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x0A, (byte)(byte)0x01, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x0A, (byte)(byte)0x0E, (byte)(byte)0x06, (byte)(byte)0x80, (byte)(byte)0x07, (byte)(byte)0x01, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x01, (byte)(byte)0x76, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x01, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x05, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x1D, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0xC1, (byte)(byte)0x06, (byte)(byte)0x00, (byte)(byte)0x01, (byte)(byte)0xAB, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x09, (byte)(byte)0x04, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x0A, (byte)(byte)0x06, (byte)(byte)0x01, (byte)(byte)0x80, (byte)(byte)0x09, (byte)(byte)0x00, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x0A, (byte)(byte)0x07, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x0A, (byte)(byte)0x09, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x0A, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x05, (byte)(byte)0x00, (byte)(byte)0x05, (byte)(byte)0x04, (byte)(byte)0x03, (byte)(byte)0x80, (byte)(byte)0x09, (byte)(byte)0x01, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x05, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        // com/oracle/jcclassic/samples/wallet/javacard/RefLocation.cap
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB2, (byte)(byte)0x09, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x09, (byte)(byte)0x00, (byte)(byte)0x20, (byte)(byte)0x09, (byte)(byte)0x00, (byte)(byte)0x3C, (byte)(byte)0x00, (byte)(byte)0x0F, (byte)(byte)0x13, (byte)(byte)0x1F, (byte)(byte)0x1E, (byte)(byte)0x0D, (byte)(byte)0x67, (byte)(byte)0x41, (byte)(byte)0x11, (byte)(byte)0x05, (byte)(byte)0x05, (byte)(byte)0x41, (byte)(byte)0x0E, (byte)(byte)0x05, (byte)(byte)0x20, (byte)(byte)0x09, (byte)(byte)0x1C, (byte)(byte)0x00, (byte)(byte)0x29, (byte)(byte)0x05, (byte)(byte)0x04, (byte)(byte)0x07, (byte)(byte)0x2A, (byte)(byte)0x04, (byte)(byte)0x06, (byte)(byte)0x06, (byte)(byte)0x08, (byte)(byte)0x0D, (byte)(byte)0x07, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB4, (byte)(byte)0x09, (byte)(byte)0x00, (byte)(byte)0x1F, (byte)(byte)0x05, (byte)(byte)0x10, (byte)(byte)0x0D, (byte)(byte)0x1D, (byte)(byte)0x06, (byte)(byte)0x06, (byte)(byte)0x06, (byte)(byte)0x07, (byte)(byte)0x08, (byte)(byte)0x08, (byte)(byte)0x04, (byte)(byte)0x09, (byte)(byte)0x12, (byte)(byte)0x15, (byte)(byte)0x10, (byte)(byte)0x10, (byte)(byte)0x08, (byte)(byte)0x04, (byte)(byte)0x09, (byte)(byte)0x12, (byte)(byte)0x15, (byte)(byte)0x0D, (byte)(byte)0x0F, (byte)(byte)0x05, (byte)(byte)0x0B, (byte)(byte)0x05, (byte)(byte)0x19, (byte)(byte)0x07, (byte)(byte)0x05, (byte)(byte)0x0A, (byte)(byte)0x08, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBC, (byte)(byte)0x09, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xBA, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x00, (byte)(byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);

        apdu = new Apdu(new byte[]{(byte)0x80, (byte)(byte)0xB8, (byte)0x00, (byte)0x00, (byte)0x14, (byte)0x0a, (byte)(byte)0xa0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x62, (byte)0x3, (byte)0x1, (byte)0xc, (byte)0x6, (byte)0x1, (byte)0x08, (byte)0x0, (byte)0x0, (byte)0x05, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
        apdu = new Apdu(new byte[]{(byte)0x00, (byte)(byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x0a, (byte)(byte)0xa0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x62, (byte)0x3, (byte)0x1, (byte)0xc, (byte)0x6, (byte)0x1, (byte)0x7F});
        cad.exchangeApdu(apdu);
        System.out.println(apdu);
    }
}
