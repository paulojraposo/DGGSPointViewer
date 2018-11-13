import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.*;
import com.Ostermiller.util.LabeledCSVParser;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class MarkerLayerMaker {

    /*
    Utility class to make a WorldWind MarkerLayer from the points in a CSV parser.
     */

    LabeledCSVParser lCSVParser;
    // Default markers are yellow spheres with full opacity, as below:
    BasicMarkerAttributes defaultMarkerAttributes = new BasicMarkerAttributes(Material.YELLOW, BasicMarkerShape.SPHERE, 1.0);
    ArrayList<Marker> markers;

    public MarkerLayerMaker(LabeledCSVParser lcp){
        // Constructed by handing-in an Ostermiller LabeledCSVParser, which should
        // have already been given a CSV file.
        this.lCSVParser = lcp;
    }

    public void makeMarkers(){
        markers = new ArrayList<Marker>();
        try {
            while (this.lCSVParser.getLine() != null) {

                // TODO: Add choropleth quality to markers based on their statistic of interest. Essentially, drive the marker Material with that.

                // We search the CSV data for these latitude and longitude fields specifically,
                // so we require exactly those names, case-sensitive, and unique among the fields.
                double lat = Double.valueOf(this.lCSVParser.getValueByLabel("latitude"));
                double lon = Double.valueOf(this.lCSVParser.getValueByLabel("longitude"));
                Angle latAngle = Angle.fromDegrees(lat);
                Angle lonAngle = Angle.fromDegrees(lon);
                LatLon ll = new LatLon(latAngle, lonAngle);
                Marker marker = new BasicMarker(new Position(ll, 0.0), defaultMarkerAttributes);
                BasicMarkerAttributes bmA = new BasicMarkerAttributes();
                // Markers scale with viewing height by default. Allowing them to be as small as 5 m, and big as 35km.
                bmA.setMinMarkerSize(1.0);
                bmA.setMaxMarkerSize(30000.0);
                bmA.setMaterial(new Material(Color.YELLOW));
                marker.setAttributes(bmA);
                // marker.getAttributes().setOpacity(0.8);
                markers.add(marker);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MarkerLayer makeMarkerLayer(){

        MarkerLayer layer = new MarkerLayer();
        layer.setKeepSeparated(false);
        layer.setMarkers(markers);
        layer.setElevation(0.0);
        layer.setOverrideMarkerElevation(true); // So markers will show on top regardless of their individual elevations.
        return layer;
    }

}
