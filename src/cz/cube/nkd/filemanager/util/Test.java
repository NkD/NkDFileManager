package cz.cube.nkd.filemanager.util;

import android.util.DisplayMetrics;

public class Test {

    private Test() {
        // utility class
    }

    public static String getDisplayMetricInfo(DisplayMetrics dm) {
        StringBuilder sb = new StringBuilder("DisplayMetric:\n");
        sb.append("  density = ").append(dm.density).append("\n");
        sb.append("  densityDpi = ").append(dm.densityDpi).append("\n");
        sb.append("  scaledDensity = ").append(dm.scaledDensity).append("\n");
        sb.append("  widthPixels = ").append(dm.widthPixels).append(", heightPixels = ").append(dm.heightPixels).append("\n");
        sb.append("  xdpi = ").append(dm.xdpi).append(", ydpi = ").append(dm.ydpi).append("\n");
        return sb.toString();
    }

}
