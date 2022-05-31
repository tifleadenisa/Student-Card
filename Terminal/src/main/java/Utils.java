import com.opencsv.CSVWriter;
import com.sun.javacard.apduio.Apdu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Utils {

    public static byte[] intToByte(int value){
        int length = String.valueOf(value).length();
        byte[] number = new byte[length];
        for (int i = length-1; i >= 0; i--) {
            number[i] = Byte.parseByte(Integer.toHexString(value%10));
            value /= 10;
        }
        return number;
    }

    private static int countGradesUnder(int courseId, int studentId) throws IOException {
        int grades = 0;
        BufferedReader br = new BufferedReader(new FileReader("note.csv"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            if(Objects.equals(values[0].substring(1, values[0].length()-1), Integer.toString(studentId))
                    && Objects.equals(values[1].substring(1, values[1].length()-1), Integer.toString(courseId))
                    && Integer.parseInt(values[2].substring(1, values[2].length()-1)) < 5){
                //&& Integer.parseInt(values[2].substring(1, values[2].length()-1)) < 5
                grades++;
            }
        }
        return grades;
    }

    private static boolean hasGrade11(int courseId, int studentId) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("note.csv"));
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            if(Objects.equals(values[0].substring(1, values[0].length()-1), Integer.toString(studentId))
                    && Objects.equals(values[1].substring(1, values[1].length()-1), Integer.toString(courseId))
                    && Integer.parseInt(values[2].substring(1, values[2].length()-1)) ==  11){
                return true;
            }
        }
        return false;
    }

    private static boolean hasPayedTax(int studentId, int courseId) throws IOException {
        boolean hasPayed = false;
        BufferedReader br = new BufferedReader(new FileReader("note.csv"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            if(Objects.equals(values[0], Integer.toString(studentId))
                    && Objects.equals(values[1], Integer.toString(courseId))
                    && Objects.equals(values[2], Integer.toString(1))){
                hasPayed = true;
            }
        }
        return hasPayed;
    }

    public static int addGradeToDb(int courseId, int studentID, int grade)throws IOException {
        int nota = grade;
        int gradesUnder5 = countGradesUnder(courseId, studentID);
        boolean hasGrade11 = hasGrade11(courseId, studentID);

        if(gradesUnder5 == 2 && !hasPayedTax(studentID, courseId) && !hasGrade11){
            nota = 11;
        }
        if(gradesUnder5 >= 2 && !hasPayedTax(studentID, courseId) && hasGrade11){
            nota = 11;
        }else{
            CSVWriter writer = new CSVWriter(new FileWriter("note.csv", true));

            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            String line =  studentID + ";" + courseId + ";" + nota + ";" + date;
            String [] record = line.split(";");

            writer.writeNext(record);

            writer.close();
        }

        return nota;
    }

    public static int getStudentId(Apdu apdu) throws BufferUnderflowException {
        ByteBuffer wrapped = ByteBuffer.wrap(apdu.dataOut); // big-endian by default
        //wrapped.order(ByteOrder.LITTLE_ENDIAN);
        return wrapped.getShort();
    }

    public static Map<Integer, String> getCourses() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("materii.csv"));
        String line;
        Map<Integer, String> courses = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            if(values[0].length() < 2){
                courses.put(Integer.valueOf(values[0]), values[1]);
            }
        }
        return courses;
    }
}
