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
package negev.giyusit.util;

import com.trolltech.qt.core.*;

import java.util.HashMap;
import java.util.Scanner;

public class DBValuesTranslator {

	private static HashMap<String, String> translationMap;
	
	static {
		Class<?> clazz = DBValuesTranslator.class;
		
		Scanner scanner = new Scanner(
					clazz.getResourceAsStream("/lang/dbvalues_he.txt"), "UTF-8");
		
		// Clear map
		translationMap = new HashMap<String, String>();
		
		try {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				
				// Skip empty lines and comments
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				
				String[] parts = line.split(":");
				
				if (parts.length != 2)
					continue;
				
				translationMap.put(parts[0].trim(), parts[1].trim());
			}
		}
		finally {
			scanner.close();
		}
	}
	
	public static String translate(String key) {
		if (!translationMap.containsKey(key))
			return key;
		
		return translationMap.get(key);
	}
	
	public static void translateModelHeaders(QAbstractItemModel model) {
		if (model == null)
			return;
		
		int cols = model.columnCount();
		for (int i = 0; i < cols; i++) {
			String orig = model.headerData(i, Qt.Orientation.Horizontal).toString();
			
			model.setHeaderData(i, Qt.Orientation.Horizontal, translate(orig));
		}
	}
}
