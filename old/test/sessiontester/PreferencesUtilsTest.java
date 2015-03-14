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

//import sessiontester.PreferencesUtils;
import junit.framework.TestCase;

public class PreferencesUtilsTest extends TestCase
{
    @Override
    public void setUp()
    {
        prefs = new PreferencesUtils();
    }

    @Override
    public void tearDown()
    {
        prefs.ClearPreferences();
    }

    public void testSetSessionFilePath_ValidValue()
    {
        String strOrigValue = prefs.GetSessionFilePath();

        prefs.SetSessionFilePath("x");

        assertEquals("x", prefs.GetSessionFilePath());
        prefs.SetSessionFilePath(strOrigValue);
    }

    public void testSetSessionFilePath_NullValue()
    {
        try
        {
            String strNull = null;
            prefs.SetSessionFilePath(strNull);
        }
        catch(NullPointerException e)
        {
            assertFalse(true);
        }
    }

    // Variables
    PreferencesUtils prefs;
}
