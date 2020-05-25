package contour.IDW;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.xu.geo.CRSutil;
import com.xu.geo.LngLat;
import com.xu.geo.Point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import contour.bean.Tuple5;
import contour.draw.spatial.PointD;
import contour.draw.spatial.PolyLine;
import contour.draw.spatial.Polygon;
import contour.utils.MapUtils;

/**等值线图片工具类, 主要负责根据算法得到的数据进行等值线（面）的绘制工作
 * ContourImage
 * @author xuwei
 */
public class IDWImage {
	private Logger logger = LoggerFactory.getLogger(IDWImage.class);
	// 经纬度以 “点 ” 的形式展示控制
	private DrawStyle stationStyle = new DrawStyle(true, 10, Color.RED);
	// 等值线值 显示控制、大小控制
	private DrawStyle line_value_style = new DrawStyle(true, 40, Color.BLACK);
	// 等值线是否绘制 、样式控制
	private DrawStyle line_style = new DrawStyle(true, 1, Color.ORANGE);
	// 是否填充等值线
	private boolean fillContour = true;
	//是否显示点位值
	private boolean showPointValue = false;
	// 色标
	private Color[] colorArray;
	// 色标值
	private double[] colorValues;

	List<List<PointD>> outLine;

	private int width;

	private int height;

	private double top;

	private double left;

	private double bottom;

	private double right;

	private CRSutil crsUtil;

	private double[][] data;

	private List<Polygon> contourPolygons;
	private LinkedHashMap<Double, Color> colorMap;
	private String filePath;

	public IDWImage(double[][] rawdata, 
					List<Tuple5<Double, Double, Integer, Integer, Integer>> colors, 
					double[][] bounds, 
					String filePath,
					String mapDataPath,
					Map<String, Object> crsParams
					) {
		this.filePath = filePath;
		this.left = bounds[0][0];
		this.bottom = bounds[0][1];
		this.right = bounds[1][0];
		this.top = bounds[1][1];

		this.data = rawdata;

		this.outLine = MapUtils.readMapData(mapDataPath);

		//初始化投影坐标系统
		double[] mapCenter = (double[]) crsParams.get("mapCenter");
		double clientWidth = (double) crsParams.get("clientWidth");
		double clientHeight = (double) crsParams.get("clientHeight");
		int zoom = (int) crsParams.get("zoom");
		
		crsUtil = new CRSutil(mapCenter, clientWidth, clientHeight, zoom);
		Point southWestPixel = crsUtil.lngLatToPixelPoint(new LngLat(left, bottom));
		Point northEastPixel = crsUtil.lngLatToPixelPoint(new LngLat(right, top));
		this.width = (int) (northEastPixel.x - southWestPixel.x);
		this.height = (int) (southWestPixel.y - northEastPixel.y);

		colorDeal(colors);

		//开始插值，生成等值面图片
		IDWutil idWutil = new IDWutil(rawdata, colorValues, left, right, top, bottom);
		this.contourPolygons = idWutil.interpolate();
	}

