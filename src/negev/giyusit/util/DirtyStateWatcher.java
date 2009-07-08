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
package negev.giyusit.util;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class DirtyStateWatcher extends QSignalMapper {
	
	private boolean dirty = false;
	
	public Signal1<Boolean> dirtyChanged = new Signal1<Boolean>();
	
	public DirtyStateWatcher(QObject parent) {
		super(parent);
		
		this.mappedQObject.connect(this, "signalEmited()");
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
		
		dirtyChanged.emit(dirty);
	}
	
	public boolean watchWidget(QWidget widget) {
		if (widget instanceof QLineEdit) {
			QLineEdit le = (QLineEdit) widget;
			
			le.textChanged.connect(this, "map()");
			setMapping(le, le);
			
			return true;
		}
		else if (widget instanceof QTextEdit) {
			QTextEdit te = (QTextEdit) widget;
			
			te.textChanged.connect(this, "map()");
			setMapping(te, te);
			
			return true;
		}
		else if (widget instanceof QComboBox) {
			QComboBox cb = (QComboBox) widget;
			
			cb.currentIndexChanged.connect(this, "map()");
			setMapping(cb, cb);
			
			return true;
		}
		else if (widget instanceof QAbstractButton) {
			QAbstractButton ab = (QAbstractButton) widget;
			
			ab.toggled.connect(this, "map()");
			setMapping(ab, ab);
			
			return true;
		}
		return false;
	}
	
	private void signalEmited() {
		dirty = true;
		
		dirtyChanged.emit(dirty);
	}
}
