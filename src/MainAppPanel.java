import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainAppPanel extends JPanel{

    public JFileChooser fc;
    int maxQTMLevels = 12;

    Border bGreyLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true);
    Border bBinningTitled;
    Border bMAUPTitled;
    Border bClassificationTitled;
    JPanel logoPanel;
    JLabel logoLabel;

    // Binning panel
    JPanel binningPanel;
    JLabel chooseFileLabel;
    JButton chooseFileButton;
    JLabel attrToBinLabel;
    JComboBox attrToBinCB;
    JLabel levelChoosingLabel;
    JComboBox<String> levelIntersectionCalculationCB;
    JLabel progressMessage;
    JButton binningButton; // Change the text of this when processing to tell the user.

    // MAUP panel
    JPanel maupPanel;
    JLabel levelLabel;
    JSlider levelSlider;
    JLabel EWTranslateLabel;
    JSlider EWTranslateSlider;

    // Classification and Mapping panel
    JPanel classingAndMappingPanel;
    JLabel colorLabel;
    JComboBox<String> colorCB;
    JLabel quantilesLabel;
    JSlider quantilesSlider;


    public MainAppPanel(){

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("MAUP Viewer")));

        // UT Logo
        logoPanel = new JPanel();
        logoLabel = new JLabel();
        logoLabel.setIcon(new ImageIcon("out/resources/UTGeog.png"));
        logoLabel.setVisible(true);
        logoPanel.add(logoLabel);
        this.add(logoPanel);

        // Binning panel
        binningPanel = new JPanel();
        bBinningTitled = BorderFactory.createTitledBorder(bGreyLine, "Data Input and Binning", TitledBorder.LEFT,  TitledBorder.TOP, null, Color.gray);
        binningPanel.setBorder(bBinningTitled);
        binningPanel.setLayout(new GridLayout(4,2));
        chooseFileLabel = new JLabel("Input CSV:");
        binningPanel.add(chooseFileLabel);
        chooseFileButton = new JButton("Choose File..."); // change button label when file chosen to indicate it to user.
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                // TODO: make file chooser filter for CSV files.
                int returnVal = fc.showOpenDialog(binningPanel);
                chooseFileButton.setText(fc.getSelectedFile().getName());
                Main.app.receiveUserCSVPath(fc.getSelectedFile().toPath().toString());
            }
        });
        binningPanel.add(chooseFileButton);
        levelChoosingLabel = new JLabel("Bin up to QTM level:");
        binningPanel.add(levelChoosingLabel);
        levelIntersectionCalculationCB = new JComboBox<String>(Main.app.levelOptions);
        levelIntersectionCalculationCB.setSelectedIndex(Main.app.getMaxBinningLevel());
        // below from https://stackoverflow.com/questions/11008431/how-to-center-items-in-a-java-combobox
        ((JLabel) levelIntersectionCalculationCB.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        levelIntersectionCalculationCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object lvl = levelIntersectionCalculationCB.getSelectedItem();
                Main.app.setMaxBinningLevel(Integer.parseInt(lvl.toString()));
            }
        } );
        binningPanel.add(levelIntersectionCalculationCB);
        attrToBinLabel = new JLabel("Attribute to bin:");
        binningPanel.add(attrToBinLabel);
        attrToBinCB = new JComboBox(); // The choices here need to be set once the CSV is read.
        attrToBinCB.setEnabled(false); // Also make it enabled when option is available.
        ((JLabel) attrToBinCB.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        attrToBinCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String attr = attrToBinCB.getSelectedItem().toString();
                Main.app.setAttrToBin(attr);
            }
        } );
        binningPanel.add(attrToBinCB);
        progressMessage = new JLabel(""); // starts blank, and as a filler in the GridLayout. Will be updated later to show progress messages.
        progressMessage.setHorizontalAlignment(SwingConstants.RIGHT);
        binningPanel.add(progressMessage);
        binningButton = new JButton("Run Binning");
        binningButton.setEnabled(false); // Disabled until user selects a CSV.
        binningButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Change button display here while binning to tell user it's in process.
                // Update it regularly to show progress.
                try {
                    // binningButton.setIcon(new ImageIcon("out/production/DGGSPointViewer/resources/loadinglinegraphic.gif"));
                    binningButton.setText("Binning...");
                } catch (Exception ex) {
                    System.out.println(ex);
                }
                chooseFileButton.setEnabled(false);
                levelIntersectionCalculationCB.setEnabled(false);
                attrToBinCB.setEnabled(false);
                binningButton.setEnabled(false);
                Main.app.performBinning();
            }
        } );
        binningPanel.add(binningButton);
        this.add(binningPanel);

        // MAUP panel
        maupPanel = new JPanel();
        bMAUPTitled = BorderFactory.createTitledBorder(bGreyLine, "Modifiable Areal Units", TitledBorder.LEFT,  TitledBorder.TOP, null, Color.black);
        maupPanel.setBorder(bMAUPTitled);
        maupPanel.setLayout(new GridLayout(2,2));
        levelLabel = new JLabel("<html><b>Scaling:</b> QTM level to draw:</html>");
        maupPanel.add(levelLabel);
        levelSlider = new JSlider(0, maxQTMLevels, 6);
        levelSlider.setMajorTickSpacing(2);
        levelSlider.setMinorTickSpacing(1);
        levelSlider.setPaintTicks(true);
        levelSlider.setPaintLabels(true);
        levelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("level slider moved"); // TODO: write useful method here.
            }
        });
        maupPanel.add(levelSlider);
        EWTranslateLabel = new JLabel("<html><b>Zoning:</b> Longitudinal shift<br>of mesh in degrees:</html>");
        maupPanel.add(EWTranslateLabel);
        EWTranslateSlider = new JSlider(-1*Main.app.getMaxTranslationDegrees(), Main.app.getMaxTranslationDegrees(), 0);
        EWTranslateSlider.setMajorTickSpacing(3);
        EWTranslateSlider.setMinorTickSpacing(1);
        EWTranslateSlider.setPaintTicks(true);
        EWTranslateSlider.setPaintLabels(true);
        EWTranslateSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("longitude slider moved"); // TODO: write useful method here.
            }
        });
        maupPanel.add(EWTranslateSlider);
        this.add(maupPanel);

        // Classing and Mapping Panel
        classingAndMappingPanel = new JPanel();
        bClassificationTitled = BorderFactory.createTitledBorder(bGreyLine, "Choropleth Classification", TitledBorder.LEFT, TitledBorder.TOP, null, Color.black);
        classingAndMappingPanel.setBorder(bClassificationTitled);
        GridLayout binningPanelLayout = new GridLayout(1,2); // TODO: revise x by y as needed for new elements.
        classingAndMappingPanel.setLayout(binningPanelLayout);
        quantilesLabel = new JLabel("Quantiles:");
        classingAndMappingPanel.add(quantilesLabel);
        quantilesSlider = new JSlider(2, 8, 5);
        quantilesSlider.setMajorTickSpacing(1);
        quantilesSlider.setMinorTickSpacing(1);
        quantilesSlider.setPaintTicks(true);
        quantilesSlider.setPaintLabels(true);
        quantilesSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("quantiles slider moved"); // TODO: write useful method here.
            }
        });
        classingAndMappingPanel.add(quantilesSlider);
        this.add(classingAndMappingPanel);

//        colorLabel = new JLabel("Color ramp:");
//        mappingParameterPanel.add(colorLabel);
//        colorCB = new JComboBox<>(tempColorRampsForTesting);
//        mappingParameterPanel.add(colorCB);

//        drawMapButton = new JButton("Draw Map");
//        drawMapButton.setHorizontalTextPosition(SwingConstants.LEADING);
//        drawMapButton.setEnabled(false);

        // TODO: add legend.

    }
}
