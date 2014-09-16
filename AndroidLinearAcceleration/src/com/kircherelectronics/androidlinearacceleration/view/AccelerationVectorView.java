package com.kircherelectronics.androidlinearacceleration.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

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

/**
 * Draws an analog gauge for displaying acceleration measurements in two-space
 * from device sensors.
 * 
 * Note that after Android 4.0 TextureView exists, as does SurfaceView for
 * Android 3.0 which won't hog the UI thread like View will. This should only be
 * used with devices or certain libraries that require View.
 * 
 * @author Kaleb
 * @version %I%, %G%
 * @see http://developer.android.com/reference/android/view/View.html
 */
public final class AccelerationVectorView extends View
{

	/*
	 * Developer Note: In the interest of keeping everything as fast as
	 * possible, only the measurements are redrawn, the gauge background and
	 * display information are drawn once per device orientation and then cached
	 * so they can be reused. All allocation and reclaiming of memory should
	 * occur before and after the handler is posted to the thread, but never
	 * while the thread is running. Allocation and reclamation of memory while
	 * the handler is posted to the thread will cause the GC to run, resulting
	 * in long delays (up to 600ms) while the GC cleans up memory. The frame
	 * rate to drop dramatically if the GC is running often, so try to keep it
	 * happy and out of the way.
	 * 
	 * Avoid iterators, Set or Map collections (use SparseArray), + to
	 * concatenate Strings (use StringBuffers) and above all else boxed
	 * primitives (Integer, Double, Float, etc).
	 */

	/*
	 * Developer Note: There are some things to keep in mind when it comes to
	 * Android and hardware acceleration. What we see in Android 4.0 is “full”
	 * hardware acceleration. All UI elements in windows, and third-party apps
	 * will have access to the GPU for rendering. Android 3.0 had the same
	 * system, but now developers will be able to specifically target Android
	 * 4.0 with hardware acceleration. Google is encouraging developers to
	 * update apps to be fully-compatible with this system by adding the
	 * hardware acceleration tag in an app’s manifest. Android has always used
	 * some hardware accelerated drawing.
	 * 
	 * Since before 1.0 all window compositing to the display has been done with
	 * hardware. "Full" hardware accelerated drawing within a window was added
	 * in Android 3.0. The implementation in Android 4.0 is not any more full
	 * than in 3.0. Starting with 3.0, if you set the flag in your app saying
	 * that hardware accelerated drawing is allowed, then all drawing to the
	 * application’s windows will be done with the GPU. The main change in this
	 * regard in Android 4.0 is that now apps that are explicitly targeting 4.0
	 * or higher will have acceleration enabled by default rather than having to
	 * put android:handwareAccelerated="true" in their manifest. (And the reason
	 * this isn’t just turned on for all existing applications is that some
	 * types of drawing operations can’t be supported well in hardware and it
	 * also impacts the behavior when an application asks to have a part of its
	 * UI updated. Forcing hardware accelerated drawing upon existing apps will
	 * break a significant number of them, from subtly to significantly.)
	 */

	private static final String tag = AccelerationVectorView.class
			.getSimpleName();

	// holds the cached static part
	private Bitmap background;

	private Paint backgroundPaint;
	private Paint axisPaint;

	private Paint yAxisLengthPaint;
	private Paint xAxisLengthPaint;

	private Paint vectorPaint;

	private RectF rimRect;

