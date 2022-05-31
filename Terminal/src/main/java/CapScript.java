import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.jpcsclite.APDU;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class CapScript {

    private ArrayList<Apdu> commands;


    public CapScript() {
        commands = new ArrayList<>();
        readfile();
    }

    private void readfile() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("C:\\Program Files (x86)\\Oracle\\Java Card Development Kit Simulator 3.1.0\\samples\\classic_applets\\Wallet\\applet\\apdu_scripts\\cap-Wallet.script"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if(line.startsWith("0x")){
                line = line.substring(0, line.length() - 1);
                commands.add(getApduBuffer(line));
            }
        }
        scanner.close();
    }

    private Apdu getApduBuffer(String line){
        String[] splited = line.split("\\s+");
        short[] numbers = new short[splited.length];
        for (int i = 0; i < splited.length; i++) {
            splited[i] = splited[i].substring(2);
            //numbers[i] = Integer.decode(splited[i]).shortValue();
            numbers[i] = (short) Integer.parseInt(splited[i], 16);
        }
        return getCommand(numbers);
    }

    private Apdu getCommand(short[] numbers){
        Apdu apdu = new Apdu();
        apdu.command[APDU.CLA] = (byte) numbers[0];
        apdu.command[APDU.INS] = (byte) numbers[1];
        apdu.command[APDU.P1] = (byte) numbers[2];
        apdu.command[APDU.P2] = (byte) numbers[3];
        apdu.setLc(numbers[4]);
        apdu.setLe(numbers[numbers.length-1]);

        //if lc != 0
        if(numbers[4] != 0){
            byte[] dataIn = new byte[numbers.length - 6];
            for (int i = 0; i < dataIn.length; i++) {
                dataIn[i] = (byte) numbers[5 + i];
            }
            apdu.setDataIn(dataIn);
        }
        return apdu;
    }

    public ArrayList<Apdu> getCommands() {
        return commands;
    }
}
