package com.frakle.WordToss2;

import java.util.Random;
import java.util.Stack;

import android.graphics.Paint;
import android.graphics.Typeface;

import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

import com.frakle.WordToss2.AGLFont;

public class Cloud {
	private Random rand = new Random();
	public Object3D cloud;
	public Object3D[] letters;
	char[] alphabet = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	public Cloud(){
		cloud = Object3D.createDummyObj();
		letters = new Object3D[15];
		
		Paint paint = new Paint();
		paint.setTypeface(Typeface.create((String)null, Typeface.BOLD));
		
		paint.setTextSize(50);
		for(int i = 0;i<alphabet.length;i++){
			if(!TextureManager.getInstance().containsTexture(""+alphabet[i]))
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
    	Object3D thisLetter = Primitives.getPlane(1,10);
    	
    	thisLetter.addParent(cloud);
    	thisLetter.setTexture(thisTexture);
    	thisLetter.setName(thisName);
    	thisLetter.setTransparencyMode(Object3D.TRANSPARENCY_MODE_DEFAULT);
    	thisLetter.setTransparency(100);
    	thisLetter.align(cloud);
    	thisLetter.setOrigin(new SimpleVector(generateVector()));
    	thisLetter.setBillboarding(true);
    	thisLetter.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
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
		int seedCount = rand.nextInt(l.size()+1);
		
    	//Purge all the letters from the world.
    	for(int i=0;i<letters.length;i++){
			world.removeObject(world.getObjectByName(letters[i].getName()));	
		}
    	
    	//run through all the remaining letter objects and Remove/Replace them.
		for(int i = 0;i<letters.length;i++){
			cloud.removeChild(letters[i]);
		}
		
		//loop over the first X letters giving seeded values
		for(int i=0;i<seedCount;i++){
			letters[i] = genLetter(i,""+l.elementAt(rand.nextInt(l.size())));
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
    	int num = Integer.parseInt(letterName.substring(letterName.lastIndexOf("_")+1));
    	world.removeObject(world.getObjectByName(letterName));
    	letters[num].clearObject();
		cloud.removeChild(letters[num]);
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

}
