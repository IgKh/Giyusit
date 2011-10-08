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
package negev.giyusit.candidates;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.Arrays;
import java.util.List;

import negev.giyusit.db.LookupTableModel;
import negev.giyusit.util.DBColumnCompleter;
import negev.giyusit.DataTable;

public class FindCandidatesDialog extends QDialog {
	
	public enum Mode {
		FindOnly, SingleSelect, MultiSelect
	}
	
	private Mode mode;
	
	// Widgets
	private CandidateSearchPane searchPane;
	private DataTable dataTable;
	
	// Buttons
	private QPushButton findButton;
	private QPushButton clearButton;
	private QPushButton exportButton;
	
	private QPushButton selectButton;
	private QPushButton cancelButton;
	private QPushButton closeButton;
	
	public FindCandidatesDialog(QWidget parent, Mode mode) {
		super(parent);
		
		this.mode = mode;
		
		initUI();
	}
	
	public int[] selectedCandidates() {
		QItemSelectionModel selectionModel = dataTable.selectionModel();
		
		List<QModelIndex> selectedRows = selectionModel.selectedRows();		
		int[] result = new int[selectedRows.size()];
		
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(selectedRows.get(i).data().toString());
		}
		return result;
	}
	
	private void initUI() {
		setWindowTitle(tr("Find Candidates"));
		
		//
		// Widgets
		//
		searchPane = new CandidateSearchPane();
		
		dataTable = new DataTable();
		dataTable.setFilterEnabled(false);
		
		if (mode == Mode.MultiSelect) {
			dataTable.internalView().setSelectionMode(
							QAbstractItemView.SelectionMode.ExtendedSelection);
		}
		
		dataTable.selectionModel().selectionChanged.connect(this, "selectionChanged()");
		
		findButton = new QPushButton(tr("Find"));
		findButton.clicked.connect(this, "doFind()");
		
		clearButton = new QPushButton(tr("Clear"));
		clearButton.clicked.connect(searchPane, "clear()");
		
		exportButton = new QPushButton(tr("Export Data..."));
		exportButton.setIcon(new QIcon("classpath:/icons/export.png"));
		exportButton.setEnabled(false);
		exportButton.clicked.connect(dataTable, "exportData()");
		
		selectButton = new QPushButton(tr("Select"));
		selectButton.setEnabled(false);
		selectButton.clicked.connect(this, "accept()");
		
		cancelButton = new QPushButton(tr("Cancel"));
		cancelButton.clicked.connect(this, "reject()");
		
		closeButton = new QPushButton(tr("Close"));
		closeButton.clicked.connect(this, "close()");
		
		//
		// Layout
		//
		QGroupBox searchParametersGroup = new QGroupBox(tr("Search Parameters"));
		
		QHBoxLayout searchParametersButtonLayout = new QHBoxLayout();
		searchParametersButtonLayout.addStretch(1);
		searchParametersButtonLayout.addWidget(findButton);
		searchParametersButtonLayout.addWidget(clearButton);
		
		QVBoxLayout searchParametersLayout = new QVBoxLayout(searchParametersGroup);
		searchParametersLayout.addWidget(searchPane);
		searchParametersLayout.addLayout(searchParametersButtonLayout);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addWidget(exportButton);
		buttonLayout.addStretch(1);
		
		if (mode == Mode.FindOnly) {
			buttonLayout.addWidget(closeButton);
		}
		else {
			buttonLayout.addWidget(selectButton);
			buttonLayout.addWidget(cancelButton);
		}
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(searchParametersGroup);
		layout.addWidget(dataTable);
		layout.addLayout(buttonLayout);
	}
	
	private void doFind() {
		String query = searchPane.createQuery();
		CandidateDataView dataView = 
					new CandidateDataView("", query, "@StdCandidatesRuler");
		
		dataTable.setDataView(dataView);
		exportButton.setEnabled(true);
	}
	
	private void selectionChanged() {
		QItemSelectionModel selectionModel = dataTable.selectionModel();
		
		selectButton.setEnabled(selectionModel.selectedIndexes().size() != 0);
	}
}

class CandidateSearchPane extends QWidget {
	
	private QLineEdit id;
	private QLineEdit nationalId;
	private QLineEdit firstName;
	private QLineEdit lastName;
	private QComboBox gender;
	private QComboBox status;
	
	private QLineEdit city;
	private QLineEdit origin;
    private QLineEdit subOrigin;
    private QLineEdit page;
	private QLineEdit school;
	private QComboBox owner;
	private QComboBox recruiter;
	private QCheckBox isActive;
	
	private LookupTableModel statusesModel;
	private LookupTableModel staffModel;
	
