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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;
import sessiontester.FileUtils;
import sessiontester.Persistor;
import sessiontester.TestingSession;

/**
 * Generates a web report by recursively searching the .Session Tester directory
 * 
 */
public class WebPublisher {

    private static String HEADER = "<HTML><BODY><H1>Session Tester Report</H1>";
    private static String FOOTER = "</BODY></HTML>";

    public WebPublisher() {
    }

    public void publishTo(File publishDir) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        if (!publishDir.isDirectory()) {
            throw new IOException("A directory was specified instead of a file.");
        }

        //Nest publish directory using a time stamp
        String subDirName = "report_" + FileUtils.buildTimeStamp(new Date());
        publishDir = new File(publishDir.getPath() + File.separatorChar + subDirName);
        publishDir.mkdirs();

        scanDirectory(FileUtils.SESSION_TESTER_DIR, publishDir, new DirectoryLevel(), new BreadCrumb());
    }

    private void scanDirectory(File root, File publishDir, DirectoryLevel level, BreadCrumb history) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        File[] filesOrDirs = root.listFiles();

        buildIndexPage(root, publishDir, level, history);

        for (File fileOrDir : filesOrDirs) {
            if (fileOrDir.isDirectory()) {
                DirectoryLevel nextLevel = level.nextLevel();
                if (nextLevel.hasMoreLevels()) {
                    scanDirectory(fileOrDir, publishDir, level.nextLevel(), history.add(fileOrDir.getName()));
                }
            } else {
                buildReport(fileOrDir, publishDir);
            }
        }

    }

    private String buildReportFilename(File file) {
        return file.getName().split(".xml")[0] + ".html";
    }

    /**
     * Generates index page based on specified level.
     * Assumes all containing files or directories correspond to the level
     *
     */
    private void buildIndexPage(File directoryToIndex, File publishDir, DirectoryLevel level, BreadCrumb history) throws IOException {
        StringBuffer pageContents = new StringBuffer().append(HEADER);
        pageContents.append("<p>").append(history.buildTrailDescription());
        String filename = level.isTop() ? "index.html" : "index_" + history.buildTrailFilenameFragment() + ".html";
        File outputFile = new File(publishDir, filename);

        DirectoryLevel nextLevel = level.nextLevel();
        pageContents.append("<h1>").append(nextLevel.getLabel()).append("</h1>");

        for (File file : directoryToIndex.listFiles()) {
            StringBuffer link = new StringBuffer();
            if (nextLevel.hasMoreLevels()) {
                link.append("index_");
                if (!history.isEmpty()) {
                    link.append(history.buildTrailFilenameFragment());
                    link.append("_");
                }
                link.append(file.getName());
                link.append(".html");
            } else {
                link.append(buildReportFilename(file));
            }
            pageContents.append("<a href=\"").append(link).append("\">");
            pageContents.append(file.getName());
            pageContents.append("</a>");
            pageContents.append("<br/>");
        }

        pageContents.append(FOOTER);
        FileUtils.writeToFile(outputFile, pageContents.toString());
    }

    private void appendField(StringBuffer pageContents, String fieldName, String fieldValue) {
        pageContents.append("<h2>").append(fieldName).append("</h2>");
        pageContents.append("<p>").append(fieldValue).append("</p>");
    }

    private void buildReport(File inputFile, File publishDir) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        TestingSession session = Persistor.load(inputFile);
        StringBuffer pageContents = new StringBuffer();
        pageContents.append(HEADER);

        pageContents.append("<h1>Testing Session - ").append(inputFile.getName()).
                append("</h1>");

        appendField(pageContents, "Start", wrapInPreTag(session.start));
        appendField(pageContents, "End", wrapInPreTag(session.end));
        appendField(pageContents, "Duration", wrapInPreTag(session.duration));
        appendField(pageContents, "Tester", wrapInPreTag(session.tester));
        appendField(pageContents, "Mission", wrapInPreTag(session.mission));
        appendField(pageContents, "Task Breakdown", wrapInPreTag(session.tasks));
        appendField(pageContents, "Data Files", wrapInPreTag(session.data));
        appendField(pageContents, "Environment", wrapInPreTag(session.environment));
        appendField(pageContents, "Area", wrapInPreTag(session.area));
        appendField(pageContents, "Test Notes", wrapInPreTag(session.notes));
        appendField(pageContents, "Issues", wrapInPreTag(session.issues));
        appendField(pageContents, "Bugs", wrapInPreTag(session.bugs));

        pageContents.append(FOOTER);

        File outputFile = new File(publishDir, buildReportFilename(inputFile));
        FileUtils.writeToFile(outputFile, pageContents.toString());
    }

    private String wrapInPreTag(String content)
    {
        return wrapInTag(content,"pre");
    }

    private String wrapInTag(String content, String tag)
    {
        return "<"+ tag +">" + content + "</"+ tag + ">";
    }
}
