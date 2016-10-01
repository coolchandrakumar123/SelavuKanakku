package com.chan.selavukanakku;

import java.util.List;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.chan.selavukanakku.adapters.SimpleItemRecyclerViewAdapter;
import com.chan.selavukanakku.dummy.DummyContent;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chan.selavukanakku.sheets.ImportGoSheet;
import com.chan.selavukanakku.sheets.SheetContent;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A fragment representing a single Item detail screen. This fragment is either contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment implements EasyPermissions.PermissionCallbacks
{
    public static final String SHEET_NAME = "sheetName";
    private ImportGoSheet importGoSheet;
    private TextView noInfoText;
    private RecyclerView recyclerView;

    public ItemDetailFragment()
    {

    }


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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(getArguments().containsKey(SHEET_NAME))
        {
            String sheetName = getArguments().getString(SHEET_NAME);
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if(appBarLayout != null)
            {
                appBarLayout.setTitle(sheetName);
            }
            importGoSheet = new ImportGoSheet(getActivity(), importSheetListener, ImportGoSheet.GET_SHEET_DETAILS, sheetName);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);
        noInfoText = ((TextView) rootView.findViewById(R.id.noInfo));
        recyclerView = (RecyclerView) rootView.findViewById(R.id.item_list);
        importGoSheet.getResultsFromApi();
        return rootView;
    }


/*

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        importGoSheet.onActivityResult(requestCode, resultCode, data);
    }*/

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
