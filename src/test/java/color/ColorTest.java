package color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import contour.bean.Tuple5;
import contour.utils.ColorUtils;
import contour.utils.InterpolateRgb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorTest {
    private Logger logger = LoggerFactory.getLogger("ColorTest");

    @Test
    public void test1() {
        System.out.println(Color.RED);
        String hexString = ColorUtils.colorToHexValue(Color.RED);
        System.out.println("16进制字符串:" + hexString);
        Color color = ColorUtils.covertHexColorToRGB(hexString);
        System.out
                .println("16进制字符串转为颜色的ARGB值:(" + String.valueOf(color.getAlpha()) + "," + String.valueOf(color.getRed())
                        + "," + String.valueOf(color.getGreen()) + "," + String.valueOf(color.getBlue()) + ")");
    }

    @Test
    public void test2() {
        String hexColor = "#00E400";
        Color color = ColorUtils.covertHexColorToRGB(hexColor.substring(1));
        System.out.println("rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")");
    }

    /**
     * 颜色插值测试
     */
    @Test
    public void test3() {
        String[] baseColors = { "#0021FF", "#00FCFE", "#07FD00", "#FFF40B", "#FF000E" };
        // System.out.println(covertHexColorToRGB(baseColors[0]));
        List<int[]> colorList = new ArrayList<>(100);
        InterpolateRgb interpolate = null;
        for (int i = 0; i < baseColors.length - 1; i++) {
            Color startColor = ColorUtils.covertHexColorToRGB(baseColors[i]);
            Color endColor = ColorUtils.covertHexColorToRGB(baseColors[i + 1]);
            interpolate = new InterpolateRgb(startColor, endColor);
            for (int j = 0; j <= 100; j++) {
                Color color = interpolate.get((float) j / 100);
                int[] rgbArr = new int[] { color.getRed(), color.getGreen(), color.getBlue() };
                colorList.add(rgbArr);
            }
        }

        System.out.println(colorList.get(colorList.size() - 1));

    }


    @Test
    public void test4() {
        // Color beginColor = ColorUtils.covertHexColorToRGB("#0021FF");
        // Color endColor = ColorUtils.covertHexColorToRGB("#00FCFE");
        Color sColor = new Color(0,228,0);
        Color eColor = new Color(255,255,9);
        List<Tuple5<Double, Double, Integer, Integer, Integer>> list = ColorUtils.buildInterpolationColors(sColor, eColor, new int[]{0, 50}, 10);
        logger.info("VALUE_MIN,VALUE_MAX,R,G,B");
        for(Tuple5<Double, Double, Integer, Integer, Integer> t : list){
            logger.info(t._1+","+t._2+"|"+t._3+","+t._4+","+t._5);
        }

    }

  
}