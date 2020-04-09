/*
 * Copyright 2012 Yaqiang Wang,
 * yaqiang.wang@gmail.com
 */
package contour.algorithm;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import contour.algorithm.KDTree.Euclidean;

/**
 * Interpolate class - including the functions of interpolation
 *
 * @author Yaqiang Wang
 * @version $Revision: 1.6 $
 */
public class Cressman {

    // <editor-fold desc="IDW">
    /**
     * Create grid x/y coordinate arrays with x/y delt
     *
     * @param Xlb x of left-bottom
     * @param Ylb y of left-bottom
     * @param Xrt x of right-top
     * @param Yrt y of right-top
     * @param XDelt x delt
     * @param YDelt y delt
     * @return X/Y coordinate arrays
     */
    public static List<double[]> createGridXY_Delt(double Xlb, double Ylb, double Xrt, double Yrt, double XDelt, double YDelt) {
        int i, Xnum, Ynum;
        Xnum = (int) ((Xrt - Xlb) / XDelt + 1);
        Ynum = (int) ((Yrt - Ylb) / YDelt + 1);
        double[] X = new double[Xnum];
        double[] Y = new double[Ynum];
        for (i = 0; i < Xnum; i++) {
            X[i] = Xlb + i * XDelt;
        }
        for (i = 0; i < Ynum; i++) {
            Y[i] = Ylb + i * YDelt;
        }

        List<double[]> values = new ArrayList<>();
        values.add(X);
        values.add(Y);
        return values;
    }

    /**
     * Create grid X/Y coordinate
     *
     * @param Xlb X left bottom
     * @param Ylb Y left bottom
     * @param Xrt X right top
     * @param Yrt Y right top
     * @param X X coordinate
     * @param Y Y coordinate
     */
    public static void createGridXY_Num(double Xlb, double Ylb, double Xrt, double Yrt,
            double[] X, double[] Y) {
        int i;
        double XDelt, YDelt;
        int Xnum = X.length;
        int Ynum = Y.length;
        XDelt = (Xrt - Xlb) / Xnum;
        YDelt = (Yrt - Ylb) / Ynum;
        for (i = 0; i < Xnum; i++) {
            X[i] = Xlb + i * XDelt;
        }
        for (i = 0; i < Ynum; i++) {
            Y[i] = Ylb + i * YDelt;
        }
    }

