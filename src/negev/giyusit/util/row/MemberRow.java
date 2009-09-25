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
package negev.giyusit.util.row;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A row implementation that uses an external set of keys.
 * <br><br>
 * Objects of this class are created with a reference to an existing list of
 * key names that can't be changed afterwards. Values can't be put in the row
 * unless the key they associate to appears in list of keys. 
 * <br><br>
 * This implementation is very memory efficient, and is meant to be used as
 * a member of a large {@link RowSet} (hence the name). 
 * 
 * @author Igor Khanin
 */
public class MemberRow extends Row {

	private List<String> keys;
	private Object[] data;
	
	/**
	 * Creates new empty row with the specified allowed key list
	 * 
	 * @param keys A reference to a list of allowed key names
	 * 
	 * @throws NullPointerException If the specified key list is <code>null</code>
	 */
	public MemberRow(List<String> keys) {
		if (keys == null)
			throw new NullPointerException("keys is null");
		
		this.keys = keys;
		
		// Allocate data array
		this.data = new Object[keys.size()];
	}
	
	@Override
	public Set<String> keySet() {
		return new HashSet<String>(keys);
	}
	
	@Override
	public Object get(String key) {
		if (key == null)
			throw new NullPointerException("Null keys are not allowed");
		
		int pos = keys.indexOf(key);
		if (pos < 0)
			throw new MissingKeyException(key);
		
		return data[pos];
	}

	@Override
	public void put(String key, Object value) {
		if (key == null)
			throw new NullPointerException("Null keys are not allowed");
		
		int pos = keys.indexOf(key);
		if (pos < 0)
			throw new MissingKeyException(key);
		
		data[pos] = value;
	}
}
