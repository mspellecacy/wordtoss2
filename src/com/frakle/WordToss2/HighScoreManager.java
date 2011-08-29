package com.frakle.WordToss2;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.frakle.WordToss2.DataBaseHelper;
import com.threed.jpct.Logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class HighScoreManager {
	private DataBaseHelper dbHelp;
	private SQLiteDatabase myDB;
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_SCORE = "score";
	public static final String KEY_DATE = "date";
	public static final String KEY_GAMETYPE = "game_type";
	private String TABLE = "highscore";

	public HighScoreManager(Context context) {
		dbHelp = new DataBaseHelper(context);
		myDB = dbHelp.getWritableDatabase();
	}

	public List<String[]> getHighScores(Integer count, String gameType){
		List<String[]> toReturn = new LinkedList<String[]>();
		String WHERE_CLAUSE = KEY_GAMETYPE+"="+"'"+gameType+"'";
		String ORDER_CLAUSE = KEY_SCORE+" DESC";
		//Cursor cur = myDB.query(TABLE, new String[] { KEY_ROWID, KEY_NAME, KEY_SCORE, KEY_DATE, KEY_GAMETYPE }, WHERE_CLAUSE, null, null, null, ORDER_CLAUSE, count.toString());
		Cursor cur = myDB.rawQuery("select _id, name, score, date, game_type from highscore where game_type = \""+gameType+"\" ORDER BY score DESC", null);
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			Logger.log("Filling Score List<String[]>");
			Logger.log(Arrays.deepToString(new String[] {cur.getString(1),cur.getString(2),cur.getString(3)}));
			toReturn.add(new String[] {cur.getString(1),cur.getString(2),cur.getString(3)});
			cur.moveToNext();
		}
		cur.close();
		return toReturn;
	}

	public Cursor getHighScoresCursor(Integer count, String gameType){
		String WHERE_CLAUSE = KEY_GAMETYPE+"="+"\""+gameType+"\" ";
		String ORDER_CLAUSE = KEY_SCORE+" DESC";
		Cursor cur = myDB.query(TABLE, new String[] { KEY_ROWID, KEY_NAME, KEY_SCORE, KEY_DATE}, WHERE_CLAUSE, null, null, null, ORDER_CLAUSE, count.toString());
		cur.moveToFirst();
		return cur;
	}
	public int findScorePlacement(int curScore, String gameType){
		
		return curScore;

	}

	public List<String> getGameTypes(){
		
		List<String> toReturn = new LinkedList<String>();
		Cursor cur = myDB.query(true, TABLE, new String[] {"game_type"}, null, null, null, null, null, null);
		
		cur.moveToFirst();
		while (cur.isAfterLast() == false) {
			toReturn.add(cur.getString(0));
			cur.moveToNext();
		}
		cur.close();
		return toReturn;
	
	}
	
	public void addScore(String[] newEntry){
		Logger.log("Writing HighScore Entry");
		ContentValues cv = new ContentValues();
		cv.put(KEY_NAME, newEntry[0]);
		cv.put(KEY_SCORE, newEntry[1]);
		cv.put(KEY_GAMETYPE,newEntry[2]);
		cv.put(KEY_DATE, new SimpleDateFormat("yyyy/MMM/dd HH:mm").format(Calendar.getInstance().getTime()));
		myDB.insert(TABLE, null, cv);
	}

	public void debugScores() {
		List<String> game_types = getGameTypes();
		for(String gType : game_types){
			Logger.log("DEBUG: Dumping scores for "+gType+" gameType.");
			List<String[]> scores = getHighScores(10000,gType);
			for (String[] entry : scores){
			    Logger.log(Arrays.deepToString(entry));
			}
		}
	}
	
	public void purgeAllScores(){
		Logger.log("Purging All Highscores");
		myDB.delete(TABLE,null,null);
	}

}