    /**
     * Interpolate from grid data
     *
     * @param GridData input grid data
     * @param X input x coordinates
     * @param Y input y coordinates
     * @param unDefData undefine data
     * @param nX output x coordinate
     * @param nY output y coordinate
     * @return output grid data
     */
    public static double[][] interpolation_Grid(double[][] GridData, double[] X, double[] Y, double unDefData,
            double[] nX, double[] nY) {
        int nxNum = X.length * 2 - 1;
        int nyNum = Y.length * 2 - 1;
        nX = new double[nxNum];
        nY = new double[nyNum];
        double[][] nGridData = new double[nyNum][nxNum];
        int i, j;
        double a, b, c, d;
        List<Double> dList;
        for (i = 0; i < nxNum; i++) {
            if (i % 2 == 0) {
                nX[i] = X[i / 2];
            } else {
                nX[i] = (X[(i - 1) / 2] + X[(i - 1) / 2 + 1]) / 2;
            }
        }
        for (i = 0; i < nyNum; i++) {
            if (i % 2 == 0) {
                nY[i] = Y[i / 2];
            } else {
                nY[i] = (Y[(i - 1) / 2] + Y[(i - 1) / 2 + 1]) / 2;
            }
            for (j = 0; j < nxNum; j++) {
                if (i % 2 == 0 && j % 2 == 0) {
                    nGridData[i][j] = GridData[i / 2][j / 2];
                } else if (i % 2 == 0 && j % 2 != 0) {
                    a = GridData[i / 2][(j - 1) / 2];
                    b = GridData[i / 2][(j - 1) / 2 + 1];
                    dList = new ArrayList<>();
                    if (a != unDefData) {
                        dList.add(a);
                    }
                    if (b != unDefData) {
                        dList.add(b);
                    }

                    if (dList.isEmpty()) {
                        nGridData[i][j] = unDefData;
                    } else if (dList.size() == 1) {
                        nGridData[i][j] = dList.get(0);
                    } else {
                        nGridData[i][j] = (a + b) / 2;
                    }
                } else if (i % 2 != 0 && j % 2 == 0) {
                    a = GridData[(i - 1) / 2][j / 2];
                    b = GridData[(i - 1) / 2 + 1][j / 2];
                    dList = new ArrayList<>();
                    if (a != unDefData) {
                        dList.add(a);
                    }
                    if (b != unDefData) {
                        dList.add(b);
                    }

                    if (dList.isEmpty()) {
                        nGridData[i][j] = unDefData;
                    } else if (dList.size() == 1) {
                        nGridData[i][j] = dList.get(0);
                    } else {
                        nGridData[i][j] = (a + b) / 2;
                    }
                } else {
                    a = GridData[(i - 1) / 2][(j - 1) / 2];
                    b = GridData[(i - 1) / 2][(j - 1) / 2 + 1];
                    c = GridData[(i - 1) / 2 + 1][(j - 1) / 2 + 1];
                    d = GridData[(i - 1) / 2 + 1][(j - 1) / 2];
                    dList = new ArrayList<>();
                    if (a != unDefData) {
                        dList.add(a);
                    }
                    if (b != unDefData) {
                        dList.add(b);
                    }
                    if (c != unDefData) {
                        dList.add(c);
                    }
                    if (d != unDefData) {
                        dList.add(d);
                    }

                    if (dList.isEmpty()) {
                        nGridData[i][j] = unDefData;
                    } else if (dList.size() == 1) {
                        nGridData[i][j] = dList.get(0);
                    } else {
                        double aSum = 0;
                        for (double dd : dList) {
                            aSum += dd;
                        }
                        nGridData[i][j] = aSum / dList.size();
                    }
                }
            }
        }

        return nGridData;
    }

    // </editor-fold>
    // <editor-fold desc="Cressman">
    /**
     * Cressman analysis
     *
     * @param stationData station data array - x,y,value
     * @param X x array
     * @param Y y array
     * @param unDefData undefine data
     * @return grid data
     */
    public static double[][] cressman(double[][] stationData, double[] X, double[] Y, double unDefData) {
        List<Double> radList = new ArrayList<>();
        radList.add(10.0);
        radList.add(7.0);
        radList.add(4.0);
        radList.add(2.0);
        radList.add(1.0);

        return cressman(stationData, X, Y, unDefData, radList);
    }

    /**
     * Cressman analysis
     *
     * @param stData station data array - x,y,value
     * @param X x array
     * @param Y y array
     * @param unDefData undefine data
     * @param radList radii list
     * @return result grid data
     */
    public static double[][] cressman(double[][] stData, double[] X, double[] Y, double unDefData, List<Double> radList) {
        int xNum = X.length;
        int yNum = Y.length;
        int pNum = stData.length;
        double[][] gridData = new double[yNum][xNum];
        int irad = radList.size();
        int i, j;

        //Loop through each stn report and convert stn lat/lon to grid coordinates
        double xMin = X[0];
        double xMax = X[X.length - 1];
        double yMin = Y[0];
        double yMax = Y[Y.length - 1];
        double xDelt = X[1] - X[0];
        double yDelt = Y[1] - Y[0];
        double x, y;
        double sum = 0, total = 0;
        int stNum = 0;
        double[][] stationData = new double[pNum][3];
        for (i = 0; i < pNum; i++) {
            x = stData[i][0];
            y = stData[i][1];
            stationData[i][0] = (x - xMin) / xDelt;
            stationData[i][1] = (y - yMin) / yDelt;
            stationData[i][2] = stData[i][2];
            if (stationData[i][2] != unDefData) {
                total += stationData[i][2];
                stNum += 1;
            }
        }
        total = total / stNum;

        //Initial the arrays
        double HITOP = -999900000000000000000.0;
        double HIBOT = 999900000000000000000.0;
        double[][] TOP = new double[yNum][xNum];
        double[][] BOT = new double[yNum][xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                TOP[i][j] = HITOP;
                BOT[i][j] = HIBOT;
            }
        }

