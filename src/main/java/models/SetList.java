package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SetList<K> {
	
	private HashMap<K, Integer> hash;
	private List<K> list;
	
	public SetList(){
		
		hash = new HashMap<K, Integer>();
		list = new ArrayList<K>();
	}
	
	public SetList(HashMap<K, Integer> newHash, ArrayList<K> newList){
		
		hash = newHash;
		list = newList;
	}
	
	public boolean add(K pair){
		
		boolean success = false;
		
		if (!hash.containsKey(pair)){
			
			list.add(pair);
			hash.put(pair, list.size()-1);
			success = true;
		}
		
		return success;
	}
	
	public boolean remove(K pair){
		
		boolean success = false;
		
		if (hash.containsKey(pair)){
			
			int index = hash.get(pair);
			
			if (index != list.size()-1){
				
				list.set(index, list.get(list.size()-1));
				hash.put(list.get(list.size()-1), index);
			}
			
			list.remove(list.size()-1);
			hash.remove(pair);

			success = true;
		}
		
		return success;
	}
	
	public K getRandom(){
		
		if (list.size() > 0){
			
			Random rand = new Random();
			return list.get(rand.nextInt(list.size()));
		}
		
		else{
			
			return null;
		}
	}
	
	public boolean contains(K pair){
		
		return hash.containsKey(pair);
	}
	
	public int size(){
		
		return list.size();
	}
}