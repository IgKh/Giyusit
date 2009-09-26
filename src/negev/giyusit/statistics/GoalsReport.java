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

import java.sql.Connection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import negev.giyusit.db.ConnectionProvider;
import negev.giyusit.db.QueryWrapper;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.RowSetModel;
import negev.giyusit.util.row.BasicRow;
import negev.giyusit.util.row.Row;
import negev.giyusit.util.row.RowSet;

public class GoalsReport extends AbstractReport {
	
	private RowSet finalRowSet;
	
	public GoalsReport() {
	}
	
	@Override
	public QAbstractItemModel getModel() {
		if (getRuler() == null)
			return null;
		
		Connection conn = ConnectionProvider.getConnection();
		
		try {
			QueryWrapper wrapper = new QueryWrapper(conn);
			
			// Generate the base row set
			String sql = "select Name, Planning, ExecQuery from Goals";
			RowSet rs = wrapper.queryForRowSet(sql);
			
			// Calculate the execution property
			finalRowSet = new RowSet();
			
			for (Row row : rs) {
				String query = row.getString("ExecQuery");
				Object obj = wrapper.queryForObject(query);
				
				if (obj == null)
					continue;
				
				Row newRow = new BasicRow(row);
				newRow.put("Execution", QVariant.toInt(obj));
				
				finalRowSet.addRow(newRow);
			}
			
			// Create the model
			RowSetModel model = new RowSetModel(getRuler());
			model.setRowSet(finalRowSet);
			
			DBValuesTranslator.translateModelHeaders(model);
			
			return model;
		}
		finally {
			try { conn.close(); } catch (Exception e) { }
		}
	}
	
	@Override
	public JFreeChart getChart() {
		if (finalRowSet == null)
			return null;
		
		final String PLAN_CAT = tr("Planning");
		final String EXEC_CAT = tr("Execution");
		
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for (Row row : finalRowSet) {
			String goal = row.getString("Name");
			
			dataset.addValue(row.getInt("Planning"), PLAN_CAT, goal);
			dataset.addValue(row.getInt("Execution"), EXEC_CAT, goal);
		}
		
		return ChartFactory.createBarChart(getName(), "", "", dataset, 
								PlotOrientation.HORIZONTAL, true, true, false);
	}
}
