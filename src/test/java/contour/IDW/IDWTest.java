package contour.IDW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import contour.algorithm.IDW;
import contour.bean.Tuple5;
import contour.draw.Contour;
import contour.draw.spatial.Border;
import contour.draw.spatial.PointD;
import contour.draw.spatial.PolyLine;
import contour.draw.spatial.Polygon;
import contour.utils.ColorUtils;
import contour.utils.CsvParser;
import contour.utils.PakoGzipUtils;

/**
 * IDWTest
 */
public class IDWTest {
    private Logger logger = LoggerFactory.getLogger("ColorTest");

    public static void main(String[] args) {
        System.out.println("hello");
    }

    @Test
    public void testZhangzhouCity() {
        double[] mapCenter = { 117.661801, 24.510897 };
        int zoom = 10;
        double clientWidth = 1536d;
        double clientHeight = 731d;

        Map<String, Object> crsParams = new HashMap<>();
        crsParams.put("mapCenter", mapCenter);
        crsParams.put("zoom", zoom);
        crsParams.put("clientWidth", clientWidth);
        crsParams.put("clientHeight", clientHeight);
        crsParams.put("zoom", zoom);

        double left = 116.760922;
        double right = 118.364926;
        double bottom = 23.391427;
        double top = 25.402349;

        String filePath = "contour/city/zhangzhou/";
        // String timestamp = "2020-08-11-1000";
        // String timestamp = "2020-08-27-1200";
        String timestamp = "2020-09-16-2200-tvoc";
        double[][] bounds = { { left, bottom }, { right, top } };
        // List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = getColors(filePath);
        // List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = buildAQIColors();
        List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = buildTVOCColors();

        logger.info("VALUE_MIN,VALUE_MAX,R,G,B");
        for(Tuple5<Double, Double, Integer, Integer, Integer> t : colors){
            logger.info(t._1+","+t._2+"|"+t._3+","+t._4+","+t._5);
        }

        
        double[][] rawdata = getData(filePath, timestamp);
        IDWImage idwImage = new IDWImage(rawdata, colors, bounds, "D:/Tmp/" + timestamp, filePath, crsParams);
        idwImage.draw();
    }

    @Test
    public void testNanchongCity() {
        double[] mapCenter = { 117.661801, 24.510897 };
        int zoom = 10;
        double clientWidth = 1536d;
        double clientHeight = 731d;

        Map<String, Object> crsParams = new HashMap<>();
        crsParams.put("mapCenter", mapCenter);
        crsParams.put("zoom", zoom);
        crsParams.put("clientWidth", clientWidth);
        crsParams.put("clientHeight", clientHeight);
        crsParams.put("zoom", zoom);

        double left = 105.220547;
        double right = 107.247525;
        double bottom = 30.239248;
        double top = 32.202684;

        String filePath = "contour/city/nanchong/";
        String timestamp = "2020-09-02-0800";
        // String timestamp = "data";
        double[][] bounds = { { left, bottom }, { right, top } };
        List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = getColors(filePath);
        double[][] rawdata = getData(filePath, timestamp);
        IDWImage idwImage = new IDWImage(rawdata, colors, bounds, "D:/Tmp/" + timestamp, filePath, crsParams);
        idwImage.draw();
    }



