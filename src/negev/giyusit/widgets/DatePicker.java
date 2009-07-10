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
package negev.giyusit.widgets;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

/**
 * A custumized date picker widget
 *
 * This class is a subclass of the standard Qt QDateEdit widget, that 
 * defaults to displaying a calendar popups and the current date. 
 */
public class DatePicker extends QDateEdit {
	
	public DatePicker() {
		this(null);
	}
	
	public DatePicker(QWidget parent) {
		super(parent);
		
		setCalendarPopup(true);
		setDate(QDate.currentDate());
		
		QCalendarWidget calendar = calendarWidget();
		calendar.setGridVisible(true);
		
		// Make sure the date looks nice in every layout direction
		if (QApplication.layoutDirection() == Qt.LayoutDirection.LeftToRight)
			setDisplayFormat("dd/MM/yyyy");
		else
			setDisplayFormat("yyyy/MM/dd");
		
		// Force LTR direction on the widget itself
		setLayoutDirection(Qt.LayoutDirection.LeftToRight);
		calendar.setLayoutDirection(Qt.LayoutDirection.LeftToRight);
	}
}
