package com.rem.ifinder.interfaces;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;


public interface StreamLoader {
	public boolean open(String type, String fileName, String extension);
	public InputStream get();
	public void close() throws IOException ;
	public int[] asInts(int numberOfInts) throws IOException;
	public int asInt() throws IOException;
	public String asString() throws IOException;
	public char[] asChars() throws IOException;
	
	public static abstract class Default implements StreamLoader {
		protected InputStream inputFile;
		@Override
		public InputStream get(){
			return inputFile;
		}
		@Override
		public void close() throws IOException{
			inputFile.close();
		}
		@Override
		public int[] asInts(int numberOfInts) throws IOException{
			byte[] bytes = new byte[4*numberOfInts];
			inputFile.read(bytes);
			ByteBuffer wrapped = ByteBuffer.wrap(bytes);
			int[] result = new int[numberOfInts];
			wrapped.asIntBuffer().get(result);
			return result;
		}
		@Override
		public int asInt() throws IOException {
			byte[] bytes = new byte[4];
			inputFile.read(bytes);
			ByteBuffer wrapped = ByteBuffer.wrap(bytes);
			return wrapped.getInt();
		}
		@Override
		public String asString(){
			final InputStreamReader inputStreamReader = new InputStreamReader(inputFile);
			final BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String nextLine;
			final StringBuilder builder = new StringBuilder();
			try {
				while ((nextLine = bufferedReader.readLine()) != null) {
					builder.append(nextLine);
					builder.append('\n');
				}
				return builder.toString();
			}
			catch (IOException e){
				e.printStackTrace();
			}
			return null;
		}
		@Override
		public char[] asChars() throws IOException {
			byte[] bytes = new byte[4];
			inputFile.read(bytes);
			ByteBuffer wrapped = ByteBuffer.wrap(bytes);
			int numberOfChars = wrapped.getInt();

			bytes = new byte[numberOfChars*4];
			inputFile.read(bytes);
			wrapped = ByteBuffer.wrap(bytes);
			char[] result = new char[numberOfChars];
			wrapped.asCharBuffer().get(result);
			return result;
		}
	}
	public static class Desktop extends StreamLoader.Default {
		@Override
		public boolean open(String type, String fileName, String extension) {
			try {
				inputFile = new FileInputStream("res/"+type+"/"+fileName+"."+extension);
				return true;
			} catch (FileNotFoundException e) {
				return false;
			}
		}
	}
}
