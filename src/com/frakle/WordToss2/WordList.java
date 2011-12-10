package com.frakle.WordToss2;


import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;

import com.threed.jpct.Logger;
import java.util.Stack;
import java.util.Arrays;
import android.app.Activity;

public class WordList {
	
	private static final int WORD_COUNT = 5;
	private Random rand = new Random();
	private Context appCon;
	private SharedPreferences preferences;
	private String fullList[];
//
	public String currentWord;
	public Stack<String> wordsStack = new Stack<String>();
	public Stack<Character> currentWordStack = new Stack<Character>();
	public Stack<Character> lettersFoundStack = new Stack<Character>();
	public Stack<Character> lettersRemainingStack = new Stack<Character>();
	
	public WordList(Context appCon){
		
		this.appCon = appCon;
		preferences = PreferenceManager.getDefaultSharedPreferences(appCon);
		
		loadWords(preferences.getString("wordLanguages", "english").toLowerCase());
		
		// Fetch a random word...
		currentWord = fullList[rand.nextInt(fullList.length)];
		//Generate our first word...
		char[] firstWord = currentWord.toCharArray();
		for(int i = 0;i<firstWord.length;i++){
			currentWordStack.add(firstWord[i]);
		}
		
		//Since we're constructing so they haven't found any letters yet...
		lettersRemainingStack = (Stack<Character>) currentWordStack.clone();
		
		//Generate a list of words to follow...
		for(int i = 0;i<WORD_COUNT;i++){
			wordsStack.push(fullList[rand.nextInt(fullList.length)]);
		}
	}
	

	private void loadWords(String languageFile) {
		try {
			Logger.log("LOADING LANGUAGE FILE: "+languageFile);
			InputStream input = appCon.getAssets().open(languageFile+"_words.txt");
			int size = input.available();
			byte[] buffer = new byte[size];
			input.read(buffer);
			input.close();
			setFullList(new String(buffer).split(","));
		} catch (IOException e) {
			setFullList(new String [] {"SOMETHING","WENT","HORRIBLY","WRONG"});
			e.printStackTrace();
			
		}
	}

	public boolean restack(){
		//Logger.log("Restacking");
		//Logger.log("wordsStack1: "+Arrays.toString(wordsStack.toArray()));
		// push a new word on to the words stack...
		wordsStack.add(fullList[rand.nextInt(fullList.length)]);
		//Logger.log("wordsStack1: "+Arrays.toString(wordsStack.toArray()));
		// pop the next word off the stack...
		currentWord = wordsStack.firstElement();
		wordsStack.remove(0);
		
		//Logger.log("wordsStack1: "+Arrays.toString(wordsStack.toArray()));
		// clear the current word
		currentWordStack.clear();
		
		//refill currentWordStack
		for(int i = 0;i<currentWord.length();i++){
			currentWordStack.add(currentWord.charAt(i));
		}

		//reset found ... 
		lettersFoundStack.clear();
		
		//copy currentWordStack in to lettersRemainingStack
		lettersRemainingStack = (Stack<Character>) currentWordStack.clone();
		
		Wordtoss2Game.updateWordList();
		return true;
	}
	public boolean checkLetter(char letter){
		if(lettersRemainingStack.contains(letter)){
			lettersRemainingStack.remove(lettersRemainingStack.indexOf(letter));
			lettersFoundStack.push(letter);
			//Logger.log("CurWordStack: "+Arrays.toString(currentWordStack.toArray()));
			//Logger.log("LetRemStack: "+Arrays.toString(lettersRemainingStack.toArray()));
			//Logger.log("LetFndStack: "+Arrays.toString(lettersFoundStack.toArray()));
			return true;
		}
			
		return false;
	}
	
	public String[] getFullList() {
		return fullList;
	}

	public void setFullList(String[] fullList) {
		this.fullList = fullList;
	}
}
