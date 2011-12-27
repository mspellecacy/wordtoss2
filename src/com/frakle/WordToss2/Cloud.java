package com.frakle.WordToss2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Stack;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

import com.threed.jpct.GLSLShader;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.PolygonManager;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;

import com.frakle.WordToss2.AGLFont;

public class Cloud {
	
	private Random rand = new Random();
	private Resources res;
	private SharedPreferences preferences;
	private Context appCon;
	public Object3D cloud;
	public Object3D[] letters;
	char[] alphabet;
	

	public Cloud(Context appCon){
		this.appCon = appCon;
		this.res = appCon.getResources();
		preferences = PreferenceManager.getDefaultSharedPreferences(appCon);
		
		loadAlphabet(preferences.getString("wordLanguages","english").toLowerCase());
		
		/*
		Bitmap.Config config = Bitmap.Config.ARGB_8888; 

		Paint cPaint = new Paint(); 
		cPaint.setColor(Color.BLACK);
		
		Bitmap charImage = Bitmap.createBitmap(10, 10, config);
		Canvas canvas = new Canvas(charImage);
		
		canvas.drawColor(Color.BLACK);
		*/
		TextureManager.getInstance().addTexture("cTexture", new Texture(10,10,RGBColor.GREEN));
		//TextureManager.getInstance().addTexture("cTexture", new Texture(res.openRawResource(R.raw.paint)));
		//TextureManager.getInstance().addTexture("cTextureMap", new Texture(res.openRawResource(R.raw.ctexture_map)));
		//TextureManager.getInstance().getTexture("cTextureMap").setAsShadowMap(true);
		//255-165-0
		//TextureManager.getInstance().addTexture("cTexture", new Texture(10,10,new RGBColor(255,165,0)));
		//cloud = loadObject("tree2.3ds");
		cloud = Primitives.getSphere(20);
		
		//cloud = Primitives.getBox(25,1);
		cloud.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
		cloud.setTransparencyMode(Object3D.TRANSPARENCY_MODE_DEFAULT);
		cloud.setTransparency(100);
		cloud.setTexture("cTexture");
		cloud.calcTextureWrapSpherical();
		
    	cloud.setName("CenterCloud");
    	cloud.calcCenter();
    	cloud.strip();
    	cloud.build();
    	//cloud.setShader(centerShader());
		letters = new Object3D[15];
		
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		//paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextSize(50);
		paint.setTypeface(Typeface.create(Typeface.MONOSPACE,0));
		//paint.setARGB(0,255,255,0);
		

		for(int i = 0;i<alphabet.length;i++){
			if(!TextureManager.getInstance().containsTexture(""+alphabet[i]))
				Logger.log("LETTERTEXTURE: "+alphabet[i]);
				TextureManager.getInstance().addTexture(""+alphabet[i], new AGLFont(paint,alphabet[i]+"").pack.getTexture());
		}
		
		for(int i = 0;i<letters.length;i++){
			letters[i] = genLetter(i);
		}
	}

	//Returns an float[x,y,z] somewhere on the outer edge of an imaginary sphere
    public float[] generateVector(){

		float x = (float) (Math.random() - 0.5);
		float y = (float) (Math.random() - 0.5); 
		float z = (float) (Math.random() - 0.5);
		float k = (float) Math.sqrt(x*x + y*y + z*z);
		
		while (k < 0.2 || k > 0.3)
		{
			x = (float) (Math.random() - 0.5);
			y = (float) (Math.random() - 0.5);
			z = (float) (Math.random() - 0.5);
			k = (float) Math.sqrt(x*x + y*y + z*z);
		}
		
		float[] toReturn = {((x/k)*30),((y/k)*30),((z/k)*30)};
		return toReturn;
    }
	
    public void rotate(float x, float y){
    
    	//Rotate the whole cloud by Y
		if (y != 0)
			cloud.rotateY(y);
		
		//rotate the whole cloud by X
		if (x != 0)
			cloud.rotateX(x);
    }
    
