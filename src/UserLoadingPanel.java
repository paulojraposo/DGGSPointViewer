import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;



/*
TODO: Update interface slider for QTM level if user loads binned data at a custom QTM level.
 */


public class UserLoadingPanel extends JPanel {

    // GUI elements declaration.

    TitledBorder titledBorder;

    JLabel csvLabel;
    JPanel csvChoosingPanel = new JPanel();
    JTextField csvTextField;
    JButton chooseCSVButton;
    public JFileChooser csvFC;

    JLabel inFolderLabel;
    JTextField inFolderTextField;
    JButton inFolderButton;
    JFileChooser inFolderFC;
    JPanel inFolderPanel;

    JLabel dummyBlankLabel;
    JButton loadButton;

    // Data elements

    File userCSVFile;
    String userCSVFilePath;

    String userDirectoryPath;

    Boolean csvChosen = false;
    Boolean folderChosen = false;


    public UserLoadingPanel(){

        this.setLayout(new GridLayout(0,2)); // zero rows means as many as get asked for.

        Border bGreyLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true);
        titledBorder = BorderFactory.createTitledBorder(bGreyLine, "CSV file and folder with layers", TitledBorder.LEFT,  TitledBorder.TOP, null, Color.gray);
        this.setBorder(titledBorder);

        // Instantiate and place GUI elements.
        // We use GridLayout in several places to get elements to span
        // their cell locations, and to keep everything in consistent
        // relative dimensions.

        // We keep loadButton disabled until the user have given all of a csv file, a folder, and column name.

        /*
        CSV File
         */

        this.csvLabel = new JLabel("CSV of points:");
        this.add(this.csvLabel);
        this.csvTextField = new JTextField("choose a file...");
        this.chooseCSVButton = new JButton("Choose");
        this.chooseCSVButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Allow the user to select their CSV file with a file chooser.
                csvFC = new JFileChooser();
                int returnVal = csvFC.showOpenDialog(UserLoadingPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    userCSVFile = csvFC.getSelectedFile();
                    userCSVFilePath = userCSVFile.getAbsolutePath();
                    csvTextField.setText(userCSVFilePath);
                    // Send CSV to main app and plot.
                    Main.app.receiveUserCSVPath(userCSVFilePath);
                    Main.app.plotCSVPoints();
                    csvChosen = true;
                    tryEnableLoadButton();
//                    System.out.println("File: " + userCSVFilePath + ".");
                }
            }
        });

        this.csvChoosingPanel = new JPanel();
        this.csvChoosingPanel.setLayout(new GridLayout(1,2));
        this.csvChoosingPanel.add(this.csvTextField);
        this.csvChoosingPanel.add(this.chooseCSVButton);
        this.add(this.csvChoosingPanel);


        /*
        Folder to save to
         */

        this.inFolderLabel = new JLabel("Folder with prepared layers:");
        this.add(this.inFolderLabel);
        this.inFolderPanel = new JPanel();
        this.inFolderPanel.setLayout(new GridLayout(1,2));
        this.inFolderTextField = new JTextField("", 20);
        this.inFolderButton = new JButton("Choose Folder");
        this.inFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Allow the user to select their output folder with a file chooser.
                inFolderFC = new JFileChooser();
                inFolderFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = inFolderFC.showOpenDialog(UserLoadingPanel.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    userDirectoryPath = inFolderFC.getSelectedFile().getAbsolutePath();
                    inFolderTextField.setText(userDirectoryPath);
                    folderChosen = true;
                    tryEnableLoadButton();
//                    System.out.println("Folder: " + userDirPath + ".");
                }
            }
        });
        this.inFolderPanel.add(this.inFolderTextField);
        this.inFolderPanel.add(this.inFolderButton);
        this.add(this.inFolderPanel);

        /*
        Load layers
         */

        this.add(this.dummyBlankLabel = new JLabel(""));
        this.loadButton = new JButton("Load Layers");
        this.loadButton.setEnabled(false); // Disabled until csv file, folder, and column name are given.
        // TODO: add action listener and functionality to this button!
        this.loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //
                String dataPathFormat = buildUserDataPathFormat(userDirectoryPath);
//                System.out.println(dataPathFormat);
                Main.app.receiveUserQTMLayersFolderPath(dataPathFormat);
//              System.out.println("Folder: " + userDirPath + ".");

            }
        });



        this.add(this.loadButton);

        /*
        Panel appearance
         */

        // Setting just the width
        Dimension ps = this.getPreferredSize();
        ps.width = 600;
        this.setPreferredSize(ps);

        this.setVisible(true);

    }

    public String buildUserDataPathFormat(String userDirPath){
        String absolutePathFormat = userDirPath + File.separator + "qtmlvl%slonshft%s_agg.geojson";
        return absolutePathFormat;
    }


    private void tryEnableLoadButton(){
        // Checks whether csv file, folder, and column name values have all been given by the user.
        if (csvChosen == true & folderChosen == true){
            loadButton.setEnabled(true);
        }
    }

}