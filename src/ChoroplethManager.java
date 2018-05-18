import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChoroplethManager {

    // Good old colorbrewer.org!
    public List<String> orange3 = Arrays.asList("#fee6ce", "#fdae6b", "#e6550d");
    public List<String> orange4 = Arrays.asList("#feedde", "#fdbe85", "#fd8d3c", "#d94701");
    public List<String> orange5 = Arrays.asList("#feedde", "#fdbe85", "#fd8d3c", "#e6550d", "#a63603");
    public List<String> orange6 = Arrays.asList("#feedde", "#fdd0a2", "#fdae6b", "#fd8d3c", "#e6550d", "#a63603");
    public List<String> orange7 = Arrays.asList("#feedde", "#fdd0a2", "#fdae6b", "#fd8d3c", "#f16913", "#d94801", "#8c2d04");
    public List<String> purple3 = Arrays.asList("#efedf5", "#bcbddc", "#756bb1");
    public List<String> purple4 = Arrays.asList("#f2f0f7", "#cbc9e2", "#9e9ac8", "#6a51a3");
    public List<String> purple5 = Arrays.asList("#f2f0f7", "#cbc9e2", "#9e9ac8", "#756bb1", "#54278f");
    public List<String> purple6 = Arrays.asList("#f2f0f7", "#dadaeb", "#bcbddc", "#9e9ac8", "#756bb1", "#54278f");
    public List<String> purple7 = Arrays.asList("#f2f0f7", "#dadaeb", "#bcbddc", "#9e9ac8", "#807dba", "#6a51a3", "#4a1486");

    public HashMap<String,HashMap> colorHM;
    public HashMap<Integer,List> orangeShadesByClassNumber = new HashMap<>();
    public HashMap<Integer,List> purpleShadesByClassNumber = new HashMap<>();

    public ChoroplethManager(){

        // Build the color look up.

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

        this.colorHM.put(Main.app.orangesName, this.orangeShadesByClassNumber);
        this.colorHM.put(Main.app.purplesName, this.purpleShadesByClassNumber);

    }

    public List<String> getColorSet(String hue, Integer shadesCount){
        // A way to get the list of colors by specifying which hue and how many shades wanted.
        HashMap<Integer,List> aColorHashMap = this.colorHM.get(hue);
        List someShades = aColorHashMap.get(shadesCount);
        return someShades;
    }

}
