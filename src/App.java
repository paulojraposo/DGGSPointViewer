import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.google.common.base.Splitter.onPattern;
import static com.google.common.math.Quantiles.percentiles;

public class App {

    private MainGUI mGUI;
    private String userCSVFilePath;
    private static LabeledCSVParser csvParser;
    private MainGUI.AppFrame aF;
    private String[] csvFieldNames;

    DefaultComboBoxModel cbModel;

    // Limited maxBinningLevel to 7, down from 11, 2018-05-18, for performance's sake, for now.
    public int maxBinningLevel = 6; // 7th level's index, by default, user-changeable.
    public String[] levelOptions = new String[]{"1", "2", "3", "4", "5", "6", "7"};//,
    //"8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};

    public int minQTMLevel = 3;
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
    public int currentlySelectedQuantileCount = defaultQuantileCount;

    private String qtmLayerName = "QTM";
    private String currentlyLoadedQTMDataLayerName = qtmLayerName;
    private String dataPointsLayerName = "Data Points";

    public Boolean usingPreparedData = false;

    public Boolean hasBinned = false;

    public String orangesName = "oranges";
    public String purplesName = "purples";

    public String currentColorRampChosen = orangesName;

    public String aggregatedStatOfInterest = "Mean";

    public ArrayList<Double> currentFacetsDataValues = new ArrayList<Double>();

    public HashMap<Integer, Double> quantileIndexesByBreakIndex = new HashMap<Integer, Double>();

    public HashMap<Integer, ArrayList> quantileBoundsByIndex;

    public ChoroplethManager choroplethManager;

    private String preparedDataPathFormat = "out/resources/prepareddata/AfricaPopPlaces/qtmlvl%slonshft%s_agg.geojson";

    public String userDataPathFormat;

    public App() {

    }

    public void initialize() {
        this.mGUI = new MainGUI();
        this.aF = this.mGUI.start("Point Stats on a Discrete Global Grid", MainGUI.AppFrame.class);
        this.loadBlankGeoJSON();
        this.choroplethManager = new ChoroplethManager();
        this.quantileBoundsByIndex = new HashMap<Integer, ArrayList>();
        HashMap<Integer, ArrayList> hm = this.choroplethManager.colorHM.get(this.currentColorRampChosen);
        ArrayList<String> colorsAL = hm.get(this.currentlySelectedQuantileCount);
        this.aF.mainAppPanel.legendPanel.refreshLegend(colorsAL);
    }

    public void openBinningWindow() {
        BinningAppFrame binningFrame = new BinningAppFrame();
    }

    public void openUserLoadingWindow() {
        UserLoadingAppFrame userLoadingFrame = new UserLoadingAppFrame();
    }

    public void determineQuantileBounds() {
        this.quantileBoundsByIndex.clear();
        List<Double> theValuesAsList = new ArrayList<Double>(this.quantileIndexesByBreakIndex.values());
        Collections.sort(theValuesAsList); // sort ascending.
        for (int i = 0; i < Main.app.currentlySelectedQuantileCount; i++) {
            Double thisLowerBound = theValuesAsList.get(i);
            Double thisUpperBound = theValuesAsList.get(i + 1);
            ArrayList<Double> theseBounds = new ArrayList<Double>();
            theseBounds.add(thisLowerBound);
            theseBounds.add(thisUpperBound);
            this.quantileBoundsByIndex.put(i, theseBounds);
        }
    }

    public void triggerRedraw() {
        // Remove the previous QTM layer and use the relevant GeoJSON loading method,
        // based on whether the user has binned yet.
        this.quantileIndexesByBreakIndex.clear();
        removeLayerByName(qtmLayerName);
        removeLayerByName(this.currentlyLoadedQTMDataLayerName);
        if (hasBinned == true) {
            loadChoroplethGeoJSON();
        } else {
            loadBlankGeoJSON();
        }
        this.choroplethManager = new ChoroplethManager();
        HashMap<Integer, ArrayList> hm = this.choroplethManager.colorHM.get(this.currentColorRampChosen);
        ArrayList<String> colorsAL = hm.get(this.currentlySelectedQuantileCount);
        this.aF.mainAppPanel.legendPanel.refreshLegend(colorsAL);
        this.aF.mainAppPanel.validate();
        this.aF.mainAppPanel.repaint();
    }

