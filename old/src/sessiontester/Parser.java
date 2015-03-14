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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses a regular expression to parse the input from the user interface
 * and builds a TestingSession.
 */
public class Parser {

     public static final String[] TAGS = {
         "@area",
         "@bug",
         "@data",
         "@environment",
         "@issue",
         "@notes",
         "@task",
         "@timestamp",
     };
    
    public Parser() {
    }

    public Matcher match(String tag, String s) {
        String regex = tag + "\\s*\n(?!@)(.*?)(?=\n@|\\z)";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(s);
    }

    public Matcher matchLeadingText(String s) {
        String regex = "^([^@].*?)\n@";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(s);
    }

    public TestingSession parse(String testingSessionText) {
        TestingSession session = new TestingSession();
        Matcher m;

        //Grab any leading text without a tag and add this as notes
        m = matchLeadingText(testingSessionText);
        while (m.find()) {
            session.addNotes(m.group(1));
        }

        m = match("@notes", testingSessionText);
        while (m.find()) {
            session.addNotes(m.group(1));
        }

        m = match("@bug", testingSessionText);
        while (m.find()) {
            session.addBug(m.group(1));
        }

        m = match("@task", testingSessionText);
        while (m.find()) {
            session.addTask(m.group(1));
        }

        m = match("@issue", testingSessionText);
        while (m.find()) {
            session.addIssue(m.group(1));
        }

        m = match("@data", testingSessionText);
        while (m.find()) {
            session.addData(m.group(1));
        }

        m = match("@environment", testingSessionText);
        while (m.find()) {
            session.addEnvironment(m.group(1));
        }

        m = match("@area", testingSessionText);
        while (m.find()) {
            session.addArea(m.group(1));
        }

        return session;
    }
}
