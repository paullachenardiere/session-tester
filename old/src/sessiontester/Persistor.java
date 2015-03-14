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
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Saves and retrieves TestSessions through the file system
 *  
 */
public class Persistor {

    @SuppressWarnings("static-access")
    public static File save(File file, TestingSession session) throws IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc = builder.newDocument();
        Element root = (Element) doc.createElement("TestingSession");
        doc.appendChild(root);

        Persistor.addNode(doc, root, "Start", session.start);
        Persistor.addNode(doc, root, "End", session.end);
        Persistor.addNode(doc, root, "Duration", session.duration);
        Persistor.addNode(doc, root, "Tester", session.tester);
        Persistor.addNode(doc, root, "Mission", session.mission);
        Persistor.addNode(doc, root, "Tasks", session.tasks);
        Persistor.addNode(doc, root, "Data", session.data);
        Persistor.addNode(doc, root, "Notes", session.notes);
        Persistor.addNode(doc, root, "Issues", session.issues);
        Persistor.addNode(doc, root, "Bugs", session.bugs);
        Persistor.addNode(doc, root, "Environment", session.environment);
        Persistor.addNode(doc, root, "Area", session.area);

        try {
            Source source = new DOMSource(doc);
            Result result = new StreamResult(file);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

        return file;
    }

    private static void addNode(Document doc, Node parent, String tagName, String tagValue) {
        Node nodeTag = doc.createElement(tagName);
        Node nodeValue = doc.createTextNode(tagValue);
        nodeTag.appendChild(nodeValue);
        parent.appendChild(nodeTag);
        parent.appendChild(doc.createTextNode("\r\n"));
    }

    public static TestingSession load(File file) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(file);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        TestingSession session = new TestingSession();
        session.start = xpath.evaluate("//TestingSession/Start", doc);
        session.end = xpath.evaluate("//TestingSession/End", doc);
        session.duration = xpath.evaluate("//TestingSession/Duration", doc);
        session.tester = xpath.evaluate("//TestingSession/Tester", doc);
        session.mission = xpath.evaluate("//TestingSession/Mission", doc);
        session.tasks = xpath.evaluate("//TestingSession/Tasks", doc);
        session.data = xpath.evaluate("//TestingSession/Data", doc);
        session.notes = xpath.evaluate("//TestingSession/Notes", doc);
        session.issues = xpath.evaluate("//TestingSession/Issues", doc);
        session.bugs = xpath.evaluate("//TestingSession/Bugs", doc);
        session.area = xpath.evaluate("//TestingSession/Area", doc);
        session.environment = xpath.evaluate("//TestingSession/Environment", doc);

        return session;
    }
}
