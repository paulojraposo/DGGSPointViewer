import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.examples.GeoJSONLoader;

import javax.swing.*;
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
    private String[] csvFieldNames;

    public App(){

    }

    public void initialize(){
        this.mGUI = new MainGUI();
        this.aF = this.mGUI.start("Point Stats on a Discrete Global Grid", MainGUI.AppFrame.class);
        this.loadGeoJSON();
    }

    public void receiveUserFileReference(String aPath){
        this.userCSVFilePath = aPath;
        this.parseCSV(this.userCSVFilePath);
    }

    public void loadGeoJSON(){
        // TODO: Stub, develop me into something useful and dynamic!
        GeoJSONLoader gjLoader = new GeoJSONLoader();
        Layer lyr = gjLoader.createLayerFromSource("out/production/DGGSPointViewer/resources/qtmlevels/qtmlvl4.geojson");
        // TODO: modify layer symbology for adding?
        this.aF.getWwd().getModel().getLayers().add(lyr);
    }

    public InputStream pathToInputStream(String path) throws IOException {
        File initialFile = new File(path);
        InputStream targetStream = new FileInputStream(initialFile);
        return targetStream;
    }

    private void enableBinningButton(){
        this.aF.mainAppPanel.binningButton.setEnabled(true);
    }

    private void setOptionsAndEnableAttrCB(){
        DefaultComboBoxModel cbModel = new DefaultComboBoxModel(csvFieldNames);
        this.aF.mainAppPanel.attrToBinCB.setModel(cbModel);
        this.aF.mainAppPanel.attrToBinCB.setEnabled(true);
    }

    private void parseCSV(String filePath){
        // Read the user CSV, determine the field names, set UI
        // options enabled.
        InputStream iS = null;
        try {
            iS = pathToInputStream(filePath);
            csvParser = new LabeledCSVParser(new CSVParser(iS));
            csvFieldNames = csvParser.getLabels();
            setOptionsAndEnableAttrCB();
            enableBinningButton();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void readCSV(String filePath){
//        pointDataTriplets = new ArrayList<>();
//        InputStream iS = null;
//        try {
//            // Can iteratively get values this way, providing the field name:
//            while (csvParser.getLine() != null) {
//                String popS = csvParser.getValueByLabel("pop");
//                String latS = csvParser.getValueByLabel("latitude");
//                String lonS = csvParser.getValueByLabel("longitude");
//                String[] thisRow = {latS, lonS, popS};
//                System.out.println(thisRow[0] + "|" + thisRow[1] + "|" + thisRow[2]);
//                pointDataTriplets.add(thisRow);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void performBinning(){
        // Here, we need to run a Python script to perform the geodetic
        // binning, using a system call. Ideally that should be in a
        // separate thread, to keep the app UI from being unresponsive.
        // Needs to use the existing QTM level files, normal and lon-shifted
        // versions, at all the levels of defined by the user with the
        // combobox in the GUI. Those should be saved to a temporary
        // location on the user's disk, to be selectable as layers to
        // load onto the globe.

        System.out.println("Would be binning here.");
    }
}
