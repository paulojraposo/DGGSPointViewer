import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainAppPanel extends JPanel{

    public JFileChooser fc;

    Border bGreyLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true);
    Border bBinningTitled;
    Border bMAUPTitled;
    Border bClassificationTitled;
    Border bClearAndReset;
    JPanel logoPanel;
    JLabel logoLabel;

    // Binning panel
    JPanel binningPanel;
    JLabel chooseFileLabel;
    JPanel dataLoadingButtonsPanel;
    JButton usePrePreparedDataButton;
    String chooseFileButtonText = "Choose file";
    JButton chooseFileButton;
    JLabel attrToBinLabel;
    JComboBox attrToBinCB;
    JLabel levelChoosingLabel;
    JComboBox<String> levelIntersectionCalculationCB;
    JLabel progressMessage;
    String binningButtonText = "Plot & Run Binning";
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
    // JComboBox colorCB;
    // ImageIcon[] colorOptions = {
    //        new ImageIcon("out/resources/orangesthumbnail.png"),
    //        new ImageIcon("out/resources/purplesthumbnail.png")
    //        };
    JLabel quantilesLabel;
    JSlider quantilesSlider;
    JPanel colorRadioButtonsPanel;
    JRadioButton orangesRB;
    JRadioButton purplesRB;
    ButtonGroup colorRBGroup;

    // Clear and reset panel;
    JPanel clearAndResetPanel;
    JLabel clearAndResetLabel;
    JButton clearAndResetButton;



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
        dataLoadingButtonsPanel = new JPanel();
        FlowLayout dataLoadingButtonsPanellayout = (FlowLayout)dataLoadingButtonsPanel.getLayout();
        dataLoadingButtonsPanellayout.setVgap(0);
        dataLoadingButtonsPanellayout.setHgap(0);
        usePrePreparedDataButton = new JButton("Use built-in");
        usePrePreparedDataButton.setToolTipText("Use included and prepared Natural Earth world populated places data.");
        usePrePreparedDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Load the built-in dataset, make the choose file button unavailable,
                // and set the main app Boolean usingPreparedData, which in turn
                // signals to not actually perform binning, but use preloaded geojson
                // files.
                File preparedDataCSVFile = new File("out/resources/prepareddata/popplacesforapp.csv");
                String prepreparedDataPathString = preparedDataCSVFile.toPath().toString();
                Main.app.usingPreparedData = true;
                Main.app.receiveUserCSVPath(prepreparedDataPathString);
                disableAllBinningControls();
                Main.app.performBinning();
            }
        });
        dataLoadingButtonsPanel.add(usePrePreparedDataButton);
        chooseFileButton = new JButton(this.chooseFileButtonText);
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                // TODO: make file chooser filter for CSV files.
                int returnVal = fc.showOpenDialog(binningPanel);
                // Below, use a truncated name so the button doesn't grow (much) in size.
                chooseFileButton.setText(truncateString(fc.getSelectedFile().getName(), 8));
                usePrePreparedDataButton.setEnabled(false);
                Main.app.receiveUserCSVPath(fc.getSelectedFile().toPath().toString());
            }
        });
        dataLoadingButtonsPanel.add(chooseFileButton);
        binningPanel.add(dataLoadingButtonsPanel);
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
        progressMessage = new JLabel(""); // Blank, as a filler in the GridLayout.
        progressMessage.setHorizontalAlignment(SwingConstants.RIGHT);
        binningPanel.add(progressMessage);
        binningButton = new JButton(binningButtonText);
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
                disableAllBinningControls();
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
        levelLabel = new JLabel("<html><b>Scaling</b> (QTM level to draw):</html>");
        maupPanel.add(levelLabel);
        levelSlider = new JSlider(Main.app.minQTMLevel, Main.app.maxQTMLevels, Main.app.defaultQTMLevel);
        levelSlider.setMajorTickSpacing(1);
        // levelSlider.setMinorTickSpacing(1);
        levelSlider.setPaintTicks(true);
        levelSlider.setPaintLabels(true);
        levelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Main.app.setCurrentQTMDrawingLevel(levelSlider.getValue());
            }
        });
        maupPanel.add(levelSlider);
        EWTranslateLabel = new JLabel("<html><b>Zoning</b> (longitudinal shift<br>of mesh in degrees):</html>");
        maupPanel.add(EWTranslateLabel);
        EWTranslateSlider = new JSlider(Main.app.minimumLonShift, Main.app.maximumLonShift, Main.app.defaultLonShift);
        EWTranslateSlider.setMajorTickSpacing(2);
        EWTranslateSlider.setMinorTickSpacing(1);
        EWTranslateSlider.setPaintTicks(true);
        EWTranslateSlider.setPaintLabels(true);
        EWTranslateSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Main.app.setCurrentLonShift(EWTranslateSlider.getValue());
            }
        });
        maupPanel.add(EWTranslateSlider);
        this.add(maupPanel);

        // Classing and Mapping Panel
        classingAndMappingPanel = new JPanel();
        bClassificationTitled = BorderFactory.createTitledBorder(bGreyLine, "Choropleth Classification", TitledBorder.LEFT, TitledBorder.TOP, null, Color.black);
        classingAndMappingPanel.setBorder(bClassificationTitled);
        GridLayout binningPanelLayout = new GridLayout(2,2);
        classingAndMappingPanel.setLayout(binningPanelLayout);
        quantilesLabel = new JLabel("Quantiles:");
        classingAndMappingPanel.add(quantilesLabel);
        quantilesSlider = new JSlider(Main.app.minQuantiles, Main.app.maxQuantiles, Main.app.defaultQuantileCount);
        quantilesSlider.setMajorTickSpacing(1);
        quantilesSlider.setMinorTickSpacing(1);
        quantilesSlider.setPaintTicks(true);
        quantilesSlider.setPaintLabels(true);
        quantilesSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Main.app.setCurrentlySelectedQuantileCount(quantilesSlider.getValue());
            }
        });
        classingAndMappingPanel.add(quantilesSlider);
        colorLabel = new JLabel("Color ramp:");
        classingAndMappingPanel.add(colorLabel);
        colorRadioButtonsPanel = new JPanel();
        colorRadioButtonsPanel.setLayout(new BoxLayout(colorRadioButtonsPanel, BoxLayout.LINE_AXIS));
        orangesRB = new JRadioButton(Main.app.orangesName);
        orangesRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.app.setColorRampChosen(Main.app.orangesName);
            }
        });
        purplesRB = new JRadioButton(Main.app.purplesName);
        purplesRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.app.setColorRampChosen(Main.app.purplesName);
            }
        });
        colorRBGroup = new ButtonGroup();
        colorRBGroup.add(orangesRB);
        colorRBGroup.add(purplesRB);
        orangesRB.setSelected(true);
        colorRadioButtonsPanel.add(orangesRB);
        colorRadioButtonsPanel.add(Box.createRigidArea(new Dimension(7,0)));
        colorRadioButtonsPanel.add(purplesRB);
        classingAndMappingPanel.add(colorRadioButtonsPanel);
        this.add(classingAndMappingPanel);

        // TODO: add legend.


        clearAndResetPanel = new JPanel();
        GridLayout clearAndResetPanelLayout = new GridLayout(1,2);
        clearAndResetPanel.setLayout(clearAndResetPanelLayout);