	public void draw(){
		String tmpPath = this.filePath + "_tmp";
		try {
			logger.info("paint basic picture ...");
			drawBasic(tmpPath);
			logger.info("paint contour picture ...");
			drawContour(filePath, tmpPath);
			// logger.info("draw stations ...");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 处理色标数据
	private void colorDeal(List<Tuple5<Double, Double, Integer, Integer, Integer>> colors) {
		colorArray = new Color[colors.size()];
		colorValues = new double[colors.size()];
		int count = 0;
		for (Tuple5<Double, Double, Integer, Integer, Integer> color : colors) {
			double value_min = color._1;
			colorValues[count] = value_min;
			// colorArray[count] = new Color(color._3, color._4, color._5);
			colorArray[count] = new Color(color._3, color._4, color._5, 150);
			count++;
		}
		colorMap = new LinkedHashMap<>();
		for (int i = 0, len = colorValues.length; i < len; i++) {
			colorMap.put(colorValues[i], colorArray[i]);
		}
	}

    // 绘制底图
	public void drawBasic(String basicFile) throws IOException {
		BufferedImage base = transparencyImage(Transparency.BITMASK);
		Graphics2D g_base = base.createGraphics();
		// 填充边界线
		if (outLine != null && outLine.size() > 0) {
			borderPolygon(g_base, outLine, Color.WHITE);
		}
		OutputStream tmpStream = new FileOutputStream(new File(basicFile + ".png"));
		ImageIO.write(base, "png", tmpStream);
		tmpStream.close();
		g_base.dispose();
		base.flush();
    }

    public void drawContour(String realPath, String tmpPath) throws IOException {
		BufferedImage image = transparencyImage(Transparency.TRANSLUCENT);
		Graphics2D g2 = image.createGraphics();
		// 抗锯齿处理
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		AlphaComposite ac = AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER);
		g2.setComposite(ac);

		// 绘制等值面以及等值线
		if ((fillContour || line_style.show) && contourPolygons.size() > 0){
			drawPolygon(g2, contourPolygons);
		}

		if(stationStyle.show){
			drawStation(g2);
		}

		// 重新打开等值面区域图像
		File file = new File(tmpPath + ".png");
		// 图片装入内存
		BufferedImage src = ImageIO.read(file);
		ac = AlphaComposite.getInstance(AlphaComposite.DST_IN);
		g2.setComposite(ac);
		g2.drawImage(src, 0, 0, width, height, null);
		src.flush();
		src = null;
		// 删除临时文件
		file.delete();
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
		g2.setComposite(ac);
		// 释放对象
		g2.dispose();
		// 保存文件
		OutputStream out = new FileOutputStream(new File(realPath
				+ ".png"));
		ImageIO.write(image, "png", out);
		out.close();
		image.flush();
		logger.info("图片路径： " + realPath + ".png");
	}
    
    private BufferedImage transparencyImage(int transparency) {
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bi.createGraphics();
		// 设置画布透明模式
		bi = g2d.getDeviceConfiguration().createCompatibleImage(width, height, transparency);
		g2d.dispose();
		return bi;
    }
    
    private void borderPolygon(Graphics2D g, List<List<PointD>> outLine,
							   Color fillColor) {
		for (List<PointD> polyLine : outLine) {
			polygonLine(g, polyLine, fillColor, Color.BLACK, 5, 0, null);
		}
	}

    private void polygonLine(Graphics2D g, List<PointD> outLine,
							 Color fillColor, Color lineColor, int lineSize,
							 int fontSize, String msg) {	
		PointD point;
		int len = outLine.size();
		int[] xPoints = new int[len];
		int[] yPoints = new int[len];
		Point origin = CRSutil.toPoint(0, 0);
		Point topLeftPixel = this.crsUtil.lngLatToPixelPoint(CRSutil.toLngLat(left, top));
		Point offset = origin.clone().subtract(topLeftPixel);
		for (int j = 0; j < len; j++) {
			point = outLine.get(j);
			Point p = crsUtil.lngLatToPixelPoint(CRSutil.toLngLat(point.X, point.Y));
			p.add(offset);
			xPoints[j] = (int)Math.round(p.x);
			yPoints[j] = (int)Math.round(p.y);
		}
		java.awt.Polygon polygon = new java.awt.Polygon(xPoints, yPoints, len);
		
		// 绘制等值线
		if (lineColor != null) {
			BasicStroke bs = new BasicStroke(lineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			g.setStroke(bs);
			g.setColor(lineColor);
			g.drawPolygon(polygon);
			if (msg != null) {
				g.setColor(Color.black);
				g.setFont(new Font("微软雅黑", Font.BOLD, fontSize));
				g.drawString(msg, xPoints[0], yPoints[0]);
				g.drawString(msg, xPoints[len / 2], yPoints[len / 2]);
			}
		}

		// 填充等值面
		if (fillColor != null) {
			g.setColor(fillColor);
			// g.setColor(null);
			g.fillPolygon(polygon);
		}
	}

	private Color getSpecifyColor(double value){
		Color _color = colorMap.get(value);
		// if(_color==null){
		// 	for(int i=0, len=colorValues.length; i<len; i++){
		// 		if(colorValues[i]>value && i-1 >=0){
		// 			_color = colorArray[i-1];
		// 			break;
		// 		}
		// 	}
		// }
		return _color;
	}

    public void drawPolygon(Graphics2D g, List<Polygon> polygons) {
		Color lineColor = line_style.show ? line_style.color : null;
		int lineSize = line_style.size;
		int n = 0;
		for (Polygon polygon : polygons) {
			// if(n>5) break;
			Color fillColor = fillContour ? getSpecifyColor(polygon.LowValue) : null;
			lineColor = colorMap.get(polygon.LowValue);
			if (!polygon.IsHighCenter) {
				Color tmp = colorArray[0];
				for (Color c : colorArray) {
					if (c == fillColor) {
						fillColor = tmp;
						break;
					} else {
						tmp = c;
					}
				}
			}
			PolyLine line = polygon.OutLine;
			polygonLine(g, line.PointList, fillColor, lineColor, lineSize, line_value_style.size, null);
			n++;
		}
	}

	public void drawStation(Graphics2D g){
        if (data != null) {
			Point origin = CRSutil.toPoint(0, 0);
			Point topLeftPixel = this.crsUtil.lngLatToPixelPoint(CRSutil.toLngLat(left, top));
			Point offset = origin.clone().subtract(topLeftPixel);
			int len = data[0].length;
			for (int i = 0; i < len; i++) {
				Point p = crsUtil.lngLatToPixelPoint(CRSutil.toLngLat(data[0][i], data[1][i]));
				p.add(offset);
				g.setColor(stationStyle.color);
				g.fillOval((int)Math.round(p.x),  (int)Math.round(p.y), stationStyle.size, stationStyle.size);
				if(showPointValue){
					g.setColor(Color.black);
					g.setFont(new Font("微软雅黑", Font.PLAIN, 10));
					g.drawString(data[2][i]+"", (int)Math.round(p.x),  (int)Math.round(p.y));
				}
			}
		}
	}
	


    
    class DrawStyle {
        public boolean show;
        public int size;
        public Color color;
    
        public DrawStyle(boolean show, int size, Color color) {
            this.show = show;
            this.size = size;
            this.color = color;
        }
    }

    
}