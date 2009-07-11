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
package negev.giyusit.staff;

import negev.giyusit.db.GenericHelper;
import negev.giyusit.util.RowSet;
import negev.giyusit.util.Row;

public class StaffHelper extends GenericHelper {

	public StaffHelper() {
		super("Staff");
	}
	
	public RowSet getRealStaffMemebers() {
		String sql = "select * from Staff where RealInd = 'true'";
		
		return getQueryWrapper().queryForRowSet(sql);
	}
	
	public RowSet getTopLevelStaffMembers() {
		String sql = "select * from Staff where ParentID isnull";
		
		return getQueryWrapper().queryForRowSet(sql);
	}
	
	public RowSet getStaffMemberChildren(int parentId) {
		String sql = "select * from Staff where ParentID = ?";
		
		return getQueryWrapper().queryForRowSet(sql, parentId);
	}
	
	/*
	public int getOwnedCandidatesCount(int id) {
		String sql = "select count(*) from Candidates where OwnerID = ?";
		
		Object result = getQueryWrapper().queryForObject(sql, id);
		
		if (result == null)
			return 0;
		else
			return Integer.parseInt(result.toString());
	}
	*/
	
	public RowSet getOwnedCandidates(int id) {
		String sql = "select * from AllCandidates where OwnerID = ?";
		
		return getQueryWrapper().queryForRowSet(sql, id);
	}
	
	public RowSet getTreeOwnedCandidates(int id) {
		RowSet result = getOwnedCandidates(id);
		
		for (Row child : getStaffMemberChildren(id)) {
			result.addRowSet(getTreeOwnedCandidates(child.getInt("ID")));
		}
		return result;
	}
}
