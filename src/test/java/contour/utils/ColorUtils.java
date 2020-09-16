/*
 * Created: 2020-09-16 08:41:34
 * Author : xuwei
 * Email : wei.d.xu@outlook.com
 * -----
 * Description: 颜色工具类
 */

package contour.utils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import contour.bean.Tuple5;


public final class ColorUtils {

    /**
     * Convert Java.awt.Color to hex value
     * @param color
     * @return
     */
    public static String colorToHexValue(Color color) {
        return intToHexValue(color.getAlpha()) + intToHexValue(color.getRed()) + intToHexValue(color.getGreen())
                + intToHexValue(color.getBlue());
    }

    public static String intToHexValue(int number) {
        String result = Integer.toHexString(number & 0xff);
        while (result.length() < 2) {
            result = "0" + result;
        }
        return result.toUpperCase();
    }

    /**
     * Convert hex value to Java.awt.Color RGB Object
     * @param hexColor
     * @return
     */
    public static Color covertHexColorToRGB(String hexColor) {
        String str = hexColor;
        if ('#' == hexColor.charAt(0)) {
            str = hexColor.substring(1);
        }
        if (str.length() == 8) {
            int alpha = Integer.parseInt(str.substring(0, 2), 16);
            int red = Integer.parseInt(str.substring(2, 4), 16);
            int green = Integer.parseInt(str.substring(4, 6), 16);
            int blue = Integer.parseInt(str.substring(6, 8), 16);
            return new Color(red, green, blue, alpha);
        } else {
            int red = Integer.parseInt(str.substring(0, 2), 16);
            int green = Integer.parseInt(str.substring(2, 4), 16);
            int blue = Integer.parseInt(str.substring(4, 6), 16);
            return new Color(red, green, blue);
        }
    }


    public static List<Tuple5<Double, Double, Integer, Integer, Integer>> buildInterpolationColors(Color startColor, 
            Color endColor, int[] threshold, int interval) {
        InterpolateRgb interpolate = new InterpolateRgb(startColor, endColor);
        int min = threshold[0];
        int max = threshold[1];
        List<Tuple5<Double, Double, Integer, Integer, Integer>> retList = new ArrayList<>();
        int delta = max -min;
        for(int i=0; i<delta; i+=interval) {
            Color color = interpolate.get((float)i/delta);
            Double value_min = (double) (min+i);
            Double value_max = (double) (i+interval);
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            retList.add(new Tuple5<Double,Double,Integer,Integer,Integer>(value_min, value_max, r, g, b));
        }
        return retList;        
    }

}
