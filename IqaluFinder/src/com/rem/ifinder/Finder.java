package com.rem.ifinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.rem.ifinder.image.regions.Region;
import com.rem.ifinder.interfaces.StreamLoader;
import com.rem.ifinder.interfaces.StreamSaver;

public class Finder {
	public static StreamLoader loader = null;
	public static StreamSaver saver = null;
	public static void setup(StreamLoader loader, StreamSaver saver){
		Finder.loader = loader;
		Finder.saver = saver;
	}
	public static void setup(){
		Finder.loader = new StreamLoader.Desktop();
		Finder.saver = new StreamSaver.Desktop();
	}
	public static Finder load(String fileName){
		Finder finder = new Finder(fileName);
		finder.read(fileName);
		Region.load(fileName,finder);//only_id_buildings / test
		finder.loadOrder();
		return finder;
	}
	private final String fileName;
	private final List<Info> infos = new ArrayList<Info>(){
		private static final long serialVersionUID = -6573668798898190617L;
		private Set<Info> set = new HashSet<Info>();
		@Override
		public boolean add(Info info){
			if(set.add(info)){
				return super.add(info);
			}
			else {
				return false;
			}
		}
	};
	private final Map<Integer, List<Info>> byNumber = new HashMap<Integer, List<Info>>();
	private final Map<String, List<Info>> byWord = new HashMap<String, List<Info>>();
	private final Map<Integer, Info> byId = new HashMap<Integer, Info>();

	private Info mostRecent = null;

	public Finder(String fileName){
		this.fileName = fileName;
	}
	public void add(Info info) {
		byId.put(info.getId(), info);
		info.appendToFinder(this);
	}
	public void specify(Integer buildingNumber, Info info) {
		if(!byNumber.containsKey(buildingNumber)){
			byNumber.put(buildingNumber, new ArrayList<Info>());
		}
		byNumber.get(buildingNumber).add(info);
		infos.add(info);
	}
	public void specify(String word, Info info) {
		if(!byWord.containsKey(word.toLowerCase())){
			byWord.put(word.toLowerCase(), new ArrayList<Info>());
		}
		byWord.get(word.toLowerCase()).add(info);
		infos.add(info);
	}
	public boolean hasInfoById(int id) {
		return byId.containsKey(id);
	}
	public Info getInfoById(int id){
		return byId.get(id);
	}
	public List<Info> getAllInfos() {
		return infos;
	}
	private Pattern letterPattern = Pattern.compile("[a-zA-Z]");
	private Pattern numberPattern = Pattern.compile("\\d");
	private Query[] getQuerries(String input){
		List<Query> result = new ArrayList<Query>();
		StringBuilder builder = new StringBuilder();
		Query.Type typeBuilding = null;
		for(int i = 0;i < input.length();++i){
			if(typeBuilding==null){
				if(letterPattern.matcher(""+input.charAt(i)).matches()){
					typeBuilding = Query.Type.string;
					builder.append(input.charAt(i));
				}
				else if(numberPattern.matcher(""+input.charAt(i)).matches()){
					typeBuilding = Query.Type.integer;
					builder.append(input.charAt(i));
				}
			}
			else {
				if(letterPattern.matcher(""+input.charAt(i)).matches()){
					switch(typeBuilding){
					case string:{
						builder.append(input.charAt(i));break;
					}
					case integer:{
						result.add(new Query(Integer.parseInt(builder.toString()),result.isEmpty()));
						typeBuilding = Query.Type.string;
						builder = new StringBuilder();
						builder.append(input.charAt(i));break;
					}
					}
				}
				else if(numberPattern.matcher(""+input.charAt(i)).matches()){
					switch(typeBuilding){
					case string:{
						result.add(new Query(builder.toString(),result.isEmpty()));
						typeBuilding = Query.Type.integer;
						builder = new StringBuilder();
						builder.append(input.charAt(i));break;
					}
					case integer:{
						builder.append(input.charAt(i));break;
					}
					}
				}
				else {
					switch(typeBuilding){
					case string:{
						result.add(new Query(builder.toString(),result.isEmpty()));
						typeBuilding = null;
						builder = new StringBuilder();break;
					}
					case integer:{
						result.add(new Query(Integer.parseInt(builder.toString()),result.isEmpty()));
						typeBuilding = null;
						builder = new StringBuilder();break;
					}
					}
				}
			}
		}

		if(typeBuilding!=null){
			switch(typeBuilding){
			case string:{
				result.add(new Query(builder.toString(),result.isEmpty()));break;
			}
			case integer:{
				result.add(new Query(Integer.parseInt(builder.toString()),result.isEmpty()));break;
			}
			}

		}
		return result.toArray(new Query[0]);
	}
	public FinderEvent get(String input){
		if(input.isEmpty()){
			return mostRecent==null?new FinderEvent.NotFound():new FinderEvent.Found(mostRecent);
		}
		if(input.contains("/")){
			List<Info> result = new ArrayList<Info>();
			for(String orString: input.split("/")){
				result.addAll(get(orString).getInfos());
			}
			return new FinderEvent.Found(result);
		}
		input = input.toLowerCase().replaceAll("[^a-zA-Z0-9 \\t\\n\\r\\s]", " ");
		Query[] queries = getQuerries(input);
		List<Info> result;
		if(queries.length>0){
			switch(queries[0].getType()){
			case integer:{
				result = byNumber.get(queries[0].getInt());
				break;
			}
			case string:{
				result = byWord.get(queries[0].getString().toLowerCase());
				break;
			}
			default:{
				result = null;
			}
			}
		}
		else {
			return new FinderEvent.NotFound();
		}

		if(result==null||result.isEmpty()){
			return suggest(queries[0]);
		}
		else {
			List<Info> tempResult = new ArrayList<Info>();
			tempResult.addAll(result);
			result = tempResult;
			if(result.size()==1){
				return new FinderEvent.Found(result);
			}
			else {
				List<Info> finalResult = new ArrayList<Info>();
				finalResult.addAll(result);

				for(int i=1;i<queries.length;++i){
					for(int j=0;j<finalResult.size();){
						if(!finalResult.get(j).has(queries[i])){
							finalResult.remove(j);
						}
						else {
							++j;
						}
					}
					if(finalResult.size()==1){
						return new FinderEvent.Found(finalResult);
					}
				}
				if(finalResult.isEmpty()){
					return new FinderEvent.Found(result);
				}
				else {
					return new FinderEvent.Found(finalResult);
				}
			}
		}
	}
	public void found(Info newInfo){
		if(mostRecent!=null){
			if(mostRecent!=newInfo){
				if(mostRecent.getOrder()+1>=Integer.MAX_VALUE){
					int lowestOrder = Integer.MAX_VALUE;
					for(Info info:infos){
						if(info.getOrder()>-1&&info.getOrder()<lowestOrder){
							lowestOrder = info.getOrder();
						}
					}
					for(Info info:infos){
						if(info.getOrder()>-1){
							info.setOrder(info.getOrder()-lowestOrder);
						}
					}
				}
				newInfo.setOrder(mostRecent.getOrder()+1);
			}
		}
		else {
			newInfo.setOrder(0);
		}
		mostRecent = newInfo;
	}
	public void setRecentInfo(Info mostRecent) {
		this.mostRecent = mostRecent;
	}

