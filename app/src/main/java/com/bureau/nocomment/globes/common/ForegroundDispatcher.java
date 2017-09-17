package com.bureau.nocomment.globes.common;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;


public class ForegroundDispatcher {

    private NfcAdapter mNfcAdapter;

    public ForegroundDispatcher(Context context) {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
    }

    public void start(Activity activity) {
        Intent activityIntent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, activityIntent, 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("text/plain");
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        IntentFilter[] intentFiltersArray = new IntentFilter[] { ndef, };

        mNfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, null);
    }

    public void stop(Activity activity) {
        mNfcAdapter.disableForegroundDispatch(activity);
    }

    public boolean isNfcIntent(Intent intent) {
        return intent != null && mNfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction());
    }
}
