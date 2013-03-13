package cz.cube.nkd.filemanager.item;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cz.cube.nkd.filemanager.NkDFileManagerActivity;
import cz.cube.nkd.filemanager.R;
import cz.cube.nkd.filemanager.component.ItemImageView;
import cz.cube.nkd.filemanager.component.ItemListView;

public class ItemUp implements Item {

    private final File file;
    private final File fileParent;
    private final ItemListView itemListView;

    private OnClickListener clickUp = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ItemUp.this.onClick(itemListView);
        }
    };
    private OnClickListener clickHome = new OnClickListener() {
        @Override
        public void onClick(View v) {
            itemListView.getAdapterEx().setItem(new ItemFile(new File("/")));
        }
    };

    private OnClickListener clickRefresh = new OnClickListener() {
        @Override
        public void onClick(View v) {
            itemListView.getAdapterEx().setItem(new ItemFile(file));
        }
    };

    public ItemUp(File file, ItemListView itemListView) {

        this.file = file;
        this.fileParent = file.getParentFile();
        this.itemListView = itemListView;
    }

    @Override
    public List<Item> getChildren() {
        if (fileParent.canRead() && fileParent.isDirectory()) {
            File[] files = fileParent.listFiles();
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
        if (convertView != null && convertView.getId() == R.id.itemUp) {
            itemView = convertView;
        }
        if (itemView == null) {
            //Util.logI("CreateItemUp");
            itemView = mInflater.inflate(R.layout.item_up, null);
            ImageView ivUp = (ImageView) itemView.findViewById(R.id.itemUpUp);
            ivUp.setOnClickListener(clickUp);
            ImageView ivRefresh = (ImageView) itemView.findViewById(R.id.itemUpRefresh);
            ivRefresh.setOnClickListener(clickRefresh);
            ImageView ivHome = (ImageView) itemView.findViewById(R.id.itemUpHome);
            ivHome.setOnClickListener(clickHome);
        }
        return itemView;
    }

    @Override
    public void onDraw(Canvas canvas, ItemImageView itemImageView) {
        //nothing
    }

    @Override
    public void onClick(ItemListView itemListView) {
        if (fileParent.canRead() && fileParent.isDirectory()) {
            itemListView.getAdapterEx().setItem(new ItemFile(fileParent));
            itemListView.setSelectionFromTop(NkDFileManagerActivity.getInstance().getFolderPosition(fileParent), 0);
        }
    }

    @Override
    public boolean onLongClick(ItemListView itemListView) {
        //Util.logI("Long clicked on " + file.getPath());
        return false;
    }

    @Override
    public void setCheck(boolean check) {
        //nothing
    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public String getName() {
        return "...";
    }

}
