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

/**
 * Represents a Test Session. Also performs field validation.
 * 
 */
public class TestingSession {

    public String start = "";
    public String end = "";
    public String duration = "";
    public String tester = "";
    public String mission = "";
    public String tasks = "";
    public String data = "";
    public String notes = "";
    public String issues = "";
    public String bugs = "";
    public String environment = "";
    public String area= "";

    public TestingSession() {
    }

    void addBug(String s) {
        bugs = add(bugs, s);
    }

    void addData(String s) {
        data = add(data, s);
    }

    void addIssue(String s) {
        issues = add(issues, s);
    }

    void addNotes(String s) {
        notes = add(notes, s);
    }

    void addTask(String s) {
        tasks = add(tasks, s);
    }

    void addEnvironment(String s) {
        environment = add(environment, s);
    }

    void addArea(String s) {
        area = add(area, s);
    }

    String add(String orig, String append) {
        return orig + append + "\n";
    }

    
}
