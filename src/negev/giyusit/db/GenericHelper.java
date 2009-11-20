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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
 
import negev.giyusit.util.row.Row;
import negev.giyusit.util.row.RowSet;
 
public class GenericHelper {
 	
 	private String tableName;
 	private QueryWrapper queryWrapper;
 	
 	public GenericHelper(String tableName) {
 		this.tableName = tableName;
 		
 		queryWrapper = new QueryWrapper(ConnectionProvider.getConnection());
 	}
 	
 	protected QueryWrapper getQueryWrapper() {
 		return queryWrapper;
 	}
 	
 	public void close() {
 		try {
 			queryWrapper.getConnection().close();
 		}
 		catch (SQLException e) {
 			throw new DatabaseException(e);
 		}
 	}
 	
 	public RowSet fetchAll() {
 		String sql = "select * from " + tableName;
 		
 		return getQueryWrapper().queryForRowSet(sql);
 	}
 	
 	public Row fetchById(int id) {
 		String sql = "select * from " + tableName + " where ROWID = ?";
 		
 		return getQueryWrapper().queryForRow(sql, id);
 	}
 	
	public void insertRecord(Row record) {
 		if (record == null)
 			throw new NullPointerException("Null record");
 		
 		Set<String> keySet = record.keySet();
 		
		// Create the query template
		String sql = createInsertTemplate(tableName, keySet);
		
		// Build values list
		ArrayList<Object> values = new ArrayList<Object>();
			
		for (String key : keySet) {
			values.add(record.get(key));
		}
		
		// Into the DB
		getQueryWrapper().execute(sql, values.toArray());
	}
	
	public void updateRecord(int id, Row record) {
		if (record == null)
			throw new NullPointerException("Null record");
		
		Set<String> keySet = record.keySet();
		
		// Create the query template
		String sql = createUpdateTemplate(tableName, keySet);
		
		// Build values list
		ArrayList<Object> values = new ArrayList<Object>();
			
		for (String key : keySet) {
			values.add(record.get(key));
		}
		
		// Append WHERE clause to update only the needed row
		sql += " where ROWID = ?";
		values.add(id);
		
		// Into the DB
		getQueryWrapper().execute(sql, values.toArray());
	}
	
	public void deleteRecord(int id) {
		String sql = "delete from " + tableName + " where ROWID = ?";
		
		getQueryWrapper().execute(sql, id);
	}
	
	/**
	 * A static helper method for generating SQL templates for INSERT
	 * statements for the provided column colection or array
	 */
	public static String createInsertTemplate(String table, Iterable<String> columns) {
		//
		StringBuilder builder = new StringBuilder();
		Iterator<String> it;
		
		builder.append("insert into ").append(table).append(" (");
		
		it = columns.iterator();
		while (it.hasNext()) {
			builder.append(it.next());
			
			if (it.hasNext())
				builder.append(',');
		}
		
		builder.append(") values (");
		
		it = columns.iterator();
		while (it.hasNext()) {
			it.next();
			builder.append('?');
			
			if (it.hasNext())
				builder.append(',');
		}
		builder.append(')');
		
		return builder.toString();
	}
	
	/**
	 * A static helper method for generating SQL templates for UPDATE
	 * statements for the provided column colection or array
	 *
	 * @note: This method returns templates that do not have WHERE clauses. It's
	 * up to the caller to append an appropriate WHERE clause, or else executing 
	 * the resulting query will update every single row in the table.
	 */
	public static String createUpdateTemplate(String table, Iterable<String> columns) {
		//
		StringBuilder builder = new StringBuilder();
		
		builder.append("update ").append(table).append(" set ");
		
		Iterator<String> it = columns.iterator();
		while (it.hasNext()) {
			builder.append(it.next()).append(" = ?");
			
			if (it.hasNext())
				builder.append(", ");
		}
		return builder.append(" ").toString();
	}
}
