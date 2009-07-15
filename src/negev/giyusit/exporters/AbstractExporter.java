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
package negev.giyusit.exporters;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.*;

public abstract class AbstractExporter extends QtJambiObject {

	private String outputTitle;
	private QPrinter.Orientation orientation;
	private int[] exportedColumns;
	
	public String getOutputTitle() {
		return outputTitle;
	}
	
	public void setOutputTitle(String outputTitle) {
		this.outputTitle = outputTitle;
	}
	
	public QPrinter.Orientation getOrientation() {
		return orientation;
	}
	
	public void setOrientation(QPrinter.Orientation orientation) {
		this.orientation = orientation;
	}
	
	public boolean isColumnExported(int column) {
		// If no exported columns array is set, all columns are considered
		// exported
		if (exportedColumns == null)
			return true;
		
		int i = 0;
		boolean found = false;
		
		while (i < exportedColumns.length && !found) {
			if (exportedColumns[i] == column)
				found = true;
			else
				i++;
		}
		return found;
	}
	
	public int[] getExportedColumns() {
		return exportedColumns;
	}
	
	public void setExportedColumns(int[] exportedColumns) {
		this.exportedColumns = exportedColumns;
	}
	
	public abstract void exportModel(QAbstractItemModel model, String fileName);
}
