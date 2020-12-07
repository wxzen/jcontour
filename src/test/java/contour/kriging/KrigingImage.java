package contour.kriging;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.LoggerFactory;

import contour.algorithm.Kriging;
import contour.bean.Tuple5;
import contour.common.AbstractImage;
import contour.draw.Contour;
import contour.draw.spatial.Border;
import contour.draw.spatial.PolyLine;
import contour.draw.spatial.Polygon;

/**等值线图片工具类, 主要负责根据算法得到的数据进行等值线（面）的绘制工作
 * ContourImage
 * @author xuwei
 */
public class KrigingImage extends AbstractImage {

	private static final int DEFAULT_ALGORITHM_ROWS = 200;
	private static final int DEFAULT_ALGORITHM_COLS = 200;
	private static final double DEFAULT_ALGORITHM_UNDEFINE = Double.MIN_VALUE;


	public KrigingImage(double[][] rawdata, 
					List<Tuple5<Double, Double, Integer, Integer, Integer>> colors, 
					double[][] bounds, 
					String filePath,
					String mapDataPath,
					int zoom
					) {
		super(rawdata, colors, bounds, filePath, mapDataPath, zoom);

		this.logger = LoggerFactory.getLogger(KrigingImage.class);

        //开始插值
        this.contourPolygons = this.interpolate();
    }
    

    private List<Polygon> interpolate(){
        logger.info("普通克里金开始插值...");
        int size = this.data.length;
        double[] targetValues = new double[size];
        double[] xList = new double[size];
        double[] yList = new double[size];

        for(int i=0; i<size; i++){
            xList[i] = this.data[i][0];
            yList[i] = this.data[i][1];
            targetValues[i] = this.data[i][2];
        }

        double[][][] polygons = {{{left, bottom}, {left, top}, {right, top}, {right, bottom}}};
		double xWidth = Math.abs(right - left) / DEFAULT_ALGORITHM_ROWS;
        double yWidth = Math.abs(top - bottom) / DEFAULT_ALGORITHM_COLS;

        Kriging kriging = new Kriging(Kriging.SPHERICAL_MODEL, 0, 100);
		kriging.train(targetValues, xList, yList);
        Kriging.Grid grid = kriging.grid(polygons, xWidth, yWidth);
        double[][] gridData = grid.A;

        int nc = colorValues.length;
        int[][] S1 = new int[gridData.length][gridData[0].length];
        
        double[] x = new double[DEFAULT_ALGORITHM_ROWS+1];
        double[] y = new double[DEFAULT_ALGORITHM_COLS+1];

        createGridXY_Num(this.left, this.bottom, this.right, this.top, x, y);

		List<Border> borders = Contour.tracingBorders(gridData, x, y, S1, DEFAULT_ALGORITHM_UNDEFINE);
		List<PolyLine> contourLines = Contour.tracingContourLines(gridData, x, y, nc,
				colorValues, DEFAULT_ALGORITHM_UNDEFINE, borders, S1);

		// 平滑处理
		contourLines = Contour.smoothLines(contourLines);

		List<Polygon> contourPolygons = Contour.tracingPolygons(gridData, contourLines,
				borders, colorValues);
		Collections.sort(contourPolygons, new Comparator<Polygon>() {
			@Override
			public int compare(Polygon o1, Polygon o2) {
				return Double.compare(o2.Area, o1.Area);
			}
        });

        return contourPolygons;
    }


    public static void createGridXY_Num(double Xlb, double Ylb, double Xrt, double Yrt,
        double[] X, double[] Y) {
        int i;
        double XDelt, YDelt;
        int Xnum = X.length;
        int Ynum = Y.length;
        XDelt = (Xrt - Xlb) / Xnum;
        YDelt = (Yrt - Ylb) / Ynum;
        for (i = 0; i < Xnum; i++) {
            X[i] = Xlb + i * XDelt;
        }
        for (i = 0; i < Ynum; i++) {
            Y[i] = Ylb + i * YDelt;
        }
    }



   

    
}