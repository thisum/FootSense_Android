package com.thisum.med.foolsim;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by thisum on 4/19/2017.
 */

public class FootMapperFragment extends Fragment implements View.OnClickListener, ResultsNotifier
{
    private static final String TAG = FootMapperFragment.class.getSimpleName();
    private DecimalFormat df = new DecimalFormat("#.##");

    private Button[] rightLeg = new Button[8];
    private Button[] leftLeg = new Button[8];
    private List<Integer> rightLegValues = new ArrayList<>();
    private List<Integer> leftLegValues = new ArrayList<>();
    private ArrayList<Integer> values = new ArrayList<>();

    private Button uploadBtn;
    private String patientName = "";
    private String patientEmail = "";
    private MenuItem diffMenuItem = null;

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState )
    {
        setHasOptionsMenu( true );
        View view = inflater.inflate( R.layout.foot_mapper_fragment, container, false );

        Integer[] rightMarkers = {R.id.right_p8, R.id.right_p2, R.id.right_p3, R.id.right_p4, R.id.right_p5, R.id.right_p6, R.id.right_p7, R.id.right_p1};
        Integer[] leftMarkers = {R.id.left_p1, R.id.left_p2, R.id.left_p3, R.id.left_p4, R.id.left_p5, R.id.left_p6, R.id.left_p7, R.id.left_p8};

        setMarkers( view, rightLeg, rightMarkers );
        setMarkers( view, leftLeg, leftMarkers );

        uploadBtn = (Button)view.findViewById( R.id.upload );
        uploadBtn.setOnClickListener( this );

        df.setRoundingMode( RoundingMode.CEILING);

        return view;
    }

    private void setMarkers( View view, Button[] leg, Integer[] markers )
    {
        for( int i = 0; i < 8; i++ )
        {
            leg[i] = (Button ) view.findViewById( markers[i] );
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().registerReceiver( receiver, makeGattUpdateIntentFilter() );
    }

    @Override
    public void onStart()
    {
        super.onStart();
        getActivity().bindService( new Intent( getActivity(), BluetoothLeService.class ), mServiceConnection, Context.BIND_AUTO_CREATE );
    }

    @Override
    public void onStop()
    {
        getActivity().unbindService( mServiceConnection );
        super.onStop();
    }

    @Override
    public void onPause()
    {
        diffMenuItem = null;
        getActivity().unregisterReceiver( receiver );
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        diffMenuItem = menu.findItem( R.id.diff_indicator );
    }

    @Override
    public void onClick( View v )
    {
        if( v == uploadBtn )
        {
            uploadData();
        }
    }

    private void uploadData()
    {
        RequestLogObj logObj = new RequestLogObj( leftLegValues, rightLegValues, this.patientName, this.patientEmail );
        FootTempLoggerTask loggerTask = new FootTempLoggerTask( getActivity().getString( R.string.web_url) , this );
        loggerTask.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR, logObj );
        FileWriter.getInstance().write(leftLegValues,rightLegValues,this.patientName);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            final String action = intent.getAction();
            if( BluetoothLeService.ACTION_DATA_AVAILABLE.equals( action ) )
            {
                values.clear();
                ArrayList<Integer> rightVal = intent.getIntegerArrayListExtra( BluetoothLeService.EXTRA_DATA_RIGHT );
                ArrayList<Integer> leftVal = intent.getIntegerArrayListExtra( BluetoothLeService.EXTRA_DATA_LEFT );

                boolean rightLeg = rightVal != null;
                extractAndShowValues( (rightLeg ? rightVal : leftVal ), rightLeg );
            }
        }
    };

    private void extractAndShowValues( ArrayList<Integer> tempValues, boolean isRightLeg )
    {
        values.addAll( tempValues );
        onBLEDataReceived( values, isRightLeg );
        if( isRightLeg )
        {
            rightLegValues.clear();
            rightLegValues.addAll( values );
        }
        else
        {
            leftLegValues.clear();
            leftLegValues.addAll( values );
        }

        diffMenuItem.setIcon( getResources().getDrawable( ( hasDifferences() ? R.drawable.indicator_diff_yes : R.drawable.indicator_diff_no) , null ) );
    }

    private void onBLEDataReceived( ArrayList<Integer> temperatureList, boolean isRightLeg )
    {
        for( int i = 0; i < temperatureList.size(); i++ )
        {
            double temp = temperatureList.get( i ) / 100;
            int colour = getColour( temp );
            String tempVal = String.valueOf( temp );
            if( isRightLeg )
            {
                setMarkerColour( rightLeg[i], colour, tempVal );
            }
            else
            {
                setMarkerColour( leftLeg[i], colour, tempVal );
            }
        }
    }

    private boolean hasDifferences()
    {
        if( ! leftLegValues.isEmpty() && ! rightLegValues.isEmpty() )
        {
            for( int i = 0; i < leftLegValues.size(); i++ )
            {
                if( Math.abs( leftLegValues.get( i ).intValue() - rightLegValues.get( i ).intValue() ) >= 2 )
                {
                    return true;
                }
            }
        }
        return false;
    }

    private IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction( BluetoothLeService.ACTION_DATA_AVAILABLE );
        return intentFilter;
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected( ComponentName componentName, IBinder service )
        {
            BluetoothLeService myService = ( ( BluetoothLeService.LocalBinder ) service ).getService();
        }

        @Override
        public void onServiceDisconnected( ComponentName componentName )
        {

        }
    };

    private int getColour( double temperature )
    {
        if( temperature <= 15 )
        {
            return R.drawable.rounded_btn_c0;
        }
        else if( 15 < temperature && temperature <= 20 )
        {
            return R.drawable.rounded_btn_c1;
        }
        else if( 20 < temperature && temperature <= 24 )
        {
            return R.drawable.rounded_btn_c2;
        }
        else if( 24 < temperature && temperature <= 25 )
        {
            return R.drawable.rounded_btn_c3;
        }
        else if( 25 < temperature && temperature <= 26 )
        {
            return R.drawable.rounded_btn_c4;
        }
        else if( 26 < temperature && temperature <= 27 )
        {
            return R.drawable.rounded_btn_c5;
        }
        else if( 27 < temperature && temperature <= 29 )
        {
            return R.drawable.rounded_btn_c6;
        }
        else
        {
            return R.drawable.rounded_btn_c7;
        }
    }

    private void setMarkerColour( final Button button, final int colour, final String tempVal )
    {
        getActivity().runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                button.setBackground( getResources().getDrawable( colour, null ) );
                button.setText( tempVal );
            }
        } );
    }


    public void setPatientName( String patientName )
    {
        this.patientName = patientName;
    }

    public void setPatientEmail( String patientEmail )
    {
        this.patientEmail = patientEmail;
    }

    @Override
    public void notifyResults( Constants.Application application, final String... msg )
    {
        if( application == Constants.Application.SERVER_RESPONSE )
        {
            getActivity().runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText( getActivity(), msg[0], Toast.LENGTH_LONG ).show();
                }
            } );
        }
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
