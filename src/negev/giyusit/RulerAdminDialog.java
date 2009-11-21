/*
 * Copyright (c) 2008-2009 The Negev Project
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
package negev.giyusit;

import com.trolltech.qt.QVariant;
import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import negev.giyusit.db.RulerCache;
import negev.giyusit.util.DBValuesTranslator;
import negev.giyusit.util.MessageDialog;
import negev.giyusit.util.Ruler;
import negev.giyusit.util.RulerEntry;

public class RulerAdminDialog extends QDialog {

    private static final int ORIGINAL_NAME_ROLE = Qt.ItemDataRole.UserRole + 1;
    private static final int IS_PRIMARY_ROLE = Qt.ItemDataRole.UserRole + 2;

    private QComboBox rulerList;
    private QListWidget hiddenKeys;
    private QListWidget visibleKeys;

    public RulerAdminDialog(QWidget parent) {
        super(parent);

        initUI();

        // Initialize data
        for (String ruler : RulerCache.getRulers()) {
            rulerList.addItem(DBValuesTranslator.translate(ruler));
            rulerList.setItemData(rulerList.count() - 1, ruler);
        }
        rulerSelected(0);
    }

    private void initUI() {
        setWindowTitle(tr("Ruler Admin"));

        //
        // Widgets
        //
        rulerList = new QComboBox();
        rulerList.currentIndexChanged.connect(this, "rulerSelected(int)");

        hiddenKeys = new QListWidget();
        hiddenKeys.itemDoubleClicked.connect(this, "makeVisible()");

        visibleKeys = new QListWidget();
        visibleKeys.itemDoubleClicked.connect(this, "makeHidden()");

        QPushButton makeVisibleButton = new QPushButton(">>");
        makeVisibleButton.setToolTip(tr("Make key visible"));
        makeVisibleButton.setMaximumWidth(35);
        makeVisibleButton.clicked.connect(this, "makeVisible()");

        QPushButton makeHiddenButton = new QPushButton("<<");
        makeHiddenButton.setToolTip(tr("Make key hidden"));
        makeHiddenButton.setMaximumWidth(35);
        makeHiddenButton.clicked.connect(this, "makeHidden()");

        QPushButton moveUpButton = new QPushButton(tr("Move Up"));
        moveUpButton.clicked.connect(this, "moveUp()");

        QPushButton moveDownButton = new QPushButton(tr("Move Down"));
        moveDownButton.clicked.connect(this, "moveDown()");

        QPushButton saveButton = new QPushButton(tr("Save"));
        saveButton.setIcon(new QIcon("classpath:/icons/save.png"));
        saveButton.clicked.connect(this, "save()");

        QPushButton restoreButton = new QPushButton(tr("Restore"));
        restoreButton.setIcon(new QIcon("classpath:/icons/revert.png"));
        restoreButton.clicked.connect(this, "restore()");

        QPushButton closeButton = new QPushButton(tr("Close"));
        closeButton.setIcon(new QIcon("classpath:/icons/close.png"));
        closeButton.clicked.connect(this, "close()");

        //
        // Layout
        //
        QHBoxLayout topLayout = new QHBoxLayout();
        topLayout.addWidget(new QLabel(tr("Ruler: ")));
        topLayout.addWidget(rulerList, 1);

        QGroupBox hiddenKeysBox = new QGroupBox(tr("Hidden Keys"));

        QVBoxLayout hiddenKeyLayout = new QVBoxLayout(hiddenKeysBox);
        hiddenKeyLayout.addWidget(hiddenKeys);

        QGroupBox visibleKeysBox = new QGroupBox(tr("Visible Keys"));

        QHBoxLayout visibleKeysButtonLayout = new QHBoxLayout();
        visibleKeysButtonLayout.addStretch(1);
        visibleKeysButtonLayout.addWidget(moveUpButton);
        visibleKeysButtonLayout.addWidget(moveDownButton);
        visibleKeysButtonLayout.addStretch(1);

        QVBoxLayout visibleKeysLayout = new QVBoxLayout(visibleKeysBox);
        visibleKeysLayout.addWidget(visibleKeys, 1);
        visibleKeysLayout.addLayout(visibleKeysButtonLayout);

        QVBoxLayout middleButtonsLayout = new QVBoxLayout();
        middleButtonsLayout.addStretch(1);
        middleButtonsLayout.addWidget(makeVisibleButton);
        middleButtonsLayout.addWidget(makeHiddenButton);
        middleButtonsLayout.addStretch(1);

        QHBoxLayout centralLayout = new QHBoxLayout();
        centralLayout.addWidget(hiddenKeysBox);
        centralLayout.addLayout(middleButtonsLayout);
        centralLayout.addWidget(visibleKeysBox);

        QHBoxLayout buttonLayout = new QHBoxLayout();
        buttonLayout.addWidget(saveButton);
        buttonLayout.addWidget(restoreButton);
        buttonLayout.addStretch(1);
        buttonLayout.addWidget(closeButton);

        QVBoxLayout layout = new QVBoxLayout(this);
        layout.addLayout(topLayout);
        layout.addLayout(centralLayout, 1);
        layout.addLayout(buttonLayout);
    }

    @SuppressWarnings("unused")
    private void rulerSelected(int index) {
        if(rulerList.itemData(index) == null)
            return;

        hiddenKeys.clear();
        visibleKeys.clear();

        // Get untranslated ruler name and parse it
        String name = rulerList.itemData(index).toString();
        Ruler ruler = new Ruler(RulerCache.getRulerFromCache(name));

        //
        fillKeysList(visibleKeys, ruler.getVisibleRuler());
        fillKeysList(hiddenKeys, ruler.getHiddenRuler());
    }

    //
    // Helper method to fill a QListWidget with the keys
    // contained in a ruler
    //
    private void fillKeysList(QListWidget list, Ruler ruler) {
        for (RulerEntry entry : ruler) {
            QListWidgetItem item = new QListWidgetItem(list);

            item.setText(DBValuesTranslator.translate(entry.getName()));
            item.setData(ORIGINAL_NAME_ROLE, entry.getName());
            item.setData(IS_PRIMARY_ROLE, entry.isPrimary());

            if (entry.isPrimary()) {
                QFont font = item.font();

                font.setBold(true);
                item.setFont(font);
            }
        }
    }

    @SuppressWarnings("unused")
    private void save() {
        // Serialize ruler into a string
        List<String> rulerParts = Lists.newArrayList();
        int k;

        k = visibleKeys.count();
        for (int i = 0; i < k; i++) {
            QListWidgetItem item = visibleKeys.item(i);
            String part = item.data(ORIGINAL_NAME_ROLE).toString();

            if (QVariant.toBoolean(item.data(IS_PRIMARY_ROLE)))
                part += '*';

            rulerParts.add(part);
        }

        k = hiddenKeys.count();
        for (int i = 0; i < k; i++) {
            QListWidgetItem item = hiddenKeys.item(i);
            String part = item.data(ORIGINAL_NAME_ROLE).toString() + '+';

            if (QVariant.toBoolean(item.data(IS_PRIMARY_ROLE)))
                part += '*';

            rulerParts.add(part);
        }

        // Save to database
        String name = rulerList.itemData(rulerList.currentIndex()).toString();
        String rulerString = Joiner.on(',').join(rulerParts);

        try {
            RulerCache.updateRuler(name, rulerString);
        }
        catch (Exception e) {
            MessageDialog.showException(this, e);    
        }
    }

    @SuppressWarnings("unused")
    private void restore() {
        rulerSelected(rulerList.currentIndex());
    }

    @SuppressWarnings("unused")
    private void makeVisible() {
        QListWidgetItem item = hiddenKeys.takeItem(hiddenKeys.currentRow());
        if (item == null)
            return;

        visibleKeys.addItem(item);
    }

    @SuppressWarnings("unused")
    private void makeHidden() {
        QListWidgetItem item = visibleKeys.takeItem(visibleKeys.currentRow());
        if (item == null)
            return;

        hiddenKeys.addItem(item);
        
    }

    @SuppressWarnings("unused")
    private void moveUp() {
        int row = visibleKeys.currentRow();
        if (row <= 0)
            return;

        QListWidgetItem item = visibleKeys.takeItem(row);
        if (item == null)
            return;

        visibleKeys.insertItem(row - 1, item);
        visibleKeys.setCurrentRow(row - 1);
    }

    @SuppressWarnings("unused")
    private void moveDown() {
        int row = visibleKeys.currentRow();
        if (row >= visibleKeys.count() - 1)
            return;

        QListWidgetItem item = visibleKeys.takeItem(row);
        if (item == null)
            return;

        visibleKeys.insertItem(row + 1, item);
        visibleKeys.setCurrentRow(row + 1);
    }
}
