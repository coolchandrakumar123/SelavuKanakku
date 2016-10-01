package com.chan.selavukanakku;

import java.util.List;

import android.util.Log;
import com.chan.selavukanakku.adapters.SimpleItemRecyclerViewAdapter;
import com.chan.selavukanakku.dummy.DummyContent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chan.selavukanakku.sheets.ImportGoSheet;
import com.chan.selavukanakku.sheets.SheetContent;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * An activity representing a list of Items. This activity has different presentations for handset and tablet-size devices. On handsets, the activity presents a list of items, which when touched, lead to a {@link ItemDetailActivity} representing item details. On tablets, the activity presents the list of items and item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;
    private ImportGoSheet importGoSheet;
    private TextView noInfoText;
    private RecyclerView recyclerView;

    private ImportGoSheet.ImportSheetListener importSheetListener = new ImportGoSheet.ImportSheetListener()
    {
        @Override
        public void onSheetLoad(String sheetContent)
        {
            Log.i("ChanLog", sheetContent);
            recyclerView.setVisibility(View.GONE);
            noInfoText.setVisibility(View.VISIBLE);
            noInfoText.setText(sheetContent);
        }

        @Override
        public void onSheetContentLoaded(List<SheetContent> sheetContentList)
        {
            noInfoText.setVisibility(View.GONE);
            noInfoText.setText("");
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(sheetContentList));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        noInfoText = (TextView) findViewById(R.id.noInfo);
        recyclerView = (RecyclerView) findViewById(R.id.item_list);

        if(findViewById(R.id.item_detail_container) != null)
        {
            mTwoPane = true;
        }
        importGoSheet = new ImportGoSheet(this, importSheetListener, ImportGoSheet.GET_SHEET_LIST, null);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Importing...", Snackbar.LENGTH_LONG).show();
                importGoSheet.getResultsFromApi();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        importGoSheet.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms)
    {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms)
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
