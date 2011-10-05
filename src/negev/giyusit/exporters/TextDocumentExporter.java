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

import com.google.common.base.Objects;

/**
 * A base class for data exporters that use Qt's scribe framework
 */
public abstract class TextDocumentExporter extends AbstractExporter {

    private static final boolean RTL =
            (QApplication.layoutDirection() == Qt.LayoutDirection.RightToLeft);

    private QFont headerFont;
    private QFont bodyFont;

    TextDocumentExporter() {
        headerFont = new QFont(QApplication.font());
        headerFont.setPointSize(9);
        headerFont.setBold(true);
        headerFont.setUnderline(true);

        bodyFont = new QFont(QApplication.font());
        bodyFont.setPointSize(9);
    }

    protected QTextDocument createTextDocument(QAbstractItemModel model) {
        QTextDocument document = new QTextDocument();
        document.setDefaultFont(bodyFont);
        document.setMetaInformation(QTextDocument.MetaInformation.DocumentTitle,
                getOutputTitle());

        int rowCount = model.rowCount();
        int colCount = model.columnCount();

        // Formats
        QTextOption textOption = document.defaultTextOption();
        textOption.setTextDirection(QApplication.layoutDirection());
        document.setDefaultTextOption(textOption);

        QTextTableFormat tableFormat = new QTextTableFormat();
        tableFormat.setAlignment(Qt.AlignmentFlag.AlignRight);
        tableFormat.setHeaderRowCount(1);
        tableFormat.setBorder(0);

        QTextCharFormat headerFormat = new QTextCharFormat();
        headerFormat.setFont(headerFont);

        QTextCharFormat alternateRowFormat = new QTextCharFormat();
        alternateRowFormat.setBackground(new QBrush(QColor.lightGray));

        // Table
        QTextCursor cursor = new QTextCursor(document);
        cursor.movePosition(QTextCursor.MoveOperation.Start);

        QTextTable table = cursor.insertTable(rowCount + 1, colCount, tableFormat);

        // Header
        for (int j = 0; j < colCount; j++) {
            if (!isColumnExported(j)) {
                continue;
            }

            int col = adjustColumn(j, colCount);
            String text = model.headerData(j, Qt.Orientation.Horizontal).toString();
            setCellContent(table.cellAt(0, col), text, headerFormat);
        }

        // Body
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                if (!isColumnExported(j)) {
                    continue;
                }
                Object modelData = Objects.firstNonNull(model.data(i, j), "");
                int col = adjustColumn(j, colCount);

                setCellContent(table.cellAt(i + 1, col), modelData.toString(), null);
                if (i % 2 > 0) {
                    table.cellAt(i + 1, col).setFormat(alternateRowFormat);
                }
            }
        }

        // Summery line
        String summery = tr("%n record(s) in printout", "", rowCount);

        cursor.movePosition(QTextCursor.MoveOperation.End);
        cursor.insertText("\n\n");
        cursor.insertText(summery);

        return document;
    }

    private int adjustColumn(int col, int numCols) {
        return (RTL ? (numCols - 1 - col) : col);
    }

    private void setCellContent(QTextTableCell cell, String text, QTextCharFormat format) {
        if (text != null) {
            cell.firstCursorPosition().insertText(text, format);
        }
    }
}
