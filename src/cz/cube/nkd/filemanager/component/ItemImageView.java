package cz.cube.nkd.filemanager.component;

import java.lang.ref.WeakReference;

import cz.cube.nkd.filemanager.item.Item;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ItemImageView extends ImageView {

    private WeakReference<Item> wrItem = new WeakReference<Item>(null);

    public ItemImageView(Context context) {
        super(context);
    }

    public ItemImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ItemImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Item item = wrItem.get();
        if (item != null) item.onDraw(canvas, this);
    }

    public final void setItem(Item item) {
        this.wrItem = new WeakReference<Item>(item);
    }

}
