package contour.IDW;

import contour.bean.Tuple5;
import contour.utils.ColorUtils;
import contour.utils.CsvParser;
import contour.utils.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * IDWTest
 */
public class IDWTest {
    private Logger logger = LoggerFactory.getLogger("IDWTest");

    @Test
    public void testBuildContourImage() {
        int zoom = 9;
        String inputPath = "input/";
        String fileName = "data.csv";
        String outputPath = FileUtils.getDiskRootPath() + "/Tmp/";
        String borderDataPath = "input/border.csv";
        double[][] bounds = new double[][]{{116.760922, 23.391427}, {118.364926, 25.402349}};
        List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = buildAQIColors();
        logger.info("VALUE_MIN,VALUE_MAX,R,G,B");
        for(Tuple5<Double, Double, Integer, Integer, Integer> t : colors){
            logger.info(t._1+","+t._2+"|"+t._3+","+t._4+","+t._5);
        }
        double[][] rawdata = getData(inputPath, fileName);
        long time = System.currentTimeMillis();
        IDWImage idwImage = new IDWImage(rawdata, colors, bounds, outputPath + "idw_contour", borderDataPath, zoom);
        logger.info("Interpolation cost time: "+ (System.currentTimeMillis() - time));
        idwImage.setClipBounds(false);
        idwImage.draw();
    }

    private double[][] getData(String path, String fileName) {
        String dataPath = this.getClass().getClassLoader()
                .getResource(path+"/"+fileName).getPath();
        List<Map<String, String>> dataList = CsvParser.parse(dataPath);
        double[][] retList = new double[dataList.size()][3];
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, String> map = dataList.get(i);
            if("null".equals(map.get("VALUE"))) {
                continue;
            }
            Double lon = Double.parseDouble(map.get("LON").trim());
            Double lat = Double.parseDouble(map.get("LAT").trim());
            Double value = Double.parseDouble(map.get("VALUE").trim());
            retList[i][0] = lon;
            retList[i][1] = lat;
            retList[i][2] = value;
        }
        return retList;
    }

    public List<Tuple5<Double, Double, Integer, Integer, Integer>> buildAQIColors() {
        List<Tuple5<Double, Double, Integer, Integer, Integer>> retList = new ArrayList<>();
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(0,228,0), new Color(255,255,9), new int[]{0, 50}, 10));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,255,9), new Color(255,126,0), new int[]{50, 100}, 15));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,126,0), new Color(255,0,0), new int[]{100, 150}, 20));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,0,0), new Color(153,0,76), new int[]{150, 200}, 20));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(153,0,76), new Color(126,0,35), new int[]{200, 300}, 20));
        retList.add(new Tuple5<Double,Double,Integer,Integer,Integer>(300D, 3000D, 126, 0, 35));
        return retList;
    }


}