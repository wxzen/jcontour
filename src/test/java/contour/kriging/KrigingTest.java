package contour.kriging;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import contour.algorithm.Kriging;
import contour.bean.Tuple3;
import contour.bean.Tuple5;
import contour.utils.CsvParser;

/**
 * KrigingTest
 */
public class KrigingTest {
    //  sample: llon，llat,rlon,rlat,region
	protected List<Tuple5<Double, Double, Double, Double, String>> borders;
	//	sample: lon,lat,value
	protected List<Tuple3<Double, Double, Double>> datas;
	// sample: value_min,value_max,r,g,b
	protected List<Tuple5<Double, Double, Integer, Integer, Integer>> colors;
	//	sample: lon,lat,text
    protected List<Tuple3<Double, Double, String>> externs;
    
    protected double[] targetValues;
    protected double[] xList;
    protected double[] yList;

    private String RANGE_FLAG = "city/";
	private String NAME = "nanchong/";
	
    @Before
    public void before(){
        String dirName = "contour/"+RANGE_FLAG+NAME;
		String dataPath = this.getClass().getClassLoader().getResource(dirName+"data.csv").getPath();
		String colorPath = this.getClass().getClassLoader().getResource(dirName+"color.csv").getPath();
		String borderPath = this.getClass().getClassLoader().getResource(dirName+"border.csv").getPath();
		List<Map<String, String>> dataList = CsvParser.parse(dataPath);
		List<Map<String, String>> colorList = CsvParser.parse(colorPath);
        List<Map<String, String>> borderList = CsvParser.parse(borderPath);
        
        convertData(dataList);

		borders = borderToTuple(borderList);
		datas = dataToTuple(dataList);
		colors = colorToTuple(colorList);

		externs = new ArrayList<>();
		for (Tuple3<Double, Double, Double> t : datas) {
			externs.add(new Tuple3<>(t._1, t._2, t._3.toString()));
		}

    }



    @Test
    public void testNanchongCity() {
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

		System.out.println(grid.xWidth);


	}


	// private void borderDeal() {
	// 	for (Tuple5<Double, Double, Double, Double, String> border : borders) {
	// 		double llon = border._1;
	// 		double llat = border._2;
	// 		double rlon = border._3;
	// 		double rlat = border._4;
	// 		String region = border._5;
	// 		// 边界数据可能由多根线组成，不同的线采用  ";" 隔开
	// 		String[] borderLines = region.split(";");
	// 		for (String line : borderLines) {
	// 			// 每一条边界线由多个坐标点构成，不同的点以 "," 隔开
	// 			String[] spots = line.split(",");
	// 			List<PointD> spotPoints = new ArrayList<>();
	// 			for (String s : spots) {
	// 				PointD aPoint = new PointD();
	// 				String horizontal = s.split("\\s+")[0];
	// 				String vertical = s.split("\\s+")[1];
	// 				aPoint.X = Double.valueOf(horizontal);
	// 				aPoint.Y = Double.valueOf(vertical);
	// 				spotPoints.add(aPoint);
	// 			}
	// 			clipLines.add(spotPoints);
	// 		}
	// 		left = Math.min(llon, left);
	// 		right = Math.max(rlon, right);
	// 		top = Math.max(rlat, top);
	// 		bottom = Math.min(llat, bottom);
	// 	}
	// }

	// private void borderPolygon(Graphics2D g, List<List<PointD>> outLine,
	// 						   Color fillColor) {
	// 	for (List<PointD> polyLine : outLine) {
	// 		polygonLine(g, polyLine, fillColor, Color.BLACK, 5, 0, null);
	// 	}
	// }

