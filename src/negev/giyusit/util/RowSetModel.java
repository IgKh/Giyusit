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

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import negev.giyusit.util.row.RowSet;

/**
 * Exposes a {@link RowSet} as a tabular Qt item model.
 * <br><br>
 * RowSetModel has the concept of <em>ruler</em>: a list of key names that 
 * defines the model's columns. Every row in the underlying row set is expected
 * to have the keys listed in the model's ruler. In addition to the main ruler,
 * the model maintains an additional <em>shadow ruler</em>: a list of key names
 * that by default are not exposed to views displaying the model.
 * <br><br>
 * The model can also have an ID key, which is the primary key of the rows in
 * the row set. The ID key is exposed to views using the item data role ID_ROLE.
 *  
 * @author Igor Khanin
 */
public class RowSetModel extends QAbstractTableModel {
	
	/**
	 * The item data role used to expose the value of the model's ID key.
	 */
	public static final int ID_ROLE = Qt.ItemDataRole.UserRole + 1;
	
	// Regular expression to catch marker characters
	private static final Pattern rulerMarkerChars = Pattern.compile("(\\*|\\+)");
	
	private RowSet rowSet;
	
	private ArrayList<String> ruler;
	private ArrayList<String> shadowRuler;
	private ArrayList<String> headers;
	private String idKey;
	
	/**
	 * Creates a new model instance using a specified ruler string.
	 * <br><br>
	 * The ruler string is used to define the model's ruler. It is a comma
	 * separated list of key names. The key names can have special marker
	 * characters appended to them: The star character (*) will cause the
	 * key to be used as the model ID key, and the plus character (+) will
	 * cause the key to be added to the model's shadow ruler instead of the
	 * main ruler. More than one marker character can be applied to a key.   
	 * 
	 * @param rulerString - the ruler string that will define the model's ruler
	 */
	public RowSetModel(String rulerString) {
		if (rulerString == null)
			throw new NullPointerException("null ruler string");
		
		// Parse the ruler string, handling marker characters
		String[] arr = rulerString.split(",");
		
		ruler = new ArrayList<String>(arr.length);
		shadowRuler = new ArrayList<String>();
		
		for (String str : arr) {
			Matcher matcher = rulerMarkerChars.matcher(str);
			
			if (matcher.find()) {
				// Strip marker chars
				String key = matcher.replaceAll("");
				
				if (str.lastIndexOf('*') > 0)
					idKey = key;
				
				if (str.lastIndexOf('+') > 0)
					shadowRuler.add(key);
				else
					ruler.add(key);
			}
			else
				ruler.add(str);
		}
		
		// Copy the ruler into the headers array
		headers = new ArrayList<String>(ruler);
	}
	
	/**
	 * Returns the row set providing the data for the model.
	 * 
	 * @return The row set providing the data for the model
	 */
	public RowSet getRowSet() {
		return rowSet;
	}
	
	/**
	 * 
	 * @param rowSet
	 */
	public void setRowSet(RowSet rowSet) {
		this.rowSet = rowSet;
		
		reset();
	}
	
	@Override
	public int rowCount(QModelIndex parent) {
		if (parent != null || rowSet == null)
			return 0;
		
		return rowSet.size();
	}
	
	@Override
	public int columnCount(QModelIndex parent) { 
		if (parent != null)
			return 0;
		
		return ruler.size();
	}	
	
	@Override
	public Object data(QModelIndex index, int role) {
		if (index == null || rowSet == null)
			return null;
		
		switch (role) {
			case Qt.ItemDataRole.DisplayRole:
				return rowSet.rowAt(index.row()).get(ruler.get(index.column()));
			
			case ID_ROLE: {
				if (idKey == null)
					throw new IllegalStateException("No ID key is defined for the model");
				
				return rowSet.rowAt(index.row()).get(idKey);
			}
			
			default:
				return null;
		}
	}
	
	@Override
	public Object headerData(int section, Qt.Orientation orient, int role) {
		if (role != Qt.ItemDataRole.DisplayRole)
			return null;
		
		if (orient == Qt.Orientation.Horizontal)
			return headers.get(section); 
		else
			return (section + 1);
	}
	
	@Override
	public boolean setHeaderData(int section, Qt.Orientation orient, Object value, int role) {
		if (role != Qt.ItemDataRole.EditRole && role != Qt.ItemDataRole.DisplayRole)
			return false;
		
		if (orient != Qt.Orientation.Horizontal)
			return false;
		
		headers.set(section, (value == null) ? "" : value.toString());
		return true;
	}
}
