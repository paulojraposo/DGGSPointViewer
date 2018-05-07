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

    // Some ubiquitous elements
    public JFileChooser fc;
    String[] levelOptions = new String[]{"1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
    String[] quantilesOptions = new String[]{"Quartiles", "Quintiles", "Sextiles", "Septiles"};
//    String[] tempAttrsForTesting = new String[]{"one", "another", "one more"};
//    String[] tempColorRampsForTesting = new String[]{"oranges", "greens", "blues"};
    int maximumTranslationDegrees = 9;
    Border bGreyLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true);
    Border bMAUPTitled;
    Border bClassificationTitled;

    // Binning panel
    JPanel binningPanel;
    JPanel logoPanel;
    JLabel logoLabel;
    JPanel fileChoosingPanel;
    JLabel chosenFileLabel;
    JButton chooseFileButton;
    JPanel levelIntersectionCalculationPanel;
    JLabel levelChoosingLabel;
    JComboBox<String> levelIntersectionCalculationCB;

//    JPanel attrToBinPanel;
//    JLabel attrToBinLabel;
//    JComboBox<String> attrToBinCB;

    JButton binningButton;
    JLabel binningProgressMessageLabel;

    // MAUP panel
    JPanel maupPanel;
    JLabel attrLabel;
    JComboBox<String> attrCB;
    JLabel levelLabel;
    JComboBox<String> levelCB;
    JLabel EWTranslateLabel;
    JSlider EWTranslateSlider;

    // Classification and Mapping area
    JPanel classingAndMappingPanel;
    JLabel colorLabel;
    JComboBox<String> colorCB;
    JLabel quantilesLabel;
    JComboBox<String> quantilesCB;
    JButton drawMapButton;


    public MainAppPanel(){

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        this.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("MAUP Viewer")));

        // UT Logo
        logoPanel = new JPanel();
        logoLabel = new JLabel();
        logoLabel.setIcon(new ImageIcon("out/production/DGGSPointViewer/resources/UTGeog.png"));
        logoLabel.setVisible(true);
        logoPanel.add(logoLabel);
        this.add(logoPanel);

        // File choosing
        fileChoosingPanel = new JPanel(new FlowLayout());
        chooseFileButton = new JButton("Choose File...");
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                // TODO: make file chooser filter for CSV files.
                int returnVal = fc.showOpenDialog(levelIntersectionCalculationPanel);
                chosenFileLabel.setText(fc.getSelectedFile().getName());
                Main.app.receiveUserFileReference(fc.getSelectedFile().toPath().toString());
            }
        });
        chosenFileLabel = new JLabel("<Filename>");
        fileChoosingPanel.add(chooseFileButton);
        fileChoosingPanel.add(chosenFileLabel);
        this.add(fileChoosingPanel);

        // Level intersection calculation choosing
        levelIntersectionCalculationPanel = new JPanel(new FlowLayout());
        levelChoosingLabel = new JLabel("Calculate Intersections up to level:");
        levelIntersectionCalculationCB = new JComboBox<String>(levelOptions);
        levelIntersectionCalculationCB.setSelectedIndex(15);
        levelIntersectionCalculationPanel.add(levelChoosingLabel);
        levelIntersectionCalculationPanel.add(levelIntersectionCalculationCB);
        this.add(levelIntersectionCalculationPanel);

        // Attribute choosing
//        GridLayout attrToBinPanelLayout = new GridLayout(1,2);
//        attrToBinPanel = new JPanel(attrToBinPanelLayout);
//        attrToBinLabel = new JLabel("Attribute to bin:");
//        attrToBinCB = new JComboBox(); // The choices here need to be set once the CSV is read!
//        attrToBinCB.setEnabled(false); // Also make it enabled when option is available.
//        attrToBinPanel.add(attrToBinLabel);
//        attrToBinPanel.add(attrToBinCB);
//        this.add(attrToBinPanel);
//        attrLabel = new JLabel("Attribute to map:");
//        mappingParameterPanel.add(attrLabel);
//        attrCB = new JComboBox<String>(tempAttrsForTesting);
//        attrCB.setEnabled(false); // Enable when attribute chosen
//        mappingParameterPanel.add(attrCB);

        // Binning activation and progress
        binningPanel = new JPanel(new BorderLayout());
        binningButton = new JButton("Run Binning"); // Change text here while binning to tell user it's in process.
        binningButton.setEnabled(false);
        binningPanel.add(binningButton, BorderLayout.NORTH);
        this.add(binningPanel);

        // MAUP panel
        maupPanel = new JPanel();
        bMAUPTitled = BorderFactory.createTitledBorder(bGreyLine, "Modifiable Areal Units", TitledBorder.LEFT, TitledBorder.TOP);
        maupPanel.setBorder(bMAUPTitled);
        maupPanel.setLayout(new GridLayout(2,2));
        levelLabel = new JLabel("<html><b>Scaling:</b> QTM level to draw:</html>");
        maupPanel.add(levelLabel);
        levelCB = new JComboBox<String>(levelOptions);
        levelCB.setSelectedIndex(11);
        maupPanel.add(levelCB);
        EWTranslateLabel = new JLabel("<html><b>Zoning:</b> Longitudinal shift<br>of mesh in degrees:</html>");
        EWTranslateSlider = new JSlider(-1*maximumTranslationDegrees, maximumTranslationDegrees, 0);
        EWTranslateSlider.setMajorTickSpacing(3);
        EWTranslateSlider.setMinorTickSpacing(1);
        EWTranslateSlider.setPaintTicks(true);
        EWTranslateSlider.setPaintLabels(true);
        EWTranslateSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println("slider moved"); // TODO: write useful method here.
            }
        });
        maupPanel.add(EWTranslateLabel);
        maupPanel.add(EWTranslateSlider);
        this.add(maupPanel);

        // Classing and Mapping Panel
        classingAndMappingPanel = new JPanel();
        bClassificationTitled = BorderFactory.createTitledBorder(bGreyLine, "Choropleth Classification", TitledBorder.LEFT, TitledBorder.TOP);
        classingAndMappingPanel.setBorder(bClassificationTitled);
        GridLayout binningPanelLayout = new GridLayout(1,2); // TODO: revise x by y as needed for new elements.
        classingAndMappingPanel.setLayout(binningPanelLayout);
        quantilesLabel = new JLabel("Quantiles:");
        classingAndMappingPanel.add(quantilesLabel);
        quantilesCB = new JComboBox<String>(quantilesOptions);
        classingAndMappingPanel.add(quantilesCB);
        this.add(classingAndMappingPanel);

//        colorLabel = new JLabel("Color ramp:");
//        mappingParameterPanel.add(colorLabel);
//        colorCB = new JComboBox<>(tempColorRampsForTesting);
//        mappingParameterPanel.add(colorCB);

//        drawMapButton = new JButton("Draw Map");
//        drawMapButton.setHorizontalTextPosition(SwingConstants.LEADING);
//        drawMapButton.setEnabled(false);

        // TODO: add legend.

//        this.add(drawMapButton);

    }

}
