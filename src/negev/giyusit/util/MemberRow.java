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
package negev.giyusit.util;

import java.util.Set;

public class MemberRow extends Row {

	private RowFields fields;
	private Object[] data;
	
	public MemberRow(RowFields fields) {
		if (fields == null)
			throw new NullPointerException("fields is null");
		
		this.fields = fields;
		
		// Allocate data array
		this.data = new Object[fields.size()];
	}
	
	@Override
	public Set<String> keySet() {
		return fields.toSet();
	}
	
	@Override
	public Object get(String key) {
		if (key == null)
			throw new NullPointerException("Null keys are not allowed");
		
		int pos = fields.getFieldPos(key);
		if (pos < 0)
			throw new IllegalArgumentException("Key \"" + key + "\" not in row");
		
		return data[pos];
	}

	@Override
	public void put(String key, Object value) {
		if (key == null)
			throw new NullPointerException("Null keys are not allowed");
		
		int pos = fields.getFieldPos(key);
		if (pos < 0)
			throw new IllegalArgumentException("Key \"" + key + "\" not in row");
		
		data[pos] = value;
	}
}
