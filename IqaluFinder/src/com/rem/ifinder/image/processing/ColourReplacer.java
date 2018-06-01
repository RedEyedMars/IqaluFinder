package com.rem.ifinder.image.processing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;

/**
 * Replaces a given colour in the image with another colour.
 * @author Geoffrey
 *
 */
public class ColourReplacer extends LookupTable implements ImageProcessor {
	private final int[][][][] map = new int[256][][][];
	private final LookupOp opperation;
	
	public ColourReplacer(Color...pairs){
        super(0, 4);
        for(int i=0;i<pairs.length;i+=2){
        	int r = pairs[i].getRed();
        	int g = pairs[i].getGreen();
        	int b = pairs[i].getBlue();
        	if(map[r]==null){
        		map[r] = new int[256][][];
        	}
        	if(map[r][g]==null){
        		map[r][g] = new int[256][];
        	}
        	if(map[r][g][b]==null){
        		map[r][g][b] = new int[] {
        				pairs[i+1].getRed(),
        				pairs[i+1].getGreen(),
        				pairs[i+1].getBlue(),
        				pairs[i+1].getAlpha(),
        	        };
        	}
        	else {
        		throw new RuntimeException("Conflict at source colour "+r+","+g+","+b);
        	}
        }
		opperation = new LookupOp(this, null);
	}
    @Override
    public int[] lookupPixel(int[] src,int[] dest) {
        if (dest == null) {
            dest = new int[src.length];
        }

    	int r = src[0];
    	int g = src[1];
    	int b = src[2];
        if(map[r]!=null&&map[r][g]!=null&&map[r][g][b]!=null){
        	System.arraycopy(map[r][g][b], 0, dest, 0, map[r][g][b].length);
        }
        else {
        	System.arraycopy(src, 0, dest, 0, src.length);
        }
        return dest;
    }
	@Override
	public BufferedImage process(BufferedImage image) {
		return opperation.filter(image, null);
	}

}
