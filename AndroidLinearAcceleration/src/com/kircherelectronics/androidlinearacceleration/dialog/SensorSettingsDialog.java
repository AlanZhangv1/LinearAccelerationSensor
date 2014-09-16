package com.kircherelectronics.androidlinearacceleration.dialog;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.kircherelectronics.androidlinearacceleration.R;
import com.kircherelectronics.androidlinearacceleration.plot.PlotPrefCallback;
import com.kircherelectronics.androidlinearacceleration.prefs.PrefUtils;

/*
 * Low-Pass Linear Acceleration
 * Copyright (C) 2013, Kaleb Kircher - Kircher Engineering, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class SensorSettingsDialog extends Dialog implements
		SensorEventListener, OnItemSelectedListener, Runnable
{
	private static final String tag = SensorSettingsDialog.class
			.getSimpleName();

	private float timestamp = System.nanoTime();
	private float startTime = 0;
	private float dt = 0;
	private float sensorHz = 0;

	private int count = 0;

	private Button buttonAccept;

	private DecimalFormat df;

	private final PlotPrefCallback callback;

	// Sensor manager to access the accelerometer sensor
	private SensorManager sensorManager;

	private String frequencySelection;

	private Spinner frequencySpinner;

	private TextView sensorFrequencyTextView;

	// Handler for the UI plots so everything plots smoothly
	private Handler handler;

	public SensorSettingsDialog(Context context, PlotPrefCallback callback)
	{
		super(context);

		this.callback = callback;

		this.setTitle("Sensor Frequency");

		df = new DecimalFormat("#.##");

		// Get the sensor manager ready
		sensorManager = (SensorManager) this.getContext()
				.getSystemService(Context.SENSOR_SERVICE);

		LayoutInflater inflater = getLayoutInflater();

		View settingsView = inflater.inflate(R.layout.sensor_dialog_view, null,
				false);

		sensorFrequencyTextView = (TextView) settingsView
				.findViewById(R.id.value_sensor_frequency);

		frequencySpinner = (Spinner) settingsView
				.findViewById(R.id.sensor_frequency_spinner);

		frequencySpinner.setOnItemSelectedListener(this);

		buttonAccept = (Button) settingsView.findViewById(R.id.button_accept);

		buttonAccept.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				SensorSettingsDialog.this.dismiss();
			}
		});

		this.setContentView(settingsView);
	}

	@Override
	public void onStop()
	{
		super.onStop();

		sensorManager.unregisterListener(this);
		writeSensorPrefs();
		handler.removeCallbacks(this);

		handler = null;
	}

	public void onStart()
	{
		super.onStart();

		readSensorPrefs();

		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_SLOW))
		{
			frequencySpinner.setSelection(0);
			setSensorDelay(0);
		}

		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_MEDIUM))
		{
			frequencySpinner.setSelection(1);
			setSensorDelay(1);
		}

		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_FAST))
		{
			frequencySpinner.setSelection(2);
			setSensorDelay(2);
		}

		handler = new Handler();

		handler.post(this);
	}

	/**
	 * Read in the current user preferences.
	 */
	private void readSensorPrefs()
	{
		SharedPreferences prefs = this.getContext().getSharedPreferences(
				PrefUtils.SENSOR_PREFS, Activity.MODE_PRIVATE);

		this.frequencySelection = prefs.getString(
				PrefUtils.SENSOR_FREQUENCY_PREF,
				PrefUtils.SENSOR_FREQUENCY_FAST);
	}

	/**
	 * Write the preferences.
	 */
	private void writeSensorPrefs()
	{
		// Write out the offsets to the user preferences.
		SharedPreferences.Editor editor = this
				.getContext()
				.getSharedPreferences(PrefUtils.SENSOR_PREFS,
						Activity.MODE_PRIVATE).edit();

		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_SLOW))
		{
			editor.putString(PrefUtils.SENSOR_FREQUENCY_PREF,
					PrefUtils.SENSOR_FREQUENCY_SLOW);
		}

		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_MEDIUM))
		{
			editor.putString(PrefUtils.SENSOR_FREQUENCY_PREF,
					PrefUtils.SENSOR_FREQUENCY_MEDIUM);
		}

		if (frequencySelection.equals(PrefUtils.SENSOR_FREQUENCY_FAST))
		{
			editor.putString(PrefUtils.SENSOR_FREQUENCY_PREF,
					PrefUtils.SENSOR_FREQUENCY_FAST);
		}

		editor.commit();
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		// Initialize the start time.
		if (startTime == 0)
		{
			startTime = System.nanoTime();
		}

		timestamp = System.nanoTime();

		// Find the sample period (between updates) and convert from
		// nanoseconds to seconds. Note that the sensor delivery rates can
		// individually vary by a relatively large time frame, so we use an
		// averaging technique with the number of sensor updates to
		// determine the delivery rate.
		dt = 1 / (count++ / ((timestamp - startTime) / 1000000000.0f));

		sensorHz = 1 / dt;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id)
	{
		setSensorDelay(position);
		
		frequencySelection = frequencySpinner.getSelectedItem()
				.toString();

		writeSensorPrefs();

		SensorSettingsDialog.this.callback.checkPlotPrefs();
	}

	private void setSensorDelay(int position)
	{
		switch (position)
		{
		case 0:

			this.frequencySelection = PrefUtils.SENSOR_FREQUENCY_SLOW;

			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

			// Register for sensor updates.
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
			break;
		case 1:

			this.frequencySelection = PrefUtils.SENSOR_FREQUENCY_MEDIUM;

			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

			// Register for sensor updates.
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_GAME);
			break;
		case 2:

			this.frequencySelection = PrefUtils.SENSOR_FREQUENCY_FAST;

			sensorManager.unregisterListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));

			// Register for sensor updates.
			sensorManager.registerListener(this,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_FASTEST);
			break;

		}

		// Reset the frequency counter
		count = 0;
		startTime = 0;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void run()
	{
		handler.postDelayed(this, 100);
		sensorFrequencyTextView.setText(df.format(sensorHz));
	}
}