	private float x;
	private float y;

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 */
	public AccelerationVectorView(Context context)
	{
		super(context);
		init();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 */
	public AccelerationVectorView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	/**
	 * Create a new instance.
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public AccelerationVectorView(Context context, AttributeSet attrs,
			int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Update the measurements for the point.
	 * 
	 * @param x
	 *            the x-axis
	 * @param y
	 *            the y-axis
	 * @param color
	 *            the color
	 */
	public void updatePoint(float x, float y)
	{

		// Bound the y-axis to +/- the gravity of earth
		if (x > SensorManager.GRAVITY_EARTH)
		{
			x = SensorManager.GRAVITY_EARTH;
		}
		if (x < -SensorManager.GRAVITY_EARTH)
		{
			x = -SensorManager.GRAVITY_EARTH;
		}

		// Divide by the length of our axis.
		this.x = (x / SensorManager.GRAVITY_EARTH) * 0.4f;

		// Bound the y-axis to +/- the gravity of earth
		if (y > SensorManager.GRAVITY_EARTH)
		{
			y = SensorManager.GRAVITY_EARTH;
		}
		if (y < -SensorManager.GRAVITY_EARTH)
		{
			y = -SensorManager.GRAVITY_EARTH;
		}

		// Normalize y to 1 and then scale to half the length of the y-axis.
		this.y = (y / SensorManager.GRAVITY_EARTH) * 0.4f;

		this.invalidate();
	}

	/**
	 * Initialize the members of the instance.
	 */
	private void init()
	{
		initDrawingTools();
	}

	/**
	 * Initialize the drawing related members of the instance.
	 */
	private void initDrawingTools()
	{
		// Leave a little bit of space between the side of the screen and the
		// rectangle...
		rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);

		// the linear gradient is a bit skewed for realism
		axisPaint = new Paint();
		axisPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		axisPaint.setStrokeWidth(0.01f);
		axisPaint.setColor(Color.WHITE);
		axisPaint.setStyle(Paint.Style.STROKE);

		// the linear gradient is a bit skewed for realism
		vectorPaint = new Paint();
		vectorPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		vectorPaint.setStrokeWidth(0.01f);
		vectorPaint.setColor(Color.RED);
		vectorPaint.setStyle(Paint.Style.STROKE);

		// the linear gradient is a bit skewed for realism
		yAxisLengthPaint = new Paint();
		yAxisLengthPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		yAxisLengthPaint.setStrokeWidth(0.01f);
		yAxisLengthPaint.setColor(Color.GREEN);
		yAxisLengthPaint.setStyle(Paint.Style.STROKE);

		// the linear gradient is a bit skewed for realism
		xAxisLengthPaint = new Paint();
		xAxisLengthPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		xAxisLengthPaint.setStrokeWidth(0.01f);
		xAxisLengthPaint.setColor(Color.BLUE);
		xAxisLengthPaint.setStyle(Paint.Style.STROKE);

		backgroundPaint = new Paint();
		backgroundPaint.setFilterBitmap(true);
	}

	/**
	 * Measure the device screen size to scale the canvas correctly.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);

		int chosenDimension = Math.min(chosenWidth, chosenHeight);

		setMeasuredDimension(chosenDimension, chosenDimension);
	}

	/**
	 * Indicate the desired canvas dimension.
	 * 
	 * @param mode
	 * @param size
	 * @return
	 */
	private int chooseDimension(int mode, int size)
	{
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY)
		{
			return size;
		}
		else
		{ // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		}
	}

	/**
	 * In case there is no size specified.
	 * 
	 * @return default preferred size.
	 */
	private int getPreferredSize()
	{
		return 300;
	}

	/**
	 * Draw the gauge.
	 * 
	 * @param canvas
	 */
	private void drawAxis(Canvas canvas)
	{
		// Draw the Y axis
		canvas.drawLine(rimRect.centerX(), rimRect.top, rimRect.centerX(),
				rimRect.bottom, axisPaint);

		// Draw the X axis
		canvas.drawLine(rimRect.left, rimRect.centerY(), rimRect.right,
				rimRect.centerY(), axisPaint);

		// Draw the Y axis arrow
		Path yArrowPath = new Path();
		yArrowPath.setFillType(FillType.EVEN_ODD);

		yArrowPath.moveTo(rimRect.centerX() - 0.002f, rimRect.top);
		yArrowPath.lineTo(rimRect.centerX() + 0.05f, rimRect.top + 0.05f);
		yArrowPath.moveTo(rimRect.centerX() + 0.002f, rimRect.top);
		yArrowPath.lineTo(rimRect.centerX() - 0.05f, rimRect.top + 0.05f);

		canvas.drawPath(yArrowPath, axisPaint);

		// Draw the Y axis arrow
		Path xArrowPath = new Path();
		xArrowPath.setFillType(FillType.EVEN_ODD);

		xArrowPath.moveTo(rimRect.right, rimRect.centerY() + 0.002f);
		xArrowPath.lineTo(rimRect.right - 0.05f, rimRect.centerY() - 0.05f);

		xArrowPath.moveTo(rimRect.right, rimRect.centerY() - 0.002f);
		xArrowPath.lineTo(rimRect.right - 0.05f, rimRect.centerY() + 0.05f);

		canvas.drawPath(xArrowPath, axisPaint);

	}

