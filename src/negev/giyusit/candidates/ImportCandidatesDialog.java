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
import com.trolltech.qt.gui.*;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;

import negev.giyusit.db.ConnectionProvider;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.MessageDialog;

public class ImportCandidatesDialog extends QDialog {
	
	// Columns in Candidates table updatable via this dialog
	static List<String> mappableCols = Arrays.asList(
                        "", "NationalID", "FirstName", "LastName",
                        "Gender", "HomePhone", "CellPhone",
                        "EMail", "Address", "City", "ZipCode",
                        "Origin", "School", "Notes"
    );
	
	// Widgets
	private QComboBox sheetsCombo;
	private MappingList mappingsList;
	private QProgressBar progressBar;
	
	// Buttons
	private QPushButton fileSelectButton;
	private QPushButton importButton;
	private QPushButton cancelButton;
	
	// Properties
	private Workbook currentWorkbook;
	private Sheet currentSheet;
	
	public ImportCandidatesDialog(QWidget parent) {
		super(parent);
		
		initUI();
	}
	
	private void initUI() {
		setWindowTitle(tr("Import Candidates"));
                
        //
        // Widgets
        //
        fileSelectButton = new QPushButton(tr("Select File..."));
        fileSelectButton.setIcon(new QIcon("classpath:/icons/open.png"));
        fileSelectButton.clicked.connect(this, "selectFile()");
        
        sheetsCombo = new QComboBox();
        sheetsCombo.setEnabled(false);
        sheetsCombo.currentIndexChanged.connect(this, "initMappings()");
        
        mappingsList = new MappingList();
        mappingsList.setEnabled(false);
        
        progressBar = new QProgressBar();
        progressBar.setEnabled(false);
        
        cancelButton = new QPushButton(tr("Cancel"));
        cancelButton.clicked.connect(this, "reject()");
        
        importButton = new QPushButton(tr("Import"));
        importButton.setEnabled(false);
        importButton.clicked.connect(this, "doImport()");
        
        //
       	// Layout
        //
		QHBoxLayout sheetsLayout = new QHBoxLayout();
        sheetsLayout.addWidget(new QLabel(tr("Worksheet in file: ")));
        sheetsLayout.addWidget(sheetsCombo, 1);
        
        QGroupBox mappingGroup = new QGroupBox(tr("Mapping"));
        
        QVBoxLayout mappingLayout = new QVBoxLayout(mappingGroup);
        mappingLayout.addLayout(sheetsLayout);
        mappingLayout.addWidget(mappingsList);
        
        QHBoxLayout buttonLayout = new QHBoxLayout();
        buttonLayout.addStretch(1);
        buttonLayout.addWidget(importButton);
        buttonLayout.addWidget(cancelButton);
        
        QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(fileSelectButton);
        layout.addWidget(mappingGroup, 1);
		layout.addWidget(progressBar);
        layout.addLayout(buttonLayout);
	}
	
	private void selectFile() {
		String fileName = MessageDialog.getOpenFileName(this, tr("Select Import File"), 
        						tr("Excel Workbook (*.xls)"));
        
       	if (fileName == null || fileName.isEmpty())
               return;
        
       	// Load
       	try {
       		 currentWorkbook = Workbook.getWorkbook(new File(fileName));
       	}
       	catch (Exception e) {
       		MessageDialog.showException(this, e);
       		return;
       	}
       	
       	// Update the select file button to relect the selected file
       	String label = tr("Select File ({0})...");
       	
       	//fileSelectButton.setText(MessageFormat.format(label, 
       	//									new File(fileName).getName()));
       	
        // Update the worksheets combo. The first one will be selected automatically
        sheetsCombo.setEnabled(true);
        sheetsCombo.clear();
        
        String[] sheets = currentWorkbook.getSheetNames();
        for (int i = 0; i < sheets.length; i++)
        	sheetsCombo.addItem(sheets[i]);
   	}
   	
   	private void initMappings() {
       	currentSheet = currentWorkbook.getSheet(sheetsCombo.currentIndex());
       	
       	mappingsList.setEnabled(true);
       	importButton.setEnabled(true);
       	
       	mappingsList.clear();
       	
       	int k = currentSheet.getColumns();
       	for (int i = 0; i < k; i++) {
       			String colHeader = currentSheet.getCell(i, 0).getContents();
       			String label = MessageFormat.format("{0} ({1}):", 
       										columnNumberToLetters(i + 1),
       										colHeader);
       			
       			mappingsList.addMapping(label);
       	}
	}
	
