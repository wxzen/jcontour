package contour.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import contour.utils.MathsUtil;

/**
 * Kriging
 * 
 * @author xuwei
 */

public class Kriging {
    /**
     * 高斯模型
     */
    public final static String GAUSSIAN_MODEL = "gaussian";
    /**
     * 指数模型
     */
    public final static String EXPONENTIAL_MODEL = "exponential";
    /**
     * 球体模型
     */
    public final static String SPHERICAL_MODEL = "Spherical";

    public String model = EXPONENTIAL_MODEL;
    /**
     * 方差
     */
    public double sigma2 = 0;
    /**
     * 
     */
    public double alpha = 100;

    private double[] targetValues;

    private double[] xList;

    private double[] yList;

    private double variogram_nugget = 0;

    private double variogram_range = 0;

    private double variogram_sill = 0;

    private double variogram_A = (double) 1 / 3;

    private int variogram_n = 0;

    private double[] variogram_K;

    private double[] variogram_M;

    public Kriging(double nugget, double range, double sill, double A, int n) {
        this.variogram_nugget = nugget;
        this.variogram_range = range;
        this.variogram_sill = sill;
        this.variogram_A = A;
        this.variogram_n = n;
    }

    public Kriging(String model, double sigma2, double alpha) {
        this.model = model;
        this.sigma2 = sigma2;
        this.alpha = alpha;
    }

    private double variogram_gaussian(double h) {
        double nugget = this.variogram_nugget;
        double sill = this.variogram_sill;
        double range = this.variogram_range;
        double A = this.variogram_A;

        return nugget + ((sill - nugget) / range) * (1.0 - Math.exp(-(1.0 / A) * Math.pow(h / range, 2)));
    }

    private double variogram_exponential(double h) {
        double nugget = this.variogram_nugget;
        double sill = this.variogram_sill;
        double range = this.variogram_range;
        double A = this.variogram_A;

        return nugget + ((sill - nugget) / range) * (1.0 - Math.exp(-(1.0 / A) * (h / range)));
    }

    private double variogram_spherical(double h) {
        double nugget = this.variogram_nugget;
        double sill = this.variogram_sill;
        double range = this.variogram_range;

        if (h > range)
            return nugget + (sill - nugget) / range;

        return nugget + ((sill - nugget) / range) * (1.5 * (h / range) - 0.5 * Math.pow(h / range, 3));
    }

