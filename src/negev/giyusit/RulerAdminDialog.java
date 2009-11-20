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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.trolltech.qt.gui.*;

import negev.giyusit.db.RulerCache;

public class RulerAdminDialog extends QDialog {

    private QComboBox rulerList;
    private QListWidget hiddenKeys;
    private QListWidget visibleKeys;

    public RulerAdminDialog(QWidget parent) {
        super(parent);

        initUI();

        // Initialize data
        rulerList.addItems(Lists.newArrayList(RulerCache.getRulers()));
    }

    private void initUI() {
        setWindowTitle(tr("Ruler Admin"));

        //
        // Widgets
        //
        rulerList = new QComboBox();
        rulerList.activated.connect(this, "rulerSelected(String)");

        hiddenKeys = new QListWidget();

        visibleKeys = new QListWidget();

        //
        // Layout
        //
        QHBoxLayout topLayout = new QHBoxLayout();
        topLayout.addWidget(new QLabel(tr("Ruler: ")));
        topLayout.addWidget(rulerList, 1);

        QHBoxLayout centralLayout = new QHBoxLayout();
        centralLayout.addWidget(hiddenKeys);
        centralLayout.addWidget(visibleKeys);

        QVBoxLayout layout = new QVBoxLayout(this);
        layout.addLayout(topLayout);
        layout.addLayout(centralLayout, 1);
    }

    private void rulerSelected(String name) {
        String ruler = RulerCache.getRulerFromCache(name);

        hiddenKeys.clear();
        visibleKeys.clear();

        // Parse ruler
        // TODO: This duplicates code with RowSetModel. See if can unify
        Pattern markerPattern = Pattern.compile("(\\*|\\+)");

        String[] arr = rulerString.split(",");

		List<String> visibleKeysList = Lists.newArrayList();
		List<String> hiddenKeysList = Lists.newArrayList();
		shadowRuler = new ArrayList<String>();

		for (String str : arr) {
			Matcher matcher = rulerMarkerChars.matcher(str);

			if (matcher.find()) {
				// Strip marker chars
				String key = matcher.replaceAll("");

				if (str.lastIndexOf('*') > 0)
					idKey = key;

				if (str.lastIndexOf('+') > 0)
					shadowRuler.add(key);
				else
					ruler.add(key);
			}
			else
				ruler.add(str);
		}

    }
}
