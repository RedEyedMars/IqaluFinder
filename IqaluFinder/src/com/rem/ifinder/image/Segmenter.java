package com.rem.ifinder.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.rem.ifinder.Log;

public class Segmenter extends Thread {
	private int startX;
	private int endX;
	private int startY;
	private int endY;
	private BufferedImage input;
	private BufferedImage result;
	private String filePathStub;
	public Segmenter(BufferedImage input,String filePathStub, int startX, int startY, int endX, int endY){
		this.input = input;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.filePathStub = filePathStub;
	}
	@Override
	public void run(){
		if(endX-startX<=0|| endY-startY<=0){
			return;
		}
		result = new BufferedImage(endX-startX, endY-startY, BufferedImage.TYPE_INT_ARGB);
		for(int i=startX;i<endX;++i){
			synchronized(input){
				for(int j=startY;j<endY;++j){
					result.setRGB(i-startX, j-startY, input.getRGB(i, j));
				}
			}
		}
		try {
			ImageIO.write(result, "png", new File("res/segments/"+filePathStub+"_"+startX+"_"+startY+".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void segment(BufferedImage image, String fileName, int numberOfPieces) {


		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		int width = image.getWidth()/numberOfPieces;
		int height = image.getHeight()/numberOfPieces;

		Log.log("\tSegmenting...");
		List<Segmenter> segmenters = new ArrayList<Segmenter>();
		for(int x=0;x+width<imageWidth;x+=width){
			for(int y=0;y+height<imageHeight;y+=height){
				Segmenter segmenter = new Segmenter(image,fileName,x,y,x+width,y+height);
				segmenter.start();
				segmenters.add(segmenter);
			}
		}
		for(int x=0;x+width<=imageWidth;x+=width){
			Segmenter segmenter = new Segmenter(image,fileName,x,imageHeight-height,x+width,imageHeight);
			segmenter.start();
			segmenters.add(segmenter);
		}
		for(int y=0;y+height<=imageHeight;y+=height){
			Segmenter segmenter = new Segmenter(image,fileName,imageWidth-width,y,imageWidth,y+height);
			segmenter.start();
			segmenters.add(segmenter);
		}
		if(imageWidth%width==0&&imageHeight%height==0){
			Segmenter segmenter = new Segmenter(image,fileName,imageWidth-width,imageHeight-height,imageWidth,imageHeight);
			segmenter.start();
			segmenters.add(segmenter);
		}
		boolean failed = false;
		for(Segmenter segmenter:segmenters){
			try {
				segmenter.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				failed = true;
			}
		}
		if(!failed){
			Log.log("\tSegmented");
		}
	}
}
