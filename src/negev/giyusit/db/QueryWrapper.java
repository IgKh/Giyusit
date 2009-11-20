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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import negev.giyusit.util.row.MemberRow;
import negev.giyusit.util.row.Row;
import negev.giyusit.util.row.RowSet;

public class QueryWrapper {
 	
	private Connection conn;
	
	public QueryWrapper(Connection conn) {
		if (conn == null)
			throw new NullPointerException("Null connection provided");
		
// 		try {
// 			if (conn.isClosed())
// 				throw new IllegalArgumentException("Connection is closed");
// 		}
// 		catch (SQLException e) {}
		
		this.conn = conn;
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	public int execute(String sql, Object... params) {
		PreparedStatement stmnt = null;
		
		try {
			stmnt = conn.prepareStatement(sql);
			
			// Bind parameters
			for (int i = 0; i < params.length; i++)
				stmnt.setObject(i + 1, params[i]);
			
			// Execute the statement
			stmnt.execute();
			
			// Return the number of rows affected (or -1 if this is not a
			// DDL statement)
			return stmnt.getUpdateCount();
		}
		catch (SQLException e) {
			throw new DatabaseException("Error executing statement: " + sql, e);
		}
		finally {
			if (stmnt != null) {
				try { stmnt.close(); } catch (SQLException e) {}
			}
		}
	}
	
	public Object queryForObject(String sql, Object... params) {
		PreparedStatement stmnt = null;
		
		try {
			stmnt = conn.prepareStatement(sql);
			
			// Bind parameters
			for (int i = 0; i < params.length; i++)
				stmnt.setObject(i + 1, params[i]);
			
			// Execute the statement
			ResultSet rs = stmnt.executeQuery();
			
			if (rs.next())
				return rs.getObject(1);
			else
				return null;
		}
		catch (SQLException e) {
			throw new DatabaseException("Error executing statement: " + sql, e);
		}
		finally {
			if (stmnt != null) {
				try { stmnt.close(); } catch (SQLException e) {}
			}
		}
	}
	
	public Row queryForRow(String sql, Object... params) {
		PreparedStatement stmnt = null;
		Row result = null;
		
		try {
			stmnt = conn.prepareStatement(sql);
			
			// Bind parameters
			for (int i = 0; i < params.length; i++)
				stmnt.setObject(i + 1, params[i]);
			
			// Execute the statement
			ResultSet rs = stmnt.executeQuery();
			
			// Extract meta data
			List<String> fields = extractMetaData(rs);
			
			// Extract the row set
			if (rs.next())
				result = extractRow(rs, fields);
			
			return result;
		}
		catch (SQLException e) {
			throw new DatabaseException("Error executing statement: " + sql, e);
		}
		finally {
			if (stmnt != null) {
				try { stmnt.close(); } catch (SQLException e) {}
			}
		}
	}
	
	public RowSet queryForRowSet(String sql, Object... params) {
		PreparedStatement stmnt = null;
		RowSet result = new RowSet();
		
		try {
			stmnt = conn.prepareStatement(sql);
			
			// Bind parameters
			for (int i = 0; i < params.length; i++)
				stmnt.setObject(i + 1, params[i]);
			
			// Execute the statement
			ResultSet rs = stmnt.executeQuery();
			
			// Extract meta data
			List<String> fields = extractMetaData(rs);
			
			// Extract the row set
			while (rs.next())
				result.addRow(extractRow(rs, fields));
			
			return result;
		}
		catch (SQLException e) {
			throw new DatabaseException("Error executing statement: " + sql, e);
		}
		finally {
			if (stmnt != null) {
				try { stmnt.close(); } catch (SQLException e) {}
			}
		}
	}
	
	private List<String> extractMetaData(ResultSet rs) throws SQLException {
		List<String> fields = new ArrayList<String>();
		ResultSetMetaData metaData = rs.getMetaData();
		
		int k = metaData.getColumnCount();
		for (int i = 0; i < k; i++)
			fields.add(metaData.getColumnLabel(i + 1));
		
		return fields;
	}
	
	private Row extractRow(ResultSet rs, List<String> fields) throws SQLException {
		Row row = new MemberRow(fields);
	
		int k = fields.size();
		for (int i = 0; i < k; i++)
			row.put(fields.get(i), rs.getObject(i + 1));
		
		return row;
	}
}
