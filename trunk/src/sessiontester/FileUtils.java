/*
 * Session Tester - The Exploratory Testing Tool, a tool to help manage exploratory testing sessions, prime testing ideas and record test results.
 * 
 * Copyright (C) 2008-2009 Jonathan Kohl, Aaron West
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package sessiontester;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Utility methods for working with the file system
 */
public class FileUtils {

    public static File HOME_DIR = new File(System.getProperty("user.home"));
    public static File SESSION_TESTER_DIR = new File(HOME_DIR, ".sessiontester");
    public static String PRIMING_FILE_NAME = "priming.txt";

    public static void writeToFile(File file, String string) throws IOException {
        PrintStream stream = null;
        try {
            file.createNewFile();
            stream = new PrintStream(file);
            stream.print(string);
        } finally {
            stream.close();
        }

    }

   

    public static List<String> parseResourceFileIntoList(String fileName) throws IOException {
        List<String> result = new ArrayList<String>();
        InputStream stream = ClassLoader.getSystemResourceAsStream(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        while (reader.ready()) {
            result.add(reader.readLine());
        }

        return result;
    }

    public static String buildTimeStamp(Date date) {
        int year = DateUtils.getYear(date);
        int month = DateUtils.getMonthOfYear(date);
        int day = DateUtils.getDayOfMonth(date);
        int hour = DateUtils.getHour(date);
        int minute = DateUtils.getMinute(date);
        int seconds = DateUtils.getSeconds(date);
        int millis = DateUtils.getMillisOfSecond(date);

        return new StringBuffer().append(year).append("_").append(month).append("_").append(day).append("_").append(hour).append("_").append(minute).append("_").append(seconds).append("_").append(millis).toString();
    }

    public static File buildTestingSessionFileFromCurrentTime() {
        Date now = new Date();
        String year = Integer.toString(DateUtils.getYear(now));
        String month = Integer.toString(DateUtils.getMonthOfYear(now));
        String day = Integer.toString(DateUtils.getDayOfMonth(now));
        File path = new File(new File(new File(FileUtils.SESSION_TESTER_DIR, year), month), day);
        path.mkdirs(); //Creates directory if it does not exist

        return new File(path, "testing_session_" + buildTimeStamp(now) + ".xml");
    }
}