    public void setCurrentColorRampChosen(String colorRampName) {
        this.currentColorRampChosen = colorRampName;
        triggerRedraw();
    }

    public void setCurrentQTMDrawingLevel(Integer lvl) {
        this.currentlySelectedQTMLevel = lvl;
        triggerRedraw();
    }

    public void setCurrentLonShift(Integer lShift) {
        this.currentlySelectedLonShift = lShift;
        triggerRedraw();
    }

    public void setCurrentlySelectedQuantileCount(Integer qCount) {
        this.currentlySelectedQuantileCount = qCount;
        triggerRedraw();
    }

    public void removeQTMLayer() {
        LayerList ll = this.aF.getWwd().getModel().getLayers();
        for (Layer l : ll) {
            if (l.getName() == this.qtmLayerName) {
                this.aF.getWwd().getModel().getLayers().remove(l);
            }
        }
    }

    public void removeLayerByName(String layerName) {
        LayerList ll = this.aF.getWwd().getModel().getLayers();
        for (Layer l : ll) {
            if (l.getName() == layerName) {
                this.aF.getWwd().getModel().getLayers().remove(l);
            }
        }
    }

    public void removeDataPointsLayer() {
        LayerList ll = this.aF.getWwd().getModel().getLayers();
        for (Layer l : ll) {
            if (l.getName() == this.dataPointsLayerName) {
                this.aF.getWwd().getModel().getLayers().remove(l);
            }
        }
    }

    public void rotateGlobeToLayerByName(String layerName) {
        // TODO: write me!
    }

    public void loadBlankGeoJSON() {
        AppGeoJSONLoader gjLoader = new AppGeoJSONLoader();
        /*
        Below, we retrieve saved QTM geojson files, which have a fixed naming
        convention as produced by qtmshifter.py. The string formatting below
        does the necessary work to identify the right file, given current
        values for QTM level and longitudinal shift.
        TODO: Put this into its own method; we'll need essentially the same logic when loading binned data layers.
         */
        String qtmResourceFilePath = String.format("out/resources/prepareddata/blankQTM/qtmlvl%slonshft%s.geojson",
                String.valueOf(this.currentlySelectedQTMLevel),
                String.valueOf(Double.valueOf(this.currentlySelectedLonShift))
        );
        Layer lyr = gjLoader.createLayerFromSource(qtmResourceFilePath);
        this.currentlyLoadedQTMDataLayerName = qtmLayerName;
        lyr.setName(qtmLayerName);
        this.aF.getWwd().getModel().getLayers().add(lyr);
        // Move QTM layer to the top
        // this.aF.getWwd().getModel().getLayers().set(0,lyr);
    }

    public void loadChoroplethGeoJSON() {
        AppGeoJSONLoader gjLoader = new AppGeoJSONLoader();
        // Defaults to prepared data, but uses the user's data if usingPreparedData == false.
        String dataPathFormat = preparedDataPathFormat;
        if (this.usingPreparedData == false) {
            dataPathFormat = userDataPathFormat;
        }
        String qtmResourceFilePath = String.format(dataPathFormat,
                String.valueOf(this.currentlySelectedQTMLevel),
                String.valueOf(Double.valueOf(this.currentlySelectedLonShift)));
        // Read the data and determine quantiles so we can use them to drive
        // the appearance of the facets on the globe according to the attribute
        // value we're interested in.
        gjLoader.readDataByVariableNameFromSource(qtmResourceFilePath);
        Double quantileInterval = 100.0 / this.currentlySelectedQuantileCount;
        Integer thisQuantileBreak;// = quantileInterval.intValue();
        for (int i = 0; i < this.currentlySelectedQuantileCount; i++) {
            thisQuantileBreak = quantileInterval.intValue() * i;
            double thisPercentile = percentiles().index(thisQuantileBreak).compute(this.currentFacetsDataValues);
            quantileIndexesByBreakIndex.put(thisQuantileBreak, thisPercentile);
        }
        Integer lastQuantileBreak = 100;
        double lastPercentile = percentiles().index(lastQuantileBreak).compute(this.currentFacetsDataValues);
        this.quantileIndexesByBreakIndex.put(lastQuantileBreak, lastPercentile);

        this.determineQuantileBounds();

        Layer lyr = gjLoader.createLayerFromSource(qtmResourceFilePath);
        this.removeLayerByName(this.currentlyLoadedQTMDataLayerName);
        this.currentlyLoadedQTMDataLayerName = qtmLayerName;
        lyr.setName(this.currentlyLoadedQTMDataLayerName);

        this.aF.getWwd().getModel().getLayers().add(lyr);

    }

