/*
 * Copyright (c) 2008-2011 The Negev Project
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

import java.sql.Connection;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import negev.giyusit.db.ConnectionProvider;
import negev.giyusit.db.QueryWrapper;

public class DBColumnCompleter extends QCompleter {

    private String column;
    private String query;
    private QLineEdit dependantEditor;

    public DBColumnCompleter(String table, String column) {
        this(table, column, "", null);
    }

	public DBColumnCompleter(String table, String column,
                             String dependantColumn, QLineEdit dependantEditor) {

        Preconditions.checkNotNull(table);
        Preconditions.checkState(
                (Strings.isNullOrEmpty(dependantColumn) && dependantEditor == null) ||
                (!Strings.isNullOrEmpty(dependantColumn) && dependantEditor != null));

        this.column = Preconditions.checkNotNull(column);

        this.dependantEditor = dependantEditor;
        this.query = createQuery(table, column, dependantColumn);

        setCompletionRole(Qt.ItemDataRole.DisplayRole);
        refresh();

        if (dependantEditor != null) {
            dependantEditor.textChanged.connect(this, "refresh()");
        }
	}

    private void refresh() {
		// Do the query
		Connection conn = ConnectionProvider.getConnection();

		try {
            QueryWrapper wrapper = new QueryWrapper(conn);
			RowSetModel model = new RowSetModel(column);

            if (dependantEditor != null) {
                model.setRowSet(wrapper.queryForRowSet(query, dependantEditor.text()));
            }
            else {
                model.setRowSet(wrapper.queryForRowSet(query));
            }
			setModel(model);
		}
		finally {
			try { conn.close(); } catch (Exception ignored) { }
		}
    }

    private String createQuery(String table, String column, String dependantColumn) {
        StringBuilder sql = new StringBuilder("select distinct ").
                append(column).
                append(" from ").
                append(table);

        if (!Strings.isNullOrEmpty(dependantColumn)) {
            sql.append(" where ").append(dependantColumn).append(" = ?");
        }
        return sql.toString();
    }
}
