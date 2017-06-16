package com.thisum.med.foolsim;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by thisum on 4/19/2017.
 */

public class FootMapperFragment extends Fragment implements View.OnClickListener, ResultsNotifier
{
    private static final String TAG = FootMapperFragment.class.getSimpleName();

    private Button[] rightLeg = new Button[8];
    private Button[] leftLeg = new Button[8];
    private List<Integer> rightLegValues = new ArrayList<>();
    private List<Integer> leftLegValues = new ArrayList<>();
    private ArrayList<Integer> values = new ArrayList<>();

    private boolean rightFootLoad;
    private boolean leftFootLoad;

    private ToggleButton rightLoadBtn;
    private ToggleButton leftLoadBtn;
    private Button uploadBtn;
    private String patientName = "";
    private String patientEmail = "";

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState )
    {
        View view = inflater.inflate( R.layout.foot_mapper_fragment, container, false );

        Integer[] rightMarkers = {R.id.right_p1, R.id.right_p2, R.id.right_p3, R.id.right_p4, R.id.right_p5, R.id.right_p6, R.id.right_p7, R.id.right_p8};
        Integer[] leftMarkers = {R.id.left_p1, R.id.left_p2, R.id.left_p3, R.id.left_p4, R.id.left_p5, R.id.left_p6, R.id.left_p7, R.id.left_p8};

        setMarkers( view, rightLeg, rightMarkers );
        setMarkers( view, leftLeg, leftMarkers );

        rightLoadBtn = (ToggleButton ) view.findViewById( R.id.load_right );
        leftLoadBtn = (ToggleButton )view.findViewById( R.id.load_left );
        uploadBtn = (Button)view.findViewById( R.id.upload );

        rightLoadBtn.setOnClickListener( this );
        leftLoadBtn.setOnClickListener( this );
        uploadBtn.setOnClickListener( this );

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
        getActivity().unregisterReceiver( receiver );
        super.onPause();
    }

    @Override
    public void onClick( View v )
    {
        if( v == leftLoadBtn )
        {
            leftFootLoad = leftLoadBtn.isChecked();
            if(rightFootLoad)
            {
                rightLoadBtn.callOnClick();
            }
        }
        else if( v == rightLoadBtn )
        {
            rightFootLoad = rightLoadBtn.isChecked();
            if(leftFootLoad)
            {
                leftLoadBtn.callOnClick();
            }
        }
        else if( v == uploadBtn )
        {
            if(rightFootLoad)
            {
                rightLoadBtn.callOnClick();
            }
            if(leftFootLoad)
            {
                leftLoadBtn.callOnClick();
            }
            uploadData();
        }
    }

    private void uploadData()
    {
        RequestLogObj logObj = new RequestLogObj( leftLegValues, rightLegValues, this.patientName, this.patientEmail );
        FootTempLoggerTask loggerTask = new FootTempLoggerTask( getActivity().getString( R.string.web_url) , this );
        loggerTask.executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR, logObj );
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
                String val = intent.getStringExtra( BluetoothLeService.EXTRA_DATA );
                for( char c : val.toCharArray() )
                {
                    values.add( Character.getNumericValue( c ) );
                }
                onBLEDataReceived( values );
                if( rightFootLoad )
                {
                    rightLegValues.clear();
                    rightLegValues.addAll( values );
                }
                else if( leftFootLoad )
                {
                    leftLegValues.clear();
                    leftLegValues.addAll( values );
                }
            }
        }
    };

    private void onBLEDataReceived( ArrayList<Integer> data )
    {
        for( int i = 0; i < data.size(); i++ )
        {
            int colour = getColour( data.get( i ) );
            if( rightFootLoad )
            {
                setMarkerColour( rightLeg[i], colour );
            }
            else if( leftFootLoad )
            {
                setMarkerColour( leftLeg[i], colour );
            }
        }
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

    private int getColour( int temp )
    {
        switch( temp )
        {
            case 1: return R.drawable.rounded_btn_c0;
            case 2: return R.drawable.rounded_btn_c1;
            case 3: return R.drawable.rounded_btn_c2;
            case 4: return R.drawable.rounded_btn_c3;
            case 5: return R.drawable.rounded_btn_c4;
            case 6: return R.drawable.rounded_btn_c5;
            case 7: return R.drawable.rounded_btn_c6;
            case 8: return R.drawable.rounded_btn_c7;
            default: return R.drawable.rounded_btn_c0;
        }
    }

    private void setMarkerColour( final Button button, final int colour )
    {
        getActivity().runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                button.setBackground( getResources().getDrawable( colour, null ) );
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
