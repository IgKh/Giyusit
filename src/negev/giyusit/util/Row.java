/*
 * Copyright (c) 2008-2009 The Negev Project
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of The Negev Project nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package negev.giyusit.util;

import com.trolltech.qt.core.QDate;
import com.trolltech.qt.core.Qt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Row {
	
	private HashMap<String, Object> innerMap;
	
	public Row() {
		innerMap = new HashMap<String, Object>();
	}
	
	public Row(Row other) {
		innerMap = new HashMap<String, Object>(other.innerMap);
	}
	
	@Override
	public String toString() {
		return innerMap.toString();
	}
	
	public Set<String> keySet() {
		return innerMap.keySet();
	}
	
	public List<String> keyList() {
		return new ArrayList<String>(innerMap.keySet());
	}
	
	public Object get(String key) {
		if (!innerMap.containsKey(key))
			throw new IllegalArgumentException("Key \"" + key + "\" not in row");
		
		return innerMap.get(key);
	}
	
	/**
	 * Returns a certain value as a string. Converts NULL values into
	 * the empty string
	 */
	public String getString(String key) {
		Object obj = get(key);
		
		if (obj == null)
			return "";
		
		return obj.toString();
	}
	
	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}
	
	public boolean getBoolean(String key) {
		String str = getString(key);
		
		if (str.equalsIgnoreCase("true") || str.equals("1"))
			return true;
		
		return false;
	}
	
	public QDate getDate(String key) {
		String str = getString(key);
		
		return QDate.fromString(str, Qt.DateFormat.ISODate);
	}
	
	public void put(String key, Object value) {
		if (key == null)
			throw new NullPointerException("Null keys are not allowed");
		
		innerMap.put(key, value);
	}
	
	public void put(String key, String value) {
		if (value == null) {
			put(key, (Object) null);
			return;
		}
		
		// Convert the empty string into NULL
		put(key, (Object) (value.isEmpty() ? null : value));
	}
	
	public void put(String key, boolean value) {
		put(key, (Object) (value ? "true" : "false"));
	}
	
	public void put(String key, QDate value) {
		if (value == null) {
			put(key, (Object) null);
			return;
		}
		
		// Convert to ISO date format
		String isoDate = value.toString(Qt.DateFormat.ISODate);
		
		put(key, isoDate);
	}
}
