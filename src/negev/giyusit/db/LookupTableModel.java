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
package negev.giyusit.db;

import java.sql.Connection;

import negev.giyusit.util.RowSetModel;

public class LookupTableModel extends RowSetModel {
	
	public static final int KEY_COLUMN = 0;
	public static final int VALUE_COLUMN = 1;
	
	private String table;
	private String keyCol;
	private String valueCol;
	private String whereClause;
	
	public LookupTableModel(String table) {
		this(table, "ID", "Name", null);
	}
	
	public LookupTableModel(String table, String keyCol, String valueCol) {
		this(table, keyCol, valueCol, null);
	}
	
	public LookupTableModel(String table,
							String keyCol,
							String valueCol,
							String whereClause) {
		// The ruler
		super(keyCol + "*," + valueCol);
		
		this.table = table;
		this.keyCol = keyCol;
		this.valueCol = valueCol;
		this.whereClause = whereClause;
		
		//
		refresh();
	}
	
	public void refresh() {	
		// Build the SQL query
		StringBuilder sql = new StringBuilder();
		
		sql.append("select ");
		sql.append(keyCol).append(", ").append(valueCol);
		sql.append(" from ").append(table);
		
		if (whereClause != null && !whereClause.isEmpty())
			sql.append(" where ").append(whereClause);
		
		sql.append(" order by ").append(keyCol);
		
		// Do the query
		Connection conn = ConnectionProvider.getConnection();
		
		try {
			setRowSet(new QueryWrapper(conn).queryForRowSet(sql.toString()));
		}
		finally {
			try { conn.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public String rowToKey(int row) {
		if (row < 0)
			return null;
		
		return data(row, KEY_COLUMN).toString();
	}
	
	public int keyToRow(String key) {
		int row = 0;
		boolean found = false;
		
		if (key == null || key.isEmpty())
			return -1;
		
		int k = rowCount();
		while (row < k && !found) {
			if (data(row, KEY_COLUMN).toString().equals(key))
				found = true;
			else
				row++;
		}
		return (found ? row : -1);
	}
}