        //Initial grid values are average of station reports within the first radius
        Double rad;
        if (radList.size() > 0) {
            rad = radList.get(0);
        } else {
            rad = 4.0;
        }
        for (i = 0; i < yNum; i++) {
            y = (double) i;
            yMin = y - rad;
            yMax = y + rad;
            for (j = 0; j < xNum; j++) {
                x = (double) j;
                xMin = x - rad;
                xMax = x + rad;
                stNum = 0;
                sum = 0;
                for (int s = 0; s < pNum; s++) {
                    double val = stationData[s][2];
                    double sx = stationData[s][0];
                    double sy = stationData[s][1];
                    if (sx < 0 || sx >= xNum - 1 || sy < 0 || sy >= yNum - 1) {
                        continue;
                    }

                    if (val == unDefData || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                        continue;
                    }

                    double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                    if (dis > rad) {
                        continue;
                    }

                    sum += val;
                    stNum += 1;
                    if (TOP[i][j] < val) {
                        TOP[i][j] = val;
                    }
                    if (BOT[i][j] > val) {
                        BOT[i][j] = val;
                    }
                }
                if (stNum == 0) {
                    gridData[i][j] = unDefData;
                } else {
                    gridData[i][j] = sum / stNum;
                }
            }
        }

        //Perform the objective analysis
        for (int p = 0; p < irad; p++) {
            rad = radList.get(p);
            for (i = 0; i < yNum; i++) {
                y = (double) i;
                yMin = y - rad;
                yMax = y + rad;
                for (j = 0; j < xNum; j++) {
                    if (gridData[i][j] == unDefData) {
                        continue;
                    }

                    x = (double) j;
                    xMin = x - rad;
                    xMax = x + rad;
                    sum = 0;
                    double wSum = 0;
                    for (int s = 0; s < pNum; s++) {
                        double val = stationData[s][2];
                        double sx = stationData[s][0];
                        double sy = stationData[s][1];
                        if (sx < 0 || sx >= xNum - 1 || sy < 0 || sy >= yNum - 1) {
                            continue;
                        }

                        if (val == unDefData || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                            continue;
                        }

                        double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                        if (dis > rad) {
                            continue;
                        }

                        int i1 = (int) sy;
                        int j1 = (int) sx;
                        int i2 = i1 + 1;
                        int j2 = j1 + 1;
                        double a = gridData[i1][j1];
                        double b = gridData[i1][j2];
                        double c = gridData[i2][j1];
                        double d = gridData[i2][j2];
                        List<Double> dList = new ArrayList<>();
                        if (a != unDefData) {
                            dList.add(a);
                        }
                        if (b != unDefData) {
                            dList.add(b);
                        }
                        if (c != unDefData) {
                            dList.add(c);
                        }
                        if (d != unDefData) {
                            dList.add(d);
                        }

                        double calVal;
                        if (dList.isEmpty()) {
                            continue;
                        } else if (dList.size() == 1) {
                            calVal = dList.get(0);
                        } else if (dList.size() <= 3) {
                            double aSum = 0;
                            for (double dd : dList) {
                                aSum += dd;
                            }
                            calVal = aSum / dList.size();
                        } else {
                            double x1val = a + (c - a) * (sy - i1);
                            double x2val = b + (d - b) * (sy - i1);
                            calVal = x1val + (x2val - x1val) * (sx - j1);
                        }
                        double eVal = val - calVal;
                        double w = (rad * rad - dis * dis) / (rad * rad + dis * dis);
                        sum += eVal * w;
                        wSum += w;
                    }
                    if (wSum < 0.000001) {
                        gridData[i][j] = unDefData;
                    } else {
                        double aData = gridData[i][j] + sum / wSum;
                        gridData[i][j] = Math.max(BOT[i][j], Math.min(TOP[i][j], aData));
                    }
                }
            }
        }

