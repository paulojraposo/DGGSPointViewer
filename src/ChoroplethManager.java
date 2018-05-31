import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

public class ChoroplethManager {

    // Good old colorbrewer.org!

    public ArrayList<String> orange3 = new ArrayList<String>();
    public ArrayList<String> orange4 = new ArrayList<String>();
    public ArrayList<String> orange5 = new ArrayList<String>();
    public ArrayList<String> orange6 = new ArrayList<String>();
    public ArrayList<String> orange7 = new ArrayList<String>();

    public ArrayList<String> purple3 = new ArrayList<String>();
    public ArrayList<String> purple4 = new ArrayList<String>();
    public ArrayList<String> purple5 = new ArrayList<String>();
    public ArrayList<String> purple6 = new ArrayList<String>();
    public ArrayList<String> purple7 = new ArrayList<String>();

    public HashMap<String,HashMap> colorHM;
    public HashMap<Integer,ArrayList> orangeShadesByClassNumber;
    public HashMap<Integer,ArrayList> purpleShadesByClassNumber;

    public ChoroplethManager() {

        this.colorHM = new HashMap<String,HashMap>();
        this.orangeShadesByClassNumber = new HashMap<Integer,ArrayList>();
        this.purpleShadesByClassNumber = new HashMap<Integer,ArrayList>();

        // Build the color look up.

        this.orange3.add("#fee6ce");
        this.orange3.add("#fdae6b");
        this.orange3.add("#e6550d");

        this.orange4.add("#feedde");
        this.orange4.add("#fdbe85");
        this.orange4.add("#fd8d3c");
        this.orange4.add("#d94701");

        this.orange5.add("#feedde");
        this.orange5.add("#fdbe85");
        this.orange5.add("#fd8d3c");
        this.orange5.add("#e6550d");
        this.orange5.add("#a63603");

        this.orange6.add("#feedde");
        this.orange6.add("#fdd0a2");
        this.orange6.add("#fdae6b");
        this.orange6.add("#fd8d3c");
        this.orange6.add("#e6550d");
        this.orange6.add("#a63603");

        this.orange7.add("#feedde");
        this.orange7.add("#fdd0a2");
        this.orange7.add("#fdae6b");
        this.orange7.add("#fd8d3c");
        this.orange7.add("#f16913");
        this.orange7.add("#d94801");
        this.orange7.add("#8c2d04");

        this.purple3.add("#efedf5");
        this.purple3.add("#bcbddc");
        this.purple3.add("#756bb1");

        this.purple4.add("#f2f0f7");
        this.purple4.add("#cbc9e2");
        this.purple4.add("#9e9ac8");
        this.purple4.add("#6a51a3");

        this.purple5.add("#f2f0f7");
        this.purple5.add("#cbc9e2");
        this.purple5.add("#9e9ac8");
        this.purple5.add("#756bb1");
        this.purple5.add("#54278f");

        this.purple6.add("#f2f0f7");
        this.purple6.add("#dadaeb");
        this.purple6.add("#bcbddc");
        this.purple6.add("#9e9ac8");
        this.purple6.add("#756bb1");
        this.purple6.add("#54278f");

        this.purple7.add("#f2f0f7");
        this.purple7.add("#dadaeb");
        this.purple7.add("#bcbddc");
        this.purple7.add("#9e9ac8");
        this.purple7.add("#807dba");
        this.purple7.add("#6a51a3");
        this.purple7.add("#4a1486");

        this.orangeShadesByClassNumber.put(3, orange3);
        this.orangeShadesByClassNumber.put(4, orange4);
        this.orangeShadesByClassNumber.put(5, orange5);
        this.orangeShadesByClassNumber.put(6, orange6);
        this.orangeShadesByClassNumber.put(7, orange7);

        this.purpleShadesByClassNumber.put(3, purple3);
        this.purpleShadesByClassNumber.put(4, purple4);
        this.purpleShadesByClassNumber.put(5, purple5);
        this.purpleShadesByClassNumber.put(6, purple6);
        this.purpleShadesByClassNumber.put(7, purple7);

        this.colorHM.put(Main.app.orangesName, orangeShadesByClassNumber);
        this.colorHM.put(Main.app.purplesName, purpleShadesByClassNumber);

    }

//    public ArrayList<String> getColorSet(String hue, Integer shadesCount){
//        // A way to get the list of colors by specifying which hue and how many shades wanted.
//        HashMap<Integer,HashMap> aColorHashMap = this.colorHM.get(hue);
//        HashMap someShades = aColorHashMap.get(shadesCount);
//        String hexString = (String) someShades.get(1);
//        return someShades;
//    }

//    public JPanel buildLegend(Integer classCount, String colorUsed){
//
//        JPanel productPanel = new JPanel();
//
//        // Will do color swatches as JLabels that load icons.
//        ArrayList<JLabel> colorPatches = new ArrayList<JLabel>();
//        for (int i=0; i<classCount; i++) {
//            JLabel aLabel = new JLabel(String.valueOf(i));
//            HashMap<Integer, String> theColorsByCount = this.colorHM.get(colorUsed);
//            Object theColors = theColorsByCount.get(classCount);
//            ArrayList<String> theColorsAL = (ArrayList<String>) theColors;
//            System.out.println(theColors);
//            aLabel.setForeground(Color.decode(theColorsAL.get(i)));
//            colorPatches.add(aLabel);
//        }
//
//        for (JLabel cPatch : colorPatches) {
//            productPanel.add(cPatch);
//        }
//
//        return productPanel;
//    }

    public Integer getClassificationForValue(Double aValue){
        // Given a number, determine which quantile it falls into,
        // espressed as an integer, with 0 being the first of however
        // many are being used. Returns null if there's no match, or
        // the value passed is zero.

        // Sort the keys.
        Set<Integer> theKeys = Main.app.quantileIndexesByBreakIndex.keySet();
        List<Integer> theKeysAsList = new ArrayList<Integer>(theKeys);
        Collections.sort(theKeysAsList);

//        System.out.println(String.valueOf(theKeysAsList));

        if (aValue > 0.0){
            for (int i=0; i<theKeysAsList.size();i++){
                ArrayList<Double> bounds = Main.app.quantileBoundsByIndex.get(i);
                Double thisLowerBound = bounds.get(0);
                Double thisUpperBound = bounds.get(1);
//                System.out.println("i is "+ String.valueOf(i));
//                System.out.println("aValue is " + String.valueOf(aValue));
//                System.out.println("lower bound: " + String.valueOf(thisLowerBound));
//                System.out.println("upper bound: " + String.valueOf(thisUpperBound));
                if ((aValue >= thisLowerBound) && (aValue <= thisUpperBound)){
//                    System.out.println("returning " + String.valueOf(i));
                    return i;
                }
            }
        }

        return null;
    }

}
