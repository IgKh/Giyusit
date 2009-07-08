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
package negev.giyusit;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import negev.giyusit.widgets.DataGrid;

public class DataTable extends QWidget {
	
	private DataGrid dataGrid;
	private QLabel statusLabel;
	
	private QWidget filterWidget;
	private QLineEdit filterLine;
	
	//
	private DataView currentDataView;
	private DataTableProxyModel proxyModel;
		
	public DataTable() {
		this(null);
	}
		
	public DataTable(QWidget parent) {
		super(parent);
		
		proxyModel = new DataTableProxyModel(this);
		proxyModel.setDynamicSortFilter(true);
		
		//
		// Widgets
		//
		dataGrid = new DataGrid();
		dataGrid.setModel(proxyModel);
		dataGrid.activated.connect(this, "indexActivated(QModelIndex)");
			
		statusLabel = new QLabel();
			
		filterLine = new QLineEdit();
		filterLine.returnPressed.connect(this, "doFilter()");
		
		/*
		QPushButton filterGoButton = new QPushButton();
		filterGoButton.setFlat(true);
		filterGoButton.setIconSize(new QSize(16, 16));
		
		if (QApplication.isRightToLeft())
			filterGoButton.setIcon(new QIcon("classpath:/icons/arrow-left.png"));
		else
			filterGoButton.setIcon(new QIcon("classpath:/icons/arrow-right.png"));
		
		filterGoButton.clicked.connect(this, "doFilter()");
		*/
		
		//
		// Shortcuts
		//
		QShortcut gotoFilter = new QShortcut(new QKeySequence(tr("F7")), 
									this, Qt.ShortcutContext.WindowShortcut);
		
		gotoFilter.activated.connect(filterLine, "setFocus()");
		gotoFilter.activated.connect(filterLine, "selectAll()");
		
		QShortcut gotoDataGrid = new QShortcut(new QKeySequence(tr("F8")), 
									this, Qt.ShortcutContext.WindowShortcut);
		
		gotoDataGrid.activated.connect(dataGrid, "setFocus()");
			
		//
		// Layout
		//
		filterWidget = new QWidget();
		
		QHBoxLayout filterLayout = new QHBoxLayout(filterWidget);
		filterLayout.addWidget(new QLabel(tr("Quick Filter: ")));
		filterLayout.addWidget(filterLine);
		filterLayout.addSpacing(4);
			
		QHBoxLayout topLayout = new QHBoxLayout();
		topLayout.addWidget(statusLabel, 2);
		topLayout.addWidget(filterWidget, 1);
			
		QVBoxLayout layout = new QVBoxLayout(this);
		layout.setMargin(0);
		layout.addLayout(topLayout);
		layout.addWidget(dataGrid);
	}
	
	public QAbstractItemView internalView() {
		return dataGrid;
	}
	
	public QItemSelectionModel selectionModel() {
		return dataGrid.selectionModel();
	}
	
	public boolean isFilterEnabled() {
		return filterWidget.isVisible();
	}
	
	public void setFilterEnabled(boolean enabled) {
		filterWidget.setVisible(enabled);
	}
	
	public boolean isStatusEnabled() {
		return statusLabel.isVisible();
	}
	
	public void setStatusEnabled(boolean enabled) {
		statusLabel.setVisible(enabled);
	}
	
	public void setDataView(DataView dataView) {
		currentDataView = dataView;
		
		refresh();
	}
	
	public void setModel(QAbstractItemModel model) {
		currentDataView = null;
		
		setModelInternal(model);
	}
	
	public void refresh() {
		if (currentDataView != null) {
			// Get the model from the data view 
			QAbstractItemModel model = null;
			
			try {
				model = currentDataView.getModel();
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}
			
			//
			setModelInternal(model);
		}
	}
	
	public void exportData() {
		ExportDataDialog dlg = new ExportDataDialog(window(), proxyModel);
		
		if (currentDataView != null)
			dlg.setOutputTitle(currentDataView.getName());
		
		dlg.exec();
	}
	
	private void updateStatusLabel() {
		int sourceCount = proxyModel.sourceModel().rowCount();
		int filteredCount = proxyModel.rowCount();
		
		String msg = tr("%n record(s) displayed", "", filteredCount);
		
		if (!filterLine.text().isEmpty()) {
			msg += " ";
			
			if (sourceCount == 1) {
				msg += tr("(out of one)");
			}
			else {
				msg += tr("(out of %n)", "", sourceCount);
			}
		}
		statusLabel.setText(msg);
	}
	
	private void setModelInternal(QAbstractItemModel model) {
		proxyModel.setSourceModel(model);
		
		if (model != null) {
			// Adjust all columns
			int k = model.columnCount();
			for (int i = 0; i < k; i++)
				dataGrid.resizeColumnToContents(i);
			
			//
			updateStatusLabel();
		}
		else
			statusLabel.setText("");
	}
	
	private void doFilter() {
		proxyModel.setFilterFixedString(filterLine.text());
		updateStatusLabel();
	}
	
	private void indexActivated(QModelIndex index) {
		if (currentDataView == null)
			return;
		
		boolean refreshNeeded = currentDataView.showItemDialog(window(), index);
		
		if (refreshNeeded)
			refresh();
	}
}

class DataTableProxyModel extends QSortFilterProxyModel {

	public DataTableProxyModel(QObject parent) {
		super(parent);
	}
	
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (sourceParent != null)
			return false;
		
		// Check every column in the source row. One match is enough
		int k = sourceModel().columnCount();
		for (int i = 0; i < k; i++) {
			Object data = sourceModel().data(sourceRow, i);
			
			if (data == null)
				continue;
			
			if (filterRegExp().indexIn(data.toString()) != -1)
				return true;
		}
		return false;
	}
}
