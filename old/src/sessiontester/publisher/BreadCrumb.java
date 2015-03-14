/*
 * Session Tester - The Exploratory Testing Tool, a tool to help manage exploratory testing sessions, prime testing ideas and record test results.
 * 
 * Copyright (C) 2008 Jonathan Kohl, Aaron West
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sessiontester.publisher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a history breadcrumb.
 * Used to generate index filenames and breadcrumbs
 * Note: Object is immutable. @See BreadCrumb#add
 * 
 */
public class BreadCrumb {

	private List<String> history;

	public BreadCrumb() {
		history = new ArrayList<String>();
	}

	private BreadCrumb(List<String> historyToCopy, String historyToAdd) {

		//Avoid clone() in Java because it is evil and generics makes it worse 
		this.history = new ArrayList<String>();
		for (String string : historyToCopy) {
			history.add(string);
		}
		history.add(historyToAdd);
	}

	public BreadCrumb add(String historyToAdd) {
		return new BreadCrumb(history, historyToAdd);
	}

	boolean isEmpty() {
		return history.isEmpty();
	}

	private String buildTrail(String seperator) {

		StringBuffer result = new StringBuffer();
		for (Iterator<String> it = history.iterator(); it.hasNext();) {
			result.append(it.next());
			if (it.hasNext()) {
				result.append(seperator);
			}
		}

		return result.toString();
	}

	public String buildTrailFilenameFragment() {
		return buildTrail("_");
	}

	public String buildTrailDescription() {
		return buildTrail(" > ");
	}
	}

