/*
 * Copyright (c) 2008-2011 The Negev Project
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Scanner;

import com.google.common.base.Objects;

/**
 * This is a utility class containing various, mostly unrelated, database
 * values and operations
 */
public class DatabaseUtils {

	/**
	 *  Writes a value to the current file's parameters table. If the parameter
     *  <i>key</i> doesn't exist yet, it will be created.
     *
     * @param key The name of the parameter to update
     * @param value The new value for the parameter
     */
	public static void setFileParameter(String key, String value) {
		Connection conn = ConnectionProvider.getConnection();
		
		try {
			QueryWrapper wrapper = new QueryWrapper(conn);
			String sql = "replace into FileParams(Key, Value) values(?,?)";
			
			wrapper.execute(sql, key, value);
		}
		finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException ignored) {}
			}
		}
	}
	
	/**
	 *  Reads a value from the current file's parameters table.
     *
     * @param key Parameter to fetch
     * @return Value of the parameter or the empty string if it doesn't exist
     */
	public static String getFileParameter(String key) {
		Connection conn = ConnectionProvider.getConnection();
		
		try {
			QueryWrapper wrapper = new QueryWrapper(conn);
			String sql = "select Value from FileParams where Key = ?";
			
			Object result = wrapper.queryForObject(sql, key);
			
			return Objects.firstNonNull(result, "").toString();
		}
		finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException ignored) {}
			}
		}
	}

	/**
	 * Executes the SQL statements read from the provided stream in the
	 * current database.
	 *
	 * This operation is atomic - either all of the statements are successfully
	 * executed, or none are.
     *
     * @param stream Stream to read SQL from
     */
	public static void runSqlScript(InputStream stream) {
		Scanner scanner = new Scanner(stream, "UTF-8");
		
		try {
			// Get connection
			Connection conn = ConnectionProvider.getConnection();
			Statement stmnt = null;
			
			try {
				// Create JDBC statement and enter transaction
				stmnt = conn.createStatement();
				
				conn.setAutoCommit(false);
				
				// Start parsing file
				StringBuilder buffer = new StringBuilder();
				boolean inTrigger = false;
				int lineNo = 0;
								
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine().trim();
					lineNo++;
					
					// Skip empty lines and comments
					if (line.isEmpty() || line.startsWith("--"))
						continue;
						
					// Trigger entrance/exit control
					String upper = line.toUpperCase();
					
					if (upper.startsWith("CREATE TRIGGER"))
						inTrigger = true;
					else if (upper.startsWith("END"))
						inTrigger = false;
					
					buffer.append(line).append(' ');
					
					// Do we have a complete statement
					if (!inTrigger && buffer.lastIndexOf(";") != -1) {
						try {
							stmnt.executeUpdate(buffer.toString());
						}
						catch (SQLException e) {
							// Abort
							conn.rollback();
							
							// Wrap exception with context information
							String err = "Error executing statement in line " +
											lineNo + ": " + buffer.toString();
							
							throw new DatabaseException(err, e);
						}
						
						// Clear buffer
						buffer.delete(0, buffer.length());
					}
				}
				
				// Commit transaction
				conn.setAutoCommit(true);
			}
			catch (SQLException e) {
				throw new DatabaseException(e);
			}
			finally {
				if (stmnt != null) {
					try { stmnt.close(); } catch (SQLException ignored) {}
				}
				if (conn != null) {
					try { conn.close(); } catch (SQLException ignored) {}
				}
			}
		}
		finally {
			scanner.close();
		}
	}
}
