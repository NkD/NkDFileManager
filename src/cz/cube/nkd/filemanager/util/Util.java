package cz.cube.nkd.filemanager.util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.TypedValue;

public class Util {

    private static final String LOG_TAG = "NkD";

    private static final String[] units = new String[] { " B", " KB", " MB", " GB", " TB" };
    private static final double log10For1024 = Math.log10(1024);
    private static final DecimalFormat decimalFormat = new DecimalFormat("##0.#");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    private static final ThreadLocal<StringBuilder> threadLocalStringBuilder = new ThreadLocal<StringBuilder>();

    private Util() {
        // utility class
    }

    public static final void logI(Object message) {
        android.util.Log.i(LOG_TAG, Thread.currentThread().getName() + " : " + message.toString());
    }

    public static final void logW(Object message) {
        android.util.Log.w(LOG_TAG, Thread.currentThread().getName() + " : " + message.toString());
    }

    public static final void logE(Object message) {
        android.util.Log.e(LOG_TAG, Thread.currentThread().getName() + " : " + message.toString());
    }

    public static String bytesToDynamicSize(Long size) {
        if (size == null)
            return "";
        if (size <= 0)
            return "0 B";
        int digitGroups = (int) (Math.log10(size) / log10For1024);
        return getStringBuilder().append(decimalFormat.format(size / Math.pow(1024, digitGroups))).append(units[digitGroups]).toString();
    }

    public static String convertDate(Long time) {
        if (time == null)
            return "";
        return dateFormat.format(new Date(time));
    }
    
    public static String convertException(Throwable throwable){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    public static StringBuilder getStringBuilder() {
        StringBuilder sb = threadLocalStringBuilder.get();
        if (sb == null) {
            sb = new StringBuilder();
            threadLocalStringBuilder.set(sb);
        }
        sb.setLength(0);
        return sb;
    }

    public static long computeDirectorySize(File directory) {
        long result = 0;
        Stack<File> dirlist = new Stack<File>();
        if (directory.canRead()) {
            dirlist.push(directory);
            while (!dirlist.isEmpty()) {
                File dirCurrent = dirlist.pop();
                File[] fileList = dirCurrent.listFiles();
                for (int i = 0; i < fileList.length; i++) {
                    if (fileList[i].canRead()) {
                        if (fileList[i].isDirectory())
                            dirlist.push(fileList[i]);
                        else
                            result += fileList[i].length();
                    }
                }
            }
        }
        return result;
    }

    public static int dip2pixel(Context context, float n) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
        return value;
    }
    
    public static Bitmap createThumbnail(Bitmap source, int width, int height, int rotation){
        float ratioW = ((float) source.getWidth()) / source.getHeight();
        float ratioH = ((float) source.getHeight()) / source.getWidth();
        float scaleWidth = ((float) width) / source.getWidth();
        float scaleHeight = ((float) height) / source.getHeight();
        if (scaleWidth > scaleHeight) {
            int nw = (int) (height * ratioW) + 1;
            if ((nw % 2) != 0) nw++;
            if (nw > width) nw = width;
            scaleWidth = ((float) nw) / source.getWidth();
        } else {
            int nh = (int) (width * ratioH) + 1;
            if ((nh % 2) != 0) nh++;
            if (nh > height) nh = height;
            scaleHeight = ((float) nh) / source.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        if (rotation == 3) matrix.postRotate(180);
        if (rotation == 6) matrix.postRotate(90);
        if (rotation == 8) matrix.postRotate(270);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    
    public static String getExtension(String path){
        if (path != null){
           int i = path.lastIndexOf(".");
           int j = path.lastIndexOf("/");
           int k = path.lastIndexOf("\\");
           if (i > j && i > k && i < path.length() - 1){
               return path.substring(i + 1, path.length()).toLowerCase();
           }
        }
        return null;
    }

}
