package contour.IDW;

import java.util.List;

import org.slf4j.LoggerFactory;

import contour.bean.Tuple5;
import contour.common.AbstractImage;

/**等值线图片工具类, 主要负责根据算法得到的数据进行等值线（面）的绘制工作
 * ContourImage
 * @author xuwei
 */
public class IDWImage extends AbstractImage{

	public IDWImage(double[][] rawdata, 
					List<Tuple5<Double, Double, Integer, Integer, Integer>> colors, 
					double[][] bounds, 
					String filePath,
					String mapDataPath,
					int zoom
					) {
				
		super(rawdata, colors, bounds, filePath, mapDataPath, zoom);

		this.logger = LoggerFactory.getLogger(IDWImage.class);

		//开始插值，生成等值面图片
		IDWutil idWutil = new IDWutil(rawdata, colorValues, left, right, top, bottom);
		this.contourPolygons = idWutil.interpolate();
	}
	
    
}