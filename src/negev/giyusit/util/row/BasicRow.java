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

import java.util.HashMap;
import java.util.Set;

/**
 * A basic dynamic row implementation that is backed by a hash map.
 * 
 * When an empty instance of this class is created, it contains no keys.
 * As values are put into the row, keys are created if they don't exist
 * already.
 * 
 * @author Igor Khanin
 */
public class BasicRow extends Row {
	
	private HashMap<String, Object> innerMap;
	
	/**
	 * Creates a new empty row with no keys and values.
	 */
	public BasicRow() {
		innerMap = new HashMap<String, Object>();
	}
	
	/**
	 * Creates a new row the same keys and values as the specified existing row.
	 * 
	 * @param other - the row to copy
	 * 
	 * @throws NullPointerException If the specified existing row is <code>null</code> 
	 */
	public BasicRow(Row other) {
		if (other == null)
			throw new NullPointerException("null row provided");
		
		if (other instanceof BasicRow) {
			BasicRow bs = (BasicRow) other;
			innerMap = new HashMap<String, Object>(bs.innerMap);
		}
		else {
			innerMap = new HashMap<String, Object>();
			
			for (String key : other.keySet())
				put(key, other.get(key));
		}
	}
	
	@Override
	public String toString() {
		return innerMap.toString();
	}
	
	@Override
	public Set<String> keySet() {
		return innerMap.keySet();
	}
	
	@Override
	public Object get(String key) {
		if (!innerMap.containsKey(key))
			throw new MissingKeyException(key);
		
		return innerMap.get(key);
	}
	
	@Override
	public void put(String key, Object value) {
		if (key == null)
			throw new NullPointerException("Null keys are not allowed");
		
		innerMap.put(key, value);
	}
}
