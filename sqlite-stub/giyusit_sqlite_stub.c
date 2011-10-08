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

/**
 * This is a small sqlite3 plugin that provides stub implementations of helper SQL
 * functions that are defined in the main application and are written in Java. 
 *
 * Compile and load this plugin to be able to query the helper views (i.e. AllCanidates)
 * outside the application without getting "function not defined" errors.
 *
 * See http://www.sqlite.org/cvstrac/wiki?p=LoadableExtensions for details about 
 * building and using SQLite extensions.
 */
#include <stdlib.h>
#include <sqlite3ext.h>

SQLITE_EXTENSION_INIT1

static void getCityAreaFunction(sqlite3_context* context, int argc, sqlite3_value** argv) {
	sqlite3_result_null(context);
}

int sqlite3_extension_init(sqlite3* db, 
			   char** pszErrorMsg, 
			   const sqlite3_api_routines* pApi) {
	SQLITE_EXTENSION_INIT2(pApi)

	/* Register functions */
	sqlite3_create_function(db, "getCityArea", 1, SQLITE_ANY, NULL, 
					getCityAreaFunction, NULL, NULL);

	return 0;
}
