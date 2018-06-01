package com.rem.ifinder.image;

import java.awt.image.BufferedImage;
import java.util.Map;

import com.rem.ifinder.Info;
import com.rem.ifinder.Log;
import com.rem.ifinder.image.regions.RawRegion;

public class Regionizer {

	private Map<Integer, RawRegion> regions;
	public Regionizer(Map<Integer, RawRegion> regions){
		this.regions = regions;
	}
	public void add(int x, int y, int src) {
		if(((src & 0xff000000) >>> 24)!=255){
			return;
		}
		int id = Info.getId(
				((src & 0xff0000) >> 16),
				((src & 0xff00) >> 8),
				src & 0xff);
		if(!regions.containsKey(id)){
			
			regions.put(id, new RawRegion(id));
		}
		if(y<2000&&id<=-30){
			System.out.println("Out of Bounds:"+x+","+y+":::"+
					((src & 0xff0000) >> 16)+","+
					((src & 0xff00) >> 8)+","+
					(src & 0xff));
		}
		regions.get(id).add(x, y);
	}
	
	public static Map<Integer, RawRegion> regionalize(BufferedImage image, Map<Integer, RawRegion> regions) {

		Log.log("Regionalizing...");
		Regionizer regionalizer = new Regionizer(regions);
		
		int width = image.getWidth();
		int height = image.getHeight();
		for(int x=0;x<width;++x){
			for(int y=0;y<height;++y){
				regionalizer.add(x,y,image.getRGB(x, y));
			}
		}
		Log.log("Regionalized");
		return regions;
	}
}
