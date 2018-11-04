import javax.swing.*;
import java.awt.*;

public class BinningAppFrame extends JFrame {

    public BinningAppPanel binningPanel;

    public BinningAppFrame(){
        this.setTitle("Binning Input CSV");
        this.binningPanel = new BinningAppPanel();
        this.getContentPane().add(BorderLayout.CENTER, this.binningPanel);
        this.pack();
        this.setVisible(true);
    }

}
