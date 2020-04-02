package contour.CRSutils;

import com.xu.geo.CRSutil;
import com.xu.geo.EPSG3857;
import com.xu.geo.LngLat;
import com.xu.geo.Point;

import org.junit.Test;

/**
 * CRSTest
 */
public class CRSTest {


    @Test
    public void test1(){
       double left = 73.502355;
	   double right = 135.09567;
	   double bottom = 3.83703;
	   double top = 53.563624;

        CRSutil crsUtil = new CRSutil(new double[] {116.368324, 39.915085}, 1536, 731, 4);
        Point p = crsUtil.lngLatToPixelPoint(CRSutil.toLngLat(116.368324, 39.915085));
        System.out.println(p);

        Point southWestPixel = crsUtil.lngLatToPixelPoint(new LngLat(left, bottom));
        Point northEastPixel = crsUtil.lngLatToPixelPoint(new LngLat(right, top));

        System.out.println("---southWestPixel---");
        System.out.println(southWestPixel);
        System.out.println("---northEastPixel---");
        System.out.println(northEastPixel);




    }


    @Test
    public void test2(){
        System.out.println(EPSG3857.getInstance());

    }

    
}