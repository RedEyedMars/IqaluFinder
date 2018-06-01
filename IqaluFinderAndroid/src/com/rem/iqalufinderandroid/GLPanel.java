package com.rem.iqalufinderandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;;

public class GLPanel extends GLSurfaceView implements android.view.View.OnTouchListener {

	private GLRenderer renderer;
	private ScaleGestureDetector mScaleDetector;
	private GestureDetector  mDoubleTapDetector;

	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId;
	private float mLastTouchY;
	private float mLastTouchX;


	public GLPanel(Context context, AttributeSet set) {
		super(context,set);
		setEGLConfigChooser(false);
		setEGLContextClientVersion(2);
		getHolder().setFixedSize(600, 800);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mDoubleTapDetector = new GestureDetector(context, new DoubleTapListener());
	}
	public GLPanel(Context context) {
		super(context);
		setEGLConfigChooser(false);
		setEGLContextClientVersion(2);
		getHolder().setFixedSize(600, 800);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}
	public void setRenderer(GLRenderer renderer){
		super.setRenderer(renderer);
		this.renderer = renderer;
		this.setOnTouchListener(this);
	}

	@Override
	public boolean performClick(){
		return super.performClick();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		mScaleDetector.onTouchEvent(ev);
		if(!mScaleDetector.isInProgress()){
			mDoubleTapDetector.onTouchEvent(ev);
		}
		final int action = ev.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();

			mLastTouchX = x;
			mLastTouchY = y;
			mActivePointerId = ev.getPointerId(0);
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = ev.findPointerIndex(mActivePointerId);
			final float x = ev.getX(pointerIndex);
			final float y = ev.getY(pointerIndex);

			// Only move if the ScaleGestureDetector isn't processing a gesture.
			if (!mScaleDetector.isInProgress()) {
				renderer.move(x - mLastTouchX,y - mLastTouchY);
			}

			mLastTouchX = x;
			mLastTouchY = y;

			break;
		}

		case MotionEvent.ACTION_UP: {
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}

		case MotionEvent.ACTION_CANCEL: {
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
					>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
					final int pointerId = ev.getPointerId(pointerIndex);
					if (pointerId == mActivePointerId) {
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						mLastTouchX = ev.getX(newPointerIndex);
						mLastTouchY = ev.getY(newPointerIndex);
						mActivePointerId = ev.getPointerId(newPointerIndex);
					}
					break;
		}
		}

		return true;
	}
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			renderer.zoom(detector.getScaleFactor());
			return true;
		}
	}
	private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        @Override
        public void onLongPress(MotionEvent event){
           // renderer.clickMarker(event.getX(),event.getY());
        }
        @Override
        public boolean onDoubleTap(MotionEvent event) {
            //renderer.toggleMarkerSeeking(event.getX(),event.getY());
            return true;
        }
    }
}