    public Object3D genLetter(int nameNumber, String aLetter){
    	String thisTexture = null;
    	
    	if(aLetter == null) {
	    	//Randomly Assign A Texture (Letter)
	    	thisTexture = ""+alphabet[rand.nextInt(25)];
    	}else{
    		//Assign a specific Texture (Letter)
    		thisTexture = ""+aLetter;
    	}
    	//Construct a name we can later pars
    	String thisName = thisTexture+"_"+nameNumber;
    	//Generate the object to attach all this stuff too
    	//Object3D thisLetter = Primitives.getPlane(1,10);
    	
    	//dumpObject(thisLetter);
    	Object3D thisLetter = getLetterPlane();
    	thisLetter.addParent(cloud);
    	thisLetter.setTexture(thisTexture);
    	thisLetter.setName(thisName);
    	thisLetter.setTransparencyMode(Object3D.TRANSPARENCY_MODE_DEFAULT);
    	thisLetter.setTransparency(100);
    	thisLetter.align(cloud);
    	thisLetter.setOrigin(new SimpleVector(generateVector()));
    	thisLetter.setBillboarding(true);
    	thisLetter.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
    	thisLetter.calcCenter();
    	thisLetter.strip();
    	thisLetter.build();
    	
    	return thisLetter;
    }
    
    public Object3D genLetter(int nameNumber){
    	return genLetter(nameNumber, null);
    }
    
    public void newCloud(World world){
    	
    	//Purge all the letters from the world.
    	for(int i=0;i<letters.length;i++){
			world.removeObject(world.getObjectByName(letters[i].getName()));	
		}
    	
    	//run through all the letter objects and Remove/Replace them.
		for(int i = 0;i<letters.length;i++){
			cloud.removeChild(letters[i]);
			letters[i] = genLetter(i);
		}
		
		//Add all the letters back in to the world, all fresh new and shiny!
		world.addObjects(letters);
		
    	Logger.log("New Cloud!");
    }
    
	public void newSeededCloud(World world, Stack<Character> l) {
		//int seedCount = rand.nextInt(l.size()+1);
		int seedCount = l.size();
		
    	//Purge all the letters from the world object
    	for(int i=0;i<letters.length;i++){
			world.removeObject(world.getObjectByName(letters[i].getName()));	
		}
    	
    	//run through all the remaining letter objects in the cloud and Remove/Replace them.
		for(int i = 0;i<letters.length;i++){
			cloud.removeChild(letters[i]);
		}
		
		//loop over the first X letters giving seeded values
		for(int i=0;i<seedCount;i++){
			letters[i] = genLetter(i,""+l.elementAt(i));
		}
		for(int i=seedCount;i<letters.length;i++){
			letters[i] = genLetter(i);
		}
		//Add all the letters back in to the world, all fresh new and shiny!
		world.addObjects(letters);
	}
    
    public boolean letterClicked(String letterName, World world){
    	
    	//replaceLetter(letterName, world);
    	return true;
    }
    
    public Stack<Character> currentLetters(){
    	Stack<Character> toReturn = new Stack<Character>();
    	
    	for(int i =0;i<this.letters.length;i++){
    		toReturn.add(letters[i].getName().charAt(0));
    	}
    	return toReturn;
    }

    public boolean containsAny(Stack<Character> toFind){
    	search:
			for(int i = 0;i<toFind.size();i++){
				if( currentLetters().contains(toFind.elementAt(i))){
					break search;
				}else{
					return false;
				}
			}
		return true;
    }
    
    public void addLetter(int num, World world){
    	letters[num] = genLetter(num);
    	world.addObject(letters[num]);
    }
    
    public void addLetter(int num, World world, String aLetter){
    	letters[num] = genLetter(num, aLetter);
    	world.addObject(letters[num]);
    }
    
