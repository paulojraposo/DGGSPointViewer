import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.google.common.math.Quantiles.percentiles;

public class App {

    private MainGUI mGUI;
    private String userCSVFilePath;
    private static LabeledCSVParser csvParser;
    private MainGUI.AppFrame aF;
    private String[] csvFieldNames;

    String attrToBin;
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

    public HashMap<Integer,Double> quantileIndexesByBreakIndex = new HashMap<Integer, Double>();

    public HashMap<Integer,ArrayList> quantileBoundsByIndex;

    public ChoroplethManager choroplethManager;


    public App(){

    }

    public void initialize(){
        this.mGUI = new MainGUI();
        this.aF = this.mGUI.start("Point Stats on a Discrete Global Grid", MainGUI.AppFrame.class);
        this.loadBlankGeoJSON();
        this.choroplethManager = new ChoroplethManager();
        this.quantileBoundsByIndex = new HashMap<Integer,ArrayList>();
        HashMap<Integer,ArrayList> hm = this.choroplethManager.colorHM.get(this.currentColorRampChosen);
        ArrayList<String> colorsAL = hm.get(this.currentlySelectedQuantileCount);
        this.aF.mainAppPanel.legendPanel.refreshLegend(colorsAL);
    }

    public void determineQuantileBounds(){
        this.quantileBoundsByIndex.clear();
        List<Double> theValuesAsList = new ArrayList<Double>(this.quantileIndexesByBreakIndex.values());
        Collections.sort(theValuesAsList); // sort ascending.
        for (int i=0; i < Main.app.currentlySelectedQuantileCount ; i++){
            Double thisLowerBound = theValuesAsList.get(i);
            Double thisUpperBound = theValuesAsList.get(i+1);
            ArrayList<Double> theseBounds = new ArrayList<Double>();
            theseBounds.add(thisLowerBound);
            theseBounds.add(thisUpperBound);
            this.quantileBoundsByIndex.put(i, theseBounds);
        }
    }

    public void triggerRedraw(){
        // Remove the previous QTM layer and use the relevant GeoJSON loading method,
        // based on whether the user has binned yet.
        this.quantileIndexesByBreakIndex.clear();
        removeLayerByName(qtmLayerName);
        removeLayerByName(this.currentlyLoadedQTMDataLayerName);
        if (hasBinned == true){
            if (usingPreparedData == true){
                loadIncludedChoroplethGeoJSON();
            }
        }else{
            loadBlankGeoJSON();
        }

        HashMap<Integer,ArrayList> hm = this.choroplethManager.colorHM.get(this.currentColorRampChosen);
        ArrayList<String> colorsAL = hm.get(this.currentlySelectedQuantileCount);
        this.aF.mainAppPanel.legendPanel.refreshLegend(colorsAL);
        this.aF.mainAppPanel.validate();
        this.aF.mainAppPanel.repaint();
    }

    public void setCurrentColorRampChosen(String colorRampName){
        this.currentColorRampChosen = colorRampName;
        triggerRedraw();
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
        this.maxBinningLevel = lvl;
    }

    public void setAttrToBin(String attribute){
        this.attrToBin = attribute;
    }

    public void receiveUserCSVPath(String aPath){
        this.userCSVFilePath = aPath;
        this.parseCSV(this.userCSVFilePath);
    }

    public void removeQTMLayer(){
        LayerList ll = this.aF.getWwd().getModel().getLayers();
        for (Layer l: ll){
            if (l.getName() == this.qtmLayerName){
                this.aF.getWwd().getModel().getLayers().remove(l);
            }
        }
    }

    public void removeLayerByName(String layerName){
        LayerList ll = this.aF.getWwd().getModel().getLayers();
        for (Layer l: ll){
            if (l.getName() == layerName){
                this.aF.getWwd().getModel().getLayers().remove(l);
            }
        }
    }

    public void removeDataPointsLayer(){
        LayerList ll = this.aF.getWwd().getModel().getLayers();
        for (Layer l: ll){
            if (l.getName() == this.dataPointsLayerName){
                this.aF.getWwd().getModel().getLayers().remove(l);
            }
        }
    }

