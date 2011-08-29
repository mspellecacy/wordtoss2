package com.frakle.WordToss2;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.threed.jpct.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableRow.LayoutParams;

public class Wordtoss2Highscores extends Activity {

	private static Wordtoss2Highscores master = null;
	private static HighScoreManager hsManager;
	private static String gameType = null;
	private static int gameScore = 0;
	private RelativeLayout rl = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		
		Button saveBt = (Button) this.findViewById(R.id.save_button);
		EditText hsET = (EditText) this.findViewById(R.id.txt_name);
		TextView hsTV = (TextView) this.findViewById(R.id.gameTypeTitle);
		setContentView(R.layout.highscore_screen); 
		
		Logger.log(""+hsTV.getText());
		hsManager = new HighScoreManager(this);
		hsManager.debugScores();
		Bundle extras = getIntent().getExtras();
		try{

			gameType = extras.getString("gameType");
			gameScore = extras.getInt("gameScore");
		}catch(Exception e){ 
			// No passed in vars, just viewing highscores... 
		}
		Logger.log("--"+gameType+"--");
		/*
		if(gameType == "NO_GAME"){
			//hsTV.setText(gameType+"Game High Scores");
		}else{
			hsTV.setText("Select Game Type");
		}
		*/
		
		

		Logger.log("--"+gameType+"--");
		if(gameType == "NO_GAME"){
			hsET.setVisibility(View.GONE);
			saveBt.setVisibility(View.GONE);
			showScores(gameType);
		}
	}
	
	
	public void showScores(String displayType){
		TableLayout tl = (TableLayout) this.findViewById(R.id.highscoreAlertTL);
		List<String[]> scores = hsManager.getHighScores(10,displayType);
		
		LayoutParams rLP = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		rLP.weight = 0.04f;
		rLP.column = 1;
		LayoutParams sLP = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		sLP.weight = 0.32f;
		sLP.column = 2;
		LayoutParams nLP = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		nLP.weight = 0.32f;
		nLP.column = 3;
		LayoutParams dLP = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		dLP.weight = 0.32f;
		dLP.column = 4;

		int i=1;
		for (String[] entry : scores)
		{
			
			// Create a TableRow and give it an ID
			TableRow tr = new TableRow(this);
			tr.setId(100+i);  

			// Create a TextView to house the rank...
			TextView rankTV = new TextView(this);
			rankTV.setId(200+i);
			rankTV.setText(((Integer) i).toString());
			rankTV.setLayoutParams(rLP);
			tr.addView(rankTV);

			// Create a TextView to house the name
			TextView nameTV = new TextView(this);
			nameTV.setId(300+i);
			nameTV.setText(entry[0]);
			nameTV.setLayoutParams(nLP);
			tr.addView(nameTV);

			// Create a TextView to house the score
			TextView scoreTV = new TextView(this);
			scoreTV.setId(400+i);
			scoreTV.setText(entry[1]);
			scoreTV.setLayoutParams(sLP);
			tr.addView(scoreTV);

			// Create a TextView to house the date
			TextView dateTV = new TextView(this);
			dateTV.setId(500+i);
			dateTV.setText(entry[2]);
			dateTV.setLayoutParams(dLP);
			tr.addView(dateTV);

			// Add the TableRow to the TableLayout
			tl.addView(tr);
			i++;
		}
	}
	
	public void saveHighscore(View view){
		Logger.log("Trying save score...");
		EditText hsET = (EditText) this.findViewById(R.id.txt_name);
		Button saveBt = (Button) this.findViewById(R.id.save_button);
		String curName = hsET.getText().toString();
		
		if(curName == "")
			curName = "Anonymous";
		
		hsET.setVisibility(View.GONE);
		saveBt.setVisibility(View.GONE);
		hsManager.addScore(new String[] {curName,Integer.toString(gameScore),gameType});
		showScores(gameType);
	}
	


	public void closeHighscore(View view){
		finish();
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
			toReturn = "NO_GAME";
		}

		return toReturn;
	}
	
}
