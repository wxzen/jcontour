package contour.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Web Mercator
 */
public class SphericalMercator {
    public final static int earthDiameter = 1275674;
    public final static double deg2rad = Math.PI / 180;
    public final static double rad2deg = 180 / Math.PI;
    public final static double quadPI = Math.PI / 4;
    public final static double half2PI = 1 / (2 * Math.PI);
    private Map<Integer, Double> scaleCache = new HashMap<>();


    public double getScale(int level) {
        if(!scaleCache.containsKey(level)){
            scaleCache.put(level, 256*Math.pow(2, level));
        }
        return scaleCache.get(level);
    }

    public double[] project(double[] lngLatArr) {
        double x = lngLatArr[0] * deg2rad,
        y = lngLatArr[1] * deg2rad;
        y = Math.log(Math.tan(quadPI + y / 2));
        return new double[]{x, y};
    }
    
    private double[] transform(double[] point, double scale) {
        double a = half2PI, b = 0.5, c = -a, d = 0.5;
        return new double[]{
            scale * (a * point[0] + b),
            scale * (c * point[1] + d)
        };
    }

    public double[] unproject(double[] point) {
        double lng = point[0] * rad2deg,
        lat = (2 * Math.atan(Math.exp(point[1])) - Math.PI/2)*rad2deg;
        return new double[] {
            Math.round(lng * 1000000) / 1000000d,
            Math.round(lat * 1000000) / 1000000d
        };
    }

    private double[] untransform(double[] point, double scale) {
        double a = half2PI,
        b = 0.5,
        c = -a,
        d = 0.5;
        return new double[] {(point[0] / scale-b) / a, (point[1] / scale-d) / c};
    }

    public double[] lngLatToPointByScale(double[] lngLatArr, double scale, boolean isRound) {
        double[] p = this.transform(this.project(lngLatArr), scale);
        if(isRound){
            p[0] = Math.round(p[0]);
            p[1] = Math.round(p[1]);
        }
        return p;
    }

    public double[] lngLatToPoint(double[] lngLatArr, int level, boolean isRound){
        return lngLatToPointByScale(lngLatArr, this.getScale(level), isRound);
    }

    public double[] pointToLngLat(double[] point, int level) {
        double[] untransformedPoint = this.untransform(point, getScale(level));
        return unproject(untransformedPoint);
    }

}