	private void doImport() {
		// Setup progress bar
		progressBar.setEnabled(true);
		progressBar.setMinimum(0);
		progressBar.setMaximum(currentSheet.getRows() - 1);
		
		cancelButton.setEnabled(false);
		
		// Do the import
		try {
			int count = doImportInternal();
			
			if (count == -1) {
				cancelButton.setEnabled(true);
				return;
			}
			
			String msg = tr("%n candidate(s) added succesfully", "", count);
			
			MessageDialog.showSuccess(this, msg); 
			accept();
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
	}
	
	private int doImportInternal() {
		int count = 0;
		
		// Assemble a list of the database columns that were mapped by the
		// user and their corresponding excel columns
		HashMap<String, Integer> mappedCols = new HashMap<String, Integer>();
		
		int k = mappingsList.count();
		for (int i = 0; i < k; i++) {
			String mapping = mappingsList.getSelectedMapping(i);
			
			if (!mapping.isEmpty())
				mappedCols.put(mapping, i);
		}
		
		// If no columns were mapped, there is no reason to continue
		if (mappedCols.isEmpty()) {
			MessageDialog.showUserError(this, tr("No mappings were defined"));
			
			return -1;
		}
			
		// Make sure the first name is mapped
		if (!mappedCols.containsKey("FirstName")) {
			MessageDialog.showUserError(this, tr("First Name column wasn't mapped"));
			
			return -1;
		}
		
		int firstNameCol = mappedCols.get("FirstName");
		
		// I need the mapped columns in a stable order, and with random access
		ArrayList<String> mappedColsList = new ArrayList<String>(mappedCols.keySet());
				
		// Create a SQL template for the mapped columns
		String sql = CandidateHelper.createInsertTemplate("Candidates", mappedColsList);
		
		// Start the database work
		Connection conn = ConnectionProvider.getConnection();
		PreparedStatement stmnt = null;
		
		try {
			stmnt = conn.prepareStatement(sql.toString());
			
			// For every row, starting from the second one
			int rows = currentSheet.getRows();
			for (int i = 1; i < rows; i++) {
				// Check if there is a first name
				String fname = currentSheet.getCell(firstNameCol, i).getContents();	
				
				if (fname == null || fname.isEmpty())
					continue;
				
				// Proccess the row
				for (int j = 0; j < mappedColsList.size(); j++) {
					String value = currentSheet.getCell(
							mappedCols.get(mappedColsList.get(j)), i).getContents();
					
					if (value == null || value.isEmpty())
						stmnt.setNull(j + 1, java.sql.Types.VARCHAR);
					else
						stmnt.setObject(j + 1, value);
				}
				stmnt.addBatch();
				
				// Update progress and count
				progressBar.setValue(i);
				count++;
				
				QApplication.processEvents();
			}
			
			// 
			conn.setAutoCommit(false);
			stmnt.executeBatch();
			conn.setAutoCommit(true);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if (stmnt != null) {
				try { stmnt.close(); } catch (Exception e) {}
			}
			
			try { conn.close(); } catch (Exception e) {}
		}
		return count;
	}
	
	/*
	 * Coverts a column number to the equivelent Excel letter designation
	 * (i.e. 5 -> E)
	 */
	private String columnNumberToLetters(int colNum) {
		// From: http://stackoverflow.com/questions/181596
		String chars = "0ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String colStr = "";
		
		while (colNum > 26) {
			int nChar = colNum % 26;
			
			if (nChar == 0)
				nChar = 26;
			
			colNum = (colNum - nChar) / 26;
			colStr = chars.charAt(nChar) + colStr;
		}
		
		if (colNum != 0)
			colStr = chars.charAt(colNum) + colStr;
		
		return colStr;
	}
}

/**
 * This widget is a list of mappings between columns in the file being 
 * imported and columns in the database
 */
class MappingList extends QScrollArea {
	
	private QVBoxLayout innerLayout;
	private QWidget innerWidget;
	
	private ArrayList<MappingWidget> mappings;
	
	public MappingList() {
		mappings = new ArrayList<MappingWidget>();
		
		// The inner widget
		innerWidget = new QWidget();
		
		innerLayout = new QVBoxLayout(innerWidget);
		innerLayout.setSizeConstraint(QLayout.SizeConstraint.SetMinAndMaxSize);
		
		// Outer widget
		setWidget(innerWidget);
	}
	
	public void addMapping(String sourceColumnLabel) {
        MappingWidget widget = new MappingWidget(sourceColumnLabel);
        
        innerLayout.addWidget(widget);
        mappings.add(widget);
    }
        
    public String getSelectedMapping(int mappingNo) {
        // Return the DB column selected as the mapping for
        // the mapping at mappingNo
		return mappings.get(mappingNo).selectedDBField();
	}
	    
    public int count() {
    	return mappings.size();
   	}
    
    public void clear() {
    	for (MappingWidget mapping : mappings)
    		mapping.disposeLater();
    		
    	mappings.clear();
    }
}

/**
 * Helper widget that represents a mapping between a DB column and an 
 * Excel column
 */
class MappingWidget extends QWidget {
	
	private QComboBox fieldBox;
	
	public MappingWidget(String xlsLabel) {
        fieldBox = new QComboBox();
        fieldBox.addItems(ImportCandidatesDialog.mappableCols);
        //self.fieldBox.addItems([""] + mappableCols.keys())
        
        // Translate - put the DB field name into a hidden role, and translate
        // the display role
        int k = fieldBox.count();
        for (int i = 0; i < k; i++) {
        	String value = fieldBox.itemText(i);
        	
        	fieldBox.setItemData(i, value, Qt.ItemDataRole.UserRole);
        	fieldBox.setItemText(i, DBValuesTranslator.translate(value));
        }
        
        QHBoxLayout layout = new QHBoxLayout(this);
        layout.setMargin(0);
        layout.addWidget(new QLabel(xlsLabel));
        layout.addWidget(fieldBox);
	}
    
    public String selectedDBField() {
    	return fieldBox.itemData(fieldBox.currentIndex()).toString();
	}
}
