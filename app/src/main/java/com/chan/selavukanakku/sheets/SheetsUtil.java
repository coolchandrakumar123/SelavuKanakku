package com.chan.selavukanakku.sheets;

import java.util.Arrays;

import android.app.Application;
import android.content.Context;
import com.chan.selavukanakku.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

public class SheetsUtil extends Application
{

    private static SheetsUtil utilInstance;
    private com.google.api.services.sheets.v4.Sheets mService = null;
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};
    public GoogleAccountCredential mCredential;

    private static Context mContext;

    public void onCreate(){
        super.onCreate();
        this.mContext = this;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mCredential = GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
        //mService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, mCredential).setApplicationName(geta.getString(R.string.app_name)).build();
        utilInstance = this;
    }

    public static Context getContext(){
        return mContext;
    }

    public SheetsUtil()
    {

    }

    public static SheetsUtil getInstance()
    {
        if(utilInstance == null)
        {
            return new SheetsUtil();
        }
        return utilInstance;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}