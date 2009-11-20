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
package negev.giyusit.events;

import java.util.ArrayList;

import negev.giyusit.db.GenericHelper;
import negev.giyusit.util.row.Row;
import negev.giyusit.util.row.RowSet;

public class EventHelper extends GenericHelper {

	public EventHelper() {
		super("Events");
	}
	
	public RowSet getAllEventAttendants(int eventId) {
		String sql = "select * from EventCandidatesView where EventID = ?";
		
		return getQueryWrapper().queryForRowSet(sql, eventId);
	}
	
	public RowSet getActiveEventAttendants(int eventId) {
		String sql = "select * from EventCandidatesView where EventID = ? " +  
									"and ActiveInd = 'true'";
		
		return getQueryWrapper().queryForRowSet(sql, eventId);
	}
	
	public void addEventAttendance(int eventId, int candidateId) {
		String sql = "insert into EventAttendance (EventID, CandidateID) values (?, ?)";
		
		getQueryWrapper().execute(sql, eventId, candidateId);
	}
	
	public void deleteEventAttendance(int eventId, int candidateId) {
		String sql = "delete from EventAttendance where EventID = ? and CandidateID = ?";
		
		getQueryWrapper().execute(sql, eventId, candidateId);
	}
	
	public Row getAttendanceRow(int eventId, int candidateId) {
		String sql = "select * from EventAttendance where EventID = ? and CandidateID = ?";
		
		return getQueryWrapper().queryForRow(sql, eventId, candidateId);
	}
	
	public void updateAttendanceRow(int eventId, int candidateId, Row updatedRow) {
		// Get the internal row ID
		Row oldRow = getAttendanceRow(eventId, candidateId);
		
		// Do update
		String sql = createUpdateTemplate("EventAttendance", updatedRow.keySet());
		
		ArrayList<Object> values = new ArrayList<Object>();
		for (String key : updatedRow.keySet()) {
			values.add(updatedRow.get(key));
		}
		
		// Append WHERE clause to update only the needed row
		sql += " where ROWID = ?";
		values.add(oldRow.getInt("ID"));
		
		// Into the DB
		getQueryWrapper().execute(sql, values.toArray());
	}
}
