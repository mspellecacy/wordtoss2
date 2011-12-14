package com.frakle.WordToss2;

import com.frakle.WordToss2.AGLFont;
import com.frakle.WordToss2.WordList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.Arrays;
import java.util.Random;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.World;
import com.threed.jpct.Camera;
import com.threed.jpct.Logger;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.util.MemoryHelper;


public class MainRenderer implements GLSurfaceView.Renderer {

	private boolean PAUSED_GAME = false;
	private MainRenderer master = null;
	private FrameBuffer fb = null;
	public World world = null;
	private RGBColor back = new RGBColor(10, 10, 100);
	private Light sun = null;
	private AGLFont timerFont;
	private AGLFont wlFont;
	private AGLFont scoreFont;
	private AGLFont scoreFontTitle;
	private long time = System.currentTimeMillis();
	private boolean stop = false;
	private float curY = 0f;
	private int lfps = 0;
	private int fps = 0;
	private boolean FIRST_RUN = true;
	private String timeDisplay;
	private long LAST_STACK = System.currentTimeMillis();
	//private Camera cam;

	public boolean NEW_CLOUD = false;
	public char[] alphabet = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	public float touchTurn = 0;
	public float touchTurnUp = 0;
	public Cloud c;
	public WordList wl;
	//private float[] rotationMatrix;
	
	public MainRenderer() {
	}

