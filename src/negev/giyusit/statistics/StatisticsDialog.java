/*
 * Copyright (c) 2008-2011 The Negev Project
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
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
package negev.giyusit.statistics;

import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import negev.giyusit.db.GenericHelper;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.row.Row;
import negev.giyusit.util.row.RowSet;

public class StatisticsDialog extends QDialog {
	
	private static final int ID_ROLE = Qt.ItemDataRole.UserRole;
	
	// Widgets
	private QListWidget reportList;
	
	private QTextEdit description;
	private QLabel createDate;

    private QPushButton runReportButton;

    public StatisticsDialog(QWidget parent) {
		super(parent);
		
		initUI();
		loadReportsList();
	}
	
	private void initUI() {
		setWindowTitle(tr("Statistics"));
		
		//
		// Widgets
		//
		reportList = new QListWidget();
		reportList.itemClicked.connect(this, "reportSelected(QListWidgetItem)");
        reportList.doubleClicked.connect(this, "runReport()");
		
		description = new QTextEdit();
        description.setFrameStyle(QFrame.Shape.NoFrame.value());
        description.setMaximumHeight(45);

		createDate = new QLabel();

        runReportButton = new QPushButton(tr("Run Report..."));
        runReportButton.setEnabled(false);
        runReportButton.clicked.connect(this, "runReport()");
		
		QPushButton closeButton = new QPushButton(tr("Close"));
		closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
		closeButton.clicked.connect(this, "close()");
		
		//
		// Layout
		//
        QGroupBox reportListBox = new QGroupBox(tr("Report List"));

        QVBoxLayout reportListLayout = new QVBoxLayout(reportListBox);
        reportListLayout.addWidget(reportList);

        QGroupBox reportInfoBox = new QGroupBox(tr("Report Information"));
		
		QFormLayout reportInfoLayout = new QFormLayout(reportInfoBox);
		reportInfoLayout.addRow(tr("<b>Description:</b> "), description);
		reportInfoLayout.addRow(tr("<b>Creation Date:</b> "), createDate);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addWidget(runReportButton);
        buttonLayout.addStretch(1);
		buttonLayout.addWidget(closeButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addWidget(reportListBox);
        layout.addWidget(reportInfoBox);
		layout.addLayout(buttonLayout);
	}
	
	private void loadReportsList() {
		GenericHelper helper = new GenericHelper("StatisticReports");
		
		try {
			// Clear list
			reportList.clear();
			
			// Populate list from DB
			RowSet reports = helper.fetchAll();
			
			for (Row report : reports) {
				QListWidgetItem item = new QListWidgetItem(reportList);
				
				item.setText(report.getString("Name"));
				item.setData(ID_ROLE, report.getInt("ID"));
			}
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}

    @SuppressWarnings("unused")
	private void reportSelected(QListWidgetItem item) {
		if (item == null)
			return;

        runReportButton.setEnabled(true);
		
		GenericHelper helper = new GenericHelper("StatisticReports");
		
		try {
			Row report = helper.fetchById(QVariant.toInt(item.data(ID_ROLE)));
			
			// Update report information
			description.setText(report.getString("Description"));
			createDate.setText(report.getDate("CreateDate").toString("dd/MM/yyyy"));
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}

    @SuppressWarnings("unused")
    private void runReport() {
        QListWidgetItem item = reportList.currentItem();
        if (item == null)
			return;

		int id = QVariant.toInt(item.data(ID_ROLE));

        StatisticReportResults dlg = new StatisticReportResults(this, id);

        dlg.setModal(true);
        dlg.showMaximized();
    }
}