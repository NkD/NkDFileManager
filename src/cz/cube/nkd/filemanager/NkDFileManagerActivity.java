package cz.cube.nkd.filemanager;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import cz.cube.nkd.filemanager.component.ItemListView;
import cz.cube.nkd.filemanager.component.ItemListViewAdapter;
import cz.cube.nkd.filemanager.item.Item;
import cz.cube.nkd.filemanager.item.ItemUp;
import cz.cube.nkd.filemanager.util.CubePager;
import cz.cube.nkd.filemanager.util.HorizontalPager;

public class NkDFileManagerActivity extends Activity {

    private static WeakReference<NkDFileManagerActivity> wrActivity = new WeakReference<NkDFileManagerActivity>(null);
    private ItemListView list1;
    private ItemListView list2;
    private ItemListView list3;
    private ItemListView list4;
    private HorizontalPager horizontalPager;
    private CubePager cubePager;
    private SharedPreferences prefFolderVisited;
    private SharedPreferences prefFolderPosition;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wrActivity = new WeakReference<NkDFileManagerActivity>(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getAttributes().flags = getWindow().getAttributes().flags | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(getWindow().getAttributes());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*horizontalPager = new HorizontalPager(getApplicationContext());
        list1 = ItemListView.create(getApplicationContext(), "/mnt/sdcard/external_sd/",0xFF00FF00);
        list2 = ItemListView.create(getApplicationContext(), "/",0xFFFF0000);
        horizontalPager.addView(list1);
        horizontalPager.addView(list2);
        setContentView(horizontalPager);*/

        cubePager = new CubePager(getApplicationContext());
        list1 = ItemListView.create(cubePager.getContext(), "/", 0xFFFF0000);
        list2 = ItemListView.create(cubePager.getContext(), "/", 0xFF00FF00);
        list3 = ItemListView.create(cubePager.getContext(), "/", 0xFF0000FF);
        list4 = ItemListView.create(cubePager.getContext(), "/", 0xFFFFFF00);
        ItemListView[] itemListViews = new ItemListView[] { list1, list2, list3, list4 };
        cubePager.setItemListViews(itemListViews, 0);
        setContentView(cubePager);
        
        prefFolderVisited = getSharedPreferences("folder_visited", Context.MODE_PRIVATE);
        //prefFolderVisited.edit().clear().commit();
        prefFolderPosition = getSharedPreferences("folder_position", Context.MODE_PRIVATE);
        prefFolderPosition.edit().clear().commit();
    }

    @Override
    public void onBackPressed() {
        ItemListView itemListView;
        if (horizontalPager != null) {
            itemListView = horizontalPager.getCurrentScreen() == 0 ? list1 : list2;
        } else {
            itemListView = cubePager.getCurrent();
        }
        ItemListViewAdapter adapter = itemListView.getAdapterEx();
        Item item = adapter.getItem(0);
        if (item == null || !(item instanceof ItemUp)) {
            super.onBackPressed();
        } else {
            ItemUp iu = (ItemUp) item;
            iu.onClick(itemListView);
        }
    }

    public static NkDFileManagerActivity getInstance() {
        return wrActivity.get();
    }
    
    public void markFolderVisited(File file){
        prefFolderVisited.edit().putBoolean(file.getAbsolutePath(), true).commit();
    }
    
    public boolean isFolderVisited(File file){
        return prefFolderVisited.getBoolean(file.getAbsolutePath(), false);
    }
    
    public void setFolderPosition(File file, int position){
        prefFolderPosition.edit().putInt(file.getAbsolutePath(), position).commit();
    }
    
    public int getFolderPosition(File file){
        return prefFolderPosition.getInt(file.getAbsolutePath(), 0);
    }
    
    

}