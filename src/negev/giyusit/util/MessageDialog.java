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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A convinience class for displaying various types of messages to the user.
 * This class is used via one of its static methods.
 */
public class MessageDialog extends QMessageBox {

	private static final QSize ICON_SIZE = new QSize(48, 48);
	
	private int forcedWidth = -1;

	private MessageDialog(QWidget parent) {
		super(parent);
		
		setWindowTitle(tr("Giyusit"));
	}
		
	/**
	 * Shows a message indicating success of a proccess
	 */
	public static void showSuccess(QWidget parent, String msg) {
		MessageDialog dlg = new MessageDialog(parent);
		
		dlg.setIconPixmap(new QPixmap("classpath:/icons/ok.png").scaled(ICON_SIZE));
		dlg.setText(msg);
		
		dlg.exec();
	}
	
	/**
	 * Shows a general information message
	 */
	public static void showInformation(QWidget parent, String msg) {
		MessageDialog dlg = new MessageDialog(parent);
		
		dlg.setIconPixmap(new QPixmap("classpath:/icons/information.png").scaled(ICON_SIZE));
		dlg.setText(msg);
		
		dlg.exec();
	}
	
	/**
	 * Shows a message information the user about a wrong input or action
	 */
	public static void showUserError(QWidget parent, String msg) {
		MessageDialog dlg = new MessageDialog(parent);
		
		dlg.setIconPixmap(new QPixmap("classpath:/icons/error.png").scaled(ICON_SIZE));
		dlg.setText(msg);
		
		dlg.exec();
	}
	
	/**
	 * Displays an exception to the user in the form of an error message. The
	 * exception's stack trace is added to the message.
	 */
	public static void showException(QWidget parent, Throwable th) {
		if (th == null)
			throw new RuntimeException("Null throwable provided");
		
		MessageDialog dlg = new MessageDialog(parent);
		dlg.setIconPixmap(new QPixmap("classpath:/icons/error.png").scaled(ICON_SIZE));
		dlg.setText(dlg.tr("<b>An unexpected system error has occured</b>"));
		dlg.setInformativeText(dlg.tr("More details about the error are available " + 
								 	 	"by clicking the \"Show Details\" button"));
		
		// Extract the stack trace
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		th.printStackTrace(pw);
		dlg.setDetailedText(sw.toString());
		
		// Display the dialog
		dlg.exec();
	}
	
	/**
	 * A convinience method to display an open file dialog whose start dir
	 * is "My Documents" on Windows and the home directory on *nix systems,
	 * and takes a regular string for the filter
	 */
	public static String getOpenFileName(QWidget parent, String title, String filter) {
		return QFileDialog.getOpenFileName(parent, title, getPlatfromStartDir(),
				new QFileDialog.Filter(filter));
	}
	
	/**
	 * A convinience method to display a save file dialog whose start dir
	 * is "My Documents" on Windows and the home directory on *nix systems,
	 * and takes a regular string for the filter
	 */
	public static String getSaveFileName(QWidget parent, String title, String filter) {
		return QFileDialog.getSaveFileName(parent, title, getPlatfromStartDir(),
				new QFileDialog.Filter(filter));
	}
	
	private static String getPlatfromStartDir() {
		String os = System.getProperty("os.name").toLowerCase();
		String startDir = "";
		
		if (os.indexOf("win") != -1) {
			// On Windows
			startDir = QDesktopServices.storageLocation(QDesktopServices.StandardLocation.DocumentsLocation);
		}
		else
			startDir = System.getProperty("user.home");
		
		return startDir;
	}
}