    public Integer determineMaxQTMLevelFromLayerFiles(String folderPath) {
        // Looks at all the files in the directory, and splits them on contiguous groups
        // of alphabet characters. This depends on the file naming convention:
        // qtmlvlXlonshftX_agg.geojson
        // where X is an integer or decimal number, positive or negative.
        // We want the first X, which will be a positive integer indicating the QTM
        // level of the file. We want to know the highest level present in the files.
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        int highestLevel = 0;
        Splitter spl =  Splitter.onPattern("[a-zA-Z]+").omitEmptyStrings(); // regex for any number of contiguous alphabet characters.
        for(File f: listOfFiles){
            if (f.isFile()) {
                List<String> l = spl.splitToList(f.getName());
                int thisLevel = Integer.parseInt(l.get(0));
                if (thisLevel > highestLevel){
                    highestLevel = thisLevel;
                }
            }
        }
        return highestLevel;
    }

    public void adjustQTMLevelSlider(Integer maxLevels){
        this.aF.mainAppPanel.levelSlider.setMaximum(maxLevels);
        if (maxLevels > 14){
            // Space out labels to every other tick if there are more than 14 - it gets crowded otherwise.
            // With thanks to Pinkilla: https://stackoverflow.com/questions/13458076/howto-set-labels-match-major-ticks-with-jslider-in-java
            // this.aF.mainAppPanel.levelSlider.setMajorTickSpacing(2);
            this.aF.mainAppPanel.levelSlider.setLabelTable(this.aF.mainAppPanel.levelSlider.createStandardLabels(2));
        }
    }

    public InputStream pathToInputStream(String path) throws IOException {
        File initialFile = new File(path);
        InputStream targetStream = new FileInputStream(initialFile);
        return targetStream;
    }

    public void receiveUserCSVPath(String aPath){
        this.userCSVFilePath = aPath;
        this.parseCSV(this.userCSVFilePath);
    }

    public void receiveUserQTMLayersFolderPathAndFormat(String aPath, String aFormat){

        this.userDataPathFormat = aPath + File.separator + aFormat;
        this.hasBinned = true;
        int levels = this.determineMaxQTMLevelFromLayerFiles(aPath);
        this.maxQTMLevels = levels;
        this.adjustQTMLevelSlider(this.maxQTMLevels);
        this.triggerRedraw();
    }

