import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PointFileSelectionPanel extends JPanel{

    JFileChooser fc;
    JLabel chosenFile;
    JButton chooseFileButton;

    public PointFileSelectionPanel(){

        setLayout(new BorderLayout());

        chooseFileButton = new JButton("Choose File...");
        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(PointFileSelectionPanel.this);
                chosenFile.setText(fc.getSelectedFile().getName());
            }
        });

        chosenFile = new JLabel("hi there");

        this.add(chooseFileButton, BorderLayout.WEST);
        this.add(chosenFile);

    }

}
