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

public class RowSetModel extends QAbstractTableModel {
	
	private RowSet rowSet;
	private String[] ruler;
	private String[] headers;
	
	/**
	 * Creates a RowSetModel using a specific ruler. The headers will
	 * be the same as the ruler elements
	 */
	public RowSetModel(String[] ruler) {
		this.ruler = ruler;
		
		// Copy the ruler into the headers
		headers = new String[ruler.length];
		
		for (int i = 0; i < ruler.length; i++)
			headers[i] = ruler[i];
	}
	
	/**
	 * Creates a RowSetModel using a specific ruler and headers
	 */
	public RowSetModel(String[] ruler, String[] headers) {
		// Ruler and headers must match
		if (ruler.length != headers.length)
			throw new IllegalArgumentException("Ruler and headers don't match");
		
		this.ruler = ruler;
		this.headers = headers;
	}
	
	public void setData(RowSet rowSet) {
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
		
		return ruler.length;
	}	
	
	@Override
	public Object data(QModelIndex index, int role) {
		if (index == null || rowSet == null || role != Qt.ItemDataRole.DisplayRole)
			return null;
		
		return rowSet.rowAt(index.row()).get(ruler[index.column()]);
	}
	
	@Override
	public Object headerData(int section, Qt.Orientation orient, int role) {
		if (role != Qt.ItemDataRole.DisplayRole)
			return null;
		
		if (orient == Qt.Orientation.Horizontal)
			return headers[section]; 
		else
			return (section + 1);
	}
	
	@Override
	public boolean setHeaderData(int section, Qt.Orientation orient, Object value, int role) {
		if (role != Qt.ItemDataRole.EditRole && role != Qt.ItemDataRole.DisplayRole)
			return false;
		
		if (orient != Qt.Orientation.Horizontal)
			return false;
		
		headers[section] = (value == null) ? "" : value.toString();
		return true;
	}
}