	/**
	 * Draw the background of the canvas.
	 * 
	 * @param canvas
	 */
	private void drawBackground(Canvas canvas)
	{
		// Use the cached background bitmap.
		if (background == null)
		{
			Log.w(tag, "Background not created");
		}
		else
		{
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		drawBackground(canvas);

		float scale = (float) getWidth();
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(scale, scale);

		drawAxisLength(canvas);
		drawVectorLength(canvas);

		canvas.restore();
	}

	private void drawVectorLength(Canvas canvas)
	{
		// Draw the vector.
		canvas.drawLine(rimRect.centerX(), rimRect.centerY(), rimRect.centerX()
				- this.x, rimRect.centerY() + this.y, vectorPaint);

		// Use the magnitude of the acceleration to determine the size of the
		// vectors arrow. We have to scale it up by 250% because we had
		// initially scaled it down by by 40% and we want to normalize to 1.
		float magnitude = (float) Math.sqrt(Math.pow(this.x, 2)
				+ Math.pow(this.y, 2)) * 2.5f;

		// Rotate the canvas so the arrows rotate with the vector. Note we have
		// to rotate by 90 degrees so 0 degrees is pointing in the positive Y
		// axis. We also change the rotation from counter clockwise to clockwise
		// with a negative out front. Also, note the atan2 produces a range from
		// 0 to 180 degrees and 0 to -180 degrees. I add 360 degrees and then
		// take mod 360 so the range is 0 to 360 degrees.
		canvas.rotate(
				(float) -((Math.toDegrees(Math.atan2(this.y, this.x)) + 450) % 360),
				rimRect.centerX() - this.x, rimRect.centerY() + this.y);

		// Draw the vector arrows. Note that the length of the arrows are scaled
		// by the magnitude.
		canvas.drawLine(rimRect.centerX() - this.x + 0.002f, rimRect.centerY()
				+ this.y, rimRect.centerX() - this.x - (0.05f * magnitude),
				rimRect.centerY() + this.y + (0.05f * magnitude), vectorPaint);

		canvas.drawLine(rimRect.centerX() - this.x - 0.002f, rimRect.centerY()
				+ this.y, rimRect.centerX() - this.x + (0.05f * magnitude),
				rimRect.centerY() + this.y + (0.05f * magnitude), vectorPaint);

		canvas.restore();
	}

	private void drawAxisLength(Canvas canvas)
	{
		// Draw the Y axis
		canvas.drawLine(rimRect.centerX(), rimRect.centerY(),
				rimRect.centerX(), rimRect.centerY() + this.y, yAxisLengthPaint);

		// Draw the X axis
		canvas.drawLine(rimRect.centerX(), rimRect.centerY(), rimRect.centerX()
				- this.x, rimRect.centerY(), xAxisLengthPaint);
	}

	/**
	 * Indicate the desired size of the canvas has changed.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Log.d(tag, "Size changed to " + w + "x" + h);

		regenerateBackground();
	}

	/**
	 * Regenerate the background image. This should only be called when the size
	 * of the screen has changed. The background will be cached and can be
	 * reused without needing to redraw it.
	 */
	private void regenerateBackground()
	{
		// free the old bitmap
		if (background != null)
		{
			background.recycle();
		}

		background = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas backgroundCanvas = new Canvas(background);
		float scale = (float) getWidth();
		backgroundCanvas.scale(scale, scale);

		drawAxis(backgroundCanvas);
	}
}
