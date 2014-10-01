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
package sessiontester.client;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages interactions with the System Tray
 */
public class SystemTrayManager {

	private SystemTray tray;

	public SystemTrayManager(final ClientController controller) {

		if (SystemTray.isSupported()) {
			try {
				tray = SystemTray.getSystemTray();
				URL url = Client.class.getResource("sessiontester_icon.png");

				Image image = Toolkit.getDefaultToolkit().
					getImage(url);
				TrayIcon trayIcon = new TrayIcon(image);
                                trayIcon.setImageAutoSize(true);

                                final PopupMenu popup = new PopupMenu();

                                // Create a popup menu components
                                MenuItem aboutItem = new MenuItem("About Session Tester");
                                MenuItem exitItem = new MenuItem("Exit");

                                popup.add(aboutItem);
                                popup.addSeparator();
                                popup.add(exitItem);

                                trayIcon.setPopupMenu(popup);
                                trayIcon.setToolTip("Session Tester");

				tray.add(trayIcon);

                                // Handle about
                                aboutItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        controller.showAboutDialog();
                                    }
                                });

                                // Handle exit
                                exitItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        controller.ExitApplication();
                                    }
                                });

                                // Handle double-click on trayIcon to show
                                // Session Tester
                                trayIcon.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        controller.showSessionTester();
                                    }
                                });


			} catch (AWTException ex) {
				Logger.getLogger(Client.class.getName()).
					log(Level.SEVERE, null, ex);
			}
		}
	}

	public void displayMessage(String caption, String text) {
		if (tray != null) {
                    TrayIcon trayIcons[] = tray.getTrayIcons();
                    for(TrayIcon trayIcon : trayIcons) {
                        String toolTip = trayIcon.getToolTip();
                        if(null != toolTip && toolTip.equals("Session Tester")) {
                            trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO);
                        }
                    }
		}
	}

}
