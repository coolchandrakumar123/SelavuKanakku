package com.chan.selavukanakku.sheets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.chan.selavukanakku.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ImportGoSheet
{

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    //private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};
    //GoogleAccountCredential mCredential;
    //ProgressDialog mProgress;
    private Context context;
    private Activity activity;
    private ImportSheetListener importSheetListener;
    private int actionType;

    public static final int GET_SHEET_LIST = 101;
    public static final int GET_SHEET_DETAILS = 102;
    private String sheetName = null;

    public ImportGoSheet(Context context, ImportSheetListener importSheetListener, int actionType, String sheetName)
    {
        // Initialize credentials and service object.
        //this.mCredential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
        this.context = context;
        this.activity = ((Activity) context);
        this.importSheetListener = importSheetListener;
        this.actionType = actionType;
        this.sheetName = sheetName;
    }

    public void getResultsFromApi()
    {
        if(!isGooglePlayServicesAvailable())
        {
            acquireGooglePlayServices();
        }
        else if(SheetsUtil.getInstance().mCredential.getSelectedAccountName() == null)
        {
            chooseAccount();
        }
        else if(!isDeviceOnline())
        {
            importSheetListener.onSheetLoad("No network connection available.");
        }
        else
        {
            new MakeRequestTask(SheetsUtil.getInstance().mCredential, actionType).execute();
        }
    }

    private boolean isGooglePlayServicesAvailable()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if(apiAvailability.isUserResolvableError(connectionStatusCode))
        {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode)
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog((Activity) context, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount()
    {
        if(EasyPermissions.hasPermissions(activity, Manifest.permission.GET_ACCOUNTS))
        {
            String accountName = activity.getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if(accountName != null)
            {
                SheetsUtil.getInstance().mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            }
            else
            {
                // Start a dialog from which the user can choose an account
                activity.startActivityForResult(SheetsUtil.getInstance().mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        }
        else
        {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(activity, "This app needs to access your Google account (via Contacts).", REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        }
    }

    private boolean isDeviceOnline()
    {
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if(resultCode != Activity.RESULT_OK)
                {
                    importSheetListener.onSheetLoad("This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.");
                }
                else
                {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if(resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null)
                {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if(accountName != null)
                    {
                        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        SheetsUtil.getInstance().mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if(resultCode == Activity.RESULT_OK)
                {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * An asynchronous task that handles the Google Sheets API call. Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<SheetContent>>
    {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;
        private String spreadsheetId = null;
        private int actionType;

        public MakeRequestTask(GoogleAccountCredential credential, int actionType)
        {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, credential).setApplicationName(activity.getString(R.string.app_name)).build();
            spreadsheetId = "1bgGu_F0LJ4T7Jigtk6bEaO_nHazofIkoRIVjC6c9q50";
            this.actionType = actionType;
        }

        @Override
        protected List<SheetContent> doInBackground(Void... params)
        {
            try
            {
                if(actionType == GET_SHEET_LIST)
                {
                    return getSheetListApi();
                }
                return getSheetDetailsApi();
            }
            catch(Exception e)
            {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private List<SheetContent> getSheetListApi() throws IOException
        {
            //String range = "Class Data!A2:E";
            List<SheetContent> results = new ArrayList<>();
            Sheets.Spreadsheets spreadsheets = this.mService.spreadsheets();
            Spreadsheet spreadSheet = spreadsheets.get(spreadsheetId).setIncludeGridData(false).execute ();
            List<Sheet> sheetsList = spreadSheet.getSheets();
            if(sheetsList != null)
            {
                int index = 0;
                for(Sheet sheet : sheetsList)
                {
                    SheetContent sheetContent = new SheetContent((++index + ""), sheet.getProperties().getTitle(), sheet.getProperties().getSheetType());
                    results.add(sheetContent);
                }
            }

            return results;
        }

        private List<SheetContent> getSheetDetailsApi() throws IOException
        {
            //String range = "Class Data!A2:E";
            List<SheetContent> results = new ArrayList<>();
            Sheets.Spreadsheets spreadsheets = this.mService.spreadsheets();
            String range = sheetName + "!A2:B";
            ValueRange response = spreadsheets.values().get(spreadsheetId, range).execute();
            List<List<Object>> values = response.getValues();
            if(values != null)
            {
                int index = 0;
                for(List row : values)
                {
                    SheetContent sheetContent = new SheetContent((++index + ""), (String) row.get(0), (String) row.get(1));
                    results.add(sheetContent);
                }
            }
            return results;
        }

        @Override
        protected void onPreExecute()
        {
            //mProgress.show();
        }

        @Override
        protected void onPostExecute(List<SheetContent> output)
        {
            //mProgress.hide();
            if(output == null || output.size() == 0)
            {
                importSheetListener.onSheetLoad("No results returned.");
            }
            else
            {
                //output.add(0, "Data retrieved using the Google Sheets API:");
                importSheetListener.onSheetContentLoaded(output);
            }
        }

        @Override
        protected void onCancelled()
        {
            //mProgress.hide();
            if(mLastError != null)
            {
                if(mLastError instanceof GooglePlayServicesAvailabilityIOException)
                {
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                }
                else if(mLastError instanceof UserRecoverableAuthIOException)
                {
                    activity.startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), ImportGoSheet.REQUEST_AUTHORIZATION);
                }
                else
                {
                    importSheetListener.onSheetLoad("The following error occurred:\n" + mLastError.getMessage());
                }
            }
            else
            {
                importSheetListener.onSheetLoad("Request cancelled.");
            }
        }
    }

    public interface ImportSheetListener
    {
        void onSheetLoad(String sheetContent);

        void onSheetContentLoaded(List<SheetContent> sheetContentList);
    }
}
