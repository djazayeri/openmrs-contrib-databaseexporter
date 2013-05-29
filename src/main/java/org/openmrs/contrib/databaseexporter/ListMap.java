/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.contrib.databaseexporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Convenience class for creating a Map of Lists
 */
public class ListMap<K, V> implements Map<K, List<V>>  {
	
	// Internal properties
	
	protected static final long serialVersionUID = 1;
	private Map<K, List<V>> underlyingMap = null;
	
	//***** Constructors *****
	
	/**
	 * Default constructor
	 */
    public ListMap() {
    	underlyingMap = new HashMap<K, List<V>>();
    }
    
    /**
     * Constructor allowing to have an underlying LinkedHashMap
     */
    public ListMap(Boolean ordered) {
    	if (ordered) {
    		underlyingMap = new LinkedHashMap<K, List<V>>();
    	}
    	else {
    		underlyingMap = new HashMap<K, List<V>>();
    	}
    }
    
    /**
     * Constructor allowing to have an underlying TreeMap
     */
    public ListMap(Comparator<K> comparator) {
    	underlyingMap = new TreeMap<K, List<V>>(comparator);
    }
    
    //***** INSTANCE METHODS *****

	/**
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		underlyingMap.clear();
	}

	/**
	 * @see java.util.Map#containsKey(Object)
	 */
	public boolean containsKey(Object key) {
		return underlyingMap.containsKey(key);
	}

	/**
	 * @see java.util.Map#containsValue(Object)
	 */
	public boolean containsValue(Object value) {
		return underlyingMap.containsValue(value);
	}

	/**
	 * @see java.util.Map#containsValue(Object)
	 */
	public boolean containsValueInList(K key, V value) {
		List<V> l = underlyingMap.get(key);
		return l != null && l.contains(value);
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	public Set<Entry<K, List<V>>> entrySet() {
		return underlyingMap.entrySet();
	}

	/**
	 * @see java.util.Map#get(Object)
	 */
	public List<V> get(Object key) {
		return underlyingMap.get(key);
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return underlyingMap.isEmpty();
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	public Set<K> keySet() {
		return underlyingMap.keySet();
	}

	/**
	 * @see java.util.Map#put(Object, Object)
	 */
	public List<V> put(K key, List<V> value) {
		return underlyingMap.put(key, value);
	}

	/**
	 * @see java.util.Map#put(Object, Object)
	 */
	public List<V> putInList(K key, V value) {
		List<V> l = get(key);
		if (l == null) {
			l = new ArrayList<V>();
			put(key, l);
		}
		l.add(value);
		return l;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends K, ? extends List<V>> map) {
		underlyingMap.putAll(map);
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(K key, List<V> values) {
		if (values != null) {
			for (V v : values) {
				putInList(key, v);
			}
		}
	}

	/**
	 * @see java.util.Map#remove(Object)
	 */
	public List<V> remove(Object key) {
		return underlyingMap.remove(key);
	}

	/**
	 * @see java.util.Map#size()
	 */
	public int size() {
		return underlyingMap.size();
	}

	/**
	 * @see java.util.Map#values()
	 */
	public Collection<List<V>> values() {
		return underlyingMap.values();
	}
}
