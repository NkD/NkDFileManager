package cz.cube.nkd.filemanager.job.thumbs;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import cz.cube.nkd.filemanager.util.Util;

public class CacheForThumbs {

    private static File cacheDir = null;

    private static String createKeyFromFile(File file) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(file.getAbsolutePath().hashCode()));
        sb.append("_");
        sb.append(Long.toHexString(file.lastModified()));
        sb.append(".jpg");
        return sb.toString();
    }

    public static void put(File sourceFile, Bitmap bitmap, Context context) {
        try {
            File cacheDir = getCacheDir(context);
            File thumbFile = new File(cacheDir, createKeyFromFile(sourceFile));
            if (thumbFile.exists()) thumbFile.delete();
            FileOutputStream fos = new FileOutputStream(thumbFile);
            bitmap.compress(CompressFormat.JPEG, 50, fos);
            fos.flush();
            fos.close();
        } catch (Throwable e) {
            Util.logE(Util.convertException(e));
        }
    }

    public static Bitmap get(File sourceFile, Context context) {
        File cacheDir = getCacheDir(context);
        File cacheFile = new File(cacheDir, createKeyFromFile(sourceFile));
        if (cacheFile != null && cacheFile.exists()) {
            return BitmapFactory.decodeFile(cacheFile.getPath());
        }
        return null;
    }

    public static void clearCache(Context context) {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            File cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), ".NkDFileManager/thumbs_cache");
            if (cacheDir.exists()) {
                for (File file : cacheDir.listFiles()) {
                    file.delete();
                }
            }
        }
        File cacheDir = context.getCacheDir();
        if (cacheDir.exists()) {
            for (File file : cacheDir.listFiles()) {
                file.delete();
            }
        }
    }

    public static File getCacheDir(Context context) {
        if (cacheDir == null) {
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), ".NkDFileManager/thumbs_cache");
            } else {
                cacheDir = context.getCacheDir();
            }
            if (!cacheDir.exists()) {
                synchronized (CacheForThumbs.class) {
                    if (!cacheDir.exists()) cacheDir.mkdirs();
                }

            }
        }
        return cacheDir;
    }

}
