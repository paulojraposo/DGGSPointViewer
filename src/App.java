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
import java.util.HashMap;

public class App {

    private MainGUI mGUI;
    private static String userCSVFilePath;
    private static LabeledCSVParser csvParser;
    private static ArrayList<String[]> pointDataTriplets;
    private MainGUI.AppFrame aF;
    private String[] csvFieldNames;
    private int maximumTranslationDegrees = 9; // absolute value, -9 to 9.
    private int maxBinningLevel = 11; // 11 by default, user-changeable.
    private String attrToBin;
    public String[] levelOptions = new String[]{"1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
    // To keep track of intersected QTM facets with binned data,
    // we keep a hashmap of hashmaps. The first hashmap is keyed
    // by QTM level, and those it returns to be keyed by
    // longitudinal shift.
    public HashMap<Integer,HashMap> intersectionLevelHM;

    public App(){

    }

    public void initialize(){
        this.mGUI = new MainGUI();
        this.aF = this.mGUI.start("Point Stats on a Discrete Global Grid", MainGUI.AppFrame.class);
        this.loadGeoJSON();
    }

    public void setMaxBinningLevel(Integer lvl){
        maxBinningLevel = lvl;
    }

    public Integer getMaxBinningLevel(){
        return maxBinningLevel;
    }

    public Integer getMaxTranslationDegrees(){
        return maximumTranslationDegrees;
    }

    public void setAttrToBin(String attribute){
        attrToBin = attribute;
    }

    public void receiveUserCSVPath(String aPath){
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

    private void setAttrCBOptionsAndEnable(){
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
            setAttrCBOptionsAndEnable();
            enableBinningButton();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void plotCSVPoints(){
        MarkerLayerMaker mlm = new MarkerLayerMaker(csvParser);
        mlm.makeMarkers();
        Layer markerLayer = mlm.makeMarkerLayer();
        this.aF.getWwd().getModel().getLayers().add(markerLayer);
    }

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

        plotCSVPoints(); // just for testing and for show right now.

    }
        private void readCSV(String filePath){
        pointDataTriplets = new ArrayList<>();
        InputStream iS = null;
        try {
            // Can iteratively get values this way, providing the field name:
            while (csvParser.getLine() != null) {
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
