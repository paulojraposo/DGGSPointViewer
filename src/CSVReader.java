import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class CSVReader {

    // With thanks to authors of this:
    // http://www.javainterviewpoint.com/how-to-read-and-parse-csv-file-in-java/

    public CSVReader(){

    }

    public void readCSV(){ // TODO: make this accept a file

        Scanner scanner = null;

        //Delimiters used in the CSV file
        String COMMA_DELIMITER = ",";

        try {
            //Get a scanner instance
            scanner = new Scanner(new File("Employee.csv")); // TODO: put in read-in file
            // Use Delimiter as COMMA
            scanner.useDelimiter(COMMA_DELIMITER);
            while(scanner.hasNext())
            {
                System.out.print(scanner.next()+"   ");
            }
        }
        catch (FileNotFoundException fe)
        {
            fe.printStackTrace();
        }
//        finally
//        {
//            scanner.close();
//        }

    }

}


