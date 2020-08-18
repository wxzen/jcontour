package color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import contour.utils.InterpolateRgb;

public class ColorTest {

    @Test
    public void test1() {
        String hexString = colorToHexValue(Color.RED);
        System.out.println("16进制字符串:" + hexString);
        Color color = covertHexColorToRGB(hexString);
        System.out
                .println("16进制字符串转为颜色的ARGB值:(" + String.valueOf(color.getAlpha()) + "," + String.valueOf(color.getRed())
                        + "," + String.valueOf(color.getGreen()) + "," + String.valueOf(color.getBlue()) + ")");
    }

    @Test
    public void test2() {
        String hexColor = "#00E400";
        Color color = covertHexColorToRGB(hexColor.substring(1));
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
            Color startColor = covertHexColorToRGB(baseColors[i]);
            Color endColor = covertHexColorToRGB(baseColors[i + 1]);
            interpolate = new InterpolateRgb(startColor, endColor);
            for (int j = 0; j <= 100; j++) {
                Color color = interpolate.get((float) j / 100);
                int[] rgbArr = new int[] { color.getRed(), color.getGreen(), color.getBlue() };
                colorList.add(rgbArr);
            }
        }

        System.out.println(colorList.get(colorList.size() - 1));

    }

    private static String colorToHexValue(Color color) {
        return intToHexValue(color.getAlpha()) + intToHexValue(color.getRed()) + intToHexValue(color.getGreen())
                + intToHexValue(color.getBlue());
    }

    private static String intToHexValue(int number) {
        String result = Integer.toHexString(number & 0xff);
        while (result.length() < 2) {
            result = "0" + result;
        }
        return result.toUpperCase();
    }

    private Color covertHexColorToRGB(String hexColor) {
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
}