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

/**
 * Represents current location in directory heirarchy.
 * Used by recursive directory scan.
 * 
 */
public class DirectoryLevel {

	private static final String[] LABELS = {"top", "year", "month", "day", "files"};
	private int index;

	public DirectoryLevel() {
		index = 0;
	}

	private DirectoryLevel(int index) {
		if (index > (LABELS.length - 1)) {
			throw new RuntimeException("Can't traverse unexpected directory structure");
		}
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public DirectoryLevel nextLevel() {
		return new DirectoryLevel(index + 1);
	}

	public boolean hasMoreLevels() {
		return (index < LABELS.length - 1);
	}

	public String getLabel() {
		return LABELS[index];
	}

	boolean isTop() {
		return index == 0;
	}
	}
