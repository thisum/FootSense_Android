package com.thisum.med.foolsim;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ResultsNotifier
{
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000 );

        PowerManager powerManager = ( PowerManager ) getSystemService( POWER_SERVICE );
        wakeLock = powerManager.newWakeLock( PowerManager.FULL_WAKE_LOCK, "WatchFaceWakelockTag" );

        if( !getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) )
        {
            Toast.makeText( this, R.string.ble_not_supported, Toast.LENGTH_SHORT ).show();
            finish();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        wakeLock.acquire();
        gotoSetupPage();
    }

    @Override
    protected void onPause()
    {
        wakeLock.release();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.legend, menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public void notifyResults( Constants.Application application, String... msg )
    {
        if( application.equals( Constants.Application.FOOT_SCAN ) )
        {
            gotoFootScanPage( msg );
        }
        else if( application.equals( Constants.Application.SETUP ) )
        {
            gotoSetupPage();
        }
    }

    private void gotoSetupPage()
    {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        SetupFragment setupFragment = new SetupFragment();
        setupFragment.setResultNotifier( this );
        transaction.replace( R.id.fragment_container, setupFragment ).addToBackStack( "app" );

        transaction.commit();
    }

    private void gotoFootScanPage( String... patientDetails )
    {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        FootMapperFragment footMapperFragment = new FootMapperFragment();
        footMapperFragment.setPatientName( patientDetails[0] );
        footMapperFragment.setPatientEmail( patientDetails[1] );
        transaction.replace( R.id.fragment_container, footMapperFragment ).addToBackStack( "app" );

        transaction.commit();
    }

}
