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
                // We search the CSV data for these latitude and longitude fields specifically,
                // so we require exactly those names, case-sensitive, and unique among the fields.
                double lat = Double.valueOf(this.lCSVParser.getValueByLabel("latitude"));
                double lon = Double.valueOf(this.lCSVParser.getValueByLabel("longitude"));
                Angle latAngle = Angle.fromDegrees(lat);
                Angle lonAngle = Angle.fromDegrees(lon);
                LatLon ll = new LatLon(latAngle, lonAngle);
                Marker marker = new BasicMarker(new Position(ll, 0.0), defaultMarkerAttributes);
                BasicMarkerAttributes bmA = new BasicMarkerAttributes();
                bmA.setMinMarkerSize(10000.0);
                bmA.setMaxMarkerSize(35000.0);
                bmA.setMaterial(new Material(Color.YELLOW));
                marker.setAttributes(bmA);
                markers.add(marker);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MarkerLayer makeMarkerLayer(){

        MarkerLayer layer = new MarkerLayer();
        layer.setKeepSeparated(false);
        layer.setElevation(0.0);
        layer.setMarkers(markers);

        return layer;
    }

}
