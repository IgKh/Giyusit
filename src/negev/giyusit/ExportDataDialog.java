/*
 * Copyright (c) 2008-2011 The Negev Project
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
package negev.giyusit;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.io.File;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import negev.giyusit.exporters.AbstractExporter;
import negev.giyusit.exporters.CsvExporter;
import negev.giyusit.exporters.ExcelExporter;
import negev.giyusit.exporters.HtmlExporter;
import negev.giyusit.exporters.PdfExporter;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.widgets.DialogField;

public class ExportDataDialog extends QDialog {

	private QLineEdit title;
	private QGridLayout columnsLayout;

    private QAbstractItemModel model;

	public ExportDataDialog(QWidget parent, QAbstractItemModel model) {
		super(parent);
		
		initUI();
		
		this.model = model;
		
		// Populate columns layout
		int k = model.columnCount();
		for (int i = 0; i < k; i++) {
			String col = model.headerData(i, 
						Qt.Orientation.Horizontal).toString();
			
			QCheckBox checkBox = new QCheckBox(col);
			checkBox.setChecked(true);
			
			columnsLayout.addWidget(checkBox, i / 3, i % 3);
		}
	}
	
	public void setOutputTitle(String outputTitle) {
		title.setText(outputTitle);
	}
	
	private void initUI() {
		setWindowTitle(tr("Export Data"));
		
		//
		// Widgets
		//
		title = new QLineEdit();
		title.setText(tr("Report"));

        QPushButton saveButton = new QPushButton(tr("Save"));
		saveButton.setIcon(new QIcon("classpath:/icons/save.png"));
		saveButton.clicked.connect(this, "save()");

        QPushButton printButton = new QPushButton(tr("Print"));
		printButton.setIcon(new QIcon("classpath:/icons/print.png"));
		printButton.clicked.connect(this, "print()");

        QPushButton closeButton = new QPushButton(tr("Close"));
		closeButton.clicked.connect(this, "close()");
		
		//
		// Layout
		//
		QGroupBox exportColumnsBox = new QGroupBox(tr("Export Columns"));
		
		columnsLayout = new QGridLayout(exportColumnsBox);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addWidget(saveButton);
		buttonLayout.addWidget(printButton);
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(closeButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(new DialogField(tr("Title: "), title));
		layout.addWidget(exportColumnsBox, 1);
		layout.addLayout(buttonLayout);
	}
	
	private void save() {
		// Assemble filters list
		StringBuilder filters = new StringBuilder();
		
		filters.append(tr("Excel Workbook (*.xls)"));
		filters.append(";;").append(tr("CSV file (*.csv)"));
        filters.append(";;").append(tr("HTML file (*.html)"));
		filters.append(";;").append(tr("PDF Document (*.pdf)"));
		
		// Show file dialog
		String fileName = MessageDialog.getSaveFileName(this, tr("Save File"), 
				filters.toString());
		
		if (fileName == null || fileName.isEmpty())
			return;
		
		// Select the correct exporter based on the extension
		String ext = new QFileInfo(fileName).suffix();
		AbstractExporter exporter;
		
		if (ext.equalsIgnoreCase("XLS")) {
			exporter = new ExcelExporter();
		}
		else if (ext.equalsIgnoreCase("CSV")) {
			exporter = new CsvExporter();
		}
        else if (ext.equalsIgnoreCase("HTML")) {
			exporter = new HtmlExporter();
		}
		else if (ext.equalsIgnoreCase("PDF")) {
			exporter = new PdfExporter();
		}
		else {
			MessageDialog.showUserError(this, tr("Unknown file extension: ") + ext);
			return;
		}
		
		//		
		exporter.setOutputTitle(title.text());
		exporter.setExportedColumns(getExportedColumns());

        QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
        setEnabled(false);

        QApplication.processEvents();

		try {
			exporter.exportModel(model, fileName);
            accept();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e); 
		}
        finally {
            QApplication.restoreOverrideCursor();
        }
	}
	
	private void print() {
		// We're not really printing anything here, just generating a PDF
		// in a temporary file, and opening it in the user's favorite PDF
		// reader.
		PdfExporter exporter = new PdfExporter();
		
		exporter.setOutputTitle(title.text());
		exporter.setExportedColumns(getExportedColumns());

        QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
        setEnabled(false);

        QApplication.processEvents();
		
		try {
			String fileName = File.createTempFile("giyusit_", ".pdf").getAbsolutePath();
			exporter.exportModel(model, fileName);
			
			QDesktopServices.openUrl(QUrl.fromLocalFile(fileName));
            accept();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e); 
		}
        finally {
            QApplication.restoreOverrideCursor();
        }
	}
	
	private Set<Integer> getExportedColumns() {
		ImmutableSet.Builder<Integer> columnSetBuilder = ImmutableSet.builder();
		
		int k = columnsLayout.count();
		for (int i = 0; i < k; i++) {
			QWidget widget = columnsLayout.itemAt(i).widget();
			
			if (widget instanceof QCheckBox) {
				QCheckBox cb = (QCheckBox) widget;
				
				if (cb.isChecked()) {
					columnSetBuilder.add(i);
                }
			}
		}
        return columnSetBuilder.build();
	}
}
