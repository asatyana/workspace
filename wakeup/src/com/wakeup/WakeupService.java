package com.wakeup;

import java.util.ArrayList;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Vibrator;
import android.widget.Toast;

public class WakeupService extends Service {
	private NotificationManager nm;
	private int counter = 0, incrementby = 1;
	private static boolean isRunning = false;

	// Keeps track of all currently registered clients.
	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); 
	
	int mValue = 0; // Holds last value set by a client.
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_SET_INT_VALUE = 3;
	static final int MSG_SET_STRING_VALUE = 4;
	
	// Target we publish for clients to send messages to IncomingHandler.
	final Messenger mMessenger = new Messenger(new IncomingHandler()); 

	
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	// Incoming message handler
	
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_SET_INT_VALUE:
				incrementby = msg.arg1;
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void sendMessageToUI(int intvaluetosend) {
		for (int i = mClients.size() - 1; i >= 0; i--) {
			try {
				// Send data as an Integer
				mClients.get(i).send(
						Message.obtain(null, MSG_SET_INT_VALUE, intvaluetosend,
								0));

				// Send data as a String
				Bundle b = new Bundle();
				b.putString("str1", "ab" + intvaluetosend + "cd");
				Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
				msg.setData(b);
				mClients.get(i).send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going
				// through the list from back to front so this is safe to do
				// inside the loop.
				mClients.remove(i);
			}
		}
	}

    @Override
    public void onCreate() {
        isRunning = true;
        Toast.makeText(this, "WakeupService.onCreate()", Toast.LENGTH_LONG).show();
    }

	@SuppressWarnings("deprecation")
	private void showNotification() {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.service_started);
		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_launcher,
				text, System.currentTimeMillis());
		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, WakeupActivity.class), 0);
		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.service_label),
				text, contentIntent);
		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		nm.notify(R.string.service_started, notification);
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Get the payload
        Bundle extras = intent.getExtras();
        int count = intent.getIntExtra("COUNT", 1);
        int duration = intent.getIntExtra("DURATION", 1);
        boolean nightMode = intent.getBooleanExtra("NIGHTMODE", true);
        boolean weekendMode = intent.getBooleanExtra("WEEKENDMODE", true);

        // Acquire partial wake lock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WAKEUP");
        wl.acquire();

        // Perform the operation
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        for (int i = 0; i < count; i++) {
            v.vibrate(duration * 1000);
            try {
                // Wait till vibration completes (duration of vibration + buffer)
                Thread.sleep((duration * 1000) + Long.parseLong("500"));
            } catch (Exception e) {
                // Do nothing
            }
        }

        Toast.makeText(this, "WakeupService.onStart()", Toast.LENGTH_LONG).show();

        // Release the partial wake lock
        wl.release();
        return super.onStartCommand(intent, flags, startId);
    }

	public static boolean isRunning() {
		return isRunning;
	}

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
        Toast.makeText(this, "WakeupService.onDestroy()", Toast.LENGTH_LONG).show();
    }
}