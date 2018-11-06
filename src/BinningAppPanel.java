import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class BinningAppPanel extends JPanel {

    // GUI elements declaration.

    TitledBorder titledBorder;

    JLabel csvLabel;
    JPanel csvChoosingPanel = new JPanel();
    JTextField csvTextField;
    JButton chooseCSVButton;
    public JFileChooser csvFC;

    JLabel columnLabel;
    JPanel columnTextFieldPanel;
    JTextField columnTextField;

    JLabel levelLabel;
    JPanel levelPanel;
    JComboBox levelCB;

    JLabel saveLabel;
    JTextField saveTextField;
    JButton saveToButton;
    JFileChooser saveToFC;
    JPanel savePanel;

    JLabel loadLabel;
    JCheckBox loadChkBox;
    JPanel loadPanel;

    JLabel dummyBlankLabel;
    JButton runButton;

    JLabel statusLabel;
    JLabel status;


    public BinningAppPanel(){

        this.setLayout(new GridLayout(0,2)); // zero rows means as many as get asked for.

        Border bGreyLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true);
        titledBorder = BorderFactory.createTitledBorder(bGreyLine, "Binning", TitledBorder.LEFT,  TitledBorder.TOP, null, Color.gray);
        this.setBorder(titledBorder);

        // Instantiate and place GUI elements.
        // We use GridLayout in several places to get elements to span
        // their cell locations, and to keep everything in consistent
        // relative dimensions.

        this.csvLabel = new JLabel("CSV of points:");
        this.add(this.csvLabel);
        this.csvTextField = new JTextField("choose a file...");
        this.chooseCSVButton = new JButton("Choose CSV");
        // TODO: action listener for this button!
        this.csvChoosingPanel = new JPanel();
        this.csvChoosingPanel.setLayout(new GridLayout(1,2));
        this.csvChoosingPanel.add(this.csvTextField);
        this.csvChoosingPanel.add(this.chooseCSVButton);
        this.add(this.csvChoosingPanel);

        this.columnLabel = new JLabel("Name of column to bin:");
        this.add(this.columnLabel);
        this.columnTextFieldPanel = new JPanel();
        this.columnTextFieldPanel.setLayout(new GridLayout(1,1));
        this.columnTextField = new JTextField("", 20);
        this.columnTextFieldPanel.add(this.columnTextField);
        this.add(this.columnTextFieldPanel);

        this.levelLabel = new JLabel("Bin up to QTM level:");
        this.add(this.levelLabel);
        this.levelPanel = new JPanel();
        this.levelPanel.setLayout(new GridLayout(1,1));
        this.levelCB = new JComboBox<String>(Main.app.levelOptions);
        this.levelPanel.add(this.levelCB);
        this.add(this.levelPanel);

        this.saveLabel = new JLabel("Save to folder:");
        this.add(this.saveLabel);
        this.savePanel = new JPanel();
        this.savePanel.setLayout(new GridLayout(1,2));
        this.saveTextField = new JTextField("", 20);
        this.saveToButton = new JButton("Choose Folder");
        // TODO: action listener for this button!
        this.savePanel.add(this.saveTextField);
        this.savePanel.add(this.saveToButton);
        this.add(this.savePanel);

        this.loadLabel = new JLabel("Load binned data to viewer:");
        this.add(this.loadLabel);
        this.loadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.loadChkBox = new JCheckBox();
        this.loadChkBox.setSelected(true);
        this.loadPanel.add(this.loadChkBox);
        this.add(this.loadPanel);

        this.add(this.dummyBlankLabel = new JLabel(""));
        this.runButton = new JButton("Perform binning");
        // TODO: add action listener and functionality to this button!
        this.add(this.runButton);

        this.statusLabel = new JLabel("Status:");
        this.add(this.statusLabel);
        this.status = new JLabel("Ready");
        this.add(this.status);

        // Set a preferred panel size.
        Dimension ps = this.getPreferredSize();
        ps.width = 500;
        this.setPreferredSize(ps);

        this.setVisible(true);

    }


}