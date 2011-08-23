package com.frakle.WordToss2;

import java.lang.reflect.Field;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;




import com.threed.jpct.Logger;

public class Wordtoss2Activity extends Activity {
    /** Called when the activity is first created. */
 
	private static Wordtoss2Activity master = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");
		
		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.greet_screen);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
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
	
	public void startGame(int gameTime){
		Intent gameIntent = new Intent(this, Wordtoss2Game.class);
		gameIntent.putExtra("gameLength",gameTime);
		this.startActivity(gameIntent);
	}
	public void twoMinuteGame(View view){
		startGame(2);
	}
	public void fiveMinuteGame(View view){
		startGame(5);
	}
	public void nineMinuteGame(View view){
		startGame(9);
	}
	public void endlessGame(View view){
		startGame(0);
	}
	public void exitGame(View view){
		finish();
	}
    
	protected boolean isFullscreenOpaque() {
		return true;
	}
	
}