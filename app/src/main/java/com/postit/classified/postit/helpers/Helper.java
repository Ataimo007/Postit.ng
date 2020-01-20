package com.postit.classified.postit.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GoogleApiAvailability;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.postit.classified.postit.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by Edem's Family on 2/23/2018.
 */

public class Helper
{
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024; // 1 mega byte
    private static final DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);

    static {
        decimalFormat.applyPattern( "#,###,###.##" );
    }

//    public static void main(String[] args) {
//        String value = monetaryRepresentation(100000000);
//        System.out.println( "The monetary value is " + value );
//        value = valueRepresentation(value);
//        System.out.println( "The value representation is " + value );
//    }

    public static class Worker extends AsyncTask< BackgroundAction, Void, UIAction >
    {
        @Override
        protected UIAction doInBackground(BackgroundAction... backgroundActions) {
            return backgroundActions[ 0 ].perform();
        }

        @Override
        protected void onPostExecute(UIAction done) {
            done.perform();
            super.onPostExecute(done);
        }

        public static void executeTask(BackgroundAction backAction )
        {
            new Worker().execute( backAction );
        }

        public static void executeAllTask(BackgroundAction... backgroundActions )
        {
            new Worker().execute( backgroundActions );
        }
    }

    public static void toast(Context context, String message )
    {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        View view = toast.getView();

        //Gets the actual oval background of the Toast then sets the colour filter
        view.setBackgroundResource( R.color.app_sub2_trans );

        //Gets the TextView from the Toast so it can be editted
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(ResourcesCompat.getColor( context.getResources(), android.R.color.white, null ));

        toast.show();
    }

    public static interface UIAction
    {
        public void perform();
    }

    public static interface BackgroundAction
    {
        public UIAction perform();
    }

    public static boolean isDeviceOnline(Activity activity)
    {
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

//    private static boolean isGooglePlayServicesAvailable( Activity activity )
//    {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable( activity );
//        return connectionStatusCode == ConnectionResult.SUCCESS;
//    }
//
//    /**
//     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
//     * Play Services installation via a user dialog, if possible.
//     */
//    private static void acquireGooglePlayServices( Activity activity ) {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(activity );
//        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
//            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode, activity);
//        }
//    }
//
//    public static void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode, Activity activity ) {
//        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
//        Dialog dialog = apiAvailability.getErrorDialog(activity, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
//        dialog.show();
//    }

    public static byte[] copyAllToBuffer(InputStream inputStream) {
        byte[] raw = null;
        try {
            raw = new byte[inputStream.available()];
            inputStream.read(raw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return raw;
    }

    public static byte[] copyToBuffer(InputStream inputStream)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int read = -1;
        int streamSize = 0;
        try
        {
            streamSize = inputStream.available();
            int bufferSize = streamSize < DEFAULT_BUFFER_SIZE ? streamSize : DEFAULT_BUFFER_SIZE;
            byte[] stream = new byte[ bufferSize ];
            read = inputStream.read( stream, 0, bufferSize );
            while ( read > 0 )
            {
                outputStream.write( stream, 0, read );
                streamSize = inputStream.available();
                bufferSize = streamSize < DEFAULT_BUFFER_SIZE ? streamSize : DEFAULT_BUFFER_SIZE;
                read = inputStream.read( stream, 0, bufferSize );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputStream.toByteArray();
    }

    public static void showBundle(Bundle bundle, String TAG)
    {
        if (bundle != null)
        {
            for (String key : bundle.keySet())
            {
                Object value = bundle.get(key);
                Log.d(TAG, String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }
    }

    public static void updateNumberField(TextInputEditText field, String value )
    {
        if ( !value.isEmpty() )
        {
            if ( value.contains(",") )
                value = value.replaceAll( ",", "" );
            try
            {
                double doubleValue = Double.parseDouble(value);
                String format = decimalFormat.format(doubleValue);
                if ( doubleValue == 0 && value.length() > format.length() )
                    return;
                field.setText( format );
                field.setSelection( format.length() );
            }
            catch ( NumberFormatException ignored) { }

        }
    }

    public static String monetaryRepresentation( String value )
    {
        Log.d( "Monetary", "value is " + value );
        double doubleValue = Double.parseDouble(value);
        return decimalFormat.format(doubleValue);
    }

    public static String monetaryRepresentation( double value )
    {
        return decimalFormat.format(value);
    }

    public static String valueRepresentation(String value )
    {
        try {
            return String.valueOf( decimalFormat.parse( value ) );
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void log(String TAG, String message, int maxLogSize ) {
        for(int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            android.util.Log.d(TAG, message.substring(start, end));
        }
    }

}
