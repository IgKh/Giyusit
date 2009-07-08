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
package negev.giyusit.exporters;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.text.MessageFormat;

public class PdfExporter extends AbstractExporter {
		
	// Size constraintes
	private static final int HEADER_HEIGHT = 60;
	private static final int FOOTER_HEIGHT = 60;
	private static final int SPACING = 4;
    
    // Calculated variables
    private int rowHeight;
    private int linesPerPage;
    private int pages;
    
    // Fonts
    private QFont headerFont;
    private QFont font;
    
    private QFontMetrics headerMetrics;
    private QFontMetrics fontMetrics;
	
	// Properties
	private String footerPagesTemplate;
	private String footerDatesTemplate;
	
	private int[] columnWidths;
	
	//
	private QAbstractItemModel model;
	private QPrinter printer;
	
	public PdfExporter() {
		// Create fonts
		QFont appFont = QApplication.font();
		
		headerFont = new QFont(appFont);
		headerFont.setPointSize(9);
        headerFont.setBold(true);
        headerFont.setUnderline(true);
		
        font = new QFont(appFont);
        font.setPointSize(9);
		
		headerMetrics = new QFontMetrics(headerFont);
		fontMetrics = new QFontMetrics(font);
		
		// Create footer templates
		footerPagesTemplate = tr("Page {0} of {1}");
		
		footerDatesTemplate = MessageFormat.format(tr("Produced at {0} {1} by {2}"),
									QDate.currentDate().toString(),
									QTime.currentTime().toString(),
									System.getProperty("user.name"));
	}
	
	public void exportModel(QAbstractItemModel model, String fileName) {
		if (model == null)
			throw new NullPointerException("model is null");
		
		this.model = model;
		
		// Setup printer
		printer = new QPrinter();
		
		printer.setOutputFormat(QPrinter.OutputFormat.PdfFormat);
		printer.setPageSize(QPrinter.PageSize.A4);
		printer.setOutputFileName(fileName);
		
		// Update variables
		setupPage();

        // Print the PDF
        QPainter painter = new QPainter(printer);
        
		for (int page = 1; page <= pages; page++) {
			// Paint the page
			paintPage(painter, page);
			
			if (page < pages)
				printer.newPage();
		}
		painter.end();
	}
	
	//
	// Calculate page size and other parameters
	//
	private void setupPage() {
		// Calculate row height
		rowHeight = font.pointSize() + SPACING;
		
		// Calculate number of lines per page
		linesPerPage = ((printer.pageRect().height() - HEADER_HEIGHT - 
												FOOTER_HEIGHT) / rowHeight) - 3;
		
		// Calculate number of pages
		int rowCount = model.rowCount();
		
		pages = rowCount / linesPerPage;
		
		if (rowCount % linesPerPage != 0)
			pages = pages + 1;
		
		// Calculate the minimal width of each column
		// FIXME: what if the total width is wider the the page?
		int colCount = model.columnCount();
		
		columnWidths = new int[colCount];
		
		for (int i = 0; i < colCount; i++)
			columnWidths[i] = getColumnWidth(i);
	}
	
	//
	// Calculates the minimal width of a column
	//
	private int getColumnWidth(int col) {
		int width = 0;
		
		// Get data's maximum width
		int start = 0;
		int end = model.rowCount();
		
		if (end > 1000)
			end = 1000;
		
		for (int i = 0; i < end; i++) {
			Object data = model.data(i, col);
			
			if (data == null)
				continue;
			
			int w = fontMetrics.width(data.toString());
			
			if (w > width)
				width = w;
		}
			
		// Get header's width
		String header = model.headerData(col, Qt.Orientation.Horizontal, 
											Qt.ItemDataRole.DisplayRole).toString();
		
		int w = headerMetrics.width(header);
		
		if (w > width)
			width = w;
		
		return width + 5;
	}
	
	//
	// Paints a page into the painter
	//
	private void paintPage(QPainter painter, int pageNo) {
		int colCount = model.columnCount();
		int rowCount = model.rowCount();
		
		// Paint the table header
		int colOffset = 0;
		int headerY = HEADER_HEIGHT + (SPACING * 2);
		
		painter.setFont(headerFont);
		
		for (int i = 0; i < colCount; i++) {
			if (!isColumnExported(i))
				continue;
			
			int colSize = columnWidths[i];
			
			// Get header label
			String text = model.headerData(i, Qt.Orientation.Horizontal, 
											Qt.ItemDataRole.DisplayRole).toString();
			
			// Draw the label
			painter.drawText(colOffset, headerY, colSize, rowHeight, 
								/*Qt.AlignmentFlag.AlignCenter.value()*/0, text);
			
			// Advance offset
			colOffset += colSize;
		}
		
		// Paint table rows
		int rowY;
		
		painter.setFont(font);
		
		for (int i = 0; i < linesPerPage; i++) {
			colOffset = 0;
			rowY = HEADER_HEIGHT + (rowHeight * 2) + (rowHeight * i);
			
			int rowNo = (pageNo - 1) * linesPerPage + i;
			if (rowNo > rowCount)
				break;
			
			for (int j = 0; j < colCount; j++) {
				if (!isColumnExported(j))
					continue;
				
				int colSize = columnWidths[j];
				
				// Get label
				Object obj = model.data(rowNo, j);
				String text = (obj == null) ? "" : obj.toString();
				
				// Elide text
				text = painter.fontMetrics().elidedText(text, 
									Qt.TextElideMode.ElideRight, colSize - 5);
				
				// Draw the label
				painter.drawText(colOffset, rowY, colSize, rowHeight, 0, text);
				
				// Advance offset
				colOffset += colSize;
			}
		}
		
		painter.setPen(new QPen(QColor.black, 1.0));
		
		// Paint header
		if (HEADER_HEIGHT > 0) {
			// Title
			int titleWidth = painter.fontMetrics().width(getOutputTitle());
			int titleX = (printer.pageRect().width() / 2) - (titleWidth / 2);
			int titleY = (HEADER_HEIGHT / 2) - (painter.fontMetrics().height() / 2);
			
			painter.drawText(titleX, titleY, titleWidth, 
								painter.fontMetrics().height(), 0, getOutputTitle());
			
			// Separator line
			painter.drawLine(0, 
							 HEADER_HEIGHT, 
							 printer.pageRect().width(),
							 HEADER_HEIGHT);
		}
		
		// Paint footer
		if (FOOTER_HEIGHT > 0) {
			// Spearator line
			painter.drawLine(0, 
							 printer.pageRect().height() - FOOTER_HEIGHT, 
							 printer.pageRect().width(),
							 printer.pageRect().height() - FOOTER_HEIGHT);
			
			// Pages count
			String pageCount = MessageFormat.format(footerPagesTemplate, pageNo, pages);
			
			painter.drawText(0, 
							 printer.pageRect().height() - FOOTER_HEIGHT + SPACING, 
							 painter.fontMetrics().width(pageCount), 
							 painter.fontMetrics().height(), 
							 0, 
							 pageCount);
			
			// Date
			int w = painter.fontMetrics().width(footerDatesTemplate);
			
			painter.drawText(printer.pageRect().width() - w, 
							 printer.pageRect().height() - FOOTER_HEIGHT + SPACING, 
							 w, 
							 painter.fontMetrics().height(), 
							 0, 
							 footerDatesTemplate);
		}
	}
}
