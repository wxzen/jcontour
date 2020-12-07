package contour.IDW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.awt.Color;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import contour.algorithm.IDW;
import contour.bean.Tuple5;
import contour.utils.ColorUtils;
import contour.utils.CsvParser;

/**
 * IDWTest
 */
public class IDWTest {
    private Logger logger = LoggerFactory.getLogger("IDWTest");

    public static void main(String[] args) {
        System.out.println("hello");
    }

    @Test
    public void testBuildContourImage() {
        int zoom = 16;
        double left = 117.59752;
        double right = 117.64294;
        double bottom = 24.527879;
        double top = 24.572112;

        String filePath = "contour/";
        String fileName = "testdata";
        double[][] bounds = { { left, bottom }, { right, top } };
        List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = buildTVOCColors();
        logger.info("VALUE_MIN,VALUE_MAX,R,G,B");
        for(Tuple5<Double, Double, Integer, Integer, Integer> t : colors){
            logger.info(t._1+","+t._2+"|"+t._3+","+t._4+","+t._5);
        }
        double[][] rawdata = getData(filePath, fileName);
        IDWImage idwImage = new IDWImage(rawdata, colors, bounds, "D:/Tmp/" + fileName, filePath, zoom);
        idwImage.setClipBounds(false);
        idwImage.draw();
    }

    @Test
    public void testCountry() {
        int zoom = 4;
        double left = 60.42;
        double right = 152.48;
        double bottom = 10.01;
        double top = 57.35;

        String filePath = "contour/";

        double[][] bounds = { { left, bottom }, { right, top } };
        // List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = getColors(filePath);
        List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = buildAQIColors();
        double[][] rawdata = getData(filePath, "");
        IDWImage idwImage = new IDWImage(rawdata, colors, bounds, "D:/tmp/country", filePath, zoom);
        // idwImage.setClipBounds(false);
        idwImage.draw();
    }

    public double[][] getData(String path, String timestamp) {
        String _path = "";
        if (!"".equals(timestamp) && timestamp != null) {
            _path = path + timestamp + ".csv";
        } else {
            _path = path + "data.csv";
        }
        System.out.println(_path);
        String dataPath = this.getClass().getClassLoader().getResource(_path).getPath();
        List<Map<String, String>> dataList = CsvParser.parse(dataPath);
        double[][] retList = new double[dataList.size()][3];
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, String> map = dataList.get(i);
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

    public List<Tuple5<Double, Double, Integer, Integer, Integer>> buildTVOCColors() {
        List<Tuple5<Double, Double, Integer, Integer, Integer>> retList = new ArrayList<>();
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(0,0,255), new Color(0,255,255), new int[]{0, 1000}, 100));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(0,255,255), new Color(0,255,0), new int[]{1000, 2000},100));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(0,255,0), new Color(255,255,0), new int[]{2000, 3000}, 100));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,255,0), new Color(255,0,0), new int[]{3000, 4000}, 100));
        retList.add(new Tuple5<Double,Double,Integer,Integer,Integer>(4000D, 5000D, 255, 0, 0));
        return retList;
    }
    // public List<Tuple5<Double, Double, Integer, Integer, Integer>> buildTVOCColors() {
    //     List<Tuple5<Double, Double, Integer, Integer, Integer>> retList = new ArrayList<>();
    //     retList.addAll(ColorUtils.buildInterpolationColors( new Color(0,228,0), new Color(255,255,0), new int[]{0, 100}, 10));
    //     retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,255,0), new Color(255,126,0), new int[]{100, 200}, 10));
    //     retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,126,0), new Color(255,0,0), new int[]{200, 300}, 10));
    //     retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,0,0), new Color(153,0,76), new int[]{300, 400}, 10));
    //     retList.addAll(ColorUtils.buildInterpolationColors( new Color(153,0,76), new Color(153,0,35), new int[]{400, 500}, 10));
    //     retList.add(new Tuple5<Double,Double,Integer,Integer,Integer>(500D, 600D, 153, 0, 35));
    //     return retList;
    // }

    @Test
    public void testSaveTestInterplateGridDataToJSON() {
        double left = 117.59752;
        double right = 117.64294;
        double bottom = 24.527879;
        double top = 24.572112;

        String filePath = "contour/";
        String fileName = "testdata";
        double[][] rawdata = getData(filePath, fileName);
        double[] x = new double[200];
        double[] y = new double[200];
        int neighborNumber = 30;
        IDW.createGridXY_Num(left, bottom, right, top, x, y);
        double[][] gridData = IDW.interpolation_IDW_Radius(rawdata, x, y, neighborNumber, 100, -9999.0);
        StringBuilder sb = new StringBuilder(40000);
        sb.append("[");
        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                sb.append(gridData[i][j] + ",");
            }
        }
        sb.replace(sb.length() - 1, sb.length(), "]");
        writeDataToDisk(sb.toString(), "D:/Tmp/" + fileName + "_200x200.json");

    }

    private void writeDataToDisk(String data, String filePath) {
        PrintStream ps = null;
        try {
            File file = new File(filePath);
            ps = new PrintStream(new FileOutputStream(file));
            ps.println(data);
            // ps.println(PakoGzipUtils.compress(data));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ps.close();
        }
    }

}