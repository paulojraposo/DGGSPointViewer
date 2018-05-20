import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

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

    String attrToBin;

    // Limited maxBinningLevel to 7, down from 11, 2018-05-18, for performance's sake, for now.
    private int maxBinningLevel = 6; // 7th level's index, by default, user-changeable.
    public String[] levelOptions = new String[]{"1", "2", "3", "4", "5", "6", "7"};//,
            //"8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};

    // To keep track of intersected QTM facets with binned data,
    // we keep a hashmap of hashmaps. The first hashmap is keyed
    // by QTM level, and those it returns to be keyed by
    // longitudinal shift.
    public HashMap<Integer,HashMap> intersectionLevelHM;

    public int maxQTMLevels = 6;
    public int defaultQTMLevel = 4;
    private int currentlySelectedQTMLevel = defaultQTMLevel;

    public Integer maximumLonShift = 5;
    public Integer minimumLonShift = maximumLonShift * -1;
    public Integer defaultLonShift = 0;
    private Integer currentlySelectedLonShift = defaultLonShift;

    public int minQuantiles = 3;
    public int maxQuantiles = 7;
    public int defaultQuantileCount = 5;
    private int currentlySelectedQuantileCount = defaultQuantileCount;

    private String qtmLayerName = "QTM";

    public Boolean hasBinned = false;

    public String orangesName = "oranges";
    public String purplesName = "purples";

    public String colorRampChosen = orangesName;


    public App(){

    }

    public void initialize(){
        this.mGUI = new MainGUI();
        this.aF = this.mGUI.start("Point Stats on a Discrete Global Grid", MainGUI.AppFrame.class);
        this.loadBlankGeoJSON();
    }

    public void triggerRedraw(){
        // Remove the previous QTM layer and use the relevant GeoJSON loading method,
        // based on whether the user has binned yet.
        removeQTMLayer();
        if (hasBinned == true){
            loadChoroplethGeoJSON();
        }else {
            loadBlankGeoJSON();
        }
    }

    public void setColorRampChosen(String colorRampName){
        this.colorRampChosen = colorRampName;
    }

    public void setCurrentQTMDrawingLevel(Integer lvl){
        this.currentlySelectedQTMLevel = lvl;
        triggerRedraw();
    }

    public void setCurrentLonShift(Integer lShift){
        this.currentlySelectedLonShift = lShift;
        triggerRedraw();
    }

    public void setCurrentlySelectedQuantileCount(Integer qCount){
        this.currentlySelectedQuantileCount = qCount;
        triggerRedraw();
    }

    public void setMaxBinningLevel(Integer lvl){
        maxBinningLevel = lvl;
    }

    public Integer getMaxBinningLevel(){
        return maxBinningLevel;
    }

    public Integer getMaxTranslationDegrees(){
        return maximumLonShift;
    }

    public void setAttrToBin(String attribute){
        attrToBin = attribute;
    }

    public void receiveUserCSVPath(String aPath){
        this.userCSVFilePath = aPath;
        this.parseCSV(this.userCSVFilePath);
    }

    public void removeQTMLayer(){
        LayerList ll = this.aF.getWwd().getModel().getLayers();
        for (Layer l: ll){
            if (l.getName() == "QTM"){
                this.aF.getWwd().getModel().getLayers().remove(l);
            }
        }
    }

    public void loadBlankGeoJSON(){
        AppGeoJSONLoader gjLoader = new AppGeoJSONLoader();
        /*
        Below, we retrieve saved QTM geojson files, which have a fixed naming
        convention as produced by qtmshifter.py. The string formatting below
        does the necessary work to identify the right file, given current
        values for QTM level and longitudinal shift.
        TODO: Put this into its own method; we'll need essentially the same logic when loading binned data layers.
         */
        Integer lonShiftAbsVal = Math.abs(this.currentlySelectedLonShift);
        String positiveOrNegative = "p";
        if (this.currentlySelectedLonShift < 0.0){
            positiveOrNegative = "n";
        }
        String qtmResourceFilePath = String.format("out/resources/qtmlevels/qtmlvl%s_lon%s%s.geojson",
                String.valueOf(this.currentlySelectedQTMLevel),
                positiveOrNegative,
                String.valueOf(Math.abs(this.currentlySelectedLonShift)) + ".0"
                );
        // System.out.println(qtmResourceFilePath);
        Layer lyr = gjLoader.createLayerFromSource(qtmResourceFilePath);
        lyr.setName(qtmLayerName);
        this.aF.getWwd().getModel().getLayers().add(lyr);
    }

    public void loadChoroplethGeoJSON(){
        // TODO: write me!
        System.out.println("Would be loading data-filled choropleth GeoJSON here.");
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

        // Change hasBinned to true, to change what we draw for GeoJSON polygons.
        hasBinned = true;
        this.aF.mainAppPanel.binningButton.setText("Done binning.");

    }


}
