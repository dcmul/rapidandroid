package org.rapidandroid.activity.chart.message;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.rapidandroid.activity.chart.IChartBroker;
import org.rapidandroid.data.SmsDbHelper;
import org.rapidsms.java.core.Constants;
import org.rapidsms.java.core.model.Message;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 29, 2009 Summary:
 */
public class MessageDataBroker implements IChartBroker {

	private WebView mAppView;
	SmsDbHelper rawDB;
	private String[] variables;
	private int variablechosen = 0;
	private Date mStartDate = Constants.NULLDATE;
	private Date mEndDate = Constants.NULLDATE;
	private ProgressDialog mProgress = null;
	private Activity mParentActivity;
	
	final Handler mTitleHandler = new Handler();
	final Runnable mUpdateActivityTitle = new Runnable() {
		public void run() {
			mParentActivity.setTitle(variables[variablechosen]);
		}
	};
	
	public MessageDataBroker(Activity activity, WebView appView, Date startDate, Date endDate) {
		this.mParentActivity = activity;
		this.mAppView = appView;
		this.rawDB = new SmsDbHelper(appView.getContext());
		

		this.variables = new String[] { "Trends by day", "Receipt time of day" };

//		Toast.makeText(appView.getContext(), "To see chart, load a variable with the menus below.", Toast.LENGTH_LONG);
		mStartDate = startDate;
		mEndDate= endDate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapidandroid.activity.chart.IChartBroker#getGraphTitle()
	 */
	public String getGraphTitle() {
		// TODO Auto-generated method stub
		return "message graphs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapidandroid.activity.chart.IChartBroker#loadGraph()
	 */

	public void loadGraph() {
		mProgress = ProgressDialog.show(mAppView.getContext(), "Rendering Graph...", "Please Wait",true,false);
		
		int width = mAppView.getWidth();
		int height = 0;
		if (width == 480) {
			height = 320;
		} else if (width == 320) {
			height = 480;
		}
		height = height - 50;

		JSONArray arr = new JSONArray();

		if (variablechosen == 0) {
			// this is a count of messages per day
			// select date(time), count(*) from rapidandroid_message group by
			// date(time)
			arr.put(loadMessageTrends());
		} else if (variablechosen == 1) {
			arr.put(chartMessagesPerHour());

		}

		mAppView.loadUrl("javascript:SetGraph(\"" + width + "px\", \"" + height + "px\")");
		mAppView.loadUrl("javascript:GotGraph(" + arr.toString() + ")");
	}

	private JSONObject loadMessageTrends() {
		JSONObject result = new JSONObject();
		SQLiteDatabase db = rawDB.getReadableDatabase();

		StringBuilder rawQuery = new StringBuilder();
		rawQuery.append("select time, count(*) from rapidandroid_message ");

		if(mStartDate.compareTo(Constants.NULLDATE) != 0 && mEndDate.compareTo(Constants.NULLDATE) != 0) {
			rawQuery.append(" WHERE time > '" + Message.SQLDateFormatter.format(mStartDate) + "' AND time < '" + Message.SQLDateFormatter.format(mEndDate) + "' ");
		}
		
		rawQuery.append(" group by date(time) order by time ASC");

		// the string value is column 0
		// the magnitude is column 1

		Cursor cr = db.rawQuery(rawQuery.toString(), null);
		int barCount = cr.getCount();

		if (barCount == 0) {
			return result;
		} else {
			Date[] xVals = new Date[barCount];
			int[] yVals = new int[barCount];
			cr.moveToFirst();
			int i = 0;
			do {
				try {
					xVals[i] = Message.SQLDateFormatter.parse(cr.getString(0));
					xVals[i].setHours(12);
					xVals[i].setMinutes(0);
					xVals[i].setSeconds(0);
				} catch (Exception ex) {

				}
				yVals[i] = cr.getInt(1);

				i++;
			} while (cr.moveToNext());

			try {
				result.put("label", "Messages");
				result.put("data", prepareDateData(xVals, yVals));
				result.put("lines", getShowTrue());
				result.put("points", getShowTrue());
				result.put("xaxis", getDateOptions());

			} catch (Exception ex) {

			}
			cr.close();
			return result;
		}
	}

	private JSONObject chartMessagesPerHour() {
		JSONObject result = new JSONObject();
		SQLiteDatabase db = rawDB.getReadableDatabase();

		String rawQuery = "select strftime('%H',time), count(*) from rapidandroid_message group by strftime('%H',time) order by strftime('%H',time)";

		// the string value is column 0
		// the magnitude is column 1

		Cursor cr = db.rawQuery(rawQuery, null);
		int barCount = cr.getCount();

		if (barCount == 0) {
			return result;
		} else {
			String[] xVals = new String[barCount];
			int[] yVals = new int[barCount];
			cr.moveToFirst();
			int i = 0;
			do {
				xVals[i] = cr.getString(0);
				yVals[i] = cr.getInt(1);
				i++;
			} while (cr.moveToNext());

			try {
				result.put("label", "Messages");
				result.put("data", prepareData(yVals));
				result.put("bars", getShowTrue());

			} catch (Exception ex) {

			}
			cr.close();
			return result;
		}
	}

	private JSONArray prepareDateData(Date[] xvals, int[] yvals) {
		JSONArray arr = new JSONArray();
		int datalen = xvals.length;
		for (int i = 0; i < datalen; i++) {
			JSONArray elem = new JSONArray();
			elem.put(xvals[i].getTime());
			elem.put(yvals[i]);
			arr.put(elem);
		}
		return arr;
	}

	private JSONArray prepareData(int[] values) {
		JSONArray arr = new JSONArray();
		int datalen = values.length;
		for (int i = 0; i < datalen; i++) {
			JSONArray elem = new JSONArray();
			elem.put(i);
			elem.put(values[i]);
			arr.put(elem);
		}
		return arr;
	}

	private JSONObject getDateOptions() {
		JSONObject rootxaxis = new JSONObject();

		try {
			rootxaxis.put("mode", "time");
		} catch (Exception ex) {

		}
		return rootxaxis;
	}

	private JSONObject getXaxisOptions(String[] tickvalues) {
		JSONObject rootxaxis = new JSONObject();
		JSONArray arr = new JSONArray();
		int ticklen = tickvalues.length;

		for (int i = 0; i < ticklen; i++) {
			JSONArray elem = new JSONArray();
			elem.put(i);
			elem.put(tickvalues[i]);
			arr.put(elem);
		}

		try {
			rootxaxis.put("ticks", arr);
			rootxaxis.put("tickFormatter", "string");
		} catch (Exception ex) {

		}
		return rootxaxis;
	}

	private JSONObject getShowTrue() {
		JSONObject ret = new JSONObject();
		try {
			ret.put("show", true);
		} catch (Exception ex) {

		}
		return ret;
	}

	private JSONObject getShowFalse() {
		JSONObject ret = new JSONObject();
		try {
			ret.put("show", false);
		} catch (Exception ex) {

		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapidandroid.activity.chart.IChartBroker#getVariables()
	 */

	public String[] getVariables() {
		// TODO Auto-generated method stub
		return variables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapidandroid.activity.chart.IChartBroker#setVariable(int)
	 */

	public void setVariable(int id) {
		this.variablechosen = id;

	}

	public void setRange(Date startTime, Date endTime) {
		mStartDate = startTime;
		mEndDate= endTime;
	}

	/* (non-Javadoc)
	 * @see org.rapidandroid.activity.chart.IChartBroker#finishGraph()
	 */
	public void finishGraph() {
		if(mProgress!= null) {
			mProgress.dismiss();
			mProgress= null;
		}
		
		mTitleHandler.post(mUpdateActivityTitle);
		
	}

}
