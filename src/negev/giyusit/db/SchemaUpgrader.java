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
package negev.giyusit.db;

import com.trolltech.qt.core.*;

import java.io.InputStream;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Various utility methods pertaining to database schema upgrades
 */
public class SchemaUpgrader {

    private static final String SCHEMA_REVISION_DB_PARAMETER = "SchemaRevision";

    /**
     * @return The schema revision of the current database
     */
    public static int getCurrentFileSchemaRevision() {
        return Integer.parseInt(
                DatabaseUtils.getFileParameter(SCHEMA_REVISION_DB_PARAMETER));
    }

    /**
     * Sets the current database's schema revision to the provided value
     *
     * @param revision The requested schema revision.
     * @throws IllegalArgumentException If <i>revision</i> is not positive
     */
    public static void setCurrentFileSchemaRevision(int revision) {
        Preconditions.checkArgument(revision >= 0, "Revision not positive");

        DatabaseUtils.setFileParameter(
                SCHEMA_REVISION_DB_PARAMETER, String.valueOf(revision));
    }

    /**
     * @return The list of known schema upgrade scripts
     */
    public static List<String> availableSchemaFiles() {
        QDir dir = new QDir("classpath:/sql");
        dir.setNameFilters(ImmutableList.of("*.sql"));
        dir.setSorting(QDir.SortFlag.Name);

        return dir.entryList();
    }

    /**
     * @return  The maximal database schema revision the application is aware of,
     *           or -1 if there are no schema files known.
     */
    public static int maximalApplicativeSchmeaRevison() {
        List<String> schemaFiles = availableSchemaFiles();

        if (!schemaFiles.isEmpty()) {
            return getSchemaFileRevision(Iterables.getLast(schemaFiles));
        }
        else {
            return -1;
        }
    }

    public static boolean schemaUpgradePossible() {
        return getCurrentFileSchemaRevision() < maximalApplicativeSchmeaRevison();
    }

    public static void trySchemaUpgrade() {
        if (!schemaUpgradePossible()) {
            return;
        }

        int currentRevision = getCurrentFileSchemaRevision();
        for (String file : availableSchemaFiles()) {
            // Make sure that the file is not already applied
            int fileRevision = getSchemaFileRevision(file);
            if (fileRevision <= currentRevision) {
                continue;
            }

            InputStream stream = SchemaUpgrader.class.getResourceAsStream("/sql/" + file);

            DatabaseUtils.runSqlScript(stream);
            setCurrentFileSchemaRevision(fileRevision);
        }
        assert getCurrentFileSchemaRevision() == maximalApplicativeSchmeaRevison();
    }

    public static void initializeDatabase() {
        for (String file : availableSchemaFiles()) {
            InputStream stream = SchemaUpgrader.class.getResourceAsStream("/sql/" + file);

            DatabaseUtils.runSqlScript(stream);
            setCurrentFileSchemaRevision(getSchemaFileRevision(file));
        }
        assert getCurrentFileSchemaRevision() == maximalApplicativeSchmeaRevison();
    }

    //
    // Helper method - returns a schema file's revision from its name
    //
    private static int getSchemaFileRevision(String file) {
        return Integer.parseInt(file.substring(0, 4));
    }
}
