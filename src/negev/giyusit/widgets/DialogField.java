/**
 * Copyright (c) 2008-2009 The Negev Project
 */
package negev.giyusit.widgets;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class DialogField extends QWidget {
	
	public DialogField(String title, QWidget widget) {
		QLabel lbl = new QLabel(title);
		lbl.setBuddy(widget);
			
		QHBoxLayout layout = new QHBoxLayout(this);
		layout.setMargin(0);
		layout.addWidget(lbl);
		layout.addWidget(widget, 1);
	}
}
