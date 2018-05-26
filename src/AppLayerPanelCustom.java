import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwindx.examples.layermanager.LayerManagerPanel;
import javax.swing.*;
import java.awt.*;

public class AppLayerPanelCustom extends JPanel {

    protected LayerManagerPanel layerManagerPanel;

    public AppLayerPanelCustom(WorldWindow wwd)
    {
        super(new BorderLayout(10, 10));

        AppLayerManagerPanel lmP = new AppLayerManagerPanel(wwd);
        this.add(lmP, BorderLayout.CENTER);

        // Remove selected layers from app.
        // This seems a kludgy way to do this, as the official documentation
        // says to use a configuration file, but I can't seem to get that
        // working :/
        // The below with guidance from:
        // https://stackoverflow.com/questions/12297094/how-to-remove-the-worldmap-from-nasa-worldwind
        // System.out.println(wwd.getModel().getLayers());
        String[] layersToRemove = {
                "NASA Blue Marble Image",
                "i-cubed Landsat",
                "USGS NAIP Plus",
                "USGS Topo Base Map",
                "USGS Topo Base Map Large Scale",
                "USGS Topo Scanned Maps 1:250K",
                "USGS Topo Scanned Maps 1:100K",
                "USGS Topo Scanned Maps 1:24K",
                "Political Boundaries"
        };
        for (String l: layersToRemove){
            Layer worldMapLayer = wwd.getModel().getLayers().getLayerByName(l);
            wwd.getModel().getLayers().remove(worldMapLayer);

        }
    }

    public void updateLayers(WorldWindow wwd)
    {
        this.layerManagerPanel.update(wwd);

    }
}
