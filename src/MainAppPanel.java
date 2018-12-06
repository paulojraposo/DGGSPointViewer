import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class MainAppPanel extends JPanel{


    public JFileChooser fc;

    Border bGreyLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true);
    Border bBinningTitled;
    Border bMAUPTitled;
    Border bClassificationTitled;
    JPanel logoPanel;
    JButton logoButton;

    // Binning panel
    JPanel binningPanel;
    JLabel chooseFileLabel;
    JPanel dataLoadingButtonsPanel;
    JButton usePreparedDataButton;
    JButton loadUserLayersButton;

    // MAUP panel
    JPanel maupPanel;
    JLabel levelLabel;
    JSlider levelSlider;
    JLabel EWTranslateLabel;
    JSlider EWTranslateSlider;

    // Classification and Mapping panel
    JPanel classingAndMappingPanel;
    JLabel colorLabel;
    JLabel statOfInterestLabel;
    JComboBox statOfInterestCB;
    JPanel statOfInterestCBPanel;
    JLabel quantilesLabel;
    JSlider quantilesSlider;
    JLabel dummyLegendLabel;
    AppLegend legendPanel;
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
        this.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), new TitledBorder("")));

        // UT Logo, in a button, that, when pressed, takes the user to my UT Geography webpage.
        logoPanel = new JPanel();
        logoButton = new JButton();
        InputStream logoIS = this.getClass().getResourceAsStream("resources/UTGeog.png");
        BufferedImage utIcon = null;
        try {
            utIcon = ImageIO.read(logoIS);
            ImageIcon ii = new ImageIcon(utIcon);
            logoButton.setIcon(ii);
            logoButton.setVisible(true);
            logoButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    URI uri = null;
                    try {
                        uri = new URI("http://geography.utk.edu/about-us/faculty/dr-paulo-raposo/");
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                    Main.app.openURI(uri);
                }
            });
            logoButton.setFocusable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logoPanel.add(logoButton);
        this.add(logoPanel);

        // Data panel
        binningPanel = new JPanel();
        bBinningTitled = BorderFactory.createTitledBorder(bGreyLine, "Data Input", TitledBorder.LEFT,  TitledBorder.TOP, null, Color.gray);
        binningPanel.setBorder(bBinningTitled);
        binningPanel.setLayout(new GridLayout(0,2));
        chooseFileLabel = new JLabel("Input CSV:");
        binningPanel.add(chooseFileLabel);
        dataLoadingButtonsPanel = new JPanel();
        FlowLayout dataLoadingButtonsPanelLayout = (FlowLayout) dataLoadingButtonsPanel.getLayout();
        dataLoadingButtonsPanelLayout.setVgap(0);
        dataLoadingButtonsPanelLayout.setHgap(0);
        usePreparedDataButton = new JButton("Use built-in");
        usePreparedDataButton.setToolTipText("Use included and prepared data: Natural Earth populated places throughout Africa.");
        usePreparedDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Load the built-in dataset, make the choose file button unavailable,
                // and set the main app Boolean usingPreparedData, which in turn
                // signals to not actually perform binning, but use preloaded geojson
                // files.
                loadUserLayersButton.setEnabled(false);
                File preparedDataCSVFile = new File("out/resources/prepareddata/AfricaPopPlaces/AfricaPopulatedPlacesForApp.csv");
                String prepreparedDataPathString = preparedDataCSVFile.toPath().toString();
                Main.app.hasBinned = true;
                Main.app.usingPreparedData = true;
                Main.app.receiveUserCSVPath(prepreparedDataPathString);
                Main.app.bypassBinning();
            }
        });
        dataLoadingButtonsPanel.add(usePreparedDataButton);
        loadUserLayersButton = new JButton("Load data");
        loadUserLayersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usePreparedDataButton.setEnabled(false);
                Main.app.openUserLoadingWindow();
            }
        });
        dataLoadingButtonsPanel.add(loadUserLayersButton);
        binningPanel.add(dataLoadingButtonsPanel);
        this.add(binningPanel);

        // MAUP panel
        maupPanel = new JPanel();
        bMAUPTitled = BorderFactory.createTitledBorder(bGreyLine, "Modifiable Areal Units", TitledBorder.LEFT,  TitledBorder.TOP, null, Color.black);
        maupPanel.setBorder(bMAUPTitled);
        maupPanel.setLayout(new GridLayout(2,2));
        levelLabel = new JLabel("<html><b>Scaling</b> (QTM level):</html>");
        maupPanel.add(levelLabel);
        levelSlider = new JSlider(Main.app.minQTMLevel, Main.app.maxQTMLevels, Main.app.defaultQTMLevel);
        levelSlider.setMajorTickSpacing(1);
        levelSlider.setPaintTicks(true);
        levelSlider.setPaintLabels(true);
        levelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Main.app.setCurrentQTMDrawingLevel(levelSlider.getValue());
            }
        });
        maupPanel.add(levelSlider);
        EWTranslateLabel = new JLabel("<html><b>Zoning</b> (East-West shift<br>of QTM in degrees):</html>");
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

        // Visualization Panel
        classingAndMappingPanel = new JPanel();
        bClassificationTitled = BorderFactory.createTitledBorder(bGreyLine, "Visualization", TitledBorder.LEFT, TitledBorder.TOP, null, Color.black);
        classingAndMappingPanel.setBorder(bClassificationTitled);
        GridLayout binningPanelLayout = new GridLayout(4,2);
        classingAndMappingPanel.setLayout(binningPanelLayout);
        statOfInterestLabel = new JLabel("Aggregate statistic:");
        statOfInterestCB = new JComboBox(Main.app.statsAvailable.toArray());
        statOfInterestCB.setSelectedItem(Main.app.aggregatedStatOfInterest);
        statOfInterestCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.app.aggregatedStatOfInterest = (String) statOfInterestCB.getSelectedItem();
                Main.app.triggerRedraw();
            }
        });
        statOfInterestCBPanel = new JPanel();
        statOfInterestCBPanel.add(statOfInterestCB);
        classingAndMappingPanel.add(statOfInterestLabel);
        classingAndMappingPanel.add(statOfInterestCBPanel);
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
        dummyLegendLabel = new JLabel("");
        classingAndMappingPanel.add(dummyLegendLabel);
        legendPanel = new AppLegend();
        classingAndMappingPanel.add(legendPanel);
        colorLabel = new JLabel("Choropleth color ramp:");
        classingAndMappingPanel.add(colorLabel);
        colorRadioButtonsPanel = new JPanel();
        colorRadioButtonsPanel.setLayout(new GridBagLayout());
        orangesRB = new JRadioButton(Main.app.orangesName);
        orangesRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.app.setCurrentColorRampChosen(Main.app.orangesName);
            }
        });
        purplesRB = new JRadioButton(Main.app.purplesName);
        purplesRB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Main.app.setCurrentColorRampChosen(Main.app.purplesName);
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

        // Clear and Reset Panel
        clearAndResetPanel = new JPanel();
        GridLayout clearAndResetPanelLayout = new GridLayout(1,2);
        clearAndResetPanel.setLayout(clearAndResetPanelLayout);
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
        this.usePreparedDataButton.setEnabled(false);
        this.loadUserLayersButton.setEnabled(false);
    }

    public void resetAllBinningControls(){
        this.usePreparedDataButton.setEnabled(true);
        this.loadUserLayersButton.setEnabled(true);
    }

}
