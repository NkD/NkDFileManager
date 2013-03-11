package cz.cube.nkd.filemanager.job.thumbs;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import cz.cube.nkd.filemanager.R;
import cz.cube.nkd.filemanager.job.Job;
import cz.cube.nkd.filemanager.util.Util;

public class JobForThumbs extends Job {

    private static final int LOAD_IMAGE = 1;
    private static final int LOAD_VIDEO = 2;
    private static final int LOAD_APK = 3;

    private final WeakReference<ImageView> wrImageView;
    private int asyncLoad = -1;
    private Options bitmapFactoryOpts;

    public JobForThumbs(ImageView imageView) {
        super();
        this.wrImageView = new WeakReference<ImageView>(imageView);
        cancelPreviousJob();
    }

    @Override
    public boolean onPreExecute(Object... params) {
        File file = (File) params[0];
        ImageView iv = wrImageView.get();
        Bitmap thumb = CacheForThumbs.get(file, iv.getContext());
        if (thumb != null) {
            iv.setImageBitmap(thumb);
        } else {
            String fileName = file.getName().toLowerCase();
            if (file.isDirectory()) {
                if (file.isHidden() || file.getName().startsWith(".")) {
                    setImageResource(R.drawable.folder_hidden);
                } else {
                    setImageResource(R.drawable.folder);
                }
            } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
                setImageResource(R.drawable.file_audio);
            } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".bmp") || fileName.endsWith(".gif")) {
                setImageResource(R.drawable.file_image);
                asyncLoad = LOAD_IMAGE;
            } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mpg") || fileName.endsWith(".mkv")) {
                setImageResource(R.drawable.file_video);
                asyncLoad = LOAD_VIDEO;
            } else if (fileName.endsWith(".apk")) {
                setImageResource(R.drawable.file_unknown);
                asyncLoad = LOAD_APK;
            } else if (fileName.endsWith(".zip") || fileName.endsWith(".gz") || fileName.endsWith(".rar")) {
                setImageResource(R.drawable.file_package);
            } else if (fileName.endsWith(".txt") || fileName.endsWith(".ini") || fileName.endsWith(".doc")) {
                setImageResource(R.drawable.file_text);
            } else {
                setImageResource(R.drawable.file_unknown);
            }
            if (asyncLoad != -1) {
                if (iv != null) iv.setTag(this);
            }
        }
        return asyncLoad != -1;
    }

    @Override
    protected Object[] doInBackground(Object... params) {
        File file = (File) params[0];
        ImageView iv = wrImageView.get();
        Bitmap thumb = null;
        if (iv != null) {
            LayoutParams lp = (LayoutParams) iv.getLayoutParams();
            if (asyncLoad == LOAD_IMAGE) {
                bitmapFactoryOpts = new Options();
                bitmapFactoryOpts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getPath(), bitmapFactoryOpts);
                bitmapFactoryOpts.inSampleSize = Math.max(bitmapFactoryOpts.outWidth / 100, bitmapFactoryOpts.outHeight / 100);
                bitmapFactoryOpts.inJustDecodeBounds = false;
                Bitmap preThumb = BitmapFactory.decodeFile(file.getPath(), bitmapFactoryOpts);
                int rotation = 1;
                try {
                    ExifInterface exif = new ExifInterface(file.getPath());
                    rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                } catch (Throwable e) {
                    rotation = 1;
                }
                if (preThumb != null) {
                    thumb = Util.createThumbnail(preThumb, lp.width, lp.height, rotation);
                    preThumb.recycle();
                    CacheForThumbs.put(file, thumb, iv.getContext());
                }
            } else if (asyncLoad == LOAD_VIDEO) {
                Bitmap preThumb = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
                if (preThumb != null) {
                    thumb = Util.createThumbnail(preThumb, lp.width, lp.height, 1);
                    Bitmap t = Bitmap.createBitmap(lp.width, lp.height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(t);
                    int left = (lp.width - thumb.getWidth()) / 2;
                    int top = (lp.height - thumb.getHeight()) / 2;
                    canvas.drawBitmap(thumb, left, top, null);
                    Bitmap mask = BitmapFactory.decodeResource(iv.getResources(), R.drawable.video_mask);
                    Rect src = new Rect(0, 0, mask.getWidth(), mask.getHeight());
                    Rect dest = new Rect(0, 0, lp.width, lp.height);
                    canvas.drawBitmap(mask, src, dest, null);
                    thumb = t;
                    if (thumb != null) CacheForThumbs.put(file, thumb, iv.getContext());
                }
            } else if (asyncLoad == LOAD_APK) {
                String filePath = file.getPath();
                PackageInfo packageInfo = iv.getContext().getPackageManager().getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                if (Build.VERSION.SDK_INT >= 8) {
                    appInfo.sourceDir = filePath;
                    appInfo.publicSourceDir = filePath;
                }
                Drawable icon = appInfo.loadIcon(iv.getContext().getPackageManager());
                if (icon != null) {
                    Bitmap bmpIcon = ((BitmapDrawable) icon).getBitmap();
                    thumb = Util.createThumbnail(bmpIcon, lp.width, lp.height, 1);
                    CacheForThumbs.put(file, thumb, iv.getContext());
                }
            }
        }
        return new Object[] { thumb };
    }

    @Override
    public void onPostExecute(Object... result) {
        ImageView iv = wrImageView.get();
        if (iv != null) {
            if (!isCancelled() && result != null && result.length > 0) {
                Bitmap bmp = (Bitmap) result[0];
                if (bmp != null && iv.getTag() == this) {
                    iv.setImageBitmap(bmp);
                }
            }
        }
    }

    @Override
    public void onCancel() {
        if (bitmapFactoryOpts != null) {
            bitmapFactoryOpts.requestCancelDecode();
        }
    }

    private void cancelPreviousJob() {
        ImageView iv = wrImageView.get();
        if (iv != null) {
            Job job = (Job) iv.getTag();
            if (job != null) job.cancel();
            iv.setTag(null);
        }
    }

    private void setImageResource(int id) {
        ImageView iv = wrImageView.get();
        if (iv != null) iv.setImageResource(id);
    }

}
