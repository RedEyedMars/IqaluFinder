package com.rem.ifinder;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.rem.ifinder.image.Imager;
import com.rem.ifinder.image.Segmenter;

public class Main {

	public static void main(String[] args){
		Finder.setup();
		//completely_process();
		/*
		Finder finder = Finder.load("iqaluit");
		for(Info info:finder.get("Blo").getInfos()){
			System.out.println(info.getFullDetails()+":"+info.getRegion());
		}*/
		
		segment();
	}
	public static void segment(){
		try {
			Segmenter.segment(ImageIO.read(new File("res/full.png")), "iqaluit", 32);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void completely_process(){
		Finder finder = new Finder("iqaluit");
		finder.read("iqaluit");
		try {
			Imager.completely_process("iqaluit",finder); //only_id_buildings / test
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

}
