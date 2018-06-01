package com.rem.ifinder.image.regions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RawBoundary {
	public int lowX = 0;
	public int highX = 0;
	public int y = 0;
	private Set<Integer> xs = new HashSet<Integer>();
	private List<Integer> limitStarts = new ArrayList<Integer>(); 
	private List<Integer> limitEnds = new ArrayList<Integer>(); 
	public int save(int index, int[] data) {
		data[index] = lowX;
		data[index+1] = highX;
		data[index+2] = y;
		data[index+3] = limitStarts.size();
		index+=4;
		for(int i=0;i<limitStarts.size();++i){
			data[index] = limitStarts.get(i);
			data[index+1] = limitEnds.get(i);
			index+=2;
		}
		return index;
	}
	public int getDataSize() {
		return 4+limitStarts.size()*2;
	}
	public void add(int x) {
		if(x>highX){
			highX = x;
		}
		if(x<lowX){
			lowX = x;
		}
		xs .add(x);
	}
	public void bind(){
		int current = lowX;
		for(int i=lowX;i<=highX;++i){
			if(xs.contains(i)){
				if(limitStarts.size()==limitEnds.size()){
					limitStarts.add(current);
				}
			}
			else {
				if(limitStarts.size()!=limitEnds.size()){
					limitEnds.add(current-1);
				}
				++current;
			}
		}
		if(limitStarts.size()!=limitEnds.size()){
			limitEnds.add(current-1);
		}
	}
}
