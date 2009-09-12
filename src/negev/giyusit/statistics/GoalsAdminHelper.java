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

import com.trolltech.qt.gui.*;

import negev.giyusit.config.GenericAdminHelper;
import negev.giyusit.util.BasicRow;
import negev.giyusit.util.GenericItemDialog;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.RowSet;
import negev.giyusit.util.Row;

public class GoalsAdminHelper extends GenericAdminHelper {
	
	class ItemDialog extends GenericItemDialog {
		
		private QLabel name;
		private QLineEdit planning;
		
		public ItemDialog(QWidget parent) {
			super(parent);
			
			initUI();
		}
		
		private void initUI() {
			name = new QLabel();
			
			planning = new QLineEdit();
			
			QIntValidator validator = new QIntValidator(this);
			validator.setBottom(0);
			planning.setValidator(validator);
			
			// Layout
			addField(tr("Name: "), name);
			addField(tr("Planning: "), planning);
		}
		
		@Override
		public boolean validate() {
			return true;
		}
		
		@Override
		public Row toRow() {
			Row row = new BasicRow();
			
			row.put("Name", name.text());
			row.put("Planning", planning.text());
			
			return row;
		}
		
		@Override
		public void fromRow(Row row) {
			name.setText(row.getString("Name"));
			planning.setText(row.getString("Planning"));
		}
	}
	
	//--------------------------------------------------------------------------
	
	public GoalsAdminHelper() {
		super("Goals");
	}
	
	@Override
	public String[] getRuler() {
		return new String[] {"ID","Name","Planning"};
	}
	
	@Override
	public GenericItemDialog createItemDialog(QWidget parent) {
		return new ItemDialog(parent);
	}
	
	@Override
	public RowSet getValues() {
		return getQueryWrapper().queryForRowSet("select ID, Name, Planning from Goals");
	}
	
	@Override
	public void deleteItem(int id) {
		// Do nothing
	}
	
	@Override
	public boolean addRemoveAllowed() {
		return false;
	}
}
