package com.rem.iqalufinderandroid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Segment {

	public static int screenWidth;
	public static int screenHeight;
	public static int imageWidth;
	public static int imageHeight;
	public static int segmentation = 32;
	private static FloatBuffer mColors;
	public static int overviewSampleSize;
	public static int mediumSampleSize;
	public static int zoomedSampleSize;
	static {
		// R, G, B, A
		final float[] colorData = {		
				1.0f, 1.0f, 1.0f, 1.0f,				
				1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 1.0f,				
				1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 1.0f,
		};
		mColors = ByteBuffer.allocateDirect(colorData.length * GLRenderer.mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mColors.put(colorData).position(0);
	}
	private int y;
	private int x;
	private int overviewTexture = 0;
	private int zoomedTexture = 0;
	private boolean used = true;
	public Segment(int x, int y){
		this.x = x;
		this.y = y;

	}
	public void draw(GLRenderer renderer, int[] rendererInfo){
		if(renderer.getZoom()<5f){
			if(overviewTexture == 0){
				overviewTexture = renderer.loadTexture(x,y);
				if(overviewTexture != 0 && zoomedTexture != 0){
					renderer.unloadTexture(zoomedTexture);
					zoomedTexture = 0;
				}
			}	
		}
		else {
			if(zoomedTexture == 0){
				zoomedTexture = renderer.loadTexture(x,y);
				if(overviewTexture != 0 && zoomedTexture != 0){
					renderer.unloadTexture(overviewTexture);
					overviewTexture = 0;
				}
			}
		}
		if(!used){
			if(overviewTexture!=0){
				renderer.draw(overviewTexture,x,y,rendererInfo,mColors);
			}
			else if(zoomedTexture!=0){
				renderer.draw(zoomedTexture,x,y,rendererInfo,mColors);
			}
			used = true;
		}
	}
	public void destroyIfUnused(GLRenderer renderer) {
		if(!used){
			if(overviewTexture!=0){
				renderer.unloadTexture(overviewTexture);
				overviewTexture = 0;
			}
			if(zoomedTexture!=0){
				renderer.unloadTexture(zoomedTexture);
				zoomedTexture = 0;
			}
		}
		else {
			used = false;
		}
	}
}
