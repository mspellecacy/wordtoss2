package com.frakle.WordToss2;

public class HighScore {
	private String mName;
	private String mScore;
	private String mDate;
	
	public HighScore(String name, String score, String date){
		mName = name;
		mScore = score;
		mDate = date;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getName() {
		return mName;
	}

	public void setScore(String score) {
		mScore = score;
	}

	public String getScore() {
		return mScore;
	}

	public void setDate(String date) {
		mDate = date;
	}

	public String getDate() {
		return mDate;
	}
}
