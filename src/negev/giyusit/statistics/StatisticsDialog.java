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
package negev.giyusit.statistics;

import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import negev.giyusit.widgets.ChartViewer;
import negev.giyusit.widgets.DataGrid;

import negev.giyusit.db.ConnectionProvider;
import negev.giyusit.db.QueryWrapper;
import negev.giyusit.db.GenericHelper;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.util.RowSet;
import negev.giyusit.util.Row;
import negev.giyusit.util.MessageDialog;
import java.sql.Connection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class StatisticsDialog extends QDialog {
	
	private static final int ID_ROLE = Qt.ItemDataRole.UserRole;
	
	// Widgets
	private QListWidget reportList;
	
	private QLabel description;
	private QLabel createDate;
	private DataGrid dataGrid;
	private ChartViewer chartViewer;
	
	private QGroupBox reportInfoBox;
	private QGroupBox reportResultsBox;
	
	public StatisticsDialog(QWidget parent) {
		super(parent);
		
		initUI();
		loadReportsList();
		
		// Initialy disable report-related UI
		reportInfoBox.setEnabled(false);
		reportResultsBox.setEnabled(false);
	}
	
	private void initUI() {
		setWindowTitle(tr("Statistics"));
		
		//
		// Widgets
		//
		reportList = new QListWidget();
		reportList.itemClicked.connect(this, "reportSelected(QListWidgetItem)");
		
		description = new QLabel();
		createDate = new QLabel();
		
		dataGrid = new DataGrid();
		
		chartViewer = new ChartViewer();
		
		QPushButton closeButton = new QPushButton(tr("Close"));
		closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
		closeButton.clicked.connect(this, "close()");
		
		//
		// Layout
		//
		reportInfoBox = new QGroupBox(tr("Report Information"));
		
		QFormLayout reportInfoLayout = new QFormLayout(reportInfoBox);
		reportInfoLayout.addRow(tr("<b>Description:</b> "), description);
		reportInfoLayout.addRow(tr("<b>Creation Date:</b> "), createDate);
		
		reportResultsBox = new QGroupBox(tr("Report Results"));
		
		QHBoxLayout reportResultsLayout = new QHBoxLayout(reportResultsBox);
		reportResultsLayout.addWidget(dataGrid, 1);
		reportResultsLayout.addWidget(chartViewer, 3);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(reportInfoBox);
		mainLayout.addWidget(reportResultsBox);
		
		QHBoxLayout topLayout = new QHBoxLayout();
		topLayout.addWidget(reportList, 1);
		topLayout.addLayout(mainLayout, 3);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(closeButton);
		
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.addLayout(topLayout);
		layout.addLayout(buttonLayout);
	}
	
	private void loadReportsList() {
		GenericHelper helper = new GenericHelper("StatisticReports");
		
		try {
			// Clear list
			reportList.clear();
			
			// Popularte list from DB
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
	
	private void reportSelected(QListWidgetItem item) {
		if (item == null)
			return;
		
		// Enable report-related UI
		reportInfoBox.setEnabled(true);
		reportResultsBox.setEnabled(true);
		
		GenericHelper helper = new GenericHelper("StatisticReports");
		
		try {
			int id = QVariant.toInt(item.data(ID_ROLE));
			Row report = helper.fetchById(id);
			
			// Update report information
			description.setText(report.getString("Description"));
			createDate.setText(report.getDate("CreateDate").toString("dd/MM/yyyy"));
			
			// Create the reporting object via reflection
			Class<?> clazz = Class.forName(report.getString("Class"));
			
			if (!AbstractReport.class.isAssignableFrom(clazz))
				throw new IllegalArgumentException("Class " + clazz.getName() + " is not a report class");
			
			AbstractReport reportObj = (AbstractReport) clazz.newInstance();
			
			reportObj.setName(report.getString("Name"));
			reportObj.setQuery(report.getString("Query"));
			reportObj.setRuler(report.getString("Ruler"));
			
			// Update data grid and chart viewer
			dataGrid.setModel(reportObj.getModel());
			chartViewer.setChart(reportObj.getChart());
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
	}
}
