import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BinningPanel extends JPanel{

    JFileChooser fc;
    JPanel logoPanel;
    JLabel logoLabel;
    JPanel fileChoosingPanel;
    JLabel chosenFileLabel;
    JButton chooseFileButton;
    JPanel levelChoosingPanel;
    JLabel levelChoosingLabel;
    String[] levelOptions = new String[]{"1", "2", "3", "4", "5", "6", "7",
    "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};
    JComboBox<String> levelOptionCB;
    JPanel binningPanel;
    JButton binningButton;
    JLabel binningProgressMessageLabel;

    public BinningPanel(){

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        this.setBorder(
            new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("MAUP Viewer")));

        // UT Logo
        // fixme :)
//        logoPanel = new JPanel();
//        logoLabel = new JLabel("hi");
//        try {
//            BufferedImage myPicture = ImageIO.read(new File("resources/UTGeog.jpg"));
//            ImageIcon utIcon = new ImageIcon(myPicture);
//            logoLabel.setIcon(utIcon);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        logoPanel.add(logoLabel);
//        this.add(logoPanel);

        // File choosing
        fileChoosingPanel = new JPanel(new FlowLayout());
        chooseFileButton = new JButton("Choose File...");
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(levelChoosingPanel);
                chosenFileLabel.setText(fc.getSelectedFile().getName());
            }
        });
        chosenFileLabel = new JLabel("<Filename>");
        fileChoosingPanel.add(chooseFileButton);
        fileChoosingPanel.add(chosenFileLabel);
        this.add(fileChoosingPanel);

        // level choosing
        levelChoosingPanel = new JPanel(new FlowLayout());
        levelChoosingLabel = new JLabel("Calculate Intersections up to level:");
        levelOptionCB = new JComboBox<String>(levelOptions);
        levelChoosingPanel.add(levelChoosingLabel);
        levelChoosingPanel.add(levelOptionCB);
        this.add(levelChoosingPanel);

        // Binning activation and progress
        binningPanel = new JPanel(new BorderLayout());
        binningButton = new JButton("Run Binning");
        binningProgressMessageLabel = new JLabel("Ready");
        binningPanel.add(binningButton, BorderLayout.NORTH);
        binningPanel.add(binningProgressMessageLabel, BorderLayout.SOUTH);
        this.add(binningPanel);

        // Visual separation between file choosing and mapping options
        this.add(new JSeparator(JSeparator.HORIZONTAL));

        // TODO: add mapping options panel here.

    }

}
