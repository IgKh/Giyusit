/*
 * Copyright (c) 2008-2011 The Negev Project
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistribution of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright notice,
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

import com.trolltech.qt.gui.*;

/**
 * A custom line edit that performs "soft validation".
 *
 * Instead of blocking invalid input, this line edit allows it but highlights
 * itself in red.
 */
public class ValidatingLineEdit extends QLineEdit {
    private QValidator softValidator;
   
    private QPalette originalPalette;
    private QPalette invalidPalette;

    public ValidatingLineEdit() {
        textChanged.connect(this, "textChanged()");

        originalPalette = palette();

        invalidPalette = new QPalette(palette());
        invalidPalette.setColor(QPalette.ColorRole.Base, new QColor("lightcoral"));
    }

    public QValidator getSoftValidator() {
        return softValidator;
    }

    public void setSoftValidator(QValidator softValidator) {
        this.softValidator = softValidator;
    }

    private void textChanged() {
        if (text().isEmpty()) {
            setPalette(originalPalette);
            return;
        }

        if (softValidator != null) {
            QValidator.State state = softValidator.validate(new QValidator.QValidationData(text(), 0));

            switch (state) {
                case Acceptable:
                    setPalette(originalPalette);
                    break;

                default:
                    setPalette(invalidPalette);
            }
        }
    }
}
