package cz.cube.nkd.filemanager.component;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import cz.cube.nkd.filemanager.item.Item;
import cz.cube.nkd.filemanager.item.ItemFile;

public class ItemListView extends ListView {

    public static ItemListView create(Context context, String path, int color) {

        ItemListView itemListView = new ItemListView(context);
        
        itemListView.setFastScrollEnabled(true);
        itemListView.setClickable(true);
        
        int[] colors = {color, color}; // red for the example
        itemListView.setDivider(new GradientDrawable(Orientation.LEFT_RIGHT, colors));
        itemListView.setDividerHeight(1);
        ItemFile item = new ItemFile(new File(path));
        final ItemListViewAdapter itemAdapter = new ItemListViewAdapter(itemListView, item);
        itemListView.setAdapter(itemAdapter);
        OnItemClick oic = new OnItemClick();
        itemListView.setOnItemClickListener(oic);
        // itemListView.setOnItemLongClickListener(oic);
        itemListView.setOnTouchListener(new OnTouch());

        return itemListView;
    }

    @SuppressWarnings("unchecked")
    public <E extends ItemListViewAdapter> E getAdapterEx() {
        return (E) getAdapter();
    }

    private ItemListView(Context context) {
        super(context);
    }

    private static class OnItemClick implements OnItemClickListener, OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ItemListView ilv = (ItemListView) parent;
            ItemListViewAdapter ia = ilv.getAdapterEx();
            Item item = ia.getItem(position);
            item.onClick(ilv);
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ItemListView ilv = (ItemListView) parent;
            ItemListViewAdapter ia = ilv.getAdapterEx();
            Item item = ia.getItem(position);
            return item.onLongClick(ilv);
        }

    }

    private static class OnTouch implements OnTouchListener {
        private boolean processSelection = false;
        private boolean check;
        private int previousItemIndex = -1;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ListView listView = (ListView) v;
            ItemListViewAdapter itemAdapter = (ItemListViewAdapter) listView.getAdapter();
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x < 90) {
                    int itemIndex = listView.pointToPosition(x, y);
                    if (itemIndex > -1 && itemIndex < itemAdapter.getCount()) {
                        previousItemIndex = itemIndex;
                        Item item = itemAdapter.getItem(itemIndex);
                        //if (item.canRead()) {
                        check = !item.isChecked();
                        item.setCheck(check);
                        processSelection = true;
                        //}
                        itemAdapter.notifyDataSetChanged();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (processSelection) {
                    if (x < 90) {
                        int itemIndex = listView.pointToPosition(x, y);
                        if (itemIndex > -1 && itemIndex < itemAdapter.getCount()) {
                            if (previousItemIndex != itemIndex) {
                                int min = Math.min(itemIndex, previousItemIndex);
                                int max = Math.max(itemIndex, previousItemIndex);
                                previousItemIndex = itemIndex;
                                boolean changed = false;
                                for (int i = min; i <= max; i++) {
                                    if (i > -1 && i < itemAdapter.getCount()) {
                                        Item item = itemAdapter.getItem(i);
                                        //if (item.canRead()) {
                                        if (item.isChecked() != check) {
                                            item.setCheck(check);
                                            changed = true;
                                        }
                                        //}
                                    }
                                }
                                if (changed) itemAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (processSelection) {
                    previousItemIndex = -1;
                    processSelection = false;
                    return true;
                }
                break;
            }
            return processSelection;
            //return false;
        }

    }

}
