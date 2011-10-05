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
package negev.giyusit.exporters;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.text.MessageFormat;
import java.util.List;

import com.google.common.base.Preconditions;

public class PdfExporter extends TextDocumentExporter {

	// Size constraints
	private static final int HEADER_HEIGHT = 60;
	private static final int FOOTER_HEIGHT = 60;
	private static final int SPACING = 4;
	
	// Properties
	private String footerPagesTemplate;
	private String footerDatesTemplate;

	private QPrinter printer;
	
	public PdfExporter() {
        super();

		// Create footer templates
		footerPagesTemplate = tr("Page {0} of {1}");
		
		footerDatesTemplate = MessageFormat.format(tr("Produced at {0} {1} by {2}"),
									QDate.currentDate().toString(),
									QTime.currentTime().toString(),
									System.getProperty("user.name"));
	}
	
	public void exportModel(QAbstractItemModel model, String fileName) {
		Preconditions.checkNotNull(model, "Model is null");
		
		// Setup printer
		printer = new QPrinter();
		
		printer.setOutputFormat(QPrinter.OutputFormat.PdfFormat);
		printer.setPageSize(QPrinter.PageSize.A4);
		printer.setOutputFileName(fileName);
		
		// Orientation
		if (getOrientation() == null) {
			setOrientation(QPrinter.Orientation.Portrait);
        }
		printer.setOrientation(getOrientation());
		
        // Print the PDF
        QPainter painter = new QPainter(printer);
        paintDocument(createTextDocument(model), painter);
		painter.end();
	}
	
	public void exportBatch(List<QAbstractItemModel> models, List<String> titles, String fileName) {
		Preconditions.checkArgument(models.size() == titles.size(),
                "Titles and models don't match in length");
		
		// Setup printer
		printer = new QPrinter();
		
		printer.setOutputFormat(QPrinter.OutputFormat.PdfFormat);
		printer.setPageSize(QPrinter.PageSize.A4);
		printer.setOutputFileName(fileName);
		
		// Orientation
		if (getOrientation() == null) {
			setOrientation(QPrinter.Orientation.Portrait);
        }
		printer.setOrientation(getOrientation());

        // Painting
		QPainter painter = new QPainter(printer);

		for (int i = 0; i < models.size(); i++) {
			QAbstractItemModel model = models.get(i);
			
			// Update title
			setOutputTitle(titles.get(i));
			
			// Print model
            paintDocument(createTextDocument(model), painter);

			// Unless we are at the  last model
			if (i + 1 < models.size()) {
				printer.newPage();
			}
		}
		painter.end();
	}

	//
	// Paints a text document into the painter
	//
	private void paintDocument(QTextDocument document, QPainter painter) {
        // Make room for the header and footer
        QRect innerRect = printer.pageRect();
        innerRect.setTop(innerRect.top() + HEADER_HEIGHT);
        innerRect.setBottom(innerRect.bottom() - FOOTER_HEIGHT);

        document.setPageSize(new QSizeF(innerRect.size()));

		QRect contentRect = new QRect(new QPoint(0, 0), document.size().toSize());
        QRect currentRect = new QRect(new QPoint(0, 0), innerRect.size());

        int page = 0;

        painter.save();
        painter.translate(0, FOOTER_HEIGHT);

        while (currentRect.intersects(contentRect)) {
            page++;

            // Draw page
            document.drawContents(painter, new QRectF(currentRect));
            currentRect.translate(0, currentRect.height());

            // Draw header & footer
            painter.restore();

            // Header
            int titleWidth = painter.fontMetrics().width(getOutputTitle());
            int titleX = (printer.pageRect().width() / 2) - (titleWidth / 2);
            int titleY = (HEADER_HEIGHT / 2) - (painter.fontMetrics().height() / 2);

            painter.drawText(titleX, titleY, titleWidth,
                    painter.fontMetrics().height(), 0, getOutputTitle());

            // Top separator line
            painter.drawLine(0,
                    HEADER_HEIGHT,
                    printer.pageRect().width(),
                    HEADER_HEIGHT);

            // Bottom separator line
            painter.drawLine(0,
                    printer.pageRect().height() - FOOTER_HEIGHT,
                    printer.pageRect().width(),
                    printer.pageRect().height() - FOOTER_HEIGHT);

            // Page count
            String pageCount = MessageFormat.format(footerPagesTemplate, page, document.pageCount());

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

            // Next page
            painter.save();
            painter.translate(0, -currentRect.height() * page + FOOTER_HEIGHT);

            if (currentRect.intersects(contentRect)) {
                printer.newPage();
            }
        }
        painter.restore();
	}
}
