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

import com.google.common.collect.Lists;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.List;

import negev.giyusit.util.row.RowSet;

/**
 * Exposes a {@link RowSet} as a tabular Qt item model.
 * <br><br>
 * RowSetModel has the concept of <em>ruler</em>: a list of key names that 
 * defines the model's columns. Every row in the underlying row set is expected
 * to have the keys listed in the model's ruler.
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

	private RowSet rowSet;
	
	private Ruler ruler;
	private List<String> headers;
	private String idKey = null;
	
	/**
	 * Creates a new model instance using a specified ruler string.
	 *
	 * @param rulerString - the ruler string that will define the model's ruler
     * @see negev.giyusit.util.Ruler#Ruler(String) 
	 */
	public RowSetModel(String rulerString) {
		ruler = new Ruler(rulerString);

        // Get the primary key
        RulerEntry primary = ruler.getPrimaryKey();
        idKey = (primary == null) ? null : primary.getName();

        // Reduce the ruler to just its visible part
        ruler = ruler.getVisibleRuler();

		// Copy the ruler into the headers list
		headers = Lists.newArrayList(ruler.getKeyNames());
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
				return rowSet.rowAt(index.row()).get(ruler.getKeyName(index.column()));
			
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
