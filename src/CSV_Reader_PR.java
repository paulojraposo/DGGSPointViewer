import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class CSV_Reader_PR {

    private ArrayList<String> headers;

    // With thanks to authors of this:
    // http://www.javainterviewpoint.com/how-to-read-and-parse-csv-file-in-java/

    public CSV_Reader_PR(){

    }

    public void readCSV(String filePath){

        Scanner scanner = null;

        //Delimiters used in the CSV file
        String COMMA_DELIMITER = ",";

        try {

            //Get a scanner instance
            scanner = new Scanner(new File(filePath));
            // Use Delimiter as COMMA
            scanner.useDelimiter(COMMA_DELIMITER);

            this.headers = new ArrayList<>();

            int i = 0;
            while(scanner.hasNext())
            {
                if (i == 0){

                }
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


