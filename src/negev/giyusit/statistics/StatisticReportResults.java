/*
 * Copyright (c) 2008-2009 The Negev Project
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

import com.trolltech.qt.gui.*;

import negev.giyusit.DataTable;
import negev.giyusit.db.GenericHelper;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.row.Row;
import negev.giyusit.widgets.ChartViewer;

/**
 * A dialog that displays the results of a statistical report
 */
public class StatisticReportResults extends QDialog {

    private DataTable dataTable;
    private ChartViewer chartViewer;

    public StatisticReportResults(QWidget parent, int reportId) {
        super(parent);

        initUI();
        loadReport(reportId);
    }

    private void initUI() {
        //
        // Widgets
        //
        dataTable = new DataTable();
        dataTable.setFilterEnabled(false);

        chartViewer = new ChartViewer();

        QPushButton exportDataButton = new QPushButton(tr("Export Data..."));
        exportDataButton.setIcon(new QIcon("classpath:/icons/export.png"));
        exportDataButton.clicked.connect(dataTable, "exportData()");

        QPushButton closeButton = new QPushButton(tr("Close"));
        closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
        closeButton.clicked.connect(this, "close()");

        //
        // Layout
        //
        QHBoxLayout topLayout = new QHBoxLayout();
        topLayout.addWidget(dataTable, 1);
        topLayout.addWidget(chartViewer, 2);
        
        QHBoxLayout buttonLayout = new QHBoxLayout();
        buttonLayout.addWidget(exportDataButton);
        buttonLayout.addStretch(1);
        buttonLayout.addWidget(closeButton);

        QVBoxLayout layout = new QVBoxLayout(this);
        layout.addLayout(topLayout, 1);
        layout.addLayout(buttonLayout);
    }

    private void loadReport(int id) {
        GenericHelper helper = new GenericHelper("StatisticReports");

		try {
			Row report = helper.fetchById(id);

			// Update title
            setWindowTitle(report.getString("Name"));

            // Load report class via reflection
            Class clz = Class.forName(report.getString("Class"));

            if (!AbstractReport.class.isAssignableFrom(clz))
                throw new IllegalArgumentException(clz.getName() + " is not a valid report class");

            // Create report
            AbstractReport reportObj = (AbstractReport) clz.newInstance();

            reportObj.setName(report.getString("Name"));
            reportObj.setQuery(report.getString("Query"));
            reportObj.setRuler(report.getString("Ruler"));

            dataTable.setModel(reportObj.getModel());
            chartViewer.setChart(reportObj.getChart());

            if (chartViewer.getChart() == null) {
                chartViewer.setVisible(false);
            }
		}
		catch (Exception e) {
			MessageDialog.showException(this, e);
		}
		finally {
			helper.close();
		}
    }
}
