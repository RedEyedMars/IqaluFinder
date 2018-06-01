package com.rem.ifinder.image.regions;

import java.util.List;

import com.rem.ifinder.Finder;
import com.rem.ifinder.Info;
import com.rem.ifinder.Log;

import java.io.IOException;
import java.util.ArrayList;

public class Region {

	public static int imageWidth = -1;
	public static int imageHeight = -1;
	private List<Boundary> boundaries = new ArrayList<Boundary>();
	private Point bottomLeft = new Point(Integer.MAX_VALUE,Integer.MAX_VALUE);
	private Point topRight = new Point(0,0);
	private final int id;
	public Region(int id){
		this.id = id;
	}
	public boolean encompasses(float x, float y){
		return x>=bottomLeft.x&&x<=topRight.x&&y>=bottomLeft.y&&y<=topRight.y;
	}
	private static int load(int index, int[] inputData, Finder finder) {
		Region newRegion = new Region(inputData[index]);
		newRegion.bottomLeft.x = inputData[index+1];
		newRegion.bottomLeft.y = inputData[index+2];
		newRegion.topRight.x = inputData[index+3];
		newRegion.topRight.y = inputData[index+4];
		int numberOfBoundaries = inputData[index+5];
		index += 6;
		for(int i=0;i<numberOfBoundaries;++i){
			index = Boundary.load(index,inputData,newRegion.boundaries);
		}
		if(!finder.hasInfoById(newRegion.getId())){
			finder.add(Info.createFromId(newRegion.getId(),newRegion));
		}
		finder.getInfoById(newRegion.getId()).setRegion(newRegion);
		return index;
	}

	public static void load(String fileName, Finder finder){
		Log.cat("Regions("+fileName+")");
		load(fileName,finder,true);
	}
	public static void load(String fileName,Finder finder, boolean unused){
		Log.log("Loading...");
		try {
			Finder.loader.open("regions",fileName,"dat");
			int numberOfInts = Finder.loader.asInt();
			int[] inputData = Finder.loader.asInts(numberOfInts);
			Finder.loader.close();
			Region.imageWidth = inputData[0];
			Region.imageHeight = inputData[1];
			int numberOfRegions = inputData[2];
			int index = 3;
			for(int i=0;i<numberOfRegions;++i){
				index = Region.load(index,inputData,finder);
			}
			Log.log("Loaded");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getId(){
		return id;
	}
	public int getMiddleX(){
		return bottomLeft.x+(topRight.x-bottomLeft.x)/2;
	}
	public int getMiddleY(){
		return bottomLeft.y+(topRight.y-bottomLeft.y)/2;
	}
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(id);
		builder.append("=(");
		builder.append(bottomLeft);
		builder.append(" ");
		builder.append(topRight);
		builder.append(")");
		return builder.toString();
	}
}
