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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public class CsvExporter extends AbstractExporter {
	
	// Regex used to test if a value needs escaping
	private static final QRegExp escapeRegex = new QRegExp("(,|\\n|\\r|\")");
	
	public void exportModel(QAbstractItemModel model, String fileName) {
		try {
			// Create streams
			BufferedWriter buffer = new BufferedWriter(new FileWriter(fileName));
			PrintWriter printer = new PrintWriter(buffer);
						
			// Write header row
			int colNum = model.columnCount();
			int rowNum = model.rowCount();
			
			StringBuilder header = new StringBuilder();
			
			for (int i = 0; i < colNum; i++) {
				if (!isColumnExported(i))
					continue;
				
				String col = model.headerData(i, Qt.Orientation.Horizontal).toString();
				
				header.append(escapeField(col));
				
				if (i + 1 < colNum)
					header.append(',');
			}
			printer.println(header.toString());
			
			// Write the rest
			for (int i = 0; i < rowNum; i++) {
				StringBuilder builder = new StringBuilder();
				
				for (int j = 0; j < colNum; j++) {
					if (!isColumnExported(j))
						continue;
					
					Object obj = model.data(i, j);
					String str = (obj == null) ? "" : obj.toString();
					
					builder.append(escapeField(str));
				
					if (j + 1 < colNum)
						builder.append(',');
				}
				printer.println(builder.toString());
			}
			
			// Close
			printer.close();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// Escapes a field value according to the rules of the Excel-flavored 
	// CSV format. From: http://www.creativyst.com/Doc/Articles/CSV/CSV01.htm
	private String escapeField(String value) {
		// Stage 1: Use a regexp to see if there are any characters that need
		// escaping in the value
		if (escapeRegex.indexIn(value) == -1)
			return value;
		
		// Stage 2: Escape double-quotes
		value = value.replace("\"", "\"\"");
		
		// Stage 3: Return the value surrounded by double quotes
		return ("\"" + value + "\"");
	}
}
