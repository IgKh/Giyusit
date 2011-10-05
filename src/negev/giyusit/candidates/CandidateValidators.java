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
package negev.giyusit.candidates;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import negev.giyusit.util.CitiesCompleter;

/**
 * Common validators for candidate screens
 */
public class CandidateValidators {
    private static QObject obj = new QObject();

    /**
     * A validator for gender fields. Expects a single letter gender code.
     */
   	public static final QValidator GENDER_VALIDATOR = new QRegExpValidator(
            new QRegExp(obj.tr("[MF]", "Gender Regexp")), obj);

    /**
     * A validator for an Israeli post code. Expects a five digit number.
     */
	public static final QValidator ZIP_CODE_VALIDATOR = new QRegExpValidator(
            new QRegExp("\\d{5}"), obj);

    /**
     * A validator for Israeli new-style phone numbers. Expects a two or three digit
     * prefix, and optional hyphen and a 7 digit number.
     */
	public static final QValidator PHONE_VALIDATOR = new QRegExpValidator(
            new QRegExp("0\\d{1,2}(\\-)?\\d{7}"), obj);

    /**
     * A validator for e-mail addresses.
     */
	public static final QValidator EMAIL_VALIDATOR = new QRegExpValidator(
            new QRegExp("\\S+@\\S+\\.\\S+"), obj);

    /**
     * A validator for city names
     */
    public static final QValidator CITY_VALIDATOR = new QValidator(obj) {

        @Override
        public State validate(QValidationData data) {
            String value = data.string.substring(data.position);

            if (CitiesCompleter.containsCity(value)) {
                return State.Acceptable;
            }
            else {
                return State.Invalid;
            }
        }
    };
}