        //Return
        return gridData;
    }

    /**
     * Cressman analysis - KDTree
     *
     * @param stationData station data array - x,y,value
     * @param X x array
     * @param Y y array
     * @param unDefData undefine data
     * @return grid data
     */
    public static double[][] cressman_kdTree(double[][] stationData, double[] X, double[] Y, double unDefData) {
        List<Double> radList = new ArrayList<>();
        radList.add(10.0);
        radList.add(7.0);
        radList.add(4.0);
        radList.add(2.0);
        radList.add(1.0);

        return cressman_kdTree(stationData, X, Y, unDefData, radList);
    }

    /**
     * Cressman analysis - KDTree
     *
     * @param stData station data array - x,y,value
     * @param X x array
     * @param Y y array
     * @param unDefData undefine data
     * @param radList radii list
     * @return result grid data
     */
    public static double[][] cressman_kdTree(double[][] stData, double[] X, double[] Y, double unDefData, List<Double> radList) {
        int xNum = X.length;
        int yNum = Y.length;
        int pNum = stData.length;
        
        double[][] gridData = new double[yNum][xNum];
        int irad = radList.size();
        int i, j;

        //Loop through each stn report and convert stn lat/lon to grid coordinates
        double xMin = X[0];
        double xMax = X[X.length - 1];
        double yMin = Y[0];
        double yMax = Y[Y.length - 1];
        double xDelt = X[1] - X[0];
        double yDelt = Y[1] - Y[0];
        double x, y;
        double sum = 0, total = 0;
        int stNum = 0;
        double[][] stationData = new double[pNum][3];
        for (i = 0; i < pNum; i++) {
            x = stData[i][0];
            y = stData[i][1];
            stationData[i][0] = (x - xMin) / xDelt;
            stationData[i][1] = (y - yMin) / yDelt;
            stationData[i][2] = stData[i][2];
            if (stationData[i][2] != unDefData) {
                total += stationData[i][2];
                stNum += 1;
            }
        }
        total = total / stNum;

        Euclidean<double[]> kdTree = new Euclidean<>(2);
        for(i = 0; i < pNum; i++){
        	kdTree.addPoint(new double[]{stationData[i][0], stationData[i][1]}, stationData[i]);
        }

        //Initial the arrays
        double HITOP = -999900000000000000000.0;
        double HIBOT = 999900000000000000000.0;
        double[][] TOP = new double[yNum][xNum];
        double[][] BOT = new double[yNum][xNum];
        for (i = 0; i < yNum; i++) {
            for (j = 0; j < xNum; j++) {
                TOP[i][j] = HITOP;
                BOT[i][j] = HIBOT;
            }
        }

        //Initial grid values are average of station reports within the first radius
        double rad;
        if (radList.size() > 0) {
            rad = radList.get(0);
        } else {
            rad = 4;
        }
        for (i = 0; i < yNum; i++) {
            y = (double) i;
            yMin = y - rad;
            yMax = y + rad;
            for (j = 0; j < xNum; j++) {
                x = (double) j;
                xMin = x - rad;
                xMax = x + rad;
                stNum = 0;
                sum = 0;                
                ArrayList<double[]> neighbours = kdTree.ballSearch(new double[]{x,y}, rad*rad);
                for (double[] station: neighbours) {
                    double val = station[2];
                    double sx = station[0];
                    double sy = station[1];
                    if (sx < 0 || sx >= xNum - 1 || sy < 0 || sy >= yNum - 1) {
                        continue;
                    }

                    if (val == unDefData || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                        continue;
                    }

                    double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                    if (dis > rad) {
                        continue;
                    }

                    sum += val;
                    stNum += 1;
                    if (TOP[i][j] < val) {
                        TOP[i][j] = val;
                    }
                    if (BOT[i][j] > val) {
                        BOT[i][j] = val;
                    }
                }
                
                if (stNum == 0) {
                    gridData[i][j] = unDefData;
                    //gridData[i, j] = total;
                } else {
                    gridData[i][j] = sum / stNum;
                }
            }
        }

        //Perform the objective analysis
        for (int p = 0; p < irad; p++) {
            rad = radList.get(p);
            for (i = 0; i < yNum; i++) {
                y = (double) i;
                yMin = y - rad;
                yMax = y + rad;
                for (j = 0; j < xNum; j++) {
                    if (gridData[i][j] == unDefData) {
                        continue;
                    }

                    x = (double) j;
                    xMin = x - rad;
                    xMax = x + rad;
                    sum = 0;
                    double wSum = 0;
                    ArrayList<double[]> neighbours = kdTree.ballSearch(new double[]{x,y}, rad*rad);
                    for(double[] station: neighbours){
                    	double val = station[2];
                        double sx = station[0];
                        double sy = station[1];
                        if (sx < 0 || sx >= xNum - 1 || sy < 0 || sy >= yNum - 1) {
                            continue;
                        }

                        if (val == unDefData || sx < xMin || sx > xMax || sy < yMin || sy > yMax) {
                            continue;
                        }

                        double dis = Math.sqrt(Math.pow(sx - x, 2) + Math.pow(sy - y, 2));
                        if (dis > rad) {
                            continue;
                        }

                        int i1 = (int) sy;
                        int j1 = (int) sx;
                        int i2 = i1 + 1;
                        int j2 = j1 + 1;
                        double a = gridData[i1][j1];
                        double b = gridData[i1][j2];
                        double c = gridData[i2][j1];
                        double d = gridData[i2][j2];
                        List<Double> dList = new ArrayList<>();
                        if (a != unDefData) {
                            dList.add(a);
                        }
                        if (b != unDefData) {
                            dList.add(b);
                        }
                        if (c != unDefData) {
                            dList.add(c);
                        }
                        if (d != unDefData) {
                            dList.add(d);
                        }

                        double calVal;
                        if (dList.isEmpty()) {
                            continue;
                        } else if (dList.size() == 1) {
                            calVal = dList.get(0);
                        } else if (dList.size() <= 3) {
                            double aSum = 0;
                            for (double dd : dList) {
                                aSum += dd;
                            }
                            calVal = aSum / dList.size();
                        } else {
                            double x1val = a + (c - a) * (sy - i1);
                            double x2val = b + (d - b) * (sy - i1);
                            calVal = x1val + (x2val - x1val) * (sx - j1);
                        }
                        double eVal = val - calVal;
                        double w = (rad * rad - dis * dis) / (rad * rad + dis * dis);
                        sum += eVal * w;
                        wSum += w;
                    }
                    
                    if (wSum < 0.000001) {
                        gridData[i][j] = unDefData;
                    } else {
                        double aData = gridData[i][j] + sum / wSum;
                        gridData[i][j] = Math.max(BOT[i][j], Math.min(TOP[i][j], aData));
                    }
                }
            }
        }

        //Return
        return gridData;
    }

    // </editor-fold>
    // <editor-fold desc="Others">
    /**
     * Assign point value to grid value
     *
     * @param SCoords point value array
     * @param X x coordinate
     * @param Y y coordinate
     * @param unDefData undefine value
     * @return grid data
     */
    public static double[][] assignPointToGrid(double[][] SCoords, double[] X, double[] Y,
            double unDefData) {
        int rowNum, colNum, pNum;
        colNum = X.length;
        rowNum = Y.length;
        pNum = SCoords.length;
        double[][] GCoords = new double[rowNum][colNum];
        double dX = X[1] - X[0];
        double dY = Y[1] - Y[0];
        int[][] pNums = new int[rowNum][colNum];

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                pNums[i][j] = 0;
                GCoords[i][j] = 0.0;
            }
        }

        for (int p = 0; p < pNum; p++) {
            if (doubleEquals(SCoords[p][2], unDefData)) {
                continue;
            }

            double x = SCoords[p][0];
            double y = SCoords[p][1];
            if (x < X[0] || x > X[colNum - 1]) {
                continue;
            }
            if (y < Y[0] || y > Y[rowNum - 1]) {
                continue;
            }

            int j = (int) ((x - X[0]) / dX);
            int i = (int) ((y - Y[0]) / dY);
            pNums[i][j] += 1;
            GCoords[i][j] += SCoords[p][2];
        }

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if (pNums[i][j] == 0) {
                    GCoords[i][j] = unDefData;
                } else {
                    GCoords[i][j] = GCoords[i][j] / pNums[i][j];
                }
            }
        }

        return GCoords;
    }

    private static boolean doubleEquals(double a, double b) {
        //if (Math.Abs(a - b) < 0.000001)
        if (Math.abs(a / b - 1) < 0.00000000001) {
            return true;
        } else {
            return false;
        }
    }
    // </editor-fold>
}
