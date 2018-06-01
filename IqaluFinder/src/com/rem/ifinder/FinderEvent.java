package com.rem.ifinder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public interface FinderEvent {
	

	public boolean hasInfos();
	public int numberOfInfos();
	public Collection<Info> getInfos();
	
	public static class Found implements FinderEvent {
		private TreeSet<Info> infos = new TreeSet<Info>();
		public Found(Info... infos){
			this.infos.addAll(Arrays.asList(infos));
		}
		public Found(List<Info> infos){
			this.infos.addAll(infos);
		}
		@Override
		public boolean hasInfos(){
			return !infos.isEmpty();
		}
		@Override
		public Collection<Info> getInfos(){
			return infos;
		}
		@Override
		public int numberOfInfos() {
			return infos.size();
		}
		@Override
		public String toString(){
			return "Found:"+infos;
		}
	}
	public static class NotFound implements FinderEvent {

		@Override
		public boolean hasInfos() {
			return false;
		}
		@Override
		public Collection<Info> getInfos() {
			return null;
		}
		@Override
		public int numberOfInfos() {
			return 0;
		}

	}
}
