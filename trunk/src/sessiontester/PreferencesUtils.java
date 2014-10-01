/*
 * Session Tester - The Exploratory Testing Tool, a tool to help manage
 * exploratory testing sessions, prime testing ideas and record test results.
 *
 * Copyright (C) 2010 Jonathan Kohl, Aaron West
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sessiontester;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;

import java.util.logging.Logger;
import java.util.logging.Level;

import sessiontester.FileUtils;

/*
 * TODO:
 * - add file stream to xml file
 * - add javadoc
 *
 */

/**
 * PreferenceUtils is an utility method for working with Preferences.
 * Session Tester specific utility functions has been created.
 */
public class PreferencesUtils
{
    public PreferencesUtils()
    {
        GenerateDefaultPreferences();
        SetupNode();
        SetupPreferencesFile();
    }

    /**
     * Resets to default values of preferences object.
     */
    public void ResetPreferences()
    {
        SetTesterName(strDefTesterName);
        SetWebReportPath(strDefWebReportPath);
        SetSessionFilePath(strDefSessionFilePath);
    }

    /**
     * Setup the node object.
     */
    private void SetupNode()
    {
        try
        {
            logger.info("Setting up preferences utilities");
            prefs = Preferences.userNodeForPackage(
                    sessiontester.client.Client.class
                    );
        }
        catch(NullPointerException e)
        {
            logger.log(Level.SEVERE,
                    "Null Pointer Exception:\n", e);
        }
        catch(SecurityException e)
        {
            logger.log(Level.SEVERE,
                    "Security Exception:\n", e);
        }
    }

    /**
     * Setup the preferences file by creating a new if none exists or
     * import the existing one if there is one already.
     */
    private void SetupPreferencesFile()
    {
        File f = new File("preferences.xml");
        if(f.exists() == true)
        {
            ImportPrefs();
        }
        else
        {
            try
            {
                f.createNewFile();
                ResetPreferences();
                ExportPrefs();
            }
            catch (IOException e)
            {
                logger.log(Level.SEVERE,
                        "IO Exception:\n", e);
            }
        }

    }

    /**
     * Clears the preferences object of all stored values and keys.
     */
    public void ClearPreferences()
    {
        try
        {
            prefs.remove(prefTesterName);
            prefs.remove(prefWebReportPath);
            prefs.remove(prefSessionFilePath);
        }
        catch(NullPointerException e)
        {
            logger.log(Level.SEVERE,
                    "Null Pointer Exception:\n", e);
        }
        catch(IllegalStateException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal State Exception:\n", e);
        }
    }

    /**
     * Set the tester name in the preferences object.
     * @param strTesterName as name of the tester to be stored.
     */
    public void SetTesterName(String strTesterName)
    {
        try
        {
            prefs.put(prefTesterName, strTesterName);
        }
        catch(IllegalArgumentException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal Argument Exception:\n", e);
        }
        catch(IllegalStateException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal State Exception:\n", e);            
        }
        catch(NullPointerException e)
        {
            logger.log(Level.SEVERE,
                    "Null Pointer Exception:\n", e);
        }
    }

    /**
     * Sets the web report path in the preferences object.
     * @param strWebReportPath as path to where web reports are to be stored.
     */
    public void SetWebReportPath(String strWebReportPath)
    {
        try
        {
            prefs.put(prefWebReportPath, strWebReportPath);
        }
        catch(IllegalArgumentException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal Argument Exception:\n", e);
        }
        catch(IllegalStateException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal State Exception:\n", e);
        }
        catch(NullPointerException e)
        {
            logger.log(Level.SEVERE,
                    "Null Pointer Exception:\n", e);
        }
    }


    /**
     * Sets the session file path in the preferences object.
     * @param strSessionFilePath as path to where session files
     * are to be stored.
     */
    public void SetSessionFilePath(String strSessionFilePath)
    {
        try
        {
            prefs.put(prefSessionFilePath, strSessionFilePath);
        }
        catch(IllegalArgumentException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal Argument Exception:\n", e);
        }
        catch(IllegalStateException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal State Exception:\n", e);
        }
        catch(NullPointerException e)
        {
            logger.log(Level.SEVERE,
                    "Null Pointer Exception:\n", e);
        }
    }

