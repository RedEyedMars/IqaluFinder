package com.rem.ifinder.image.processing;

import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;

public interface ColourAssimilator {

	public static class BlueToEven extends LookupTable implements ImageProcessor {
		private final LookupOp opperation;
		public BlueToEven(){
	        super(0, 4);
	        opperation = new LookupOp(this, null);
		}
	    @Override
	    public int[] lookupPixel(int[] src,int[] dest) {
	        if (dest == null) {
	            dest = new int[src.length];
	        }
	    	System.arraycopy(
	    			new int[]{
	    					src[0],
	    					src[1],
	    					src[3]==255?src[2]%2==0?src[2]:src[2]+1:src[2],
	    					src[3]
	    			}, 0, 
	    			dest, 0, dest.length);
	        return dest;
	    }
		@Override
		public BufferedImage process(BufferedImage image) {
			return opperation.filter(image, null);
		}
	}
}
