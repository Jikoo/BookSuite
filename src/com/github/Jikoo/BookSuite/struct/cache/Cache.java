/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
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

	public Cache(int maxSize) {
		this.maxSize = maxSize;
	}

	public void insert(K k, V v) {
		ListItem<K, V> kv = new ListItem<K, V>();
		kv.key = k;
		kv.value = v;
		map.put(k, kv);
		kv.child = newest;
		newest = kv;

		if (map.size() > this.maxSize) {
			this.remove(oldest.key);
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

	public int getSize() {
		return this.map.size();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Cache size: ").append(
				this.getSize()).append("\n");
		for (V v : this) {
			sb.append(v.toString()).append("\n");
		}
		return sb.toString();
	}

	public Iterator<V> iterator() {
		return new CacheIterator<V>(newest);
	}

	@SuppressWarnings("hiding")
	private class CacheIterator<V> implements Iterator<V> {

		ListItem<K, V> current;

		public CacheIterator(ListItem<K, V> newest) {
			this.current = newest;
		}

		public boolean hasNext() {
			return current != null && current.child != null;
		}

		public V next() {
			V v = current.value;
			this.remove();
			return v;
		}

		public void remove() {
			this.current = this.current.child;
		}

	}
}