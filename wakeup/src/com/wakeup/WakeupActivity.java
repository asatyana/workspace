package com.wakeup;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;


public class WakeupActivity extends Activity implements OnClickListener{
	Button btnStart, btnStop, btnBind, btnUnbind, btnUpby1, btnUpby10;
	TextView textStatus, textIntValue, textStrValue;
	Messenger mService = null;
    private PendingIntent pendingIntent;
	boolean mIsBound;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WakeupService.MSG_SET_INT_VALUE:
				textIntValue.setText("Int Message: " + msg.arg1);
				break;
			case WakeupService.MSG_SET_STRING_VALUE:
				String str1 = msg.getData().getString("str1");
				textStrValue.setText("Str Message: " + str1);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			textStatus.setText("Attached.");
			try {
				Message msg = Message.obtain(null,
						WakeupService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				Toast.makeText(getApplicationContext(), "Unable to connect to  Service", Toast.LENGTH_LONG).show();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			mService = null;
			Toast.makeText(getApplicationContext(),"Disconnected...", Toast.LENGTH_LONG).show();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_wakeup);
		setContentView(R.layout.wakeup);
		loadPreferences();

        // Add listeners for the buttons
        Button save = (Button) findViewById(R.id.startButton);
        Button close = (Button) findViewById(R.id.closeButton);
        Button exit = (Button) findViewById(R.id.exitButton);
        save.setOnClickListener(this);
        close.setOnClickListener(this);
        exit.setOnClickListener(this);		

		CheckIfServiceIsRunning();
	}

	private void CheckIfServiceIsRunning() {
		// If the service is running when the activity starts, we want to
		// automatically bind to it.
		if (WakeupService.isRunning()) {
			doBindService();
		}
	}


	private void sendMessageToService(int intvaluetosend) {
		if (mIsBound) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							WakeupService.MSG_SET_INT_VALUE, intvaluetosend, 0);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}

	void doBindService() {
		bindService(new Intent(this, WakeupService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it,
			// then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							WakeupService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has
					// crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			doUnbindService();
		} catch (Throwable t) {
			Log.e("Wakeup", "Failed to unbind from the service", t);
		}
	}
	
    private void loadPreferences() {
        MyPreferences prefs = MyPreferences.getPreferences(this);

        // Retrieve values from the preferences
        int interval = prefs.getPrefsInterval();
        int count = prefs.getPrefsCount();
        int duration = prefs.getPrefsDuration();
        boolean isNightMode = prefs.getNightMode();
        boolean isWeekendMode = prefs.getWeekendMode();

        // Get handle to UI widgets for setting the values
        NumberPicker frequencyWidget = (NumberPicker) findViewById(R.id.frequencyPicker);
        NumberPicker durationWidget = (NumberPicker) findViewById(R.id.durationPicker);
        NumberPicker countWidget = (NumberPicker) findViewById(R.id.vibrationsPicker);
        CheckBox nightMode = (CheckBox) findViewById(R.id.nightModeCB);
        CheckBox weekendMode = (CheckBox) findViewById(R.id.weekEndModeCB);

        // Set saved values to the widgets
        frequencyWidget.setValue(interval);
        durationWidget.setValue(duration);
        countWidget.setValue(count);
        nightMode.setChecked(isNightMode);
        weekendMode.setChecked(isWeekendMode);
        
        // Set min/max values for pickers
        frequencyWidget.setMinValue(10);
        frequencyWidget.setMaxValue(120);
        durationWidget.setMinValue(1);
        durationWidget.setMaxValue(5);
        countWidget.setMinValue(1);
        countWidget.setMaxValue(5);
    }
    
    public void onClick(View view) {

        if (((Button) view).getId() == R.id.startButton) {

            // Start button is pressed
            MyPreferences prefs = MyPreferences.getPreferences(this);

            // Retrieve the values in the widgets for saving
            NumberPicker intervalText = (NumberPicker) findViewById(R.id.frequencyPicker);
            NumberPicker durationText = (NumberPicker) findViewById(R.id.durationPicker);
            NumberPicker countText = (NumberPicker) findViewById(R.id.vibrationsPicker);
            CheckBox nightMode = (CheckBox) findViewById(R.id.nightModeCB);
            CheckBox weekendMode = (CheckBox) findViewById(R.id.weekEndModeCB);

            // Save values in the preferences
            prefs.setPrefsInterval(intervalText.getValue());
            prefs.setPrefsCount(countText.getValue());
            prefs.setPrefsDuration(durationText.getValue());
            prefs.setPrefsNightMode(nightMode.isChecked());
            prefs.setPrefsWeekendMode(weekendMode.isChecked());

            // Start the service
            startWakeupService();
        } else if (((Button) view).getId() == R.id.exitButton) {

            // Clean-up before exit. Cancel Alarm and finish the activity
            Intent myIntent = new Intent(WakeupActivity.this, WakeupService.class);
            pendingIntent = PendingIntent.getService(WakeupActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarm.cancel(pendingIntent);
            finish();
        } else {

            // Close button is pressed, return to the HOME screen
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }
    
    private void startWakeupService() {

        // Get handle to Preferences
        MyPreferences prefs = MyPreferences.getPreferences(this);

        // Create the intent
        Intent myIntent = new Intent(WakeupActivity.this, WakeupService.class);

        // Payload for the intent
        myIntent.putExtra("DURATION", prefs.getPrefsDuration());
        myIntent.putExtra("COUNT", prefs.getPrefsCount());
        myIntent.putExtra("NIGHTMODE", prefs.getNightMode());
        myIntent.putExtra("WEEKENDMODE", prefs.getWeekendMode());

        // Create the PendingIntent
        pendingIntent = PendingIntent.getService(WakeupActivity.this, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Get AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Start repeating alarm after 10 seconds
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), prefs.getPrefsInterval() * 60 * 1000, pendingIntent);

        Toast.makeText(WakeupActivity.this, "Start Alarm", Toast.LENGTH_LONG).show();
    }
}