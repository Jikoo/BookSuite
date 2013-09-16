package com.github.Jikoo.BookSuite.struct.cache;

import java.util.HashMap;
import java.util.Iterator;

public class Cache<K, V> implements Iterable<V> {
	@SuppressWarnings("hiding")
	private class ListItem<K, V> {
		private ListItem<K, V> parent;
		private ListItem<K, V> child;
		private V value;
		private K key;
	}

	private int maxSize;
	private HashMap<K, ListItem<K, V>> map = new HashMap<K, ListItem<K, V>>();

	private ListItem<K, V> newest = new ListItem<K, V>();
	private ListItem<K, V> oldest = newest;

	private boolean hasChanged = true;
	private CacheIterator<V> iterator;

	public Cache(int maxSize) {
		this.maxSize = maxSize;
	}

	public void insert(K k, V v) {
		ListItem<K, V> kv = new ListItem<K, V>();
		kv.key = k;
		kv.value = v;
		map.put(k, kv);
		newest.child = kv;
		newest = kv;

		if (map.size() > this.maxSize) {
			ListItem<K, V> condemned = this.oldest;
			this.oldest.child.parent = null;
			this.map.remove(condemned.key);
		}
	}

	public void remove(K k) {
		ListItem<K, V> con = map.get(k);
		ListItem<K, V> below = con.child;
		ListItem<K, V> above = con.parent;
		below.parent = above;
		above.child = below;
		map.remove(k);
	}

	public V get(K k) {
		ListItem<K, V> con = map.get(k);
		ListItem<K, V> below = con.child;
		ListItem<K, V> above = con.parent;
		below.parent = above;
		above.child = below;
		con.child = null;
		con.parent = this.newest;
		this.newest = con;
		return this.mostRecent();
	}

	public V mostRecent() {
		return this.newest.value;
	}

	public V leastRecent() {
		return oldest.value;
	}

	@Override
	public Iterator<V> iterator() {
		if (this.hasChanged) {
			this.iterator = new CacheIterator<V>(newest);
			this.hasChanged = false;
		}
		return this.iterator;
	}

	@SuppressWarnings("hiding")
	private class CacheIterator<V> implements Iterator<V> {

		ListItem<K, V> current;

		public CacheIterator(ListItem<K, V> newest) {
			this.current = newest;
		}

		@Override
		public boolean hasNext() {
			return current != null && current.child != null;
		}

		@Override
		public V next() {
			V v = current.value;
			this.remove();
			return v;
		}

		@Override
		public void remove() {
			this.current = this.current.child;
		}

	}
}