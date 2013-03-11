package cz.cube.nkd.filemanager.item;

import java.util.List;

import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import cz.cube.nkd.filemanager.component.ItemImageView;
import cz.cube.nkd.filemanager.component.ItemListView;

public interface Item {

    public List<Item> getChildren();

    public View getView(int position, View convertView, LayoutInflater mInflater);
    
    public void onDraw(Canvas canvas, ItemImageView itemImageView);
    
    public void onClick(ItemListView itemListView);
    
    public boolean onLongClick(ItemListView itemListView);
    
    public void setCheck(boolean check);
    
    public boolean isChecked();
    
    public String getName();
    
}
