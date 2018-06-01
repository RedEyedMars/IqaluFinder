package com.rem.ifinder.interfaces;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

public interface StreamSaver {
	public boolean open(String type, String fileName, String extension)throws FileNotFoundException;
	public OutputStream get();
	public void close() throws IOException;
	
	public void asInts(int[] data) throws IOException;
	public void asChars(String data) throws IOException;
	
	public static abstract class Default implements StreamSaver{
		protected OutputStream outputFile;

		@Override
		public OutputStream get(){
			return outputFile;
		}
		@Override
		public void close() throws IOException{
			outputFile.close();
		}
		@Override
		public void asInts(int[] data) throws IOException {
			IntBuffer buffer = IntBuffer.wrap(data);
			ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
			byteBuffer.asIntBuffer().put(buffer);
			outputFile.write(byteBuffer.array());
		}
		
		@Override
		public void asChars(String data) throws IOException {
			IntBuffer numberBuffer = IntBuffer.wrap(new int[]{data.length()});
			ByteBuffer numberByteBuffer = ByteBuffer.allocate( 4 );
			numberByteBuffer.asIntBuffer().put(numberBuffer);
			outputFile.write(numberByteBuffer.array());
			
			CharBuffer buffer = CharBuffer.wrap(data);
			ByteBuffer byteBuffer = ByteBuffer.allocate(4*data.length());
			byteBuffer.asCharBuffer().put(buffer);
			outputFile.write(byteBuffer.array());
		}
	}
	public static class Desktop extends StreamSaver.Default {

		@Override
		public boolean open(String type, String filePath, String extension) {
			try {
				outputFile = new FileOutputStream("res/"+type+"/"+filePath+"."+extension);
				return true;
			}
			catch (FileNotFoundException e){
				return false;
			}
		}


	}
}
