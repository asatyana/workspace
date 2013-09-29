package com.wakeup;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by asatyana on 25/6/13.
 */
public class MyPreferences {
    // Preferences
    private static final String prefsInterval = "wakemeup.interval";
    private static final String prefsDuration = "wakemeup.duration";
    private static final String prefsCount = "wakemeup.count";
    private static final String prefsNightMode = "wakemeup.nightMode";
    private static final String prefsWeekendMode = "wakemeup.weekendMode";
    private static final String appId = "WakeMeUp";
    private static MyPreferences prefs = null;
    private SharedPreferences savedPrefs;
    private Activity parent;

    private MyPreferences(Activity parentActivity) {
        this.parent = parentActivity;
        savedPrefs = parent.getSharedPreferences(appId, Context.MODE_PRIVATE);
    }

    public static MyPreferences getPreferences(Activity parent) {
        if (prefs == null) {
            prefs = new MyPreferences(parent);
        }
        return prefs;
    }

    public void setPrefsNightMode(boolean nightMode) {
        savedPrefs.edit().putBoolean(prefsNightMode, nightMode).commit();
    }

    public void setPrefsWeekendMode(boolean weekendMode) {
        savedPrefs.edit().putBoolean(prefsWeekendMode, weekendMode).commit();
    }

    public int getPrefsInterval() {
        return (int) savedPrefs.getInt(prefsInterval, 0);
    }

    public void setPrefsInterval(int interval) {
        savedPrefs.edit().putInt(prefsInterval, interval).commit();
    }

    public int getPrefsDuration() {
        return (int) savedPrefs.getInt(prefsDuration, 0);
    }

    public void setPrefsDuration(int duration) {
        savedPrefs.edit().putInt(prefsDuration, duration).commit();
    }

    public int getPrefsCount() {
        return (int) savedPrefs.getInt(prefsCount, 0);
    }

    public void setPrefsCount(int count) {
        savedPrefs.edit().putInt(prefsCount, count).commit();
    }

    public boolean getNightMode() {
        return (boolean) savedPrefs.getBoolean(prefsNightMode, true);
    }

    public boolean getWeekendMode() {
        return (boolean) savedPrefs.getBoolean(prefsWeekendMode, true);
    }
}