	// private void polygonLine(Graphics2D g, List<PointD> outLine,
	// 						 Color fillColor, Color lineColor, int lineSize,
	// 						 int fontSize, String msg) {	
	// 	PointD point;
	// 	int len = outLine.size();
	// 	int[] xPoints = new int[len];
	// 	int[] yPoints = new int[len];
	// 	Point origin = CRSutil.toPoint(0, 0);
	// 	Point topLeftPixel = this.crsUtil.lngLatToPixelPoint(CRSutil.toLngLat(left, top));
	// 	Point offset = origin.clone().subtract(topLeftPixel);
	// 	for (int j = 0; j < len; j++) {
	// 		point = outLine.get(j);
	// 		Point p = crsUtil.lngLatToPixelPoint(CRSutil.toLngLat(point.X, point.Y));
	// 		p.add(offset);
	// 		xPoints[j] = (int)Math.round(p.x);
	// 		yPoints[j] = (int)Math.round(p.y);
	// 	}
	// 	java.awt.Polygon polygon = new java.awt.Polygon(xPoints, yPoints, len);
	// }

	// public void drawMap(String basicFile) throws IOException {
	// 	BufferedImage base = transparencyImage(Transparency.BITMASK);
	// 	Graphics2D g_base = base.createGraphics();
	// 	// 填充边界线
	// 	if (outLine != null && outLine.size() > 0) {
	// 		borderPolygon(g_base, outLine, Color.WHITE);
	// 	}
	// 	OutputStream tmpStream = new FileOutputStream(new File(basicFile + ".png"));
	// 	ImageIO.write(base, "png", tmpStream);
	// 	tmpStream.close();
	// 	g_base.dispose();
	// 	base.flush();
	// }

	/**
	 * 设置生成图片的透明模式
	 * @param width
	 * @param height
	 * @param transparency
	 * @return
	 */
	private BufferedImage transparencyImage(int width, int height, int transparency) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		// 设置画布透明模式
		bi = g2d.getDeviceConfiguration().createCompatibleImage(width, height, transparency);
		g2d.dispose();
		return bi;
	}

	

	public void clipMap(){
		double left =  105.220547;
		double right = 107.247525;
		double bottom = 30.239248;
		double top = 32.202684;
		double[] center = {06.082974, 30.795281};
		int zoom = 8;
		double width = 1536;
		double height = 731;

		// CRSutil crsUtil = new CRSutil(center, width, height, zoom);
		// Point southWestPixel = crsUtil.lngLatToPixelPoint(new LngLat(left, bottom));
		// Point northEastPixel = crsUtil.lngLatToPixelPoint(new LngLat(right, top));


	}





    private void convertData(List<Map<String, String>> dataList){
        int size = dataList.size();
        targetValues = new double[size];
        xList = new double[size];
        yList = new double[size];
        for(int i=0; i<size; i++){
            Map<String, String> dataMap = dataList.get(i);
            targetValues[i] = Double.valueOf(dataMap.get("VALUE"));
            xList[i] = Double.valueOf(dataMap.get("LON"));
            yList[i] = Double.valueOf(dataMap.get("LAT"));
        }
    }



    private List<Tuple3<Double, Double, Double>> dataToTuple(List<Map<String, String>> listMaps) {
		List<Tuple3<Double, Double, Double>> retList = new ArrayList<>();
		for (Map<String, String> map : listMaps) {
			Double lon = Double.parseDouble(map.get("LON").trim());
			Double lat = Double.parseDouble(map.get("LAT").trim());
			Double value = Double.parseDouble(map.get("VALUE").trim());
			retList.add(new Tuple3<>(lon, lat, value));
		}
		return retList;
	}

	private List<Tuple5<Double, Double, Double, Double, String>> borderToTuple(List<Map<String, String>> listMaps) {
		List<Tuple5<Double, Double, Double, Double, String>> retList = new ArrayList<>();
		for (Map<String, String> map : listMaps) {
			Double llon = Double.parseDouble(map.get("LLON").trim());
			Double llat = Double.parseDouble(map.get("LLAT").trim());
			Double rlon = Double.parseDouble(map.get("RLON").trim());
			Double rlat = Double.parseDouble(map.get("RLAT").trim());
			String region = map.get("REGION").trim();
			retList.add(new Tuple5(llon, llat, rlon, rlat, region));
		}
		return retList;
	}

	private List<Tuple5<Double, Double, Integer, Integer, Integer>> colorToTuple(List<Map<String, String>> listMaps) {
		List<Tuple5<Double, Double, Integer, Integer, Integer>> retList = new ArrayList<>();
		for (Map<String, String> map : listMaps) {
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