package com.thisum.med.foolsim;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

/**
 * Created by thisum on 4/19/2017.
 */

public class SetupFragment extends Fragment implements View.OnClickListener
{
    public static final String TAG = SetupFragment.class.getSimpleName();

    private Button startBtn;
    private Button stopBtn;
    private Button hideBtn;
    private TextView statusTxt;
    private EditText patientNameTxt;
    private EditText patientEmailTxt;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;

    private BluetoothDevice targetBLEDevice = null;
    private ResultsNotifier resultNotifier;

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.setup_fragment, container, false );

        startBtn = ( Button ) view.findViewById( R.id.start_btn );
        stopBtn = ( Button ) view.findViewById( R.id.stop_btn );
        hideBtn = ( Button ) view.findViewById( R.id.hide_btn );
        statusTxt = ( TextView ) view.findViewById( R.id.ble_status_txt );
        patientNameTxt = ( EditText ) view.findViewById( R.id.patient_name_text );
        patientEmailTxt = ( EditText ) view.findViewById( R.id.email_text );
        addListeners();

        mHandler = new Handler();

        final BluetoothManager bluetoothManager = ( BluetoothManager ) getActivity().getSystemService( Context.BLUETOOTH_SERVICE );
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if( mBluetoothAdapter == null )
        {
            Toast.makeText( getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT ).show();
            getActivity().finish();
        }

        return view;
    }

    private void addListeners()
    {
        startBtn.setOnClickListener( this );
        stopBtn.setOnClickListener( this );
        hideBtn.setOnClickListener( this );
    }


    @Override
    public void onClick( View v )
    {
        if( v == startBtn )
        {
            scanBLEDevice( true );
        }
        else if( v == stopBtn )
        {
            disconnectBLE();
        }
        else if( v == hideBtn )
        {
            resultNotifier.notifyResults( Constants.Application.FOOT_SCAN, patientNameTxt.getText().toString(), patientEmailTxt.getText().toString() );
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if( !mBluetoothAdapter.isEnabled() )
        {
            if( !mBluetoothAdapter.isEnabled() )
            {
                Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
                startActivityForResult( enableBtIntent, Constants.REQUEST_ENABLE_BT );
            }
        }
        getActivity().registerReceiver( receiver, makeGattUpdateIntentFilter() );
    }

    @Override
    public void onPause()
    {
        getActivity().unregisterReceiver( receiver );
        super.onPause();
    }

    private void disconnectBLE()
    {
        getActivity().stopService( new Intent( getActivity(), BluetoothLeService.class ) );
    }

    private void scanBLEDevice( final boolean enable )
    {
        if( enable )
        {
            mHandler.postDelayed( new Runnable()
            {
                @Override
                public void run()
                {
                    mScanning = false;
                    mBluetoothLeScanner.stopScan( scanCallback );
                    startServices();
                }
            }, Constants.SCAN_PERIOD );


            ScanSettings scanSettings = new ScanSettings.Builder().build();
            mBluetoothLeScanner.startScan( null, scanSettings, scanCallback );
            statusTxt.setText( "Connecting..." );
            mScanning = true;
        }
        else
        {
            mBluetoothLeScanner.stopScan( scanCallback );
            mScanning = false;
        }
    }

    private void startServices()
    {
        if( mScanning )
        {
            mBluetoothLeScanner.stopScan( scanCallback );
            mScanning = false;
        }
        if( targetBLEDevice != null )
        {
            Intent intent = new Intent( getActivity(), BluetoothLeService.class );
            intent.putExtra( BluetoothLeService.EXTRA_DEVICE_ADDRESS, targetBLEDevice.getAddress() );
            getActivity().startService( intent );
        }
    }


    private ScanCallback scanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult( int callbackType, ScanResult result )
        {
            try
            {
                BluetoothDevice device = result.getDevice();
                if( device.getAddress().equals( Constants.DEVICE ) )
                {
                    targetBLEDevice = result.getDevice();
                }
            }
            catch( Exception ex )
            {
                Log.d( TAG, ex.toString() );
            }
        }

        @Override
        public void onScanFailed( int errorCode )
        {
            super.onScanFailed( errorCode );
            Toast.makeText( getActivity(), "onScanFailed: " + String.valueOf( errorCode ), Toast.LENGTH_LONG ).show();
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            final String action = intent.getAction();
            if( BluetoothLeService.ACTION_GATT_CONNECTED.equals( action ) )
            {
                statusTxt.setText( "Connected" );
                statusTxt.setTextColor( Color.parseColor( "#00C853" ) );
            }
            if( BluetoothLeService.ACTION_GATT_DISCONNECTED.equals( action ) )
            {
                statusTxt.setText( "Disonnected" );
                statusTxt.setTextColor( Color.parseColor( "#f43f3f" ) );
            }
        }
    };

    private IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction( BluetoothLeService.ACTION_GATT_CONNECTED );
        intentFilter.addAction( BluetoothLeService.ACTION_GATT_DISCONNECTED );
        return intentFilter;
    }

    public void setResultNotifier( ResultsNotifier resultNotifier )
    {
        this.resultNotifier = resultNotifier;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        try
        {
            Field childFragmentManager = Fragment.class.getDeclaredField( "mChildFragmentManager" );
            childFragmentManager.setAccessible( true );
            childFragmentManager.set( this, null );

        }
        catch( NoSuchFieldException e )
        {
            throw new RuntimeException( e );
        }
        catch( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
    }
}