//        bClearAndReset = BorderFactory.createTitledBorder(bGreyLine, "", TitledBorder.LEFT,  TitledBorder.TOP, null, Color.black);
//        clearAndResetPanel.setBorder(bClearAndReset);
        clearAndResetLabel = new JLabel("");
        clearAndResetPanel.add(clearAndResetLabel);
        clearAndResetButton = new JButton("Clear & Reset App");
        clearAndResetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Main.app.resetAppState();
            }
        } );
        clearAndResetPanel.add(clearAndResetButton);
        this.add(clearAndResetPanel);
    }

    // Below from https://www.dotnetperls.com/truncate-java.
    public static String truncateString(String value, int length) {
        // Ensure String length is longer than requested size.
        if (value.length() > length) {
            return value.substring(0, length) + "...";
        } else {
            return value;
        }
    }
    public void disableAllBinningControls(){
        this.usePrePreparedDataButton.setEnabled(false);
        this.chooseFileButton.setEnabled(false);
        this.levelIntersectionCalculationCB.setEnabled(false);
        this.attrToBinCB.setEnabled(false);
        this.binningButton.setEnabled(false);
    }

    public void resetAllBinningControls(){
        this.usePrePreparedDataButton.setEnabled(true);
        this.chooseFileButton.setText(this.chooseFileButtonText);
        this.chooseFileButton.setEnabled(true);
        this.levelIntersectionCalculationCB.setEnabled(true);
        this.attrToBinCB.setModel(Main.app.cbModel);
        this.attrToBinCB.setEnabled(false);
        this.binningButton.setEnabled(false);
        this.binningButton.setText(this.binningButtonText);
    }

}
