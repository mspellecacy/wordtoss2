package com.frakle.WordToss2;

import com.frakle.WordToss2.AGLFont;
import com.frakle.WordToss2.WordList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

import android.graphics.Paint;

import java.util.Arrays;
import java.util.Random;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Interact2D;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.World;
import com.threed.jpct.Camera;
import com.threed.jpct.Logger;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.util.MemoryHelper;


public class MainRenderer implements GLSurfaceView.Renderer{

	private MainRenderer master = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(10, 10, 100);
	private Light sun = null;
	private AGLFont buttonFont;
	private AGLFont wlFont;
	private long time = System.currentTimeMillis();
	private boolean stop = false;
	private int lfps = 0;
	private int fps = 0;
	
	public boolean NEW_CLOUD = false;
	public char[] alphabet = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	public float touchTurn = 0;
	public float touchTurnUp = 0;
	public Cloud cloud = new Cloud();
	public WordList wl = new WordList();


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
					
					for(int i=0;i<cloud.letters.length;i++){
						world.removeObject(world.getObjectByName(cloud.letters[i].getName()));	
					}
					cloud.newCloud();
					world.addObjects(cloud.letters);
					NEW_CLOUD = false;
				}
				
				//Rotate the main cloud body
				cloud.rotate(touchTurnUp, touchTurn);
				
				//Purge the old values
				if(touchTurn != 0)
					touchTurn=0;
				if(touchTurnUp != 0)
					touchTurnUp = 0;

				if(world == null){
					world = new World();
					world.setAmbientLight(20, 20, 20);
				}

				//Render
				fb.clear(back);
				world.renderScene(fb);
				world.draw(fb);
				
				buttonFont.blitString(fb, "fps: "+lfps, 10, 40, 10, RGBColor.WHITE);
				wlFont.blitString(fb, wl.currentWord, fb.getWidth()-300, fb.getHeight()-300, 10, RGBColor.RED);
				//buttonFont.blitString(fb, "fps: "+lfps, fb.getWidth()-30, 40, 10, RGBColor.WHITE);
				//glFont.blitString(fb, , 5, fb.getHeight()-10, 10, RGBColor.WHITE);
				
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
			
			sun = new Light(world);
			sun.setIntensity(250, 250, 250);
			//add letters
			
			try{
				world.addObjects(cloud.letters);
			}catch(Exception e){Logger.log(e.toString());}
			
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			
			paint.setTextSize(50);
			buttonFont = new AGLFont(paint);
			wlFont = new AGLFont(paint);
			
			Camera cam = world.getCamera();
			cam.moveCamera(Camera.CAMERA_MOVEOUT, 150);
			cam.lookAt(new SimpleVector(30,0,0));

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
		Random rand = new Random();
		SimpleVector dir=Interact2D.reproject2D3DWS(world.getCamera(), fb, (int) xpos, (int) ypos).normalize();
		Object[] res=world.calcMinDistanceAndObject3D(world.getCamera().getPosition(), dir, 10000);
		boolean NEED_SPECIFIC_LETTER = false;
		if(res[1] != null){
			Logger.log(Arrays.toString(wl.wordsStack.toArray()));
			Object3D touchedObject = (Object3D) res[1];
			Logger.log(Arrays.toString(cloud.currentLetters().toArray()));
			
			if(wl.checkLetter(touchedObject.getName().charAt(0))){
				Logger.log("LETTER CORRECT: "+ touchedObject.getName().charAt(0));
				
				cloud.removeLetter(touchedObject.getName(), world);
				//named break... AWESOME POSSUM!
				search:
					for(int i = 0;i<wl.lettersRemainingStack.size();i++){
						if( cloud.currentLetters().contains(wl.lettersRemainingStack.elementAt(i))){
							break search;
						}else{
							NEED_SPECIFIC_LETTER = true;
						}
	
					}
				//LULZ - I know this is kinda gross, at least it looks like it to me...
				//If there is a better way I'm all ears. 
				if(NEED_SPECIFIC_LETTER){
					cloud.addLetter(Integer.parseInt(touchedObject.getName().substring(touchedObject.getName().lastIndexOf("_")+1)),world,
							Character.toString(wl.lettersRemainingStack.get(rand.nextInt(wl.lettersRemainingStack.size()))));
					//NEED_SPECIFIC_LETTER = false;
				} else {
					cloud.addLetter(Integer.parseInt(touchedObject.getName().substring(touchedObject.getName().lastIndexOf("_")+1)),world);
				}
				
				if(wl.lettersRemainingStack.empty()){
					
					wl.restack();
				}
			} else {
				Logger.log("CurWordStack: "+Arrays.toString(wl.currentWordStack.toArray()));
				Logger.log("LetRemStack: "+Arrays.toString(wl.lettersRemainingStack.toArray()));
				Logger.log("LetFndStack: "+Arrays.toString(wl.lettersFoundStack.toArray()));
			}
			
			Logger.log(touchedObject.getName());
			return true;
		}
		return false;
	}
    
}
