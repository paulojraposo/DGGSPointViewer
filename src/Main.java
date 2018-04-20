import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwindx.examples.GeoJSONLoader;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

public class Main {

    private static String userCSVFilePath;
    private static LabeledCSVParser csvParser;
    private static ArrayList<String[]> pointDataTriplets;

    public static void main(String[] args) {
        setLAF(); // Set Nimbus Look and Feel.
        MainGUI mGUI = new MainGUI();
        MainGUI.AppFrame aF = mGUI.start("Point Stats on a Discrete Global Grid", MainGUI.AppFrame.class);

        // TODO: Below is just for testing. Move actual loading somewhere more sensible :)
        GeoJSONLoader gjLoader = new GeoJSONLoader();
        Layer lyr = gjLoader.createLayerFromSource("out/production/DGGSPointViewer/resources/qtmlevels/qtmlvl5.geojson");
        aF.getWwd().getModel().getLayers().add(lyr);
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
        pointDataTriplets = new ArrayList<>();
        InputStream iS = null;
        try {
            iS = pathToInputStream(filePath);
            csvParser = new LabeledCSVParser(new CSVParser(iS));
            while (csvParser.getLine() != null) {
                // NB: So far, hard-coding for these three fields in the input CSV.
                String popS = csvParser.getValueByLabel("pop");
                String latS = csvParser.getValueByLabel("latitude");
                String lonS = csvParser.getValueByLabel("longitude");
                String[] thisRow = {latS, lonS, popS};
                System.out.println(thisRow[0] + "|" + thisRow[1] + "|" + thisRow[2]);
                pointDataTriplets.add(thisRow);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setLAF(){
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
                ex.printStackTrace();
            }
        }
    }
}
