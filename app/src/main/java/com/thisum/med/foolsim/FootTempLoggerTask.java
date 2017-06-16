package com.thisum.med.foolsim;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Thisum on 7/12/2016.
 */
public class FootTempLoggerTask extends AsyncTask<RequestLogObj, Void, String>
{
    public static final String TAG = FootTempLoggerTask.class.getSimpleName();
    private final String uploadingUrl;
    private final ResultsNotifier notifier;

    public FootTempLoggerTask( String uploadingUrl, ResultsNotifier resultNotifier )
    {
        this.uploadingUrl = uploadingUrl;
        this.notifier = resultNotifier;
    }

    @Override
    protected String doInBackground( RequestLogObj... params )
    {
        HttpURLConnection conn = null;

        if( params != null && params.length > 0 )
        {
            try
            {
                RequestLogObj logObj = params[0];
                URL connectURL = getUrl( this.uploadingUrl, logObj );
                conn = createHttpRequest(connectURL);
                conn.connect();
                return readResponse( conn );
            }
            catch( Exception e )
            {
                Log.e( TAG, e.getMessage() );
            }
            finally
            {
                try
                {
                    conn.disconnect();
                }
                catch( Exception e )
                {
                    Log.e( TAG, e.getMessage() );
                }
            }
        }
        return "";
    }

    private URL getUrl( String uploadingUrl, RequestLogObj logObj) throws MalformedURLException
    {
        try
        {
            Uri.Builder buildUri = Uri.parse( uploadingUrl ).buildUpon();
            buildUri.appendQueryParameter( Constants.QUERY_PARAM_LEFT_LEG, logObj.getLeftLeg() );
            buildUri.appendQueryParameter( Constants.QUERY_PARAM_RIGHT_LEG, logObj.getRightLeg() );
            buildUri.appendQueryParameter( Constants.QUERY_PARAM_PATIENT_EMAIL, logObj.getPatientEmail() );
            buildUri.appendQueryParameter( Constants.QUERY_PARAM_PATIENT_NAME, logObj.getPatientName() );

            Uri queryString = buildUri.build();
            Log.i( TAG, queryString.toString() );

            return new URL( queryString.toString() );
        }
        catch( Exception e )
        {
            Log.e( TAG, e.toString() );
        }

        return null;
    }

    private HttpURLConnection createHttpRequest( URL connectURL) throws Exception
    {
        HttpURLConnection conn = ( HttpURLConnection ) connectURL.openConnection();
        conn.setDoInput( true );
        conn.setDoOutput( true );
        conn.setUseCaches( false );
        conn.setRequestMethod( "POST" );
        conn.setRequestProperty( "Connection", "Keep-Alive" );
        conn.setRequestProperty( "Cache-Control", "no-cache" );
        conn.setRequestProperty( "Content-Type", "application/octet-stream" );

        return conn;
    }

    private String readResponse( HttpURLConnection conn ) throws Exception
    {
        try( InputStream responseStream =new BufferedInputStream( conn.getInputStream() );
             InputStreamReader inputStreamReader = new InputStreamReader( responseStream );
             BufferedReader bufferedReader = new BufferedReader( inputStreamReader )
            )
        {
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

            while( ( line = bufferedReader.readLine() ) != null )
            {
                stringBuilder.append( line ).append( "\n" );
            }
            String response = stringBuilder.toString();
            JSONObject responseObj = null;
            if( !response.isEmpty() )
            {
                try
                {
                    responseObj = new JSONObject( response );
                    return responseObj.getString( "message" );
                }
                catch( JSONException e )
                {
                    Log.e( TAG, "Error parsing data(JSON) " + e.toString() );
                }
            }
        }
        return "";
    }

    @Override
    protected void onPostExecute( String s )
    {
        if( notifier != null )
        {
            notifier.notifyResults( Constants.Application.SERVER_RESPONSE, s );
        }
    }
}
