/*
 * Created: 2020-08-08 09:50:28
 * Author : xuwei
 * Email : xuw@ahtsoft.com
 * -----
 * Description: 线性颜色插值类
 */

package contour.utils;

import java.awt.Color;

public class InterpolateRgb {
    private Color startRgb;
    private int deltR;
    private int deltG;
    private int deltB;
    private int deltA;

    /**
     * 
     * @param startRgb rgb颜色下限
     * @param endRgb   rgb颜色上限
     */
    public InterpolateRgb(Color startRgb, Color endRgb) {
        this.startRgb = startRgb;
        this.deltR = endRgb.getRed() - startRgb.getRed();
        this.deltG = endRgb.getGreen() - startRgb.getGreen();
        this.deltB = endRgb.getBlue() - startRgb.getBlue();
        this.deltA = endRgb.getAlpha() - startRgb.getAlpha();
    }

    private int linearR(float t) {
        return (int) (startRgb.getRed() + t * this.deltR);
    }

    private int linearG(float t) {
        return (int) (startRgb.getGreen() + t * this.deltG);
    }

    private int linearB(float t) {
        return (int) (startRgb.getBlue() + t * this.deltB);
    }

    private float linearA(float t) {
        return startRgb.getAlpha() + t * this.deltA;
    }

    /**
     * 
     * @param ratio 位于[0,1]区间
     * @return
     */
    public Color get(float ratio) {
        int r = linearR(ratio);
        int g = linearG(ratio);
        int b = linearB(ratio);
        if (deltA != 0) {
            float a = linearA(ratio);
            return new Color(r, g, b, a);
        } else {
            return new Color(r, g, b);
        }
    }

}