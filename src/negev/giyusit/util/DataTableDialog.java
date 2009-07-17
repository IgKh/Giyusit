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

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import negev.giyusit.DataTable;

public class DataTableDialog extends QDialog {

	private DataTable dataTable;
	
	public DataTableDialog(QWidget parent) {
		super(parent);
		
		initUI();
	}
	
	public DataTable getDataTable() {
		return dataTable;
	}
	
	private void initUI() {
		//
		// Widgets
		//
		dataTable = new DataTable();
		dataTable.setFilterEnabled(false);
		
		QPushButton exportButton = new QPushButton(tr("Export Data..."));
		exportButton.setIcon(new QIcon("classpath:/icons/export.png"));
		exportButton.clicked.connect(dataTable, "exportData()");
		
		QPushButton closeButton = new QPushButton(tr("Close"));
		closeButton.clicked.connect(this, "close()");
		
		//
		// Layout
		//
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addWidget(exportButton);
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(closeButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(dataTable);
		layout.addLayout(buttonLayout);
	}
}
