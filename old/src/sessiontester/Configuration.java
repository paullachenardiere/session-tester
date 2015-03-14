/*
 * Session Tester - The Exploratory Testing Tool, a tool to help manage exploratory testing sessions, prime testing ideas and record test results.
 * 
 * Copyright (C) 2009 Jonathan Kohl, Aaron West
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sessiontester;

import java.io.IOException;
import java.util.List;

/**
 * Represents runtime configuration attributes, used by SessionTestingClient
 *
 */
public class Configuration {
    private List<String> primingList;

    public List<String> getPrimingList() {
        return primingList;
    }

    public void setPrimingList(List<String> primingList) {
        this.primingList = primingList;
    }

    public static Configuration BuildConfiguation() throws IOException {
       Configuration configuration = new Configuration();
       configuration.setPrimingList(FileUtils.parseResourceFileIntoList(FileUtils.PRIMING_FILE_NAME));
       return configuration;
    }
}
