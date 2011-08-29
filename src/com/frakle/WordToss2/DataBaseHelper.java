package com.frakle.WordToss2;

import com.threed.jpct.Logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper{

	private static final String DATABASE_CREATE_HIGHSCORE = "CREATE TABLE highscore (date TEXT, name TEXT, score NUMERIC, game_type TEXT, _id integer primary key autoincrement);";
	private static final String DATABASE_CREATE_METADATA = "CREATE TABLE android_metadata ('locale' TEXT DEFAULT 'en_US');";
	private static final String DATABASE_INSERT_METADATA = "INSERT INTO android_metadata VALUES('en_US');";
	private static String DATABASE_NAME = "pickspell.db";
	private static final int DATABASE_VERSION = 1;

	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}	

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_METADATA);
		database.execSQL(DATABASE_INSERT_METADATA);
		database.execSQL(DATABASE_CREATE_HIGHSCORE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Logger.log(database.getPath());
		database.execSQL("DROP TABLE IF EXISTS highscore");
		onCreate(database);
	}

}