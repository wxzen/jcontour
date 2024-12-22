package contour.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import contour.draw.spatial.PointD;

/**
 * 读取地图数据
 */
public class MapUtils {

    public static List<List<PointD>> readMapData(String filePath){
        List<List<PointD>> _clipLines = null;

        try {
            String borderPath = MapUtils.class.getClassLoader().getResource(filePath).getPath();
            List<Map<String, String>> borderList = CsvParser.parse(borderPath);
            _clipLines = parseMapData(borderList);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return _clipLines;
    }

    /* 解析地图数据
     * @param list
     * @return
     * @throws Exception
     */
    private static List<List<PointD>> parseMapData(List<Map<String, String>> list) throws Exception {
        List<List<PointD>> clipLines = new ArrayList<>();
        for(int i=0, size=list.size(); i<size; i++){
            Map<String, String> dataMap = list.get(i);
            String[] spots = dataMap.get("REGION").split(",");
            List<PointD> spotPoints = new ArrayList<>();
            for(String s: spots){
                PointD aPoint = new PointD();
                String horizontal = s.split("\\s+")[0];
                String vertical = s.split("\\s+")[1];
                aPoint.X = Double.valueOf(horizontal);
                aPoint.Y = Double.valueOf(vertical);
                spotPoints.add(aPoint);
            }
            clipLines.add(spotPoints);
        }
        return clipLines;
    }

}
