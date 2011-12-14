package com.frakle.WordToss2;

import com.frakle.WordToss2.Cloud;
import com.frakle.WordToss2.WordList;
import com.frakle.WordToss2.SoundManager;

import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.app.Activity;
import android.app.AlertDialog;
//import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.threed.jpct.Logger;
import com.threed.jpct.TextureManager;

public class Wordtoss2Game extends Activity {
	/** Called when the activity is first created. */
	//
	private static Wordtoss2Game master = null;
	//private static ArrayAdapter<String> wlAdapter;
	private static SoundManager mSoundManager = new SoundManager();
	private GLSurfaceView mGLView;
	//private ListView wlView;
	private MainRenderer renderer = null;
	private Cloud cloud;
	private WordList wordList;
	private float xpos = -1;
	private float ypos = -1;
	private int gameLength;
	private String gameType;
	//
	public static int CURRENT_SCORE;
	public static GameTimer gTimer;
	public static boolean GAME_RUNNING = false;

	public static int GAME_TIME;
	public static long GAME_TIME_MIL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		CURRENT_SCORE = 0;
		setContentView(R.layout.main);
		cloud = new Cloud(this);
		Bundle extras = getIntent().getExtras();
		gameLength = extras.getInt("gameLength");
		gameType = getGameType(gameLength);
		
		mGLView = (GLSurfaceView) this.findViewById(R.id.cloudView);
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

		//setup wordlist...
		wordList = new WordList(this);
		
		//setup renderer/cloud
		renderer = new MainRenderer(this);
		renderer.c = cloud;
		renderer.wl = wordList;
		mGLView.setRenderer(renderer);

		//setup wordList
		/**
		wlView = (ListView) findViewById(R.id.wordListView);
		wlAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,wordList.wordsStack);
		wlView.layout();
		wlView.setAdapter(wlAdapter);
		wlView.setFocusable(false);
		wlView.setItemsCanFocus(false);
		**/
		
		//For Testing give us very short games...
		//gTimer = new GameTimer(5000,1);
		GAME_TIME_MIL = 60000*gameLength;
		gTimer = new GameTimer(GAME_TIME_MIL,1);
		

		gTimer = new GameTimer((60000*gameLength),1);

		GAME_RUNNING=true;
		
		//Setup Sounds...
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mSoundManager.initSounds(this);
        mSoundManager.addSound(1, R.raw.letter_hit);
        mSoundManager.addSound(2, R.raw.letter_miss);
        mSoundManager.addSound(3, R.raw.word_complete);
        mSoundManager.addSound(4, R.raw.word_complete2);
		
	}

	private String getGameType(int gameLen) {
		String toReturn;
		switch (gameLen){
		case 0:
			toReturn = "Endless";
			break;
		case 2:
			toReturn = "2 Minute";
			break;
		case 5:
			toReturn = "5 Minute";
			break;
		case 9:
			toReturn = "9 Minute";
			break;
		default:
			toReturn = "FailBoat";
		}
		return toReturn;
	}

	@Override
	protected void onPause() {
		Logger.log("onPause'd");
		gTimer.cancel();
		GAME_RUNNING = false;
		renderer.onPause();
		mGLView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		Logger.log("onResume'd");
		gTimer = new GameTimer(GAME_TIME_MIL,1);
		GAME_RUNNING = true;
		mGLView.onResume();
		super.onResume();
		//
	}

	protected void onStop() {
		Logger.log("onStop'd");
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			Logger.log("Running Cleanup...");
			TextureManager.getInstance().flush();
			gTimer.cancel();
			GAME_RUNNING = false;
			
			renderer.stop();
			finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0,"New Cloud");	    
		menu.add(0,1,0,"Shuffle");
		menu.add(0,2,0,"Quit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			cloud.newSeededCloud(renderer.world, wordList.currentWordStack);
			//Logger.log(renderer.wl.toString());
			return true;
		case 1:
			cloud.shuffleLetters();
			return true;
		case 2:
			finish();
			return true;
		}
		return true;
	}

	private void gameRestart() {
		CURRENT_SCORE = 0;
		wordList.restack();
		cloud.newSeededCloud(renderer.world, wordList.currentWordStack);
		//For testing we want very short games...
		//gTimer = new GameTimer(20000,1000);
		gTimer = new GameTimer((60000*gameLength),1);
		GAME_RUNNING=true;
		gTimer.start();
	}

	public static void updateWordList() {
		//wlAdapter.notifyDataSetChanged();
	}


	protected boolean isFullscreenOpaque() {
		return true;
	}


	public static String getHumanTime()
	{
		long longVal = ((Integer) GAME_TIME).longValue();
		int hours = (int) longVal / 3600;
		int remainder = (int) longVal - hours * 3600;
		int mins = remainder / 60;
		remainder = remainder - mins * 60;
		int secs = remainder;

		int[] ints = {hours , mins , secs};

		//Lazy Fix, but whatever.
		if(ints[2] < 10){		 
			return ""+ints[1]+":0"+ints[2];
		}else{
			return ""+ints[1]+":"+ints[2];
		}
	}

	public void displayHighscore(){
		Intent highscoresIntent = new Intent(this, Wordtoss2Highscores.class);
		highscoresIntent.putExtra("gameType",gameType);
		highscoresIntent.putExtra("gameScore",CURRENT_SCORE);
		this.startActivity(highscoresIntent);
	}
	public void gameFinish(){
		Logger.log("Stopping game interaction...");
		GAME_RUNNING = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(Wordtoss2Game.this);
		builder.setCancelable(false);
		builder.setTitle("Game Over! Restart?");
		builder.setInverseBackgroundForced(true);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Logger.log("Running Cleanup...");
				gameRestart();
				dialog.dismiss();
			}

		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Logger.log("Running Cleanup...");
				TextureManager.getInstance().flush();
				finish();
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
		
		//push them to the highscores page real quick....
		displayHighscore();
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
			Wordtoss2Game.GAME_TIME = (int) (millisUntilFinished/1000);	
			Wordtoss2Game.GAME_TIME_MIL = millisUntilFinished;
		}

	}

	public static void addScore(String currentWord, int elapsedTime) {
		int toAdd = 0;
		int wordLen = currentWord.length();
		int eTimeSeconds = (elapsedTime/1000);
		
		int wordValue = wordLen * 100;
		//int finishedWordBonus = 1000;
		int longWordBonus = 1000;
		int wordSpeedBonus = 1000;
		
		
		if(wordLen>7){
			toAdd += longWordBonus;
		}
			
		
		if(eTimeSeconds<10)
			toAdd += wordSpeedBonus;
		
		toAdd += wordValue;
		
		CURRENT_SCORE = CURRENT_SCORE+toAdd;
	}
	

	@Override
	public void onBackPressed() {
		gTimer.cancel();
		finish();
	}

	public static void playSound(int i) {
		mSoundManager.playSound(i);
		
	}
}




















