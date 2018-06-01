package com.rem.ifinder.image.regions;

import java.util.List;

public class Boundary {
	public int lowX = 0;
	public int highX = 0;
	public int y = 0;
	private int[] limitStarts; 
	private int[] limitEnds; 
	public static int load(int index, int[] inputData, List<Boundary> boundaries) {
		Boundary boundary = new Boundary();
		boundary.lowX = inputData[index];
		boundary.highX = inputData[index+1];
		boundary.y = inputData[index+2];
		boundary.limitStarts = new int[inputData[index+3]];
		boundary.limitEnds = new int[boundary.limitStarts.length];
		index += 4;
		for(int i=0;i<boundary.limitStarts.length;++i){
			boundary.limitStarts[i] = inputData[index];
			boundary.limitEnds[i] = inputData[index+1];
			index+=2;
		}
		boundaries.add(boundary);
		return index;
	}
}
