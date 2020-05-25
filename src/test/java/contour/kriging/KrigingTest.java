package contour.kriging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import contour.algorithm.Kriging;
import contour.bean.Tuple5;
import contour.utils.CsvParser;

/**
 * KrigingTest
 */
public class KrigingTest {

    @Test
    public void testZhangzhouCity() {
		String dirName = "contour/city/zhangzhou/";
		String timestamp = "2020-04-20-1600";
		String dataPath = this.getClass().getClassLoader().getResource(dirName+"data-"+timestamp+".csv").getPath();
		List<Map<String, String>> dataList = CsvParser.parse(dataPath);
		
		// List<Tuple3<Double, Double, Double>> datas = dataToTuple(dataList);
		// List<Tuple3<Double, Double, String>> externs = new ArrayList<>();
		// for (Tuple3<Double, Double, Double> t : datas) {
		// 	externs.add(new Tuple3<>(t._1, t._2, t._3.toString()));
		// }
		int size = dataList.size();
        double[] targetValues = new double[size];
        double[] xList = new double[size];
        double[] yList = new double[size];
        for(int i=0; i<size; i++){
            Map<String, String> dataMap = dataList.get(i);
            targetValues[i] = Double.valueOf(dataMap.get("VALUE"));
            xList[i] = Double.valueOf(dataMap.get("LON"));
            yList[i] = Double.valueOf(dataMap.get("LAT"));
        }

        Kriging kriging = new Kriging(Kriging.SPHERICAL_MODEL, 0, 100);
		kriging.train(targetValues, xList, yList);
		
		double left =  105.220547;
		double right = 107.247525;
		double bottom = 30.239248;
		double top = 32.202684;

		double[] bottomLeft = {left, bottom};
		double[] topLeft = {left, top};
		double[] topRight = {right, top};
		double[] bottomRight = {right, bottom};

		double[][][] polygons = {{bottomLeft, topLeft, topRight, bottomRight}};
		double xWidth = Math.abs(right - left) / 200;
		double yWidth = Math.abs(top - bottom) / 200;

		Kriging.Grid grid = kriging.grid(polygons, xWidth, yWidth);
		System.out.println(grid.A);
	}


	@Test
	public void testZhangzhouCity2(){
		double[] mapCenter = {117.661801, 24.510897};
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
        // String timestamp = "2020-04-07-1700";
        // String timestamp = "2020-04-21-0800";       
        // String timestamp = "2020-04-20-0500";       
        // String timestamp = "2020-04-20-1600";       
        // String timestamp = "2020-04-22-0700";       
        // String timestamp = "2020-04-22-0000";       
        // String timestamp = "2020-04-27-1900";       
        // String timestamp = "2020-05-06-1600";       
        String timestamp = "2020-05-25-0900";       
        double[][] bounds = {{left, bottom}, {right, top}};
        List<Tuple5<Double, Double, Integer, Integer, Integer>> colors = getColors(filePath);
        double[][] rawdata = getData(filePath, timestamp);
        KrigingImage krigingImage = new KrigingImage(rawdata, colors, bounds, "D:/tmp/zhangzhou-"+timestamp+"_k", filePath, crsParams);
        krigingImage.draw();
	}

	public double[][] getData(String path, String timestamp){
        String _path = "";
        if(!"".equals(timestamp) && timestamp!=null){
            _path = path+"data-"+timestamp+".csv";
        }else{
            _path = path+"data.csv";
        }
        System.out.println(_path);
        String dataPath = this.getClass().getClassLoader()
            .getResource(_path).getPath();
        List<Map<String, String>> dataList = CsvParser.parse(dataPath);
        double[][] retList = new double[dataList.size()][3];
        for (int i=0; i<dataList.size(); i++) {
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