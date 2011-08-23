package com.frakle.WordToss2;

import com.frakle.WordToss2.Cloud;
import com.frakle.WordToss2.WordList;

import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;



import com.threed.jpct.Logger;
import com.threed.jpct.TextureManager;

public class Wordtoss2Game extends Activity {
    /** Called when the activity is first created. */
 
	private static Wordtoss2Game master = null;
	private static ArrayAdapter<String> wlAdapter;
	
	private GLSurfaceView mGLView;
	private ListView wlView;
	private MainRenderer renderer = null;
	private Cloud cloud = new Cloud();
	private WordList wordList = new WordList();
	private float xpos = -1;
	private float ypos = -1;
	private int gameLength;
	private GameTimer gTimer;
	public boolean GAME_RUNNING = false;
	private static int GAME_TIME;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");
		
		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Bundle extras = getIntent().getExtras();
		gameLength = extras.getInt("gameLength");
		
		Logger.log("Game Length: "+gameLength);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		mGLView = (GLSurfaceView) findViewById(R.id.cloudView);
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
		
		//setup renderer/cloud
		
		renderer = new MainRenderer();
		renderer.c = cloud;
		renderer.wl = wordList;
		mGLView.setRenderer(renderer);
		
		//setup wordList
		wlView = (ListView) findViewById(R.id.wordListView);
		wlAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,wordList.wordsStack);
		//wlView.layout();
		wlView.setAdapter(wlAdapter);
		wlView.setFocusable(false);
		wlView.setItemsCanFocus(false);
		gTimer = new GameTimer((60000*gameLength),10000);
		GAME_RUNNING=true;
		gTimer.start();
		
		//wlAdapter.
		//addContentView(wlView, new LayoutParams(100,100));
		//setContentView(mGLView);
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
		if(GAME_RUNNING){
			//Game Interaction
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
		
					cloud.rotate(yd / -100f,xd / -100f);
					//renderer.touchTurnUp = ;
					return true;
				}
			}
	
			try {
				Thread.sleep(15);
			} catch (Exception e) {
				// No need for this...
			}
		}
		
	

		return super.onTouchEvent(me);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, 0, 0,"New Cloud");	    
	    menu.add(0,1,0,"Shuffle");
	    return true;
	}
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case 0:
		        cloud.newSeededCloud(renderer.world, wordList.currentWordStack);
		        Logger.log(renderer.wl.toString());
		        return true;
		    case 1:
		    	cloud.shuffleLetters();
		    	return true;
	    }
	    return true;
	}
	
	public static void updateWordList() {
		wlAdapter.notifyDataSetChanged();
	}
	
	
	protected boolean isFullscreenOpaque() {
		return true;
	}
	
	
	public void gameFinish(){
		Logger.log("Stopping game interaction...");
		GAME_RUNNING = false;
		//TO-DO: Write code for scoring
		Logger.log("Running Cleanup...");
		TextureManager.getInstance().flush();
		finish();
	}
	
	public class GameTimer extends CountDownTimer{
        public GameTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            }

        @Override
        public void onFinish() {
        	gameFinish();
        }

        @Override
        public void onTick(long millisUntilFinished) {
        	Logger.log(""+(int) (millisUntilFinished/100));
        	Wordtoss2Game.GAME_TIME = (int) (millisUntilFinished/100);

        }
    }
	
}