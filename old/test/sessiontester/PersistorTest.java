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
package sessiontester;

import java.io.File;
import java.util.Date;
import junit.framework.TestCase;

public class PersistorTest extends TestCase {

    public void testFileName() throws Exception {
        TestingSession sessionLHS = new TestingSession();

        Date now = new Date();
        String expectedPath = ".sessiontester" + File.separatorChar + DateUtils.getYear(now) + File.separatorChar + DateUtils.getMonthOfYear(now) + File.separatorChar + DateUtils.getDayOfMonth(now);

        File file = Persistor.save(FileUtils.buildTestingSessionFileFromCurrentTime(), sessionLHS);

        assertTrue(file.getPath().indexOf(expectedPath) != -1);
        assertTrue(file.getName().indexOf("testing_session_") != -1);
        assertTrue(file.getName().indexOf(".xml") != -1);
    }

    public void testSaveAndLoad() throws Exception {
        TestingSession sessionLHS = new TestingSession();
        sessionLHS.start = "5/30/00 03:20 pm";
        sessionLHS.end = "5/30/00 05:20 pm";
        sessionLHS.duration = "63 minutes";
        sessionLHS.tester = "Some dude..";
        sessionLHS.mission = "Do stuff..";
        sessionLHS.tasks = "blah...";
        sessionLHS.data = "blah 2...";
        sessionLHS.notes = "blah 3...";
        sessionLHS.issues = "Issues..";
        sessionLHS.bugs = "blah 4...";

        File file = Persistor.save(FileUtils.buildTestingSessionFileFromCurrentTime(), sessionLHS);

        TestingSession sessionRHS = Persistor.load(file);

        assertEquals(sessionLHS.start, sessionRHS.start);
        assertEquals(sessionLHS.end, sessionRHS.end);
        assertEquals(sessionLHS.duration, sessionRHS.duration);
        assertEquals(sessionLHS.tester, sessionRHS.tester);
        assertEquals(sessionLHS.mission, sessionRHS.mission);
        assertEquals(sessionLHS.tasks, sessionRHS.tasks);
        assertEquals(sessionLHS.data, sessionRHS.data);
        assertEquals(sessionLHS.issues, sessionRHS.issues);
        assertEquals(sessionLHS.notes, sessionRHS.notes);
    }

    public void testEncodingSpecialCharacters() throws Exception {
        TestingSession sessionLHS = new TestingSession();
        sessionLHS.bugs = "<CPU is at 83%>";
        File file = Persistor.save(FileUtils.buildTestingSessionFileFromCurrentTime(), sessionLHS);
        TestingSession sessionRHS = Persistor.load(file);
        assertEquals(sessionLHS.bugs, sessionRHS.bugs);
    }
}
