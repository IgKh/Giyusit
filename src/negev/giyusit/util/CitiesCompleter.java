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
package negev.giyusit.util;

import com.trolltech.qt.gui.*;

import java.util.Map;
import java.util.Scanner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;

public class CitiesCompleter extends QCompleter {
	
	private static final Map<String, String> CITIES;
	
	static {
        ImmutableSortedMap.Builder<String, String> mapBuilder = ImmutableSortedMap.naturalOrder();

		// Load from file
		Scanner scanner = new Scanner(
				CitiesCompleter.class.getResourceAsStream("/israel_cities.txt"),
                "UTF-8");
		
		try {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				
				// Skip empty lines
				if (line.isEmpty()) {
					continue;
                }
				String[] parts = line.split(";");

				mapBuilder.put(parts[0], parts[1]);
			}
            CITIES = mapBuilder.build();
		}
        catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
		finally {
			scanner.close();
		}
	}

    public static boolean containsCity(String city) {
        return CITIES.keySet().contains(city);
    }

    public static String getCityArea(String city) {
        return CITIES.get(city);
    }
	
	public CitiesCompleter() {
		super(ImmutableList.copyOf(CITIES.keySet()));
	}	
}
