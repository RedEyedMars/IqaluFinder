package com.rem.ifinder;

public abstract class Log {

	public static Log log = new Log.Desktop();
	protected String catagory;
	protected StringBuilder builder = new StringBuilder();
	public void c(String cat){
		catagory = cat;
	}
	public void l(String log){
		builder.append(log);
	}
	public static void cat(String message){
		log.c(message);
	}
	public static void log(String message){
		log.l(message);
	}
	public static class Desktop extends Log{
		@Override
		public void c(String message){
			System.out.println(message);
		}
		@Override
		public void l(String message){
			System.out.println("\t"+message);
		}
	}
}
