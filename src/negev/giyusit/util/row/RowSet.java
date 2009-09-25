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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.RandomAccess;

import negev.giyusit.util.RowSetModel;

/**
 * A collection of row objects, with both iterator and random access.
 * <br><br>
 * Note: a row set's elements don't have to be homogeneous - a row set may
 * contain rows with completely different key sets. 
 * 
 * @author Igor Khanin
 */
public class RowSet implements Iterable<Row>, RandomAccess {
	
	private ArrayList<Row> innerList;
	
	/**
	 * Creates a new empty row set.
	 */
	public RowSet() {
		innerList = new ArrayList<Row>();
	}
	
	/**
	 * Creates a new row set with the same rows as an existing row set. Note 
	 * that this is only a shallow copy - the rows themselves are not copied.
	 * 
	 * @param other - an existing row set to copy
	 * 
	 * @throws NullPointerException If the specified row set is <code>null</code>
	 */
	public RowSet(RowSet other) {
		innerList = new ArrayList<Row>(other.innerList);
	}
	
	/**
	 * Convenience constructor that wraps an existing row in a new row set. 
	 * 
	 * It is useful when a row has to be exposed as a row set, i.e. for use in
	 * {@link RowSetModel}. 
	 * 
	 * @param row - an existing row to wrap
	 * 
	 * @throws NullPointerException If the specified row is <code>null</code>
	 */
	public RowSet(Row row) {
		this();
		addRow(row);
	}
	
	/**
	 * Returns the number of rows stored in this row set. 
	 * 
	 * @return The number of rows stored in this row set
	 */
	public int size() {
		return innerList.size();
	}
	
	/**
	 * Returns the row at the specified position in this row set.
	 * 
	 * @param i - index of the row to return
	 * @return The row at the specified position in this row set
	 * 
	 *  @throws IndexOutOfBoundsException If the index is out of range
	 */
	public Row rowAt(int i) {
		return innerList.get(i);
	}
	
	/**
	 * Appends the specified row to the end of this row set. 
	 * 
	 * @param row - the row to be appended to this row set 
	 * 
	 * @throws NullPointerException If the specified row is <code>null</code>
	 */
	public void addRow(Row row) {
		if (row == null)
			throw new NullPointerException("row is null!");
		
		innerList.add(row);
	}
	
	/**
	 * Appends all of the rows in the specified row set to the end of this 
	 * row set. 
	 * 
	 * @param rowSet - row set whose elements are to be added to this list 
	 * 
	 * @throws NullPointerException If the specified row set is <code>null</code>
	 */
	public void addRowSet(RowSet rowSet) {
		if (rowSet == null)
			throw new NullPointerException("rowset is null!");
		
		innerList.addAll(rowSet.innerList);
	}
	
	public Iterator<Row> iterator() {
		return innerList.iterator();
	}
}
