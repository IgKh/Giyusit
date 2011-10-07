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
package negev.giyusit.staff;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import negev.giyusit.util.row.Row;
import negev.giyusit.util.row.RowSet;

/**
 * A tree model for staff member admin dialog
 */
public class StaffTreeModel extends QTreeModel {

    public final Signal0 treeModified = new Signal0();

    private static final String DRAG_DROP_MIME_TYPE = "application/vnd.giyusit.staff-member-id";

    private class Item {
        int id;
        String name;
        boolean isReal;

        List<Item> children = Lists.newArrayList();

        public Item(int id, String name, boolean isReal) {
            this.id = id;
            this.name = name;
            this.isReal = isReal;
        }
    }

    private Item rootItem;

    public StaffTreeModel() {
        rootItem = new Item(-1, tr("Staff Members"), true);
    }

    public void rebuildModel() {
        StaffHelper helper = new StaffHelper();

        try {
            rootItem.children = doItems(helper.getTopLevelStaffMembers(), helper);
            reset();
        }
        finally {
            helper.close();
        }
    }

    private List<Item> doItems(RowSet items, StaffHelper helper) {
        List<Item> result = Lists.newArrayList();

        for (Row row : items) {
            int id = row.getInt("ID");
            Item item = new Item(id, row.getString("Name"), row.getBoolean("RealInd"));

            item.children = doItems(helper.getStaffMemberChildren(id), helper);

            result.add(item);
        }
        return result;
    }

    private Item indexToItem(QModelIndex index) {
        return ((Item) Preconditions.checkNotNull(indexToValue(index)));
    }

    public boolean isRoot(QModelIndex index) {
        return indexToItem(index) == rootItem;
    }

    public int indexId(QModelIndex index) {
        return indexToItem(index).id;
    }

    public void updateName(QModelIndex index, String newName) {
        indexToItem(index).name = newName;
        dataChanged.emit(index, index);
    }

    public QModelIndex getIndexById(int id) {
        return valueToIndex(getIndexById(id, rootItem));
    }

    private Item getIndexById(int id, Item start) {
        if (id == start.id) {
            return start;
        }

        for (Item item : start.children) {
            Item result = getIndexById(id, item);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Object child(Object object, int i) {
        if (object == null) {
            return rootItem;
        }
        return ((Item) object).children.get(i);
    }

    @Override
    public int childCount(Object object) {
        if (object == null) {
            return 1;
        }
        return ((Item) object).children.size();
    }

    @Override
    public String text(Object object) {
       if (object == null) {
            return null;
       }
       return ((Item) object).name;
    }

    /*
     * Drag and drop related methods
     */

    @Override
    public Qt.DropActions supportedDropActions() {
        return new Qt.DropActions(Qt.DropAction.MoveAction);
    }

    @Override
    public Qt.ItemFlags flags(QModelIndex index) {
        Qt.ItemFlags itemFlags = super.flags(index);

        if (index != null) {
            itemFlags.set(Qt.ItemFlag.ItemIsDropEnabled);

            if (!isRoot(index)) {
                itemFlags.set(Qt.ItemFlag.ItemIsDragEnabled);
            }
        }
        return itemFlags;
    }

    @Override
    public List<String> mimeTypes() {
        return ImmutableList.of(DRAG_DROP_MIME_TYPE);
    }

    @Override
    public QMimeData mimeData(List<QModelIndex> indexes) {
        QMimeData data = new QMimeData();
        QByteArray encodedData = new QByteArray();

        QDataStream stream = new QDataStream(encodedData, QIODevice.OpenModeFlag.WriteOnly);
        for (QModelIndex index : indexes) {
            if (index != null) {
                stream.writeInt(indexToItem(index).id);
            }
        }

        data.setData(DRAG_DROP_MIME_TYPE, encodedData);
        return data;
    }

    @Override
    public boolean dropMimeData(QMimeData data, Qt.DropAction action, int row, int column, QModelIndex parent) {
        if (action == Qt.DropAction.IgnoreAction) {
            return true;
        }

        if (!data.hasFormat(DRAG_DROP_MIME_TYPE) || parent == null || column > 0) {
            return false;
        }

        Item targetItem = indexToItem(parent);
        Integer newParentId = (targetItem == rootItem) ? null : targetItem.id;

        StaffHelper helper = new StaffHelper();
        try {
            QByteArray encodedData = data.data(DRAG_DROP_MIME_TYPE);
            QDataStream stream = new QDataStream(encodedData, QIODevice.OpenModeFlag.ReadOnly);

            while(!stream.atEnd()) {
                int id = stream.readInt();

                helper.reparentStaffMember(id, newParentId);
            }

            treeModified.emit();
            return true;
        }
        finally {
            helper.close();
        }
    }
}
