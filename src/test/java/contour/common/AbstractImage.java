package contour.common;

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

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import contour.bean.Tuple5;
import contour.draw.spatial.PointD;
import contour.draw.spatial.PolyLine;
import contour.draw.spatial.Polygon;
import contour.utils.MapUtils;
import contour.utils.SphericalMercator;

/**等值线图片工具类, 主要负责根据算法得到的数据进行等值线（面）的绘制工作
 * ContourImage
 * @author xuwei
 */
public abstract class AbstractImage {
	protected Logger logger = LoggerFactory.getLogger(AbstractImage.class);
	// 经纬度以 “点 ” 的形式展示控制
	private DrawStyle stationStyle = new DrawStyle(false, 10, Color.RED);
	// 等值线值 显示控制、大小控制
	private DrawStyle line_value_style = new DrawStyle(false, 40, Color.BLACK);
	// 等值线是否绘制 、样式控制
	private DrawStyle line_style = new DrawStyle(false, 1, Color.ORANGE);
	// 是否填充等值线
	private boolean fillContour = true;
	//是否显示点位值
	private boolean showPointValue = true;
	// 色标
	private Color[] colorArray;
	// 色标值
	protected double[] colorValues;
	//是否裁剪
	private boolean clipBounds = true;

	List<List<PointD>> outLine;

	private int width;

	private int height;

	protected double top;

	protected double left;

	protected double bottom;

	protected double right;

	private int zoom;

	private SphericalMercator mercator = new SphericalMercator();

	protected double[][] data;

	private String mapDataPath;

	protected List<Polygon> contourPolygons;
	private LinkedHashMap<Double, Color> colorMap;
	private String filePath;

	public AbstractImage(double[][] rawdata, 
					List<Tuple5<Double, Double, Integer, Integer, Integer>> colors, 
					double[][] bounds, 
					String filePath,
					String mapDataPath,
					int zoom
					) {
		this.filePath = filePath;
		this.left = bounds[0][0];
		this.bottom = bounds[0][1];
		this.right = bounds[1][0];
		this.top = bounds[1][1];

		this.data = rawdata;
		this.mapDataPath = mapDataPath;
		this.zoom = zoom;
		double[] southwestPixel = this.mercator.lngLatToPoint(new double[]{left, bottom}, zoom, false);
		double[] northEastPixel = this.mercator.lngLatToPoint(new double[]{right, top}, zoom, false);
		this.width = (int) (northEastPixel[0] - southwestPixel[0]);
		this.height = (int) (southwestPixel[1] - northEastPixel[1]);

		colorDeal(colors);

		//开始插值，生成等值面图片
		// IDWutil idWutil = new IDWutil(rawdata, colorValues, left, right, top, bottom);
		// this.contourPolygons = idWutil.interpolate();
	}

	public void draw(){
		if(this.clipBounds){
			this.outLine = MapUtils.readMapData(mapDataPath);
		}
		String tmpPath = this.filePath + "_tmp";
		try {
			logger.info("paint basic picture ...");
			drawBasic(tmpPath);
			logger.info("paint contour picture ...");
			if(this.clipBounds){
				drawContourByClip(filePath, tmpPath);
			}else{
				drawContour(filePath, tmpPath);
			}
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
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = image.createGraphics();
		// 抗锯齿处理
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
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
		g2.drawImage(src, 0, 0, width, height, null);
		src.flush();
		src = null;
		// 删除临时文件
		file.delete();
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

    public void drawContourByClip(String realPath, String tmpPath) throws IOException {
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
		double[] topLeftPixel = this.mercator.lngLatToPoint(new double[]{left, top}, this.zoom, false);
		for (int j = 0; j < len; j++) {
			point = outLine.get(j);
			double[] p = this.mercator.lngLatToPoint(new double[]{point.X, point.Y}, this.zoom, false);
			p[0] = p[0] - topLeftPixel[0];
			p[1] = p[1] - topLeftPixel[1];
			xPoints[j] = (int)Math.round(p[0]);
			yPoints[j] = (int)Math.round(p[1]);
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
		if(_color==null){
			for(int i=0, len=colorValues.length; i<len; i++){
				if(colorValues[i]>value && i-1 >=0){
					_color = colorArray[i-1];
					break;
				}
			}
		}
		return _color;
	}

    public void drawPolygon(Graphics2D g, List<Polygon> polygons) {
		Color lineColor = line_style.show ? line_style.color : null;
		int lineSize = line_style.size;
		int n = 0;
		for (Polygon polygon : polygons) {
			// if(n>5) break;
			Color fillColor = fillContour ? getSpecifyColor(polygon.LowValue) : null;
			// lineColor = colorMap.get(polygon.LowValue);
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

	public void drawStation(Graphics2D g) {
        if (data != null) {
			double[] topLeftPixel = this.mercator.lngLatToPoint(new double[]{left, top}, this.zoom, false);
			int len = data[0].length;
			for (int i = 0; i < len; i++) {
				double[] p = this.mercator.lngLatToPoint(new double[]{data[0][i], data[1][i]}, this.zoom, false);
				p[0] = p[0] - topLeftPixel[0];
				p[1] = p[1] - topLeftPixel[1];
				// g.setColor(stationStyle.color);
				// g.fillOval((int)Math.round(p.x),  (int)Math.round(p.y), stationStyle.size, stationStyle.size);
				if(showPointValue){
					g.setColor(Color.black);
					g.setFont(new Font("微软雅黑", Font.PLAIN, 10));
					g.drawString(data[i][2]+"", (int)Math.round(p[0]),  (int)Math.round(p[1]));
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


	public boolean isClipBounds() {
		return clipBounds;
	}

	public void setClipBounds(boolean clipBounds) {
		this.clipBounds = clipBounds;
	}

    
}