	public void stop() {
		stop = true;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		try {
			if (!stop) {
				
				if(NEW_CLOUD){
					c.newCloud(world);
					NEW_CLOUD = false;
				}
				
				//Rotate the main cloud body
				c.rotate(touchTurnUp, touchTurn);

				//Purge the old values
				if(touchTurn != 0)
					touchTurn=0;
				if(touchTurnUp != 0)
					touchTurnUp = 0;
				
				//Render
				fb.clear();
				world.renderScene(fb);
				world.draw(fb);
				//
				if(FIRST_RUN){
					timeDisplay = "Tap to Start";
				}else if(PAUSED_GAME){
					timeDisplay = "Tap to Resume";
				} else {
					timeDisplay = Wordtoss2Game.getHumanTime();
				}

				// On Screen FPS counter for debug...
				//buttonFont.blitString(fb, "fps: "+lfps, 10, 40, 10, RGBColor.WHITE);
				timerFont.blitString(fb, "Time: "+timeDisplay, 10, 40, 10, RGBColor.WHITE);
				scoreFontTitle.blitString(fb, "Score:", 10, fb.getHeight()-60, 10, RGBColor.WHITE);
				scoreFont.blitString(fb,""+Wordtoss2Game.CURRENT_SCORE, 10, fb.getHeight()-10, 10, RGBColor.WHITE);
				//wlFont.blitStringReverse(fb, wl.currentWord, wl.lettersRemainingStack, fb.getWidth()-60, 120, 10, RGBColor.RED, RGBColor.GREEN);
				wlFont.blitStringReverse(fb, wl.currentWord, wl.lettersRemainingStack, fb.getWidth()-60, fb.getHeight()/2, 10, RGBColor.RED, RGBColor.GREEN);
				fb.display();
				if (System.currentTimeMillis() - time >= 1000) {
					lfps = (fps + lfps) >> 1;
					//Logger.log(fps + "fps");
					fps = 0;
					time = System.currentTimeMillis();
				}
				fps++;
				
			} else {
				if (fb != null) {
					fb.dispose();
					fb = null;
				}
			}
		} catch (Exception e) {
			Logger.log(e, Logger.MESSAGE);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(gl, w, h);

		if (master == null) {
			
			world = new World();
			world.setAmbientLight(20, 20, 20);
			world.setFogging(0);
			world.setFogParameters(1000, 255, 255, 0);
			sun = new Light(world);
			sun.setIntensity(250, 250, 250);
			//add letters

			try{
				world.addObject(c.cloud);
				world.addObjects(c.letters);
			}catch(Exception e){Logger.log(e.toString());}
			
			//Setup Paint... we'll use this to make our letters/numbers
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setTypeface(Typeface.create(Typeface.MONOSPACE,0));
			paint.setTextSize(50);
			timerFont = new AGLFont(paint);
			wlFont = new AGLFont(paint);
			scoreFont = new AGLFont(paint);
			scoreFontTitle = new AGLFont(paint);
			
			//Setup our world Camera
			Camera cam = world.getCamera();
			cam.moveCamera(Camera.CAMERA_MOVEOUT, 150);
			cam.lookAt(new SimpleVector(30,0,0));
			
			//start with a usable cloud (even though we already constructed one... when initially creating it...but that might be bad
			c.newSeededCloud(world,wl.currentWordStack);
			SimpleVector sv = new SimpleVector();
			sv.set(0f,0f,0f);
			sv.y -= 100;
			sv.z -= 100;
			sun.setPosition(sv);
			
			MemoryHelper.compact();
			if (master == null) {
				Logger.log("Saving master Activity!");
				master = MainRenderer.this;
			}
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}

	public boolean objectTouched(float xpos, float ypos) {
		Logger.log("objectTouched'd");
		if(FIRST_RUN){
			FIRST_RUN = false;
			LAST_STACK = System.currentTimeMillis();
			Wordtoss2Game.gTimer.start();
		}
		if(PAUSED_GAME){
			Logger.log("game paused");
			PAUSED_GAME = false;
			LAST_STACK = System.currentTimeMillis();
			Wordtoss2Game.gTimer.start();
		}
		Random rand = new Random();
		SimpleVector dir=Interact2D.reproject2D3DWS(world.getCamera(), fb, (int) xpos, (int) ypos).normalize();
		Object[] res=world.calcMinDistanceAndObject3D(world.getCamera().getPosition(), dir, 10000);
		boolean NEED_SPECIFIC_LETTER = false;
		if(res[1] != null){
			//Logger.log(Arrays.toString(wl.wordsStack.toArray()));
			Object3D touchedObject = (Object3D) res[1];
			if(touchedObject.getName() != "CenterCloud"){
				//Logger.log(Arrays.toString(c.currentLetters().toArray()));
	
				if(wl.checkLetter(touchedObject.getName().charAt(0))){
					//Logger.log("LETTER CORRECT: "+ touchedObject.getName().charAt(0));
					
					c.removeLetter(touchedObject.getName(), world);
					//named break... AWESOME POSSUM!
					search:
						for(int i = 0;i<wl.lettersRemainingStack.size();i++){
							if( c.currentLetters().contains(wl.lettersRemainingStack.elementAt(i))){
								break search;
							}else{
								NEED_SPECIFIC_LETTER = true;
							}
	
						}
					//LULZ - I know this is kinda gross, at least it looks like it to me...
					//If there is a better way I'm all ears. 
					if(NEED_SPECIFIC_LETTER){
						
						c.addLetter(Integer.parseInt(touchedObject.getName().substring(touchedObject.getName().lastIndexOf("_")+1)),world,
								Character.toString(wl.lettersRemainingStack.get(rand.nextInt(wl.lettersRemainingStack.size()))));
						//NEED_SPECIFIC_LETTER = false;
					} else {
						c.addLetter(Integer.parseInt(touchedObject.getName().substring(touchedObject.getName().lastIndexOf("_")+1)),world);
					}
	
					if(wl.lettersRemainingStack.empty()){
						//Logger.log("Time Diff"+(int) (LAST_STACK - curTime.getTime()));
						Wordtoss2Game.addScore(wl.currentWord,(int) (System.currentTimeMillis() - LAST_STACK));
						Wordtoss2Game.playSound(3);
						LAST_STACK = System.currentTimeMillis();
						wl.restack();
						c.newSeededCloud(world,wl.currentWordStack);
	
					}
	
					Wordtoss2Game.playSound(1);
				} else {
					//This prevents the negative sound trigger when touching the cloud
					//Usually this happens when touching to drag the letters
					if(touchedObject.getName().toString() != "CenterCloud"){
						Wordtoss2Game.playSound(2);
					}
					//Logger.log("CurWordStack: "+Arrays.toString(wl.currentWordStack.toArray()));
					//Logger.log("LetRemStack: "+Arrays.toString(wl.lettersRemainingStack.toArray()));
					//Logger.log("LetFndStack: "+Arrays.toString(wl.lettersFoundStack.toArray()));
				}
			} //---- res[1] = null

			//Logger.log(touchedObject.getName());
			return true;
		}
		return false;
	}
	
	public void setRotationMatrix(float[] matrix){
		//rotationMatrix = matrix;
	}

	public void onPause(){
		PAUSED_GAME = true;
	}

}
