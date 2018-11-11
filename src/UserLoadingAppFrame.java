import javax.swing.*;
import java.awt.*;

public class UserLoadingAppFrame extends JFrame {

    public UserLoadingPanel userLoadingPanel;

    public UserLoadingAppFrame(){
        this.setTitle("Load Prepared Data");
        this.userLoadingPanel = new UserLoadingPanel();
        this.getContentPane().add(BorderLayout.CENTER, this.userLoadingPanel);
        this.pack();
        this.setVisible(true);
    }

}
