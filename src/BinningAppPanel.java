import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;



/*
TODO: Update interface slider for QTM level if user loads binned data at a custom QTM level.
 */




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

    // Data elements

    File userCSVFile;
    String userCSVFilePath;

    String[] customLevelOptions = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};

    String userDirPath;

    Boolean csvChosen = false;
    Boolean folderChosen = false;


    public BinningAppPanel(){

        this.setLayout(new GridLayout(0,2)); // zero rows means as many as get asked for.

        Border bGreyLine = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true);
        titledBorder = BorderFactory.createTitledBorder(bGreyLine, "Binning", TitledBorder.LEFT,  TitledBorder.TOP, null, Color.gray);
        this.setBorder(titledBorder);

        // Instantiate and place GUI elements.
        // We use GridLayout in several places to get elements to span
        // their cell locations, and to keep everything in consistent
        // relative dimensions.

        // We keep runButton disabled until the user have given all of a csv file, a folder, and column name.

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
                int returnVal = csvFC.showOpenDialog(BinningAppPanel.this); // component right here?
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    userCSVFile = csvFC.getSelectedFile();
                    userCSVFilePath = userCSVFile.getAbsolutePath();
                    csvTextField.setText(userCSVFilePath);
                    // Send CSV to main app and plot.
                    Main.app.receiveUserCSVPath(userCSVFilePath);
                    Main.app.plotCSVPoints();
                    csvChosen = true;
                    tryEnableRunButton();
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
        CSV Field
         */

        this.columnLabel = new JLabel("Name of column to bin:");
        this.add(this.columnLabel);
        this.columnTextFieldPanel = new JPanel();
        this.columnTextFieldPanel.setLayout(new GridLayout(1,1));
        this.columnTextField = new JTextField("", 20);
        // Below, for waiting until user enters something in this text field before enabling the run button.
        this.columnTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                tryEnableRunButton();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                tryEnableRunButton();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                tryEnableRunButton();
            }
        });
        this.columnTextFieldPanel.add(this.columnTextField);
        this.add(this.columnTextFieldPanel);

        /*
        QTM Level to bin up to
         */
        this.levelLabel = new JLabel("Bin up to QTM level:");
        this.add(this.levelLabel);
        this.levelPanel = new JPanel();
        this.levelPanel.setLayout(new GridLayout(1,1));
//        this.levelCB = new JComboBox<String>(Main.app.levelOptions);
        this.levelCB = new JComboBox(customLevelOptions);
        this.levelCB.setSelectedItem(customLevelOptions[11]);
        this.levelPanel.add(this.levelCB);
        this.add(this.levelPanel);

        /*
        Folder to save to
         */

        this.saveLabel = new JLabel("Save to folder:");
        this.add(this.saveLabel);
        this.savePanel = new JPanel();
        this.savePanel.setLayout(new GridLayout(1,2));
        this.saveTextField = new JTextField("", 20);
        this.saveToButton = new JButton("Choose Folder");
        this.saveToButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Allow the user to select their output folder with a file chooser.
                saveToFC = new JFileChooser();
                saveToFC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = saveToFC.showOpenDialog(BinningAppPanel.this); // component right here?
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    userDirPath = saveToFC.getSelectedFile().getAbsolutePath();
                    saveTextField.setText(userDirPath);
                    folderChosen = true;
                    tryEnableRunButton();
//                    System.out.println("Folder: " + userDirPath + ".");
                }
            }
        });
        this.savePanel.add(this.saveTextField);
        this.savePanel.add(this.saveToButton);
        this.add(this.savePanel);

        /*
        Loading data to viewer?
         */

        this.loadLabel = new JLabel("Load binned data to viewer:");
        this.add(this.loadLabel);
        this.loadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.loadChkBox = new JCheckBox();
        this.loadChkBox.setSelected(true);
        this.loadPanel.add(this.loadChkBox);
        this.add(this.loadPanel);

        /*
        Run binning
         */

        this.add(this.dummyBlankLabel = new JLabel(""));
        this.runButton = new JButton("Perform binning");
        this.runButton.setEnabled(false); // Disabled until csv file, folder, and column name are given.
        // TODO: add action listener and functionality to this button!
        this.add(this.runButton);

        /*
        Status messages
         */

        this.statusLabel = new JLabel("Status:");
        this.add(this.statusLabel);
        this.status = new JLabel("Ready");
        this.add(this.status);

        /*
        Panel appearance
         */

        // Setting just the width
        Dimension ps = this.getPreferredSize();
        ps.width = 600;
        this.setPreferredSize(ps);

        this.setVisible(true);

    }

    private void tryEnableRunButton(){
        // Checks whether csv file, folder, and column name values have all been given by the user.
        if (csvChosen == true & folderChosen == true & columnTextField.getText().equals("") == false){
            runButton.setEnabled(true);
        }
    }

}