    /**
     * training sample data
     * 
     */
    public void train(double[] targetValues, double[] xList, double[] yList) {
        this.targetValues = targetValues;
        this.xList = xList;
        this.yList = yList;

        // lag distance/semivariance
        int size = this.targetValues.length;
        int distance_capacity = size * (size - 1) / 2;
        List<double[]> distances = new ArrayList<double[]>(distance_capacity);
        int i, j, k, l;
        for (i = 0, k = 0; i < size; i++) {
            for (j = 0; j < i; j++, k++) {
                double distance = Math.sqrt(Math.pow(xList[i] - xList[j], 2) + Math.pow(yList[i] - yList[j], 2));
                double detaValue = Math.abs(targetValues[i] - targetValues[j]);
                double[] arr = { distance, detaValue };
                distances.add(arr);
            }
        }
        Collections.sort(distances, new Comparator<double[]>() {
            @Override
            public int compare(double[] o1, double[] o2) {
                double delta = o1[0] - o2[0];
                if (delta < 0) {
                    return -1;
                } else if (delta > 0) {
                    return 1;
                }
                return 0;
            }
        });
        this.variogram_range = distances.get(distance_capacity - 1)[0];

        // Bin lag distance
        int lags = distance_capacity > 30 ? 30 : distance_capacity;
        double tolerance = (double) this.variogram_range / lags;
        double[] lag = new double[lags];
        double[] semi = new double[lags];
        if (lags < 30) {
            for (l = 0; l < lags; l++) {
                double[] distance = distances.get(l);
                lag[l] = distance[0];
                semi[l] = distance[1];
            }
        } else {
            for (i = 0, j = 0, k = 0, l = 0; i < lags && j < distance_capacity; i++, k = 0) {
                while (distances.get(j)[0] <= ((i + 1) * tolerance)) {
                    lag[l] += distances.get(j)[0];
                    semi[l] += distances.get(j)[1];
                    j++;
                    k++;
                    if (j >= distance_capacity)
                        break;
                }
                if (k > 0) {
                    lag[l] /= k;
                    semi[l] /= k;
                    l++;
                }
            }
            if (l < 2) {
                try {
                    throw new Exception("Not enough points.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }; 
        }

        // Feature transformation
        size = l;
        this.variogram_range = lag[size - 1] - lag[0];
        double[] X = new double[size*2];
        for(int n=0;n<size*2; n++){
            X[n] = 1;
        }
        double[] Y = new double[size];
        double A = this.variogram_A;
        double range = this.variogram_range;
        for(i = 0; i < size; i++){
            switch(model){
                case GAUSSIAN_MODEL:
                    X[i * 2 + 1] = 1.0 - Math.exp(-(1.0 / A) * Math.pow(lag[i] / range, 2));
                break;
                case EXPONENTIAL_MODEL:
                    X[i * 2 + 1] = 1.0 - Math.exp(-(1.0 / A) * lag[i] / range);
                break;
                case SPHERICAL_MODEL:
                    X[i * 2 + 1] = 1.5 * (lag[i] / range) - 0.5 * Math.pow(lag[i] / range, 3);
                break;
            }
            Y[i] = semi[i];
        }

        //Least squares
        double[] Xt = MathsUtil.kriging_matrix_transpose(X, size, 2);
        double[] Z = MathsUtil.kriging_matrix_multiply(Xt, X, 2, size, 2);
        Z = MathsUtil.kriging_matrix_add(Z, MathsUtil.kriging_matrix_diag(1/alpha, 2), 2, 2);
        double[] cloneZ = Z.clone();
        if(MathsUtil.kriging_matrix_chol(Z, 2)){
            MathsUtil.kriging_matrix_chol2inv(Z, 2);
        }else{
            MathsUtil.kriging_matrix_chol2inv(cloneZ, 2);
            Z = cloneZ;
        }
        double[] W = MathsUtil.kriging_matrix_multiply(MathsUtil.kriging_matrix_multiply(Z, Xt, 2, 2, size), Y, 2, size, 1);

        //Variogram parameters
        this.variogram_nugget = W[0];
        this.variogram_sill = W[1] * this.variogram_range + this.variogram_nugget;
        this.variogram_n = xList.length;
        
        //Gram matrix with prior
        double[] K = null;
        size = xList.length;
        if(GAUSSIAN_MODEL.equals(model)){
           K = build_gassian_gram_matrix_with_prior(xList, yList);
        }else if(EXPONENTIAL_MODEL.equals(model)){
           K = build_exponential_gram_matrix_with_prior(xList, yList);
        }else if(SPHERICAL_MODEL.equals(model)){
           K = build_spherical_gram_matrix_with_prior(xList, yList);
        }

        // Inverse penalized Gram matrix projected to target vector
        double[] C = MathsUtil.kriging_matrix_add(K, MathsUtil.kriging_matrix_diag(sigma2, size), size, size);
		double[] cloneC = C.clone();
		if (MathsUtil.kriging_matrix_chol(C, size))
            MathsUtil.kriging_matrix_chol2inv(C, size);
		else {
			MathsUtil.kriging_matrix_solve(cloneC, size);
			C = cloneC;
		}

		// Copy unprojected inverted matrix as K
		variogram_K = C.clone();
		variogram_M = MathsUtil.kriging_matrix_multiply(C, targetValues, size, size, 1);
    }

    public double predict(double x, double y) {
        double[] k = new double[variogram_n];
        if(GAUSSIAN_MODEL.equals(model)){
            for (int i = 0; i < variogram_n; i++){
                k[i] = variogram_gaussian(Math.sqrt(Math.pow(x - this.xList[i], 2) +
                        Math.pow(y - this.yList[i], 2)));
            }
        }else if(EXPONENTIAL_MODEL.equals(model)){
            for (int i = 0; i < variogram_n; i++){
                k[i] = variogram_exponential(Math.sqrt(Math.pow(x - this.xList[i], 2) +
                        Math.pow(y - this.yList[i], 2)));
            }
        }else if(SPHERICAL_MODEL.equals(model)){
            for (int i = 0; i < variogram_n; i++){
                k[i] = variogram_spherical(Math.sqrt(Math.pow(x - this.xList[i], 2) +
                         Math.pow(y - this.yList[i], 2)));
            }
        }
        
		return MathsUtil.kriging_matrix_multiply(k, variogram_M, 1, variogram_n, 1)[0];
    }

    public double variance(double x, double y) {
       double result = 0;
       switch(this.model){
           case GAUSSIAN_MODEL:
           result = variance_gassian(x, y);
           break;
           case EXPONENTIAL_MODEL:
           result = variance_exponential(x, y);
           break;
           case SPHERICAL_MODEL:
           result = variance_spherical(x, y);
           break;           
       }
       return result;
    }
    
    public static class Grid{
        public double[][] A;
        public double[] xlim;
        public double[] ylim;
        public double[] zlim;
        public double xWidth;
        public double yWidth;
    }

    public Grid grid(double[][][]polygons, double xWidth, double yWidth){
        int i, j, k, n=polygons.length;
        if(n==0) return null;

       	// Boundaries of polygons space
		double[] xlim = {polygons[0][0][0], polygons[0][0][0]};
		double[] ylim = {polygons[0][0][1], polygons[0][0][1]};
		for (i = 0; i < n; i++) // Polygons
			for (j = 0; j < polygons[i].length; j++) { // Vertices
				if (polygons[i][j][0] < xlim[0])
					xlim[0] = polygons[i][j][0];
				if (polygons[i][j][0] > xlim[1])
					xlim[1] = polygons[i][j][0];
				if (polygons[i][j][1] < ylim[0])
					ylim[0] = polygons[i][j][1];
				if (polygons[i][j][1] > ylim[1])
					ylim[1] = polygons[i][j][1];
			}

		// Alloc for O(n^2) space
		double xtarget, ytarget;
		int[] a = new int[2];
		int[] b = new int[2];
		double[] lxlim = new double[2]; // Local dimensions
		double[] lylim = new double[2]; // Local dimensions
		int x = (int)Math.ceil((xlim[1] - xlim[0]) / xWidth);
		int y = (int)Math.ceil((ylim[1] - ylim[0]) / yWidth);

		double[][] A = new double[x + 1][];
		for (i = 0; i <= x; i++) A[i] = new double[y + 1];
		for (i = 0; i < n; i++) {
			// Range for polygons[i]
			lxlim[0] = polygons[i][0][0];
			lxlim[1] = lxlim[0];
			lylim[0] = polygons[i][0][1];
			lylim[1] = lylim[0];
			for (j = 1; j < polygons[i].length; j++) { // Vertices
				if (polygons[i][j][0] < lxlim[0])
					lxlim[0] = polygons[i][j][0];
				if (polygons[i][j][0] > lxlim[1])
					lxlim[1] = polygons[i][j][0];
				if (polygons[i][j][1] < lylim[0])
					lylim[0] = polygons[i][j][1];
				if (polygons[i][j][1] > lylim[1])
					lylim[1] = polygons[i][j][1];
			}

			// Loop through polygon subspace
			a[0] = (int)Math.floor(((lxlim[0] - ((lxlim[0] - xlim[0]) % xWidth)) - xlim[0]) / xWidth);
			a[1] = (int)Math.ceil(((lxlim[1] - ((lxlim[1] - xlim[1]) % xWidth)) - xlim[0]) / xWidth);
			b[0] = (int)Math.floor(((lylim[0] - ((lylim[0] - ylim[0]) % yWidth)) - ylim[0]) / yWidth);
			b[1] = (int)Math.ceil(((lylim[1] - ((lylim[1] - ylim[1]) % yWidth)) - ylim[0]) / yWidth);
			for (j = a[0]; j <= a[1]; j++)
				for (k = b[0]; k <= b[1]; k++) {
					xtarget = xlim[0] + j * xWidth;
					ytarget = ylim[0] + k * yWidth;
					if (pip(polygons[i], xtarget, ytarget)) {
                        A[j][k] = predict(xtarget, ytarget);
                    }
				}
        }

        double maxValue = targetValues[0], minValue = targetValues[0];
        int len = targetValues.length;
        for(i=0; i < len; i++){
            if(targetValues[i] > maxValue) maxValue = targetValues[i];
            if(targetValues[i] < minValue) minValue = targetValues[i];

        }

        Grid grid = new Grid();
        grid.A = A;
		grid.xlim = xlim;
		grid.ylim = ylim;
		grid.zlim = new double[]{minValue, maxValue};
		grid.xWidth = xWidth;
        grid.yWidth = yWidth;

		return grid;
    }
    
    public double variance_gassian(double x, double y) {
        double[] k = new double[variogram_n];
        for (int i = 0; i < variogram_n; i++){
            k[i] = variogram_gaussian(Math.sqrt(Math.pow(x - this.xList[i], 2) +
                    Math.pow(y - this.yList[i], 2)));
        }
        
		return variogram_gaussian(0) +
			MathsUtil.kriging_matrix_multiply(MathsUtil.kriging_matrix_multiply(k, variogram_K,
					1, variogram_n, variogram_n),
				k, 1, variogram_n, 1)[0];
    }
    
    public double variance_exponential(double x, double y) {
        double[] k = new double[variogram_n];
        for (int i = 0; i < variogram_n; i++){
            k[i] = variogram_exponential(Math.sqrt(Math.pow(x - this.xList[i], 2) +
                    Math.pow(y - this.yList[i], 2)));
        }
        
		return variogram_exponential(0) +
			MathsUtil.kriging_matrix_multiply(MathsUtil.kriging_matrix_multiply(k, variogram_K,
					1, variogram_n, variogram_n),
				k, 1, variogram_n, 1)[0];
	}

    public double variance_spherical(double x, double y) {
        double[] k = new double[variogram_n];
        for (int i = 0; i < variogram_n; i++){
            k[i] = variogram_spherical(Math.sqrt(Math.pow(x - this.xList[i], 2) +
                    Math.pow(y - this.yList[i], 2)));
        }
        
		return variogram_spherical(0) +
			MathsUtil.kriging_matrix_multiply(MathsUtil.kriging_matrix_multiply(k, variogram_K,
					1, variogram_n, variogram_n),
				k, 1, variogram_n, 1)[0];
	}

    private double[] build_gassian_gram_matrix_with_prior(double[] xList, double[] yList){
        int n = xList.length;
        double[] K = new double[n * n];
        for(int i = 0; i < n; i++){
            for (int j = 0; j < i; j++) {
				K[i * n + j] = variogram_gaussian(Math.sqrt(Math.pow(xList[i] - xList[j], 2) +
						Math.pow(yList[i] - yList[j], 2)));
				K[j * n + i] = K[i * n + j];
			}
			K[i * n + i] = variogram_gaussian(0);
        }
        return K;
    }

    private double[] build_exponential_gram_matrix_with_prior(double[] xList, double[] yList){
        int n = xList.length;
        double[] K = new double[n * n];
        for(int i = 0; i < n; i++){
            for (int j = 0; j < i; j++) {
				K[i * n + j] = variogram_exponential(Math.sqrt(Math.pow(xList[i] - xList[j], 2) +
						Math.pow(yList[i] - yList[j], 2)));
				K[j * n + i] = K[i * n + j];
			}
			K[i * n + i] = variogram_exponential(0);
        }
        return K;
    }

    private double[] build_spherical_gram_matrix_with_prior(double[] xList, double[] yList){
        int n = xList.length;
        double[] K = new double[n * n];
        for(int i = 0; i < n; i++){
            for (int j = 0; j < i; j++) {
				K[i * n + j] = variogram_spherical(Math.sqrt(Math.pow(xList[i] - xList[j], 2) +
						Math.pow(yList[i] - yList[j], 2)));
				K[j * n + i] = K[i * n + j];
			}
			K[i * n + i] = variogram_spherical(0);
        }
        return K;
    }

    private boolean pip(double[][] polygons, double x, double y) {
        int i, j;
        boolean c = false;
        for (i = 0, j = polygons.length - 1; i < polygons.length; j = i++) {
            if (((polygons[i][1] > y) != (polygons[j][1] > y)) &&
                (x < (polygons[j][0] - polygons[i][0]) * (y - polygons[i][1]) / (polygons[j][1] - polygons[i][1]) + polygons[i][0])) {
                c = !c;
            }
        }
        return c;
    }

}