    /**
     * Get the value for SessionFilePath in the preferences.
     * @return prefSessionFilePath
     */
    public String GetSessionFilePath()
    {
        String strSessionFilePath = null;

        try
        {
            strSessionFilePath = prefs.get(prefSessionFilePath,
                    strDefSessionFilePath);
        }
        catch(IllegalStateException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal State Exception:\n", e);
        }
        catch(NullPointerException e)
        {
            logger.log(Level.SEVERE,
                    "Null Pointer Exception:\n", e);
        }
        return strSessionFilePath;
    }

    /**
     * Get the value for TesterName in the preferences.
     * @return prefTesterName
     */
    public String GetTesterName()
    {
        String strTesterName = null;

        try
        {
            strTesterName = prefs.get(prefTesterName,strDefTesterName);
        }
        catch(IllegalStateException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal State Exception:\n", e);
        }
        catch(NullPointerException e)
        {
            logger.log(Level.SEVERE,
                    "Null Pointer Exception:\n", e);
        }
        return strTesterName;
    }

    /**
     * Get the value for WebReportPath in the preferences.
     * @return prefWebReportPath
     */
    public String GetWebReportPath()
    {
        String strWebReportPath = null;

        try
        {
            strWebReportPath = prefs.get(prefWebReportPath,
                    strDefWebReportPath);
        }
        catch(IllegalStateException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal State Exception:\n", e);
        }
        catch(NullPointerException e)
        {
            logger.log(Level.SEVERE,
                    "Null Pointer Exception:\n", e);
        }
        return strWebReportPath;
    }

    /**
     * Import preferences from an xml-file.
     */
    public void ImportPrefs()
    {
        InputStream is = null;
        try
        {
            is = new BufferedInputStream(
                    new FileInputStream("preferences.xml")
                    );
        }
        catch (FileNotFoundException e)
        {
            logger.log(Level.SEVERE,
                    "File not found:\n", e);
        }
        try
        {
            // Import the node to a file
            prefs.importPreferences(is);
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE,
                    "IO Exception:\n", e);
        }
        catch(SecurityException e)
        {
            logger.log(Level.SEVERE,
                    "Security Exception:\n", e);
        }
        catch(InvalidPreferencesFormatException e)
        {
            logger.log(Level.SEVERE,
                "Invalid Preference Format Exception:\n", e);
        }
    }

    /**
     * Export preferences to an xml-file.
     */
    public void ExportPrefs()
    {
        try
        {
            // Export the node to a file
            prefs.exportNode(new FileOutputStream("preferences.xml"));
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE,
                    "IO Exception:\n", e);
        }
        catch (BackingStoreException e)
        {
            logger.log(Level.SEVERE,
                    "Backing Store Exception:\n", e);
        }
        catch(IllegalStateException e)
        {
            logger.log(Level.SEVERE,
                    "Illegal State Exception:\n", e);
        }
    }

    /**
     * Generate the values for default preferences
     */
    private void GenerateDefaultPreferences()
    {
        strDefTesterName = "";
        strDefWebReportPath = "";
        strDefSessionFilePath = FileUtils.HOME_DIR.getAbsolutePath() + 
                "\\.sessiontester";
    }

    private Preferences prefs;
    private static Logger logger =
            Logger.getLogger(PreferencesUtils.class.getName());

    // Key value when storing preference data
    private static String   prefTesterName = "ST_TESTER_NAME";
    private static String   prefWebReportPath = "ST_WEB_REPORT_PATH";
    private static String   prefSessionFilePath = "ST_SESSION_FILE_PATH";
    private static String   prefProjectFilePath = "ST_PROJECT_FILE_PATH";
    private static String   prefSessionLength = "ST_SESSION_LENGTH";
    private static String   prefMissionReminderActivated =
            "ST_MISSION_REMINDER_ACTIVIATED";
    private static String   prefMissionReminder = "ST_MISSION_REMINDER";
    private static String   prefSessionEndWarningActivated =
            "ST_SESSION_END_WARNING_ACTIVATED";
    private static String   prefSessionEndWarning = "ST_SESSION_END_WARNING";
    private static String   prefPrimerFile = "ST_PRIMER_FILE";

    // Default values for preference data
    private static String   strDefTesterName;
    private static String   strDefWebReportPath;
    private static String   strDefSessionFilePath;
    private static String   strDefProjectFilePath;
    private static int      intDefSessionLength;
    private static boolean  boolDefMissionReminderActivated;
    private static int      intDefMissionReminder;
    private static boolean  boolDefSessionEndWarningActivated;
    private static int      intDefSessionEndWarning;
    private static String   strDefPrimerFile;
}

