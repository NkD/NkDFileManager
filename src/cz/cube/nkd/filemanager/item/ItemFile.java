package cz.cube.nkd.filemanager.item;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cz.cube.nkd.filemanager.NkDFileManagerActivity;
import cz.cube.nkd.filemanager.R;
import cz.cube.nkd.filemanager.component.ItemImageView;
import cz.cube.nkd.filemanager.component.ItemListView;
import cz.cube.nkd.filemanager.job.thumbs.JobForThumbs;
import cz.cube.nkd.filemanager.util.Util;

/**
 * @author NkD
 *
 */
public class ItemFile implements Item {

    private final File file;
    private boolean checked = false;
    private Bitmap check = null;
    private Bitmap lock = null;

    public static Comparator<Item> comparator = new Comparator<Item>() {
        private Collator collator = Collator.getInstance();

        @Override
        public int compare(Item i1, Item i2) {
            if (!(i1 instanceof ItemUp) && (i2 instanceof ItemUp)) return 1;
            if ((i1 instanceof ItemUp) && !(i2 instanceof ItemUp)) return -1;
            File f1 = ((ItemFile) i1).file;
            File f2 = ((ItemFile) i2).file;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            return collator.compare(f1.getName(), f2.getName());
        }
    };

    public ItemFile(File file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public List<Item> getChildren() {
        if (file.canRead() && file.isDirectory()) {
            File[] files = file.listFiles();
            List<Item> children = new ArrayList<Item>();
            for (File child : files) {
                children.add(new ItemFile(child));
            }
            return children;
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, LayoutInflater mInflater) {
        View itemView = null;
        if (convertView != null && convertView.getId() == R.id.itemFile) {
            itemView = convertView;
        }
        if (itemView == null) {
            itemView = mInflater.inflate(R.layout.item_file, null);
            //Util.logI("CreateItemFile");
        }
        ItemWidgetsHolder rowHolder = (ItemWidgetsHolder) itemView.getTag();
        if (rowHolder == null) {
            rowHolder = new ItemWidgetsHolder();
            rowHolder.itemIcon = (ItemImageView) itemView.findViewById(R.id.itemIcon);
            rowHolder.itemName = (TextView) itemView.findViewById(R.id.itemName);
            rowHolder.itemDataLeft = (TextView) itemView.findViewById(R.id.itemDataLeft);
            rowHolder.itemDataRight = (TextView) itemView.findViewById(R.id.itemDataRight);
            itemView.setTag(rowHolder);
        }
        rowHolder.itemName.setText(file.getName());
        if (NkDFileManagerActivity.getInstance().isFolderVisited(file)) {
            rowHolder.itemName.setTextColor(Color.YELLOW);
        } else {
            rowHolder.itemName.setTextColor(Color.WHITE);
        }
        rowHolder.itemDataLeft.setText(Util.bytesToDynamicSize(file.length()));
        rowHolder.itemDataRight.setText(Util.convertDate(file.lastModified()));
        rowHolder.itemIcon.setItem(this);

        //itemView.setBackgroundColor((position % 2) == 0 ? R.color.ITEM_BACGROUND_ODD : R.color.ITEM_BACKGROUND_EVEN);

        new JobForThumbs(rowHolder.itemIcon).execute(file);

        return itemView;
    }

    @Override
    public void onDraw(Canvas canvas, ItemImageView itemImageView) {
        if (!file.canRead()) {
            if (lock == null) lock = BitmapFactory.decodeResource(itemImageView.getResources(), R.drawable.lock);
            Rect src = new Rect(0, 0, lock.getWidth(), lock.getHeight());
            LayoutParams lp = (LayoutParams) itemImageView.getLayoutParams();
            Rect dst = new Rect(lp.width - 40, lp.height - 30, lp.width - 10, lp.height);
            canvas.drawBitmap(lock, src, dst, null);
        }
        if (checked) {
            if (check == null) check = BitmapFactory.decodeResource(itemImageView.getResources(), R.drawable.check);
            Rect src = new Rect(0, 0, check.getWidth(), check.getHeight());
            LayoutParams lp = (LayoutParams) itemImageView.getLayoutParams();
            Rect dst = new Rect(15, 0, lp.width - 15, lp.height);
            canvas.drawBitmap(check, src, dst, null);
        }
        //
    }

    @Override
    public void setCheck(boolean check) {
        this.checked = check;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void onClick(ItemListView itemListView) {
        if (file.canRead() && file.isDirectory()) {
            NkDFileManagerActivity.getInstance().setFolderPosition(file.getParentFile(), itemListView.getFirstVisiblePosition());
            //NkDFileManagerActivity.getInstance().setFolderPosition(file, 0);
            ((Vibrator) itemListView.getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(30);
            itemListView.getAdapterEx().setItem(this);
            itemListView.setSelectionFromTop(0, 0);
            NkDFileManagerActivity.getInstance().markFolderVisited(file);
        } else {
            Uri uri = Uri.fromFile(file);
            //Util.logI("Uri = " + uri.toString());
            String extension = Util.getExtension(uri.toString());
            //Util.logI("Extension = " + extension);
            if (extension != null) {
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                //Util.logI("MimeType = " + mimeType);
                if (mimeType != null) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, mimeType);
                    NkDFileManagerActivity.getInstance().startActivity(intent);
                }
            }
        }
    }

    @Override
    public boolean onLongClick(ItemListView itemListView) {
        // Util.logI("Long clicked on " + file.getPath());
        return false;
    }

    private static class ItemWidgetsHolder {
        private TextView itemName;
        private TextView itemDataRight;
        private TextView itemDataLeft;
        private ItemImageView itemIcon;
    }

    public final File getFile() {
        return file;
    }
}
