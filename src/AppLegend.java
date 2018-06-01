import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class AppLegend extends JPanel {

    public Integer numberOfChips;
    public ArrayList<JLabel> chips;

    public AppLegend(){
        this.chips = new ArrayList<JLabel>();
        this.numberOfChips = Main.app.currentlySelectedQuantileCount;
        this.setLayout(new GridBagLayout());
    }

    public static String numberSubscript(int value) {
        // this taken from https://coderanch.com/t/562692/java/Number-subscript-Java
        int remForHun = value % 100;
        int remForTen = value % 10;
        if (remForHun - remForTen == 10) {
            return "th";
        }
        switch (remForTen) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public void refreshLegend(ArrayList<String> colorSet){
        // Below, we draw "chips" of color as JLabels with
        // backgrounds set opaque and to color.
        this.removeAll();
        this.numberOfChips = colorSet.size();
        this.chips.clear();
        for (int i=0;i<colorSet.size();i++){
            // Need to have some characters for the JLabel to draw visibly.
            String numberText = String.valueOf(i+1);
            String ithText = numberSubscript(i+1);
            String superscriptText = String.format("<html>&ensp;%s<sup>%s</sup>&ensp;", numberText, ithText);
            JLabel aLabel = new JLabel(superscriptText);
            String fontName = aLabel.getFont().getName();
            aLabel.setFont(new Font(fontName, Font.PLAIN, 10));
            aLabel.setBackground(Color.decode(colorSet.get(i)));
            aLabel.setOpaque(true);
            aLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.chips.add(aLabel);
            this.add(aLabel);
        }
        this.revalidate();
        this.repaint();
    }
}
