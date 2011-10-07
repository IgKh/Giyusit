/*
 * Copyright (c) 2008-2011 The Negev Project
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
import java.text.MessageFormat;

import com.google.common.base.Preconditions;

/**
 * A convenience class for displaying various types of messages to the user.
 * This class is used via one of its static methods.
 */
public class MessageDialog extends QMessageBox {

	public enum UserResponse {
		Save, Discard, Cancel
	}

	private static final QSize ICON_SIZE = new QSize(48, 48);
	
	private MessageDialog(QWidget parent) {
		super(parent);
		
		setWindowTitle(tr("Giyusit"));
	}

    /**
	 * Shows a message indicating success of a process
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
     * Shows a general "Are you sure?" dialog
     *
     * @param parent Parent window for the dialog
     * @param text Master text. Will be shown in bold
     * @param informativeText Extra informative text. Can be null if not needed
     * @return <i>true</i> if the user chose "yes" or <i>false</i> otherwise
     */
    public static boolean areYouSure(QWidget parent, String text, String informativeText) {
        MessageDialog dlg = new MessageDialog(parent);

        if (informativeText == null) {
            informativeText = "";
        }

        dlg.setText("<b>" + text + "</b>");
        dlg.setInformativeText(informativeText);
        dlg.setIconPixmap(new QPixmap("classpath:/icons/warning.png").scaled(ICON_SIZE));

		dlg.setStandardButtons(StandardButton.Yes, StandardButton.No);
        dlg.setDefaultButton(StandardButton.No);

        int ret = dlg.exec();
        return (ret == StandardButton.Yes.value());
    }
	
	/**
	 * Displays an exception to the user in the form of an error message. The
	 * exception's stack trace is added to the message.
	 */
	public static void showException(QWidget parent, Throwable th) {
        Preconditions.checkNotNull(th, "Null throwable provided");
		
		MessageDialog dlg = new MessageDialog(parent);
		dlg.setIconPixmap(new QPixmap("classpath:/icons/error.png").scaled(ICON_SIZE));

        dlg.setText(dlg.tr("<b>An unexpected system error has occurred</b>"));
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
	 * Warns the user about unsaved data in the current dialog, and asks
	 * him how to proceed.
	 *
	 * If the <i>firstEdit</i> argument is a non-null, valid date/time object,
	 * the message box will contain a human-friendly duration since the first
	 * edit in the dialog
	 *
	 * Possible return values are:
	 *  - Save    (if the user chose to save all data and close the dialog)
	 *  - Discard (if the user chose not to save, but still close the dialog)
	 *  - Cancel  (if the user chose to stay in the dialog and not close it)
	 */
	public static UserResponse warnDirty(QWidget parent, QDateTime firstEdit) {
		MessageDialog dlg = new MessageDialog(parent);
		
		dlg.setIconPixmap(new QPixmap("classpath:/icons/warning.png").scaled(ICON_SIZE));
		dlg.setText(dlg.tr("<b>Save changes in the dialog before closing?</b>"));
		
		if (firstEdit != null && firstEdit.isValid()) {
			int secDiff = firstEdit.secsTo(QDateTime.currentDateTime());
			
			// Format diff string
			String diffStr;
			int diff;
			
			if (secDiff < 60) { // Under one minute
				diff = secDiff;
				diffStr = dlg.tr("the last %n second(s)", "", diff);
			}
			else if (secDiff < 3600) { // Under one hour
				diff = secDiff / 60;
				diffStr = dlg.tr("the last %n minute(s)", "", diff);
			}
			else if (secDiff < 86400) { // Under 24 hours (one day)
				diff = secDiff / 3600;
				diffStr = dlg.tr("the last %n hour(s)", "", diff);
			}
			else { // Over one day
				diff = secDiff / 86400;
				diffStr = dlg.tr("the last %n day(s)", "", diff);
			}
			
			// Format final message text
			String text = MessageFormat.format(
				dlg.tr("If you don''t save, all changes from {0} will be lost"),
				diffStr);
			
			dlg.setInformativeText(text);
		}
		else
			dlg.setInformativeText(dlg.tr("If you don't save, all changes will be lost"));
		
		// Buttons
		QPushButton save = dlg.addButton(dlg.tr("&Save"), QMessageBox.ButtonRole.AcceptRole);
		QPushButton discard = dlg.addButton(QMessageBox.StandardButton.Discard);
		QPushButton cancel = dlg.addButton(QMessageBox.StandardButton.Cancel);
		
		save.setIcon(new QIcon("classpath:/icons/save.png"));
		cancel.setIcon(new QIcon("classpath:/icons/cancel.png"));
		
        dlg.exec();
		
        // Extract result
		if (dlg.clickedButton() == save)
			return UserResponse.Save;
		else if (dlg.clickedButton() == discard)
			return UserResponse.Discard;
		else
			return UserResponse.Cancel;
	}
	
	/**
	 * A convenience method to display an open file dialog whose start dir
	 * is "My Documents" on Windows and the home directory on *nix systems,
	 * and takes a regular string for the filter
	 */
	public static String getOpenFileName(QWidget parent, String title, String filter) {
		return QFileDialog.getOpenFileName(parent, title, getPlatformStartDir(),
				new QFileDialog.Filter(filter));
	}
	
	/**
	 * A convenience method to display a save file dialog whose start dir
	 * is "My Documents" on Windows and the home directory on *nix systems,
	 * and takes a regular string for the filter
	 */
	public static String getSaveFileName(QWidget parent, String title, String filter) {
		return QFileDialog.getSaveFileName(parent, title, getPlatformStartDir(),
				new QFileDialog.Filter(filter));
	}
	
	private static String getPlatformStartDir() {
		String os = System.getProperty("os.name").toLowerCase();

		if (os.contains("win")) {
			// On Windows
			return QDesktopServices.storageLocation(QDesktopServices.StandardLocation.DocumentsLocation);
		}
		else {
			return System.getProperty("user.home");
        }
	}
}
