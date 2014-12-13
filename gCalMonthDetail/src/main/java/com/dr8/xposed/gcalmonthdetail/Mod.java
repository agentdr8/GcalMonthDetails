package com.dr8.xposed.gcalmonthdetail;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Mod implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	private static String targetpkg = "com.google.android.calendar";
	private static String monthclass = "com.android.calendar.month.MonthWeekEventsView";
	private static String monthfragmentclass = "com.android.calendar.month.MonthByWeekFragment";

	private static final String TAG = "XGCAL";
	private static boolean DEBUG;
	private static XSharedPreferences prefs;
	
	private static void log(String msg) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = df.format(c.getTime());
		XposedBridge.log("[" + formattedDate + "] " + TAG + ": " + msg);
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals(targetpkg)) {
			return;
		} else {
			findAndHookMethod(monthclass, lpparam.classLoader, "initView", new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam mparam) throws Throwable {
					prefs.reload();

					setBooleanField(mparam.thisObject, "mShowDetailsInMonth", true);
					setBooleanField(mparam.thisObject, "mInitialized", true);

					if (DEBUG) log("setting TEXT_SIZE_EVENT to " + prefs.getInt("textsizeevent", 12));
					setIntField(mparam.thisObject, "TEXT_SIZE_EVENT", prefs.getInt("textsizeevent", 12));
					
					if (DEBUG) log("setting TEXT_SIZE_EVENT_TITLE to " + prefs.getInt("textsizeeventtitle", 14));
					setIntField(mparam.thisObject, "TEXT_SIZE_EVENT_TITLE", prefs.getInt("textsizeeventtitle", 14));
					
					if (DEBUG) log("setting TEXT_SIZE_MORE_EVENTS to " + prefs.getInt("textsizeeventtitle", 14));
					setIntField(mparam.thisObject, "TEXT_SIZE_MORE_EVENTS", prefs.getInt("textsizeeventtitle", 14));
					
					if (DEBUG) log("setting DNA_SIDE_PADDING to " + prefs.getInt("dnapadding", 6));
					setIntField(mparam.thisObject, "DNA_SIDE_PADDING", prefs.getInt("dnapadding", 6));
					
					if (DEBUG) log("setting EVENT_SQUARE_WIDTH to " +  prefs.getInt("eventsqwidth", 10));
					setIntField(mparam.thisObject, "EVENT_SQUARE_WIDTH", prefs.getInt("eventsqwidth", 10));
					
					if (DEBUG) log("setting EVENT_SQUARE_BORDER to " + prefs.getInt("eventsqborder", 2));
					setIntField(mparam.thisObject, "EVENT_SQUARE_BORDER", prefs.getInt("eventsqborder", 2));
				}
			});
			
			findAndHookMethod(monthfragmentclass, lpparam.classLoader, "onAttach", Activity.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam mparam) throws Throwable {
					if (DEBUG) log("setting mShowDetailsInMonth to true in fragment class");
					setBooleanField(mparam.thisObject, "mShowDetailsInMonth", true);
				}
			});
		}
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		prefs = new XSharedPreferences("com.dr8.xposed.gcalmonthdetail", "com.dr8.xposed.gcalmonthdetail_preferences");
		DEBUG = prefs.getBoolean("debug", false);
	}

}
