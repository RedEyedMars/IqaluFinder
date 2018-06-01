package com.rem.ifinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rem.ifinder.image.regions.Region;

public class Info implements Comparable<Info>{

	private int id = Integer.MIN_VALUE;
	private int order = -1;
	private Set<Integer> buildingNumbers = new HashSet<Integer>();
	private Set<Character> letters = new HashSet<Character>();
	private Set<Integer> numbers = new HashSet<Integer>();
	private List<String> words = new ArrayList<String>();
	private Set<String> wordSet = new HashSet<String>();
	private Region region;
	private boolean specified = false;
	private String name = null;
	private Info(){
	}

	public static Info read(String line){
		Info info = new Info();
		info.specified = true;
		String[] fields = line.split("\\t");
		info.id = getId(Integer.parseInt(fields[1].trim()),Integer.parseInt(fields[2].trim()),Integer.parseInt(fields[3].trim()));
		String[] options = fields[0].split(",");
		for(String option:options){
			try {
				info.buildingNumbers.add(Integer.parseInt(option));
			}
			catch(NumberFormatException e){
				int indexOfDash = option.indexOf('-');
				if(indexOfDash>-1){
					String left = option.substring(0,indexOfDash);
					String right = option.substring(indexOfDash+1);
					try {
						int max = Integer.parseInt(right);
						for(int i=Integer.parseInt(left);i<=max;++i){
							info.numbers.add(i);
						}
					}
					catch (NumberFormatException f){
						char start = left.charAt(0);
						char end = right.charAt(0);
						for(;start<=end;++start){
							info.letters.add(start);
						}
					}
				}
				else {
					if(option.length()==1){
						info.letters.add(option.charAt(0));
					}
					else {
						String[] words = option.split("_");
						for(String word:words){
							if(info.wordSet.add(word)){
								info.words.add(word);
							}
						}
					}
				}
			}
		}
		return info;
	}
	public boolean has(Finder.Query query) {
		switch(query.getType()){
		case integer:{
			return buildingNumbers.contains(query.getInt())||(!query.isFirst()&&numbers.contains(query.getInt()))||(wordSet.contains(new Integer(query.getInt()).toString()));
		}
		case string:{
			if(query.getString().length()==1){
				return letters.contains(query.getString().charAt(0));
			}
			else {
				return wordSet.contains(query.getString());
			}
		}
		}
		return false;
	}
	public boolean hasBuildingNumberStartingWith(String query) {
		for(Integer buildingNumber:buildingNumbers){
			if(buildingNumber.toString().startsWith(query)){
				return true;
			}
		}
		return false;
	}
	public boolean hasWordStartingWith(String query) {
		for(String word:words){
			if(word.toLowerCase().startsWith(query)){
				return true;
			}
		}
		return false;
	}
	public void appendToFinder(Finder finder){
		for(Integer buildingNumber:buildingNumbers){
			finder.specify(buildingNumber,this);
		}
		for(String word:words){
			finder.specify(word,this);
		}
	}
	public int getId() {
		return id;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int newOrder){
		order = newOrder;
	}
	public Region getRegion(){
		return region;
	}
	public void setRegion(Region newRegion) {
		region = newRegion;
	}
	@Override
	public String toString(){
		return getName();
	}
	public String getFullDetails(){
		return id+"("+order+")"+buildingNumbers+"|"+words+"/"+letters+"/"+numbers;
	}
	public String getName(){
		if(name==null){
			StringBuilder builder = new StringBuilder();
			String space = "";
			if(buildingNumbers.size()==1){
				builder.append(buildingNumbers.iterator().next());
				space = " ";
			}
			else if(buildingNumbers.size()>1){
				int lowBuildingNumber = Integer.MAX_VALUE;
				int highBuildingNumber = Integer.MIN_VALUE;
				for(Integer buildingNumber:buildingNumbers){
					if(buildingNumber<lowBuildingNumber){
						lowBuildingNumber = buildingNumber;
					}
					if(buildingNumber>highBuildingNumber){
						highBuildingNumber = buildingNumber;
					}
				}
				builder.append(lowBuildingNumber);
				builder.append("-");
				builder.append(highBuildingNumber);
				space = " ";
			}
			if(letters.size()==1){
				builder.append(letters.iterator().next());
				space = " ";
			}
			else if(letters.size()>1){
				char lowLetter = Character.MAX_VALUE;
				char highLetter = Character.MIN_VALUE;
				for(Character letter:letters){
					if(letter<lowLetter){
						lowLetter = letter;
					}
					if(letter>highLetter){
						highLetter = letter;
					}
				}
				builder.append(lowLetter);
				builder.append("-");
				builder.append(highLetter);
				space = " ";
			}

			for(String word:words){
				builder.append(space);
				builder.append(word);
				space = " ";
			}
			if(numbers.size()==1){
				builder.append(" ");
				builder.append(numbers.iterator().next());
				space = " ";
			}
			else if(numbers.size()>1){
				builder.append(" ");
				int lowNumber = Integer.MAX_VALUE;
				int highNumber = Integer.MIN_VALUE;
				for(Integer number:numbers){
					if(number<lowNumber){
						lowNumber = number;
					}
					if(number>highNumber){
						highNumber = number;
					}
				}
				builder.append(lowNumber);
				builder.append("-");
				builder.append(highNumber);
				space = " ";
			}
			name  = builder.toString();
		}
		return name;
	}
	public static Info createFromId(int id, Region newRegion){
		if(id<0){
			throw new RuntimeException("Numberless Regions must have a specification in their building_id.dat file. Id("+id+")"+newRegion);
		}
		else if(id%10!=0){
			throw new RuntimeException("Ids with Green value != 2  must have a specification in their building_id.dat file. Id("+id+")"+newRegion);
		}
		Info info = new Info();
		info.id = id;
		info.buildingNumbers.add(id/10);
		return info;
	}
	public static int getId(int r, int g, int b) {
		int id = Integer.MIN_VALUE;
		if(g<20){
			if(b==30){
				id = r;
			}
			else if(b==32){
				id = 100+r;
			}
			else if(b>=34&&b<=36){
				id = (b-30)*100+r;
			}
			else if(b>=38&&b<50){
				id = (b-29)*100+r;
			}
			else {
				id = (b-30)*100+r;
			}
			id*=10;
			id += (g-2);
		}
		else {
			id = 1-r/10-g;
		}
		return id;
	}
	@Override
	public int compareTo(Info info) {
		if(info.order==order){
			if(info == this){
				return 0;
			}
			else {
				if(specified==info.specified){
					return 1;
				}
				else if(specified) {
					return -1;
				}
				else {
					return 1;
				}
			}
		}
		else {
			return info.order-order;
		}
	}

}
