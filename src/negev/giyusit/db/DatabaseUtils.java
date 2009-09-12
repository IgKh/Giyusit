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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Scanner;

import negev.giyusit.db.ConnectionProvider;

/**
 * This is a utility class containing various, mostly urelated, database
 * values and operations
 */
public class DatabaseUtils {

	/**
	 * A value containing the most recent database schema revision this
	 * version of the application is aware of.
	 */
	public static final int APPLICATIVE_SCHEMA_REVISION = 5;
	
	/*
	 *
	 */
	public static void setFileParameter(String key, String value) {
		Connection conn = ConnectionProvider.getConnection();
		
		try {
			QueryWrapper wrapper = new QueryWrapper(conn);
			String sql = "replace into FileParams set Value = ? where Key = ?";
			
			wrapper.execute(sql, new Object[] {value, key});
		}
		finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}
	
	/*
	 *
	 */
	public static String getFileParameter(String key) {
		Connection conn = ConnectionProvider.getConnection();
		
		try {
			QueryWrapper wrapper = new QueryWrapper(conn);
			String sql = "select Value from FileParams where Key = ?";
			
			Object result = wrapper.queryForObject(sql, key);
			
			return (result == null) ? null : result.toString();
		}
		finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
	}
	
	public static int getFileSchemaRevision() {
		return Integer.parseInt(getFileParameter("SchemaRevision"));
	}
	
	/**
	 *
	 */
	public static void upgradeDatabaseSchema(int fromRev, int toRev) {
		Class<?> clazz = DatabaseUtils.class;
		
		// Run all upgrade script in the specified range
		for (int i = fromRev; i < toRev; i++) {
			String scriptPath = "/sql/upgrade-" + i + "-" + (i + 1) + ".sql";
			
			runSqlScript(clazz.getResourceAsStream(scriptPath));
		}
		
		// Make sure we are OK
		assert getFileSchemaRevision() == toRev : "Schema revisions don't match after upgrade";
	}
	
	/**
	 * Initializes the current (presumably blank) database with the baseline
	 * schema revision, and then upgrades it to the latest revision
	 */
	public static void initializeDatabase() {
		Class<?> clazz = DatabaseUtils.class;
		
		// Baseline schema
		runSqlScript(clazz.getResourceAsStream("/sql/base-schema.sql"));
		runSqlScript(clazz.getResourceAsStream("/sql/base-dataviews.sql"));
		
		// Upgrade
		upgradeDatabaseSchema(getFileSchemaRevision(), APPLICATIVE_SCHEMA_REVISION);
	}

	/**
	 * Executes the SQL statements read from the provided file in the
	 * current database
	 */
	public static void runSqlScript(String fileName) {
		try {
			runSqlScript(new FileInputStream(fileName));
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Executes the SQL statements read from the provided stream in the
	 * current database.
	 *
	 * This operation is atomic - either all of the statements are successfuly
	 * executed, or none are.
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
						
					// Trigger entrace/exit control
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
					try { stmnt.close(); } catch (SQLException e) {}
				}
				if (conn != null) {
					try { conn.close(); } catch (SQLException e) {}
				}
			}
		}
		finally {
			scanner.close();
		}
	}
}
