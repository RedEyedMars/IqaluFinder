package com.rem.ifinder.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.rem.ifinder.Finder;
import com.rem.ifinder.Log;
import com.rem.ifinder.image.processing.ColourAssimilator;
import com.rem.ifinder.image.processing.ColourReplacer;
import com.rem.ifinder.image.processing.ImageProcessor;
import com.rem.ifinder.image.regions.RawRegion;
import com.rem.ifinder.image.regions.Region;

public class Imager {
	public static void completely_process(String fileName, Finder finder) throws IOException{
		Log.cat("Image("+fileName+")");
		BufferedImage image = amalgamate(fileName);
		Map<Integer, RawRegion> rawRegions =
				Regionizer.regionalize(image, new HashMap<Integer, RawRegion>());
		Log.cat("Regions("+fileName+")");
		bind(rawRegions);
		/*
		for(Integer id: rawRegions.keySet()){
			Log.log(rawRegions.get(id));
		}*/
		save(rawRegions,fileName,image.getWidth(),image.getHeight());
		Region.load(fileName,finder,true);
		/*for(Integer id: regions.keySet()){
			Log.log(regions.get(id));
		}*/
	}
	public static void bind(Map<Integer, RawRegion> regions){
		Log.log("Binding...");
		for(Integer id: regions.keySet()){
			regions.get(id).bind();
		}
		Log.log("Bound");
	}
	public static void save(Map<Integer, RawRegion> regions,String fileName, int imageWidth, int imageHeight) throws IOException{
		Log.log("Saving...");

		int dataSize = 3;
		int index = dataSize+1;
		for(Integer id: regions.keySet()){
			dataSize += regions.get(id).getDataSize();
		}
		int[] data = new int[dataSize+1];
		data[0]=dataSize;
		data[1]=imageWidth;
		data[2]=imageHeight;
		data[3]=regions.size();
		for(Integer id: regions.keySet()){
			index = regions.get(id).save(index,data);
		}
		Finder.saver.open("regions",fileName,"dat");
		Finder.saver.asInts(data);
		Finder.saver.close();
		Log.log("Saved");
	}


	public static BufferedImage amalgamate(String inputFileName){
		Log.log("Pre-Processing...");
		try {
			BufferedImage image = batch(
					ImageIO.read(new File("res/"+"maps/"+inputFileName+".png")),
					new ColourReplacer(
							new Color(89,1,32),new Color(89,2,30),
							new Color(13,2,32),new Color(13,2,32),
							new Color(13,2,31),new Color(13,3,32)

							,new Color(99,2,34),new Color(99,3,34)
							,new Color(99,2,33),new Color(99,2,34)
							
							,new Color(121,2,34),new Color(121,3,34)
							,new Color(121,2,33),new Color(121,2,34)

							,new Color(105,2,28),new Color(105,2,34)
							,new Color(105,2,29),new Color(105,3,34)
							,new Color(105,2,30),new Color(105,4,34)
							,new Color(105,2,34),new Color(105,5,34)

							,new Color(4,2,39),new Color(4,2,40)
							,new Color(4,2,28),new Color(4,3,40)
							,new Color(4,2,27),new Color(4,4,40)

							,new Color(7,2,59),new Color(7,3,60)

							,new Color(106,2,62),new Color(106,3,62)

							,new Color(96,2,70),new Color(96,3,70)

							,new Color(65,2,80),new Color(65,3,80)
							),
					new ColourAssimilator.BlueToEven());
			Log.log("Pre-Processed");
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static BufferedImage batch(BufferedImage image, ImageProcessor... colourReplacer) throws IOException {

		ImageIO.write(image, "png", new File("res/maps/versioning/backup.png"));
		for(ImageProcessor processor:colourReplacer){
			image = processor.process(image);
		}
		ImageIO.write(image, "png", new File("res/maps/output.png"));
		return image;
	}
}
