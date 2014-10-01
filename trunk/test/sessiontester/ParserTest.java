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

import junit.framework.TestCase;

public class ParserTest extends TestCase {

    public void testTypicalSession() {

        String s = ("@Notes\nNOTE_TEXT" +
                "\n@area\nAREA_TEXT" +
                "\n@Environment\nENV_TEXT" +
                "\n@data\nDATA_TEXT" +
                "\n@bUG\nBUG_TEXT" +
                "\n@tAsK\nTASK_TEXT" +
                "\n@iSSue  \nISSUE_TEXT");

        TestingSession session = new Parser().parse(s);

        assertEquals("DATA_TEXT\n", session.data);
        assertEquals("NOTE_TEXT\n", session.notes);
        assertEquals("ISSUE_TEXT\n", session.issues);
        assertEquals("BUG_TEXT\n", session.bugs);
        assertEquals("AREA_TEXT\n", session.area);
        assertEquals("TASK_TEXT\n", session.tasks);
        assertEquals("ENV_TEXT\n", session.environment);

    }

    public void testEmptySession() {
        TestingSession session = new Parser().parse("");

        assertEquals("", session.start);
        assertEquals("", session.end);
        assertEquals("", session.duration);
        assertEquals("", session.tester);
        assertEquals("", session.mission);
        assertEquals("", session.tasks);
        assertEquals("", session.data);
        assertEquals("", session.notes);
        assertEquals("", session.issues);
        assertEquals("", session.bugs);
        assertEquals("", session.area);
        assertEquals("", session.environment);

    }

    public void testDuplicateTagAggregation() {
        String s = ("NOTES1_TEXT" +
                "\nPLUS_MORE" +
                "\n@Notes\nNOTES2_TEXT" +
                "\n@bUG\nBUG1_TEXT" +
                "\n@tAsK\nTASK_TEXT" +
                "\n@iSSue  \nISSUE_TEXT" +
                "\n@daTA\nDATA_TEXT" +
                "\n@nOTes\nNOTES3_TEXT" +
                "\n@BUG\nBUG2_TEXT" +
                "\n@EnvIRONment\nENV_TEXT" +
                "\n@arEA\nAREA_TEXT");

        TestingSession session = new Parser().parse(s);

        assertEquals("NOTES1_TEXT\nPLUS_MORE\nNOTES2_TEXT\nNOTES3_TEXT\n", session.notes);
        assertEquals("BUG1_TEXT\nBUG2_TEXT\n", session.bugs);
        assertEquals("TASK_TEXT\n", session.tasks);
        assertEquals("ISSUE_TEXT\n", session.issues);
        assertEquals("DATA_TEXT\n", session.data);
        assertEquals("AREA_TEXT\n", session.area);
        assertEquals("ENV_TEXT\n", session.environment);
    }

    public void testEmptyTagParsingBug() {
        //Noticed that an empty tag (eg. @notes\nNOTES\n@data\n@bug\nBUG) will suck in
        //the next as content (ie. getData() == "@bug\nbug")
        String s = "@notes\nNOTES\n@data\n@bug\nBUG";

        TestingSession session = new Parser().parse(s);

        assertEquals("", session.data);
        assertEquals("NOTES\n", session.notes);
        assertEquals("BUG\n", session.bugs);
    }

    public void testDuplicateTagParsingBug() {
        /* Multiple tags of the same type are missed in the parser
         * e.g. @bug
         * bug1
         * @bug
         * bug2
         * @bug
         * bug3
         *
         * ... the second tag is not picked up.. only bug 1 & bug 3 will appear in the output
         */

        String s = "@notes\nNOTES\n@data\nData\n@bug\nBUG_1\n@bug\nBUG_2\n@bug\nBUG_3\n@bug\nBUG_4\n@bug\nBUG_5\n@environment\nENVIRONMENT\n@bug\nBUG_6";

        TestingSession session = new Parser().parse(s);

        assertEquals("NOTES\n", session.notes);
        assertEquals("BUG_1\nBUG_2\nBUG_3\nBUG_4\nBUG_5\nBUG_6\n", session.bugs);
        assertEquals("ENVIRONMENT\n", session.environment);

    }
}
