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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Logger.log("onCreate");

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.highscore_screen);
		
		Button saveBt = (Button) this.findViewById(R.id.save_button);
		EditText hsET = (EditText) this.findViewById(R.id.txt_name);
		TextView hsTV = (TextView) this.findViewById(R.id.gameTypeTitle);

		
		
		//hsTV.setText("TITS");
		//Logger.log(""+hsTV.getText());
		hsManager = new HighScoreManager(this);
		hsManager.debugScores();
		Bundle extras = getIntent().getExtras();
		try{

			gameType = extras.getString("gameType");
			gameScore = extras.getInt("gameScore");
		}catch(Exception e){ 
			// No passed in vars, just viewing highscores... 
		}
		
		if(gameType.equals("NO_GAME")){
			
			Logger.log("No gameType...");
			hsTV.setText("Select Game Type");
			//hsRL.removeAllViewsInLayout();
			hsET.setVisibility(View.GONE);
			saveBt.setVisibility(View.GONE);
			//Gen gameType buttons so they can see high scores for everything.
			generateGameTypeButtons();
			
		} else {
			hsTV.setText(gameType+" Game High Scores");
			showScores(gameType);
		}
		
	}
	
	
	private void generateGameTypeButtons() {
		List<String> mGameTypes = hsManager.getGameTypes();
		LinearLayout hsLL = (LinearLayout) this.findViewById(R.id.highscore_entry);
		for(final String mGameType : mGameTypes){
			Button mGameTypeButton = new Button(this);
			mGameTypeButton.setText(mGameType+" Scores");
			mGameTypeButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Logger.log(mGameType);
					showScores(mGameType);
					
				}
				
			});
			hsLL.addView(mGameTypeButton);
		}
		
	}


	public void showScores(String displayType){
		
		TableLayout tl = (TableLayout) this.findViewById(R.id.highscoreAlertTL);
		TextView hsTV = (TextView) this.findViewById(R.id.gameTypeTitle);
		List<HighScore> scores = hsManager.getHighScores(8,displayType);
		
		hsTV.setText(displayType+" Game High Scores");
		
		tl.removeAllViewsInLayout();
		
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

		
		TableRow headerTR = new TableRow(this);
		headerTR.setId(001);
		//rank header.
		TextView rankHeaderTV = new TextView(this);
		rankHeaderTV.setId(200);
		rankHeaderTV.setText("#");
		rankHeaderTV.setLayoutParams(rLP);
		headerTR.addView(rankHeaderTV);
		//name header.
		TextView nameHeaderTV = new TextView(this);
		nameHeaderTV.setId(300);
		nameHeaderTV.setText("Name");
		nameHeaderTV.setLayoutParams(nLP);
		headerTR.addView(nameHeaderTV);
		//score header.
		TextView scoreHeaderTV = new TextView(this);
		scoreHeaderTV.setId(400);
		scoreHeaderTV.setText("Score");
		scoreHeaderTV.setLayoutParams(sLP);
		headerTR.addView(scoreHeaderTV);
		//date header.
		TextView dateHeaderTV = new TextView(this);
		dateHeaderTV.setId(500);
		dateHeaderTV.setText("Date");
		dateHeaderTV.setLayoutParams(dLP);
		headerTR.addView(dateHeaderTV);
		
		//Add it to the top...
		tl.addView(headerTR);
		
		int i=1;
		for (HighScore hs : scores)
		{
			
			// Create a TableRow and give it an ID
			TableRow tr = new TableRow(this);
			tr.setId(100+i);  

			// Create a TextView to hold the rank...
			TextView rankTV = new TextView(this);
			rankTV.setId(200+i);
			rankTV.setText(((Integer) i).toString());
			rankTV.setLayoutParams(rLP);
			tr.addView(rankTV);

			// Create a TextView to hold the name
			TextView nameTV = new TextView(this);
			nameTV.setId(300+i);
			nameTV.setText(hs.getName());
			nameTV.setLayoutParams(nLP);
			tr.addView(nameTV);

			// Create a TextView to hold the score
			TextView scoreTV = new TextView(this);
			scoreTV.setId(400+i);
			scoreTV.setText(hs.getScore());
			scoreTV.setLayoutParams(sLP);
			tr.addView(scoreTV);

			// Create a TextView to hold the date
			TextView dateTV = new TextView(this);
			dateTV.setId(500+i);
			dateTV.setText(hs.getDate());
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
