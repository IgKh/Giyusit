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
package negev.giyusit.util.row;

import com.trolltech.qt.core.QDate;
import com.trolltech.qt.core.Qt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A collection of key/value pairs representing a row of data, usually
 * a single row in a database table.
 * 
 * This is an abstract base class, providing a common interface to the
 * concrete implementations as well as convenience methods that convert
 * data according to SQLite's conventions. 
 * 
 * @author Igor Khanin
 */
public abstract class Row {
	
	/**
	 * @return The set of the keys currently stored in the row
	 */
	public abstract Set<String> keySet();
	
	/**
	 * Returns the keys currently stored in the row as list, with indices and
	 * random access. The returned list is immutable.
	 * 
	 * @return A list of the keys currently stored in the row
	 */
	public List<String> keyList() {
		return Collections.unmodifiableList(new ArrayList<String>(keySet()));
	}
	
	/**
	 * Retrieves the value associated with the specified key in this row.
	 * 
	 * Implementations may throw the {@link MissingKeyException} exception
	 * if the specified key is not found in the row.
	 * 
	 * @param key - the key whose associated value is to be returned 
	 * @return The value associated with the specified key
	 */
	public abstract Object get(String key);
	
	/**
	 * Associates the specified value with the specified key in this row. If 
	 * the row already contains a value associated with the key, the old value 
	 * is replaced. 
	 * 
	 * Implementations may throw the {@link MissingKeyException} exception
	 * if the specified key is not found in the row.
	 * 
	 * @param key - the key with which the specified value is to be associated
	 * @param value - the value to be associated with the specified key
	 */
	public abstract void put(String key, Object value);
	
	/**
	 * Retrieves the value associated with the specified key in this row as a
	 * string. <code>null</code> values are converted to the empty string.
	 * 
	 * @param key - the key whose associated value is to be returned 
	 * @return The value associated with the specified key as a string
	 */
	public String getString(String key) {
		Object obj = get(key);
		
		if (obj == null)
			return "";
		
		return obj.toString();
	}
	
	/**
	 * Retrieves the value associated with the specified key in this row as an
	 * integer.
	 * 
	 * @param key - the key whose associated value is to be returned 
	 * @return The value associated with the specified key as an integer
	 * 
	 * @throws NumberFormatException If the value can't be represented as an integer
	 */
	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}
	
	/**
	 * Retrieves the value associated with the specified key in this row as a
	 * boolean value. 
	 * <br><br>
	 * This method handles booleans encoded as strings in the database using
	 * the following rules:
	 * <ul>
	 * 	<li>
	 * 		If the string is equal (ignoring case) to the literals "true" or 
	 * 		"1", it is considered to be a boolean true 
	 * 	</li>
	 * 	<li>
	 * 		Otherwise, it is considered to be a boolean false
	 * 	</li>
	 * </ul>
	 * 
	 * @param key - the key whose associated value is to be returned 
	 * @return The value associated with the specified key as a boolean
	 */
	public boolean getBoolean(String key) {
		String str = getString(key);
		
		if (str.equalsIgnoreCase("true") || str.equals("1"))
			return true;
		
		return false;
	}
	
	/**
	 * Retrieves the value associated with the specified key in this row as a
	 * Qt date object.
	 * 
	 * The method assumes that the value in the row is a string following the
	 * ISO date conventions (YYYY-MM-DD). If it is not the case, than an invalid
	 * QDate will be returned.
	 * 
	 * @param key - the key whose associated value is to be returned 
	 * @return The value associated with the specified key as a QDate
	 */
	public QDate getDate(String key) {
		String str = getString(key);
		
		return QDate.fromString(str, Qt.DateFormat.ISODate);
	}
	
	/**
	 * Associates the specified string value with the specified key in this row.
	 * 
	 * Values that are equal to the empty string will be stored as a 
	 * <code>null</code> value.
	 * 
	 * @param key - the key with which the specified value is to be associated
	 * @param value - the string value to be associated with the specified key
	 */
	public void put(String key, String value) {
		if (value == null) {
			put(key, (Object) null);
			return;
		}
		
		// Convert the empty string into NULL
		put(key, (Object) (value.isEmpty() ? null : value));
	}
	
	/**
	 * Associates the specified boolean value with the specified key in this row.
	 * 
	 * The specified value will be stored in the row as a string literal, "true" 
	 * for values equal to true values and "false" otherwise. 
	 * 
	 * @param key - the key with which the specified value is to be associated
	 * @param value - the boolean value to be associated with the specified key
	 */
	public void put(String key, boolean value) {
		put(key, (Object) (value ? "true" : "false"));
	}
	
	/**
	 * Associates the specified Qt date object with the specified key in this row.
	 * 
	 * The specified value will be stored in the row as a string using the ISO 
	 * date conventions. 
	 * 
	 * @param key - the key with which the specified value is to be associated
	 * @param value - the date value to be associated with the specified key
	 */
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