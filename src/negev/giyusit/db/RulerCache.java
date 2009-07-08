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

import java.util.HashMap;
import java.sql.Connection;

import negev.giyusit.util.Row;
import negev.giyusit.util.RowSet;

public class RulerCache {

	private static HashMap<String, String> rulerCache;
	
	static {
		rulerCache = new HashMap<String, String>();
	}
	
	public static String getRulerFromCache(String key) {
		return rulerCache.get(key);
	}
	
	public static void rebuildCache() {
		rulerCache.clear();
		
		Connection conn = ConnectionProvider.getConnection();
		
		try {
			QueryWrapper wrapper = new QueryWrapper(conn);
			String sql = "select Name, Ruler from RulerLibrary";
			
			RowSet result = wrapper.queryForRowSet(sql);
			for (Row row : result) {
				rulerCache.put(row.getString("Name"), row.getString("Ruler"));
			}
		}
		finally {
			try { conn.close(); } catch (Exception e) {}
		}
	}
}
