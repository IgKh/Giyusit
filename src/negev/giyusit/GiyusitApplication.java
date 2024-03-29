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

import java.util.Properties;

public class GiyusitApplication {
	
	private static String getVersion() {
		// The build system places a file called giyusit.properties on the 
		// class path. The file contains an entry called "version" which is
		// the application's version
		try {
			Properties props = new Properties();
			Class<?> clazz = GiyusitApplication.class;
			
			props.load(clazz.getResourceAsStream("/giyusit.properties"));
			
			return props.get("version").toString();
		}
		catch (Exception e) {
			return "<unknown>";
		}
	}
	
	public static void main(String[] args) {
		QApplication.initialize(args);
		
		//
		QApplication.setOrganizationName("The Negev Project");
		QApplication.setApplicationName("Giyusit");
		
		QApplication.setApplicationVersion(getVersion());
		
		// L&F
		QApplication.setStyle("plastique");
		
		// Translations & RTL
		QTranslator appTranslator = new QTranslator();
		
		appTranslator.load("classpath:/lang/giyusit_he.qm");
		
		QApplication.installTranslator(appTranslator);
		
		QApplication.setLayoutDirection(Qt.LayoutDirection.RightToLeft);
		
		// Main window
		String file = null;
		
		if (args.length > 0)
			file = args[0];
		
		GiyusitWindow wnd = new GiyusitWindow(file);
		wnd.show();
			
		QApplication.exec();
	}
}
