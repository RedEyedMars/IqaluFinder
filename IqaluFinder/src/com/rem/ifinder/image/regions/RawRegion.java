package com.rem.ifinder.image.regions;

import java.util.ArrayList;
import java.util.List;

public class RawRegion {

	private List<RawBoundary> boundaries = new ArrayList<RawBoundary>();
	private List<Point> points = new ArrayList<Point>();
	private Point bottomLeft = new Point(Integer.MAX_VALUE,Integer.MAX_VALUE);
	private Point topRight = new Point(0,0);
	private int id;
	private int dataSize = -1;
	public RawRegion(int id){
		this.id = id;
	}
	public void add(int x, int y){
		if(x<bottomLeft.x){
			bottomLeft.x = x;
		}
		if(x>topRight.x){
			topRight.x = x;
		}
		if(y<bottomLeft.y){
			bottomLeft.y = y;
		}
		if(y>topRight.y){
			topRight.y = y;
		}
		points.add(new Point(x,y));
	}
	public void bind(){
		for(int y=bottomLeft.y;y<=topRight.y;++y){
			RawBoundary boundary = new RawBoundary();
			boundary.lowX = Integer.MAX_VALUE;
			boundary.highX = 0;
			boundary.y = y;
			boundaries.add(boundary);
		}
		for(Point point:points){
			boundaries.get(point.y-bottomLeft.y).add(point.x);
		}
		for(RawBoundary boundary:boundaries){
			boundary.bind();
		}
	}
	public int save(int index, int[] data) {
		data[index] = id;
		data[index+1] = bottomLeft.x;
		data[index+2] = bottomLeft.y;
		data[index+3] = topRight.x;
		data[index+4] = topRight.y;
		data[index+5] = boundaries.size();
		index+=6;
		for(RawBoundary boundary:boundaries){
			index = boundary.save(index,data);
		}
		return index;
	}
	public int getDataSize(){
		if(dataSize == -1){
			dataSize = 6;
			for(RawBoundary boundary:boundaries){
				dataSize += boundary.getDataSize();
			}
		}
		return dataSize;
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
