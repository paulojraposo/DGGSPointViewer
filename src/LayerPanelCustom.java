import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwindx.examples.layermanager.LayerManagerPanel;
import javax.swing.*;
import java.awt.*;


public class LayerPanelCustom extends JPanel {

    protected LayerManagerPanel layerManagerPanel;

    public LayerPanelCustom(WorldWindow wwd)
    {
        super(new BorderLayout(10, 10));

        this.add(this.layerManagerPanel = new LayerManagerPanel(wwd), BorderLayout.CENTER);
    }

    public void updateLayers(WorldWindow wwd)
    {
        this.layerManagerPanel.update(wwd);
    }

}