	public CandidateSearchPane() {
		QRegExp numbersOnly = new QRegExp("\\d+");
		
		// Widgets
		id = new QLineEdit();
		id.setMaximumWidth(50);
		id.setValidator(new QRegExpValidator(numbersOnly, this));
		
		nationalId = new QLineEdit();
		nationalId.setValidator(new QRegExpValidator(numbersOnly, this));
		
		firstName = new QLineEdit();
		
		lastName = new QLineEdit();
		
		gender = new QComboBox();
		gender.setMaximumWidth(35);
		gender.addItems(Arrays.asList("", tr("M"), tr("F")));
		
		status = new QComboBox();
		
		city = new QLineEdit();
		city.setCompleter(new DBColumnCompleter("Candidates", "City"));
		
		origin = new QLineEdit();
		origin.setCompleter(new DBColumnCompleter("Candidates", "Origin"));

        subOrigin = new QLineEdit();
        subOrigin.setCompleter(new DBColumnCompleter("Candidates", "SubOrigin"));

        page = new QLineEdit();
        page.setMaximumWidth(50);
		
		school = new QLineEdit();
		school.setCompleter(new DBColumnCompleter("Candidates", "School"));
		
		owner = new QComboBox();
		
		recruiter = new QComboBox();
		
		isActive = new QCheckBox(tr("Active Candidates Only"));
		
		// Lookup models
		staffModel = new LookupTableModel("Staff");
		statusesModel = new LookupTableModel("CandidateStatusValues");
		
		status.setModel(statusesModel);
		status.setModelColumn(LookupTableModel.VALUE_COLUMN);
		status.setCurrentIndex(-1);
		
		owner.setModel(staffModel);
		owner.setModelColumn(LookupTableModel.VALUE_COLUMN);
		owner.setCurrentIndex(-1);
		
		recruiter.setModel(staffModel);
		recruiter.setModelColumn(LookupTableModel.VALUE_COLUMN);
		recruiter.setCurrentIndex(-1);
		
		// Layout
		QGridLayout layout = new QGridLayout(this);
		layout.setMargin(0);
		
		layout.addWidget(new QLabel(tr("ID: ")), 			1, 1);
		layout.addWidget(id, 								1, 2);
		layout.addWidget(new QLabel(tr("National ID: ")), 	1, 3);
		layout.addWidget(nationalId, 						1, 4);
		layout.addWidget(new QLabel(tr("First Name: ")), 	1, 5);
		layout.addWidget(firstName, 						1, 6);
		layout.addWidget(new QLabel(tr("Last Name: ")), 	1, 7);
		layout.addWidget(lastName, 						1, 8);
		layout.addWidget(new QLabel(tr("Status: ")), 		1, 9);
		layout.addWidget(status, 							1, 10);
        layout.addWidget(new QLabel(tr("Owner: ")), 		1, 11);
		layout.addWidget(owner, 							1, 12);
        layout.addWidget(new QLabel(tr("Gender: ")), 		1, 13);
		layout.addWidget(gender, 							1, 14);

        layout.addWidget(new QLabel(tr("Page: ")), 		    2, 1);
		layout.addWidget(page, 							    2, 2);
        layout.addWidget(new QLabel(tr("Origin: ")), 		2, 3);
		layout.addWidget(origin, 							2, 4);
        layout.addWidget(new QLabel(tr("Sub Origin: ")), 	2, 5);
		layout.addWidget(subOrigin, 						2, 6);
		layout.addWidget(new QLabel(tr("City: ")), 			2, 7);
		layout.addWidget(city, 								2, 8);
		layout.addWidget(new QLabel(tr("School: ")), 		2, 9);
		layout.addWidget(school, 							2, 10);
		layout.addWidget(new QLabel(tr("Recruiter: ")), 	2, 11);
		layout.addWidget(recruiter, 						2, 12);
		
		layout.addWidget(isActive, 							3, 1, 1, 4);
	}
	
	public String createQuery() {
		StringBuilder query = new StringBuilder();
		
		query.append("select * from AllCandidates where 1 = 1");
		
		if (!id.text().isEmpty()) {
			query.append(" and ID = ").append(id.text());
		}
		if (!nationalId.text().isEmpty()) {
			query.append(" and NationalID = ").append(nationalId.text());
		}
		if (!firstName.text().isEmpty()) {
			query.append(" and FirstName like '").append(firstName.text()).append("'");
		}
		if (!lastName.text().isEmpty()) {
			query.append(" and LastName like '").append(lastName.text()).append("'");
		}
		if (!gender.currentText().isEmpty()) {
			query.append(" and Gender = '").append(gender.currentText()).append("'");
		}
		if (status.currentIndex() != -1) {
			String oid = statusesModel.rowToKey(status.currentIndex());
			
			query.append(" and StatusID = ").append(oid);
		}
		if (!city.text().isEmpty()) {
			query.append(" and City like '").append(city.text()).append("'");
		}
		if (!origin.text().isEmpty()) {
			query.append(" and Origin like '").append(origin.text()).append("'");
		}
        if (!subOrigin.text().isEmpty()) {
			query.append(" and SubOrigin like '").append(subOrigin.text()).append("'");
		}
        if (!page.text().isEmpty()) {
			query.append(" and Page like '").append(page.text()).append("'");
		}
		if (!school.text().isEmpty()) {
			query.append(" and School like '").append(school.text()).append("'");
		}
		if (owner.currentIndex() != -1) {
			String key = staffModel.rowToKey(owner.currentIndex());
			
			query.append(" and OwnerID = ").append(key);
		}
		if (recruiter.currentIndex() != -1) {
			String key = staffModel.rowToKey(recruiter.currentIndex());
			
			query.append(" and RecruiterID = ").append(key);
		}
		if (isActive.isChecked()) {
			query.append(" and ActiveInd = 'true'");
		}
		return query.toString();
	}
	
	public void clear() {
		id.setText("");
		nationalId.setText("");
		firstName.setText("");
		lastName.setText("");
		city.setText("");
		origin.setText("");
		school.setText("");
		
		gender.setCurrentIndex(0);
		status.setCurrentIndex(-1);
		owner.setCurrentIndex(-1);
		recruiter.setCurrentIndex(-1);
		
		isActive.setChecked(false);
	}

    @Override
    public QSize minimumSizeHint() {
        QSize oldHint = super.minimumSizeHint();

        return new QSize(oldHint.width() + 100, oldHint.height());
    }
}
