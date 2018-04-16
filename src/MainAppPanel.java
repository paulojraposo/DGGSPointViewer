
import org.jcp.xml.dsig.internal.dom.Utils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainAppPanel extends JPanel{

    // Some ubiquitous elements
    JFileChooser fc;
    String[] levelOptions = new String[]{"1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
    String[] quantilesOptions = new String[]{"Quartiles", "Quintiles", "Sextiles", "Septiles"};
    String[] tempAttrsForTesting = new String[]{"one", "another", "one more"};
    String[] tempColorRampsForTesting = new String[]{"oranges", "greens", "blues"};

    // Binning area
    JPanel binningPanel;
    JPanel logoPanel;
    JLabel logoLabel;
    JPanel fileChoosingPanel;
    JLabel chosenFileLabel;
    JButton chooseFileButton;
    JPanel levelChoosingPanel;
    JLabel levelChoosingLabel;
    JComboBox<String> levelOptionCB;

    JPanel attrToBinPanel;
    JLabel attrToBinLabel;
    JComboBox<String> attrToBinCB;

    JButton binningButton;
    JLabel binningProgressMessageLabel;

    // Mapping area
    JPanel mappingSuperPanel;
    JPanel mappingParameterPanel;
    JLabel attrLabel;
    JLabel levelLabel;
    JLabel colorLabel;
    JLabel quantilesLabel;
    JComboBox<String> attrCB;
    JComboBox<String> levelCB;
    JComboBox<String> colorCB;
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
                int returnVal = fc.showOpenDialog(levelChoosingPanel);
                chosenFileLabel.setText(fc.getSelectedFile().getName());
                Main.receiveUserFileReference(fc.getSelectedFile().toPath().toString());
            }
        });
        chosenFileLabel = new JLabel("<Filename>");
        fileChoosingPanel.add(chooseFileButton);
        fileChoosingPanel.add(chosenFileLabel);
        this.add(fileChoosingPanel);

        // Level choosing
        levelChoosingPanel = new JPanel(new FlowLayout());
        levelChoosingLabel = new JLabel("Calculate Intersections up to level:");
        levelOptionCB = new JComboBox<String>(levelOptions);
        levelOptionCB.setSelectedIndex(15);
        levelChoosingPanel.add(levelChoosingLabel);
        levelChoosingPanel.add(levelOptionCB);
        this.add(levelChoosingPanel);

        // Attribute choosing
        GridLayout attrToBinPanelLayout = new GridLayout(1,2);
        attrToBinPanel = new JPanel(attrToBinPanelLayout);
        attrToBinLabel = new JLabel("Attribute to bin:");
        attrToBinCB = new JComboBox(); // The choices here need to be set once the CSV is read!
        attrToBinCB.setEnabled(false); // Also make it enabled when option is available.
        attrToBinPanel.add(attrToBinLabel);
        attrToBinPanel.add(attrToBinCB);
        this.add(attrToBinPanel);

        // Binning activation and progress
        binningPanel = new JPanel(new BorderLayout());
        binningButton = new JButton("Run Binning");
        binningButton.setEnabled(false);
        binningProgressMessageLabel = new JLabel("Status: Ready");
        binningPanel.add(binningButton, BorderLayout.NORTH);
        binningPanel.add(binningProgressMessageLabel, BorderLayout.SOUTH);

        this.add(binningPanel);

        // Visual separation between file choosing and mapping options
        this.add(new JSeparator(JSeparator.HORIZONTAL));

        mappingSuperPanel = new JPanel(new BorderLayout());

        GridLayout binningPanelLayout = new GridLayout(2,2);
        mappingParameterPanel = new JPanel(binningPanelLayout);

//        attrLabel = new JLabel("Attribute to map:");
//        mappingParameterPanel.add(attrLabel);
//        attrCB = new JComboBox<String>(tempAttrsForTesting);
//        attrCB.setEnabled(false); // Enable when attribute chosen
//        mappingParameterPanel.add(attrCB);

        levelLabel = new JLabel("Level to draw:");
        mappingParameterPanel.add(levelLabel);
        levelCB = new JComboBox<String>(levelOptions);
        levelCB.setSelectedIndex(11);
        mappingParameterPanel.add(levelCB);

//        colorLabel = new JLabel("Color ramp:");
//        mappingParameterPanel.add(colorLabel);
//        colorCB = new JComboBox<>(tempColorRampsForTesting);
//        mappingParameterPanel.add(colorCB);

        quantilesLabel = new JLabel("Quantiles:");
        mappingParameterPanel.add(quantilesLabel);
        quantilesCB = new JComboBox<String>(quantilesOptions);
        mappingParameterPanel.add(quantilesCB);

        drawMapButton = new JButton("Draw Map");
        drawMapButton.setHorizontalTextPosition(SwingConstants.LEADING);
        drawMapButton.setEnabled(false);

        // TODO: add legend.

        mappingSuperPanel.add(mappingParameterPanel, BorderLayout.NORTH);
        mappingSuperPanel.add(drawMapButton, BorderLayout.SOUTH);

        this.add(mappingSuperPanel);

    }

}
