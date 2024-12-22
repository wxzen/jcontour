package contour.utils;

import java.util.List;
import java.util.Map;

public class FileUtils {
   public static String getDiskRootPath() {
        String currentPath = System.getProperty("user.dir");
        return currentPath.substring(0, currentPath.indexOf("\\"));
    }

}
