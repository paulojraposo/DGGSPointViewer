
import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static String userCSVFilePath;
    private static LabeledCSVParser csvParser;

    public static void main(String[] args) {

        // Set Nimbus Look and Feel.
        // With thanks to BenjaminLinus,
        // https://stackoverflow.com/questions/4617615/how-to-set-nimbus-look-and-feel-in-main.
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                // not worth my time
            }
        }

        MainGUI mGUI = new MainGUI();
        mGUI.start("Point Stats on a Discrete Global Grid", MainGUI.AppFrame.class);

    }

    public static void receiveUserFileReference(String aPath){
        userCSVFilePath = aPath;
        System.out.println(aPath);
        readCSV(aPath);
    }

    public static InputStream pathToInputStream(String path) throws IOException {
        File initialFile = new File(path);
        InputStream targetStream = new FileInputStream(initialFile);
        return targetStream;
    }

    private static void readCSV(String filePath) {
        InputStream iS = null;
        try {
            iS = pathToInputStream(filePath);
            csvParser = new LabeledCSVParser(new CSVParser(iS));
            while (csvParser.getLine() != null) {
                // TODO: implement reading and storing by real fields in the CSV.
                System.out.println(csvParser.getValueByLabel("nameascii"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
