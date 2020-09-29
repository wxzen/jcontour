/*
 * Created: 2020-09-17 13:59:09
 * Author : xuwei
 * Email : wei.d.xu@outlook.com
 * -----
 * Description: 双线性插值算法
 */

package contour.algorithm;

public class BilinearInterpolate {


    public static double bilinearInterpolateScalar(double x, double y, double f00, double f10, double f01, double f11) {
        double rx = 1 - x;
        double ry = 1 - y;
        return f00 * rx * ry + f10 * x * ry + f01 * rx * y + f11 * x * y;        
    }


    public static double[] bilinearInterpolateVector(double x, double y, double[] f00, double[] f10, double[] f01, double[] f11) {
        double rx = 1 - x;
        double ry = 1 - y;
        double a = rx * ry;
        double b = x * ry;
        double c = rx * y;
        double d = x * y;
        double u = f00[0] * a + f10[0] * b + f01[0] * c + f11[0] * d;
        double v = f00[1] * a + f10[1] * b + f01[1] * c + f11[1] * d;
        return new double[] {u, v, Math.sqrt(u * u + v * v)};
    }

    
}