    @Test
    public void testCountry() {
        double[] mapCenter = { 108.07031303644182, 33.882330753596406 };
        int zoom = 4;
        double clientWidth = 1536d;
        double clientHeight = 731d;

        Map<String, Object> crsParams = new HashMap<>();
        crsParams.put("mapCenter", mapCenter);
        crsParams.put("zoom", zoom);
        crsParams.put("clientWidth", clientWidth);
        crsParams.put("clientHeight", clientHeight);
        crsParams.put("zoom", zoom);

        double left = 60.42;
        double right = 152.48;
        double bottom = 10.01;
        double top = 57.35;

        String filePath = "contour/country/";

        double[][] bounds = { { left, bottom }, { right, top } };
        // List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = getColors(filePath);
        List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = buildAQIColors();
        double[][] rawdata = getData(filePath, "");
        IDWImage idwImage = new IDWImage(rawdata, colors, bounds, "D:/tmp/country", filePath, crsParams);
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

    public List<Tuple5<Double, Double, Integer, Integer, Integer>> getColors(String path) {
        String colorPath = this.getClass().getClassLoader().getResource(path + "color.csv").getPath();
        List<Map<String, String>> colorList = CsvParser.parse(colorPath);
        List<Tuple5<Double, Double, Integer, Integer, Integer>> retList = new ArrayList<>();
        for (Map<String, String> map : colorList) {
            Double value_min = Double.parseDouble(map.get("VALUE_MIN").trim());
            Double value_max = Double.parseDouble(map.get("VALUE_MAX").trim());
            int r = Integer.parseInt(map.get("R").trim());
            int g = Integer.parseInt(map.get("G").trim());
            int b = Integer.parseInt(map.get("B").trim());
            retList.add(new Tuple5(value_min, value_max, r, g, b));
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
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(0,228,0), new Color(255,255,0), new int[]{0, 100}, 10));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,255,0), new Color(255,126,0), new int[]{100, 200}, 10));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,126,0), new Color(255,0,0), new int[]{200, 300}, 10));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(255,0,0), new Color(153,0,76), new int[]{300, 400}, 10));
        retList.addAll(ColorUtils.buildInterpolationColors( new Color(153,0,76), new Color(153,0,35), new int[]{400, 500}, 10));
        retList.add(new Tuple5<Double,Double,Integer,Integer,Integer>(500D, 600D, 153, 0, 35));
        return retList;
    }


    @Test
    public void testSaveInterplateGridDataToJSON() {
        double[] mapCenter = { 117.661801, 24.510897 };
        int zoom = 10;
        double clientWidth = 1536d;
        double clientHeight = 731d;

        Map<String, Object> crsParams = new HashMap<>();
        crsParams.put("mapCenter", mapCenter);
        crsParams.put("zoom", zoom);
        crsParams.put("clientWidth", clientWidth);
        crsParams.put("clientHeight", clientHeight);
        crsParams.put("zoom", zoom);

        double left = 116.760922;
        double right = 118.364926;
        double bottom = 23.391427;
        double top = 25.402349;

        String filePath = "contour/city/zhangzhou/";
        // String timestamp = "2020-08-03-0700";
        // String timestamp = "2020-08-03-2300";
        String timestamp = "2020-08-11-1100";
        double[][] rawdata = getData(filePath, timestamp);
        double[] x = new double[200];
        double[] y = new double[200];
        int neighborNumber = 30;
        IDW.createGridXY_Num(left, bottom, right, top, x, y);
        double[][] gridData = IDW.interpolation_IDW_Radius(rawdata, x, y, neighborNumber, 100, -9999.0);
        // System.out.println(gridData);
        StringBuilder sb = new StringBuilder(40000);
        sb.append("[");
        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                sb.append(gridData[i][j] + ",");
            }
        }
        sb.replace(sb.length() - 1, sb.length(), "]");
        // System.out.println(sb.toString());
        writeDataToDisk(sb.toString(), "D:/Tmp/" + timestamp + "_200x200.json");

    }

    @Test
    public void testSaveNanchongInterplateGridDataToJSON() {
        double[] mapCenter = { 117.661801, 24.510897 };
        int zoom = 10;
        double clientWidth = 1536d;
        double clientHeight = 731d;

        Map<String, Object> crsParams = new HashMap<>();
        crsParams.put("mapCenter", mapCenter);
        crsParams.put("zoom", zoom);
        crsParams.put("clientWidth", clientWidth);
        crsParams.put("clientHeight", clientHeight);
        crsParams.put("zoom", zoom);

        double left = 105.220547;
        double right = 107.247525;
        double bottom = 30.239248;
        double top = 32.202684;

        String filePath = "contour/city/nanchong/";
        String timestamp = "2020-09-02-0800";
        double[][] rawdata = getData(filePath, timestamp);
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
        // System.out.println(sb.toString());
        writeDataToDisk(sb.toString(), "D:/Tmp/" + timestamp + "_200x200.json");

    }

    @Test
    public void testSaveContourPolygonsToJSON() {
        double[] mapCenter = { 117.661801, 24.510897 };
        int zoom = 10;
        double clientWidth = 1536d;
        double clientHeight = 731d;

        Map<String, Object> crsParams = new HashMap<>();
        crsParams.put("mapCenter", mapCenter);
        crsParams.put("zoom", zoom);
        crsParams.put("clientWidth", clientWidth);
        crsParams.put("clientHeight", clientHeight);
        crsParams.put("zoom", zoom);

        double left = 116.760922;
        double right = 118.364926;
        double bottom = 23.391427;
        double top = 25.402349;

        String filePath = "contour/city/zhangzhou/";
        String timestamp = "2020-05-25-0900";
        double[][] rawdata = getData(filePath, timestamp);
        double[] x = new double[200];
        double[] y = new double[200];
        int neighborNumber = 30;
        IDW.createGridXY_Num(left, bottom, right, top, x, y);
        double[][] gridData = IDW.interpolation_IDW_Radius(rawdata, x, y, neighborNumber, 100, -9999.0);

        int[][] S1 = new int[gridData.length][gridData[0].length];

        double[] colorValues = new double[31];
        for (int i = 0; i < 31; i++) {
            colorValues[i] = i * 10;
        }
        List<Border> borders = Contour.tracingBorders(gridData, x, y, S1, -9999.0);
        List<PolyLine> contourLines = Contour.tracingContourLines(gridData, x, y, 30, colorValues, -9999.0, borders,
                S1);

        // 平滑处理
        contourLines = Contour.smoothLines(contourLines);

        List<Polygon> contourPolygons = Contour.tracingPolygons(gridData, contourLines, borders, colorValues);
        Collections.sort(contourPolygons, new Comparator<Polygon>() {
            @Override
            public int compare(Polygon o1, Polygon o2) {
                return Double.compare(o2.Area, o1.Area);
            }
        });

        StringBuilder sb = new StringBuilder(40000);
        sb.append("[");
        // int count = 0;
        for (Polygon polygon : contourPolygons) {
            // if(count==2) break;
            sb.append("{");
            sb.append("\"area\":");
            sb.append(polygon.Area);
            sb.append(",\"value\":");
            sb.append(polygon.LowValue);
            sb.append(",\"isHighCenter\":");
            sb.append(polygon.IsHighCenter);
            sb.append(",\"lnglats\":[");
            for (PointD point : polygon.OutLine.PointList) {
                sb.append("[");
                sb.append(point.X);
                sb.append(",");
                sb.append(point.Y);
                sb.append("],");
            }
            sb.replace(sb.length() - 1, sb.length(), "]},");
            // count++;
        }
        sb.replace(sb.length() - 1, sb.length(), "]");
        writeDataToDisk(sb.toString(), "D:/Tmp/contour_polygons.json");

    }

    @Test
    public void testSaveContourPolygonsToCsv() {
        double[] mapCenter = { 117.661801, 24.510897 };
        int zoom = 10;
        double clientWidth = 1536d;
        double clientHeight = 731d;

        Map<String, Object> crsParams = new HashMap<>();
        crsParams.put("mapCenter", mapCenter);
        crsParams.put("zoom", zoom);
        crsParams.put("clientWidth", clientWidth);
        crsParams.put("clientHeight", clientHeight);
        crsParams.put("zoom", zoom);

        double left = 116.760922;
        double right = 118.364926;
        double bottom = 23.391427;
        double top = 25.402349;

        String filePath = "contour/city/zhangzhou/";
        String timestamp = "2020-05-25-0900";
        double[][] rawdata = getData(filePath, timestamp);
        double[] x = new double[200];
        double[] y = new double[200];
        int neighborNumber = 30;
        IDW.createGridXY_Num(left, bottom, right, top, x, y);
        double[][] gridData = IDW.interpolation_IDW_Radius(rawdata, x, y, neighborNumber, 100, -9999.0);

        int[][] S1 = new int[gridData.length][gridData[0].length];

        double[] colorValues = new double[31];
        for (int i = 0; i < 31; i++) {
            colorValues[i] = i * 10;
        }
        List<Border> borders = Contour.tracingBorders(gridData, x, y, S1, -9999.0);
        List<PolyLine> contourLines = Contour.tracingContourLines(gridData, x, y, 30, colorValues, -9999.0, borders,
                S1);

        // 平滑处理
        contourLines = Contour.smoothLines(contourLines);

        List<Polygon> contourPolygons = Contour.tracingPolygons(gridData, contourLines, borders, colorValues);
        Collections.sort(contourPolygons, new Comparator<Polygon>() {
            @Override
            public int compare(Polygon o1, Polygon o2) {
                return Double.compare(o2.Area, o1.Area);
            }
        });

        StringBuilder sb = new StringBuilder(40000);
        sb.append("val,lnglats\n");
        for (Polygon polygon : contourPolygons) {
            sb.append(polygon.LowValue).append(",[");
            for (PointD point : polygon.OutLine.PointList) {
                sb.append(point.X);
                sb.append(",");
                sb.append(point.Y);
                sb.append(" ");
            }
            sb.replace(sb.length() - 1, sb.length(), "");
            sb.append("]\n");
        }
        writeDataToDisk(sb.toString(), "D:/Tmp/contour_polygons.csv");
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