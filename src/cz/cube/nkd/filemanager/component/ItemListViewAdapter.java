package cz.cube.nkd.filemanager.component;

import java.io.File;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import cz.cube.nkd.filemanager.item.Item;
import cz.cube.nkd.filemanager.item.ItemFile;
import cz.cube.nkd.filemanager.item.ItemUp;

public class ItemListViewAdapter extends BaseAdapter {

    private Item item = null;
    private List<Item> children = null;
    private LayoutInflater mInflater;
    private ItemListView itemListView;
    
    public ItemListViewAdapter(ItemListView itemListView, Item rowItem) {
        this.itemListView = itemListView;
        mInflater = (LayoutInflater) itemListView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setItem(rowItem);
    }

    @Override
    public int getCount() {
        return children.size();
    }

    public int getCheckedCount() {
        int checkedCount = 0;
        for (Item child : children) {
            if (child.isChecked())
                checkedCount++;
        }
        return checkedCount;
    }

    @Override
    public Item getItem(int position) {
        return children.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        return item.getView(position, convertView, mInflater);
    }

    public final Item getItem() {
        return this.item;
    }

    public final void setItem(Item item) {
        this.item = item;
        children = item.getChildren();
        if (item instanceof ItemFile){
            File file = ((ItemFile) item).getFile();
            if (file.getParentFile() != null) {
                children.add(new ItemUp(file, itemListView));
            }
        }
        Collections.sort(children, ItemFile.comparator);
        //((Vibrator) itemListView.getContext().getSystemService(Context.VIBRATOR_SERVICE)).vibrate(30);
        notifyDataSetChanged();
    }

}
