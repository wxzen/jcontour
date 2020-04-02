package contour.IDW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import contour.bean.Tuple5;
import contour.draw.IDWImage;
import contour.utils.CsvParser;

/**
 * IDWTest
 */
public class IDWTest {


    @Test
    public void testZhangzhouCity() {
        double[] mapCenter = {117.661801, 24.510897};
        int zoom = 8;
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

        String filePath = "contour/zhangzhou/";
        
        double[][] bounds = {{left, bottom}, {right, top}};
        List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = getColors(filePath);
        double[][] rawdata = getData(filePath);
        IDWImage idwImage = new IDWImage(rawdata, colors, bounds, "E:/tmp/", filePath, crsParams);
        idwImage.draw();
        
    }


    public double[][] getData(String path){
        String dataPath = this.getClass().getClassLoader()
            .getResource(path+".csv").getPath();
        List<Map<String, String>> dataList = CsvParser.parse(dataPath);
        double[][] retList = new double[3][dataList.size()];
        for (int i=0; i<dataList.size(); i++) {
            Map<String, String> map = dataList.get(i);
			Double lon = Double.parseDouble(map.get("LON").trim());
			Double lat = Double.parseDouble(map.get("LAT").trim());
            Double value = Double.parseDouble(map.get("VALUE").trim());
            retList[0][i] = lon;
            retList[1][i] = lat;
            retList[2][i] = value;
        }
        return retList;
    }

    public List<Tuple5<Double, Double, Integer, Integer, Integer>> getColors(String path){
        String colorPath = this.getClass().getClassLoader().getResource(path+"color.csv").getPath();
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
    


}