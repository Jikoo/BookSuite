package com.github.Jikoo.BookSuite.struct.cache;

import java.util.HashMap;
import java.util.Iterator;

import com.github.Jikoo.BookSuite.struct.json.JsonValue;

public class Cache<K, V> implements Iterable<V>{
	@SuppressWarnings("hiding")
	private class ListItem<K,V> {
		private ListItem<K,V> parent;
		private ListItem<K,V> child;
		private V value;
		private K key;
	}
	
	int maxSize;
	HashMap<K,ListItem<K,V>> map = new HashMap<K, ListItem<K,V>>();
	
	
	ListItem<K,V> newest = new ListItem<K,V>();
	ListItem<K,V> oldest = newest;
	
	public Cache(int maxSize){
		this.maxSize = maxSize;
	}
	
	public void insert(K k, V v){
		ListItem<K,V> kv = new ListItem<K,V>();
		kv.key = k;
		kv.value = v;
		map.put(k, kv);
		newest.child = kv;
		newest = kv;
		
		if (map.size() > this.maxSize){
			ListItem<K,V> condemned = this.oldest;
			this.oldest.child.parent = null;
			this.map.remove(condemned.key);
		}
	}
	
	public void remove(K k){
		ListItem<K,V> con = map.get(k);
		ListItem<K,V> below = con.child;
		ListItem<K,V> above = con.parent;
		below.parent = above;
		above.child = below;
		map.remove(k);
	}
	
	public V get(K k){
		ListItem<K,V> con = map.get(k);
		ListItem<K,V> below = con.child;
		ListItem<K,V> above = con.parent;
		below.parent = above;
		above.child = below;
		con.child = null;
		con.parent = this.newest;
		this.newest = con;
		return this.mostRecent();
	} 
	
	public V mostRecent(){
		return this.newest.value;
	}
	
	public V leastRecent(){
		return oldest.value;
	}

	@Override
	public Iterator<V> iterator() {
		return null; 
	}
}