    public void removeLetter(String letterName, World world){
    	
    	try{
	    	int num = Integer.parseInt(letterName.substring(letterName.lastIndexOf("_")+1));
	    	world.removeObject(world.getObjectByName(letterName));
	    	letters[num].clearObject();
			cloud.removeChild(letters[num]);
    	}catch(Exception e){e.printStackTrace();}
    	
    }
	public void replaceLetter(String letterName, World world) {
		int num = Integer.parseInt(letterName.substring(letterName.lastIndexOf("_")+1));
		Logger.log(""+num);
		removeLetter(letterName, world);
		addLetter(num, world);
		
	}

	public void shuffleLetters() {
    	for(int i=0;i<letters.length;i++){
    		letters[i].setOrigin(new SimpleVector(generateVector()));
		}	
	}
	
	public char[] getAlphabet() {
		return alphabet;
	}

	public void setAlphabet(char[] alphabet) {
		this.alphabet = alphabet;
	}
	
	public Object3D getLetterPlane(){
		
		Object3D plane = new Object3D(2); 
		// Front
	    SimpleVector upperLeftFront=new SimpleVector(-1,-3,-1);
	    SimpleVector upperRightFront=new SimpleVector(1,-3,-1);
	    SimpleVector lowerLeftFront=new SimpleVector(-1,1,-1);
	    SimpleVector lowerRightFront=new SimpleVector(1,1,-1);
	    plane.addTriangle(upperLeftFront,0,0, lowerLeftFront,0,1, upperRightFront,1,0);
	    plane.addTriangle(upperRightFront,1,0, lowerLeftFront,0,1, lowerRightFront,1,1);
		plane.scale(4);
		//plane.build();
		
		return plane;
	}
	
	
	
	public void dumpObject(Object3D obj){
		PolygonManager pm = obj.getPolygonManager();
		Logger.log(""+pm.getMaxPolygonID());
		for(int i = 0; i < pm.getMaxPolygonID();i++){
			for(int x = 0; i <= 2;x++){
				Logger.log(i+" | "+x+" | "+pm.getTextureUV(x,0).toString());
			}
		}
	}
	
	
	private Texture loadTexture(int textureFile) {
		Texture toReturn;
		try{
			Logger.log("LOADING IMAGE FILE: "+textureFile);
			toReturn = new Texture(res.openRawResource(textureFile));
		} catch (Exception e) {
			e.printStackTrace();
			toReturn = new Texture(10,10,RGBColor.GREEN);
		}
		return toReturn;
	}
	
	private Object3D loadObject(String objectFile){
		Object3D toReturn;
		try{
			Logger.log("LOADING OBJECT FILE: "+objectFile);
			InputStream input = appCon.getAssets().open(objectFile);
			toReturn = Loader.load3DS(input, 50f)[0];
			input.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			toReturn = Primitives.getSphere(25);
		}
		return toReturn;
	}
	
	private void loadAlphabet(String languageFile) {
		try {
			Logger.log("LOADING ALPHABET FILE: "+languageFile);
			InputStream input = appCon.getAssets().open(languageFile+"_alphabet.txt");
			int size = input.available();
			byte[] buffer = new byte[size];
			input.read(buffer);
			input.close();
			setAlphabet(new String(buffer).toCharArray());
			Logger.log("ALPHABET: "+String.valueOf(getAlphabet()));
		} catch (IOException e) {
			setAlphabet(new char [] {'A','B','C'});
			e.printStackTrace();
			
		}
	}

	private GLSLShader centerShader(){
		GLSLShader toReturn = null;
		try{
			
			InputStream vertexInput = appCon.getAssets().open("centerCloudVertexShader.glsl");
			InputStream fragmentInput = appCon.getAssets().open("centerCloudFragmentShader.glsl");
			String vertexShader = Loader.loadTextFile(vertexInput);
			String fragmentShader = Loader.loadTextFile(fragmentInput);
			vertexInput.close();
			fragmentInput.close();
			
			//build the shader after all that...
			toReturn = new GLSLShader(vertexShader,fragmentShader);
			
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		//toReturn = new GLSLShader(Loader.)
		return toReturn;

	}
	
	
}
