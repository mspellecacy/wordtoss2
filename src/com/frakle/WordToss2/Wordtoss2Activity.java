package com.frakle.WordToss2;

import java.io.InputStream;
import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.threed.jpct.Logger;

public class Wordtoss2Activity extends Activity {
    /** Called when the activity is first created. */
 
	private static Wordtoss2Activity master = null;
	private GLSurfaceView mGLView;
	private MainRenderer renderer = null;
	private float xpos = -1;
	private float ypos = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");
		
		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mGLView = new GLSurfaceView(getApplication());
        
		mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
				// back to Pixelflinger on some device (read: Samsung I7500)
				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});

		renderer = new MainRenderer();
		mGLView.setRenderer(renderer);
		setContentView(mGLView);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
	}

	protected void onStop() {
		super.onStop();
	}

	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean onTouchEvent(MotionEvent me) {
		boolean OBJECT_TOUCHED = false;
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			xpos = me.getX();
			ypos = me.getY();
			
			//check if we're touching a letter or dragging around
			if(renderer.objectTouched(xpos,ypos)){OBJECT_TOUCHED=true;}
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			xpos = -1;
			ypos = -1;
			return true;
		}

		if(OBJECT_TOUCHED){
			
			OBJECT_TOUCHED = false;
		}else{
			if (me.getAction() == MotionEvent.ACTION_MOVE) {
				float xd = me.getX() - xpos;
				float yd = me.getY() - ypos;
	
				xpos = me.getX();
				ypos = me.getY();
	
				renderer.touchTurn = xd / -100f;
				renderer.touchTurnUp = yd / -100f;
				return true;
			}
		}

		try {
			Thread.sleep(15);
		} catch (Exception e) {
			// No need for this...
		}

		return super.onTouchEvent(me);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, 0, 0,"New Cloud");	    
	    return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case 0:
		        renderer.NEW_CLOUD = true;
		        Logger.log(renderer.wl.toString());
		        return true;
	    }
	    return true;
	}
	protected boolean isFullscreenOpaque() {
		return true;
	}
	
	
}