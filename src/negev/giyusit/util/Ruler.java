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
package negev.giyusit.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 */
public class Ruler extends ForwardingList<RulerEntry> {

    // Regular expression to catch marker characters
    private static final Pattern markerPattern = Pattern.compile("(\\*|\\+)");

    private ImmutableList<RulerEntry> innerList;

    /**
	 * Creates a new ruler instance using a specified ruler string.
	 * <br><br>
	 * The ruler string is a comma separated list of key names. The key
     * names can have special marker characters appended to them: The star
     * character (*) will mark the key as the ID key, and the plus character
     * (+) will mark cause the key as hidden (a {@link negev.giyusit.util.RowSetModel}
     * will not show it). More than one marker character can be applied to a key.
	 *
	 * @param rulerString - the ruler string that will define the model's ruler
	 */
    public Ruler(String rulerString) {
        Preconditions.checkNotNull(rulerString, "Ruler string is null");

        ImmutableList.Builder<RulerEntry> builder = ImmutableList.builder();
        String[] arr = rulerString.split(",");

		for (String str : arr) {
			Matcher matcher = markerPattern.matcher(str);

			if (matcher.find()) {
				// Strip marker chars
				String key = matcher.replaceAll("");

                boolean hidden = str.lastIndexOf('+') > 0;
                boolean primary = str.lastIndexOf('*') > 0;

                builder.add(new RulerEntry(key, primary, hidden));
			}
			else
				builder.add(new RulerEntry(str));
		}
        innerList = builder.build();
    }

    public Ruler(String... origin) {
        Preconditions.checkNotNull(origin, "Origin is null");

        ImmutableList.Builder<RulerEntry> builder = ImmutableList.builder();

        for (String str : origin) {
            builder.add(new RulerEntry(str));
        }
        innerList = builder.build();
    }

    public Ruler(Iterable<? extends RulerEntry> origin) {
        Preconditions.checkNotNull(origin, "Origin is null");

        innerList = ImmutableList.copyOf(origin);
    }

    public String getKeyName(int index) {
        return get(index).getName();
    }

    /**
     * Returns an immutable list of just the names of the keys
     * stored in the ruler
     */
    public ImmutableList<String> getKeyNames() {
        Iterable<String> result = Iterables.transform(innerList,
                new Function<RulerEntry, String>() {
                    public String apply(RulerEntry entry) {
                        return entry.getName();
                    }
                });

        return ImmutableList.copyOf(result);
    }

    /**
     * Returns a new ruler containing just the visible keys stored in the ruler
     */
    public Ruler getVisibleRuler() {
        Iterable<RulerEntry> results = Iterables.filter(innerList,
                new Predicate<RulerEntry>() {
                    public boolean apply(RulerEntry entry) {
                        return !entry.isHidden();
                    }
                });
        
        return new Ruler(results);
    }

    /**
     * Returns a new ruler containing just the hidden keys stored in the ruler
     */
    public Ruler getHiddenRuler() {
        Iterable<RulerEntry> results = Iterables.filter(innerList,
                new Predicate<RulerEntry>() {
                    public boolean apply(RulerEntry entry) {
                        return entry.isHidden();
                    }
                });

        return new Ruler(results);
    }

    /**
     * Returns the first key in the ruler marked as primary. If there are
     * no such keys, <code>null</code> is returned
     */
    public RulerEntry getPrimaryKey() {
        Iterable<RulerEntry> result = Iterables.filter(innerList,
                new Predicate<RulerEntry>() {
                    public boolean apply(RulerEntry entry) {
                        return entry.isPrimary();
                    }
                });

        return Iterables.getOnlyElement(result, null);
    }

    @Override
    protected List<RulerEntry> delegate() {
        return innerList;
    }
}
