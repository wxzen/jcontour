package graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * GraphicsTest
 */
public class GraphicsTest {

    
    public void test1() {
        int width = 200, height = 250;
        //创建图片对象
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        //基于图片对象打开绘图
        Graphics2D graphics = image.createGraphics();
        //绘图逻辑 START （基于业务逻辑进行绘图处理）……

        //绘制圆形
        graphics.setColor(Color.BLACK);
        // Ellipse2D.Double ellipse = new Ellipse2D.Double(20, 20, 100, 100);
        // graphics.draw(ellipse);

        // 绘图逻辑 END
        //处理绘图
        graphics.dispose();
        //将绘制好的图片写入到图片
        // ImageIO.write(image, "png", new File("abc.png"));w
        
    }


}