	public void onClose(){
		try {
			saveOrder();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	public FinderEvent suggest(Query query){
		if(query.isEmpty()){
			return mostRecent==null?new FinderEvent.NotFound():new FinderEvent.Found(mostRecent);
		}
		switch(query.getType()){
		case integer: {
			List<Info> infosWithIdStartingWith = new ArrayList<Info>();
			for(Info info:infos){
				if(info.hasBuildingNumberStartingWith(new Integer(query.getInt()).toString())){
					infosWithIdStartingWith.add(info);
				}
			}
			return new FinderEvent.Found(infosWithIdStartingWith);
		}
		case string: {
			List<Info> infosWithIdStartingWith = new ArrayList<Info>();
			for(Info info:infos){
				if(info.hasWordStartingWith(query.getString())){
					infosWithIdStartingWith.add(info);
				}
			}
			return new FinderEvent.Found(infosWithIdStartingWith);
		}
		}
		return null;
	}

	public void read(String fileName) {
		Log.cat("Info("+fileName+")");
		try {
			Log.log("Reading...");
			if(Finder.loader.open("info",fileName,"dat")){
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(Finder.loader.get()));
				String line = reader.readLine();
				while(line!=null){
					Info info = Info.read(line);
					byId.put(info.getId(), info);
					info.appendToFinder(this);
					line = reader.readLine();
				}
				reader.close();
			}
			Log.log("Read");
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public void saveOrder() throws IOException{
		int numberOfInfos = infos.size();
		int[] data = new int[numberOfInfos*2+1];
		data[0] = numberOfInfos;
		int index = 1;
		for(Info info:infos){
			data[index] = info.getId();
			data[index+1] = info.getOrder();
			index += 2;
		}
		Finder.saver.open("orderings",fileName,"dat");
		Finder.saver.asInts(data);
		Finder.saver.close();
	}
	public void loadOrder(){
		try {
			if(Finder.loader.open("orderings",fileName,"dat")){
				Log.log("Ordering");
				long highestOrder = -1;
				Info highestOrderInfo = null;
				int numberOfOrders = Finder.loader.asInt();
				int[] orderData = Finder.loader.asInts(numberOfOrders*2);
				Finder.loader.close();
				for(int i=0;i<numberOfOrders;++i){
					int id = orderData[i];
					if(byId.containsKey(id)){
						byId.get(id).setOrder(orderData[i+1]);
						Info info = byId.get(id);
						if(info.getOrder()>highestOrder){
							highestOrderInfo = info;
							highestOrder = info.getOrder();
						}
					}
				}
				setRecentInfo(highestOrderInfo);
				Log.log("Ordered");
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static class Query {
		int number = 0;
		String string = null;
		public enum Type {
			string, integer;
		}
		Type type = null;
		private boolean isFirst;
		public Query(String string, boolean isFirst){
			this.string = string;
			this.type = Type.string;
			this.isFirst = isFirst;
		}
		public Query(int integer,boolean isFirst){
			number = integer;
			this.type = Type.integer;
			this.isFirst = isFirst;
		}
		public int getInt(){
			return number;
		}
		public String getString(){
			return string;
		}
		public Type getType(){
			return type;
		}
		public boolean isEmpty(){
			return string!=null&&string.isEmpty();
		}
		public boolean isFirst() {
			return isFirst;
		}
		@Override
		public String toString(){
			return "Q:"+(type==Type.string?string:number);
		}
	}

}