    public void rotateGlobeToLayerByName(String layerName){
        // TODO: write me!
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
        String qtmResourceFilePath = String.format("out/resources/prepareddata/blankQTM/qtmlvl%slonshft%s.geojson",
                String.valueOf(this.currentlySelectedQTMLevel),
                String.valueOf(Double.valueOf(this.currentlySelectedLonShift)) //+ ".0" // Paste-on .0 since the Python script names its outputs using decimal numbers, not integers.
                );
        Layer lyr = gjLoader.createLayerFromSource(qtmResourceFilePath);
        this.currentlyLoadedQTMDataLayerName = qtmLayerName;
        lyr.setName(qtmLayerName);
        this.aF.getWwd().getModel().getLayers().add(lyr);
        // Move QTM layer to the top
        // this.aF.getWwd().getModel().getLayers().set(0,lyr);
    }

    public void loadIncludedChoroplethGeoJSON(){
        AppGeoJSONLoader gjLoader = new AppGeoJSONLoader();
        String qtmResourceFilePath = String.format("out/resources/prepareddata/AfricaPopPlaces/qtmlvl%slonshft%s_agg.geojson",
        String.valueOf(this.currentlySelectedQTMLevel),
        String.valueOf(Double.valueOf(this.currentlySelectedLonShift)));

        // Read the data and determine quantiles so we can use them to drive
        // the appearance of the facets on the globe according to the attribute
        // value we're interested in.
        gjLoader.readDataByVariableNameFromSource(qtmResourceFilePath);
        Double quantileInterval = 100.0 / this.currentlySelectedQuantileCount;
        Integer thisQuantileBreak = quantileInterval.intValue();
        for (int i = 0; i < this.currentlySelectedQuantileCount; i++){
            thisQuantileBreak =  quantileInterval.intValue() * i ;
            double thisPercentile = percentiles().index(thisQuantileBreak).compute(this.currentFacetsDataValues);
            quantileIndexesByBreakIndex.put(thisQuantileBreak, thisPercentile);
        }
        Integer lastQuantileBreak = 100;
        double lastPercentile = percentiles().index(lastQuantileBreak).compute(this.currentFacetsDataValues);
        this.quantileIndexesByBreakIndex.put(lastQuantileBreak, lastPercentile);

        this.determineQuantileBounds();

        Layer lyr = gjLoader.createLayerFromSource(qtmResourceFilePath);
        this.currentlyLoadedQTMDataLayerName = qtmLayerName + " Africa Populated Places";
        lyr.setName(this.currentlyLoadedQTMDataLayerName);
        this.removeLayerByName(this.currentlyLoadedQTMDataLayerName);
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
        cbModel = new DefaultComboBoxModel(csvFieldNames);
        this.aF.mainAppPanel.attrToBinCB.setModel(cbModel);

        if (this.usingPreparedData == true){
            try {
                this.aF.mainAppPanel.attrToBinCB.setSelectedItem("pop_max");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        markerLayer.setName(dataPointsLayerName);
        Sector sector = (Sector) markerLayer.getValue(AVKey.SECTOR);
        this.aF.getWwd().getModel().getLayers().add(markerLayer);

        // Rotate the globe to the loaded data.
        // TODO: make this location programatic, not hard-coded.
        LatLon rotateLatLon = LatLon.fromDegrees(4.0, 19.0); // Approx the center of Africa.
        this.aF.getWwd().getView().goTo(new Position(rotateLatLon,0.0), 12000000.0);
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

        this.plotCSVPoints();

        // Change hasBinned to true, to change what we draw for GeoJSON polygons.
        this.hasBinned = true;
        this.aF.mainAppPanel.binningButton.setText("Done binning.");

        this.triggerRedraw();

    }

    public void resetAppState(){
        // Reset things to the state the app is in when first started up.
        this.removeLayerByName(this.currentlyLoadedQTMDataLayerName);
        this.removeDataPointsLayer();
        this.csvFieldNames = null;
        this.cbModel = new DefaultComboBoxModel(); // A new empty DefaultComboBoxModel.
        this.userCSVFilePath = null;
        usingPreparedData = false;
        this.hasBinned = false;
        this.aF.mainAppPanel.resetAllBinningControls();
        this.quantileIndexesByBreakIndex.clear();
        this.triggerRedraw();
        this.currentFacetsDataValues.clear();
        this.aF.mainAppPanel.disableExportButtons();
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