    private void parseCSV(String filePath){
        // Read the user CSV, determine the field names, set UI
        // options enabled.
        InputStream iS = null;
        try {
            iS = pathToInputStream(filePath);
            csvParser = new LabeledCSVParser(new CSVParser(iS));
            csvFieldNames = csvParser.getLabels();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LatLon getCSVCentroid(String filePath) throws IOException {

        // Finds the center lat lon point for the input CSV. For turning
        // the globe automatically to the center of the user's plotted points.

        // Collect all the lats and lons and get the halfway point in their ranges.

        InputStream iS = pathToInputStream(filePath);
        LabeledCSVParser csvP = new LabeledCSVParser(new CSVParser(iS));

        ArrayList<Float> Lats = new ArrayList<Float>();
        ArrayList<Float> Lons = new ArrayList<Float>();
        while(csvP.getLine() != null){
            Lats.add(Float.parseFloat(csvP.getValueByLabel("latitude")));
            Lons.add(Float.parseFloat(csvP.getValueByLabel("longitude")));
        }
        Float minLat = Collections.min(Lats);
        Float maxLat = Collections.max(Lats);
        Float minLon = Collections.min(Lons);
        Float maxLon = Collections.max(Lons);

        Float centerLat = (maxLat - minLat)/2 + minLat;
        Float centerLon = (maxLon - minLon)/2 + minLon;

        return LatLon.fromDegrees(centerLat, centerLon);

    }

    public void plotCSVPoints() {
        MarkerLayerMaker mlm = new MarkerLayerMaker(csvParser);
        mlm.makeMarkers();
        Layer markerLayer = mlm.makeMarkerLayer();
        markerLayer.setName(dataPointsLayerName);
        Sector sector = (Sector) markerLayer.getValue(AVKey.SECTOR);
        this.aF.getWwd().getModel().getLayers().add(markerLayer);
        rotateGlobeToCenterOfPoints();
    }

    public void rotateGlobeToCenterOfPoints(){
        // Rotate the globe to the loaded data.
        try{
            LatLon rotateLatLon = getCSVCentroid(this.userCSVFilePath);
            this.aF.getWwd().getView().goTo(new Position(rotateLatLon,0.0), 12000000.0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to rotate globe to center of user CSV :(");
        }

    }

    public void bypassBinning(){

        // Use built in data. Hard-code some parameters for that data.
        this.plotCSVPoints();
        this.usingPreparedData = true;
        this.hasBinned = true;
        this.triggerRedraw();

    }

    public void resetAppState(){
        // Reset things to the state the app is in when first started up.
        this.removeLayerByName(this.currentlyLoadedQTMDataLayerName);
        this.removeDataPointsLayer();
        this.csvFieldNames = null;
        this.cbModel = new DefaultComboBoxModel(); // A new empty DefaultComboBoxModel.
        this.userCSVFilePath = null;
        this.usingPreparedData = false;
        this.hasBinned = false;
        this.aF.mainAppPanel.resetAllBinningControls();
        this.maxQTMLevels = 6;
        this.aF.mainAppPanel.levelSlider.setMaximum(this.maxQTMLevels);
        this.quantileIndexesByBreakIndex.clear();
        this.triggerRedraw();
        this.currentFacetsDataValues.clear();
    }

    /**
     * Causes the View attached to the specified WorldWindow to animate to the specified sector. The View starts
     * animating at its current location and stops when the sector fills the window.
     *
     * @param wwd    the WorldWindow who's View animates.
     * @param sector the sector to go to.
     *
     * @throws IllegalArgumentException if either the <code>wwd</code> or the <code>sector</code> are
     *                                  <code>null</code>.
     */
    public static void goTo(WorldWindow wwd, Sector sector)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Create a bounding box for the specified sector in order to estimate its size in model coordinates.
        Box extent = Sector.computeBoundingBox(wwd.getModel().getGlobe(),
                wwd.getSceneController().getVerticalExaggeration(), sector);

        // Estimate the distance between the center position and the eye position that is necessary to cause the sector to
        // fill a viewport with the specified field of view. Note that we change the distance between the center and eye
        // position here, and leave the field of view constant.
        Angle fov = wwd.getView().getFieldOfView();
        double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();

        // Configure OrbitView to look at the center of the sector from our estimated distance. This causes OrbitView to
        // animate to the specified position over several seconds. To affect this change immediately use the following:
        // ((OrbitView) wwd.getView()).setCenterPosition(new Position(sector.getCentroid(), 0d));
        // ((OrbitView) wwd.getView()).setZoom(zoom);
        wwd.getView().goTo(new Position(sector.getCentroid(), 0d), zoom);
    }

}
