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
package negev.giyusit.candidates;

import com.trolltech.qt.core.*;

import negev.giyusit.db.GenericHelper;
import negev.giyusit.util.RowSet;

public class CandidateHelper extends GenericHelper {

	public CandidateHelper() {
		super("Candidates");
	}
	
	public String candidateFullName(int candidateId) {
		String sql = "select (FirstName || coalesce(' ' || LastName, '')) " + 
								"from Candidates where ID = ?";
		
		return getQueryWrapper().queryForObject(sql, candidateId).toString();
	}
	
	public String candidateStatusName(int candidateId) {
		String sql = "select Status from AllCandidates where ID = ?";
		
		return getQueryWrapper().queryForObject(sql, candidateId).toString();
	}
	
	public RowSet getCandidateStatuses(int candidateId) {
		String sql = "select * from StatusesView where CandidateID = ?";
		
		return getQueryWrapper().queryForRowSet(sql, candidateId);
	}
	
	/**
	 * A method that checks if the candidate already has a status
	 * whose start date is <i>date</i>
	 */
	public boolean hasStatusInDate(int candidateId, QDate date) {
		String sql = "select 1 from CandidateStatuses where StartTime = ? " + 
							"and CandidateID = ?";
		
		// The database stores dates in the ISO format
		String isoDate = date.toString(Qt.DateFormat.ISODate);
		
		Object result = getQueryWrapper().queryForObject(sql, isoDate, candidateId);
		return (result != null);
	}
	
	public void addStatus(int candidateId, int statusId, QDate startDate) {
		String sql = "insert into CandidateStatuses " + 
						"(CandidateID, PriStatusID, StartTime) VALUES (?,?,?)";
		
		String isoDate = startDate.toString(Qt.DateFormat.ISODate);
		
		getQueryWrapper().execute(sql, 
								new Object[] {candidateId, statusId, isoDate});
	}
	
	public void removeLastStatus(int candidateId) {
		String sql = "delete from CandidateStatuses where CandidateID = ? " + 
						"and StartTime = (select max(StartTime) from CandidateStatuses where CandidateID= ?)";
		
		getQueryWrapper().execute(sql, new Object[] {candidateId, candidateId});
	}
}
