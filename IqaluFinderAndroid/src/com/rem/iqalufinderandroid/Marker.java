package com.rem.iqalufinderandroid;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.rem.ifinder.image.regions.Region;



public class Marker {
	private static int colourIndex = 0;
	private static float[] reds = new float[]  {1    ,1f,0f  ,0f,0f,0.5f};
	private static float[] greens = new float[]{0.65f,1f,0.5f,1f,0f,0f};
	private static float[] blues = new float[] {0    ,0f,0f  ,1f,1f,0.5f};

	private int x=0;
	private int y=0;
	private boolean visible = false;
	private String name = "";
	private FloatBuffer mColors;
	public Marker(){

		float r = reds[colourIndex++];
		float g = greens[colourIndex++];
		float b = blues[colourIndex++];
		if(colourIndex>=reds.length){
			colourIndex = 0;
		}

		final float[] colorData = {		
				r, g, b, 1.0f,				
				r, g, b, 1.0f,
				r, g, b, 1.0f,
				r, g, b, 1.0f,				
				r, g, b, 1.0f,
				r, g, b, 1.0f,
		};
		mColors = ByteBuffer.allocateDirect(colorData.length * GLRenderer.mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();							
		mColors.put(colorData).position(0);
	}

	public String getName(){
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setLocation(int x, int y){
		this.x = x*Segment.imageWidth/Region.imageWidth;
		this.y = y*Segment.imageHeight/Region.imageHeight;
		this.visible = true;
	}
	public void setVisible(boolean isVisible){
		this.visible = isVisible;

	}
	public void draw(GLRenderer renderer, int[] renderInfo) {
		if(visible){
			renderer.drawMarker(x,y,renderInfo,mColors);
		}
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}

	public boolean isVisible() {
		return visible;
	}
	public static interface Event {
		public void act(GLRenderer renderer,Marker marker);
		public static class Move implements Event {
			private int x;
			private int y;
			public Move(int x, int y){
				this.x = x;
				this.y = y;
			}
			public void act(GLRenderer renderer,Marker marker){
				marker.setLocation(x, y);
			}
		}
		public static class Create implements Event {
			private String name;
			public Create(String name){
				this.name = name;
			}
			public void act(GLRenderer renderer,Marker marker){
				renderer.finalizeMarker(name);
			}
		}
		public static class Show implements Event {
			public void act(GLRenderer renderer,Marker marker){
				marker.setVisible(true);
			}
		}
		public static class Hide implements Event {
			public void act(GLRenderer renderer,Marker marker){
				marker.setVisible(false);
			}
		}
	}
	public static Marker[] createMarkerList(int sizeOffset){
		return new Marker[reds.length-sizeOffset];
	}



}
