package com.frakle.WordToss2;

import java.lang.reflect.Field;

import android.app.Activity;
//import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
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
	
	public void showHighscores(View view){
		Intent highscoresIntent = new Intent(this, Wordtoss2Highscores.class);
		highscoresIntent.putExtra("gameType","NO_GAME");
		highscoresIntent.putExtra("gameScore",0);
		this.startActivity(highscoresIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0,"Quit");
		menu.add(0, 1, 0,"Settings");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			finish();
		case 1:
			startActivity(new Intent(this, Wordtoss2Settings.class));
		}
		return true;
	}

	protected boolean isFullscreenOpaque() {
		return true;
	}

}