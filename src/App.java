import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.examples.GeoJSONLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class App {

    private MainGUI mGUI;
    private static String userCSVFilePath;
    private static LabeledCSVParser csvParser;
    private static ArrayList<String[]> pointDataTriplets;
    private MainGUI.AppFrame aF;

    public App(){

    }

    public void initialize(){
        this.mGUI = new MainGUI();
        this.aF = this.mGUI.start("Point Stats on a Discrete Global Grid", MainGUI.AppFrame.class);
        this.loadGeoJSON();
    }

    public void receiveUserFileReference(String aPath){
        this.userCSVFilePath = aPath;
        System.out.println(aPath);
        this.readCSV(aPath);
    }

    public void loadGeoJSON(){
        // TODO: Stub, develop me into something useful and dynamic!
        GeoJSONLoader gjLoader = new GeoJSONLoader();
        Layer lyr = gjLoader.createLayerFromSource("out/production/DGGSPointViewer/resources/qtmlevels/qtmlvl5.geojson");
        // TODO: modify layer symbology for adding?
        this.aF.getWwd().getModel().getLayers().add(lyr);
    }

    public InputStream pathToInputStream(String path) throws IOException {
        File initialFile = new File(path);
        InputStream targetStream = new FileInputStream(initialFile);
        return targetStream;
    }

    private void readCSV(String filePath) {
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
}
