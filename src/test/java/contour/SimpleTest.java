package contour;

import org.junit.Test;

/**
 * SimpleTest
 */
public class SimpleTest {

    @Test
    public void test1(){
        // double[] arr = {1,2,3};
        // double[] cloneArr = arr.clone();
        // cloneArr[1] = 8;
        // System.out.println(arr[1]);
        // System.out.println(cloneArr[1]);
        double distance = Math.sqrt(Math.pow(106.056-106.0789, 2)+Math.pow(30.8064 - 30.8023, 2));
        System.out.println(distance);

    }
}