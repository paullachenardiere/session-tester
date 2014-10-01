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

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Manages test session reminders and progress bar
 */
class Reminder {

    //References to UI components that the reminder interacts with
    private JProgressBar progressBar;
    private JLabel timeRemainingLabel;
    private SystemTrayManager systemTrayManager;
    //Time intervals and counters; units are minutes
    private int sessionLength = 0;
    private int missionReminderInterval = 0;
    private int sessionEndReminderInterval = 0;
    private int minutesEllapsed;
    //Messages to be displayed
    private String missionReminderMessage;
    private String sessionEndMessage;
    private String sessionEndReminderMessage;
    private String caption;
    //Pause toggle.. Causes threads to not increment timer counters
    private boolean paused;
    //Stop flag. Notifies reminder threads to terminate.
    //NB: When used, Reminder object is now in an end state and must be discarded
    //Create a new reminder if another is needed
    private boolean stopped;
    
    org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(Reminder.class);
    private static long MINUTES_IN_MILLIS = 60 * 1000;
    private ClientController controller; //required for session end callback
    private static Logger logger = Logger.getLogger(Reminder.class.getName());

    public Reminder(ClientController controller, JProgressBar progressBar, SystemTrayManager systemTrayManager, JLabel timeRemainingLabel, String caption) {
        this.progressBar = progressBar;
        this.systemTrayManager = systemTrayManager;
        this.caption = caption;
        this.timeRemainingLabel = timeRemainingLabel;
        this.controller = controller;
    }

    public void setMissionReminder(int missionReminderIntervalMinutes, String message) {
        this.missionReminderInterval = missionReminderIntervalMinutes;
        this.missionReminderMessage = message;
    }

    public void setSessionEndReminder(int sessionEndReminderIntervalMinutes, String message) {
        this.sessionEndReminderInterval = sessionEndReminderIntervalMinutes;
        this.sessionEndReminderMessage = message;
    }

    public void setSessionLength(int sessionLengthMinutes, String message) {
        this.sessionLength = sessionLengthMinutes;
        this.sessionEndMessage = message;
    }

    public void stop() {
        stopped = true;
    }

    public void extendSession(int minutes) {
        if(minutes <= 0) {
            throw new IllegalArgumentException("Minutes to extend must be greater than 0");
        }
        progressBar.setMaximum(progressBar.getMaximum() + minutes);
        timeRemainingLabel.setText(Integer.toString(minutesRemaining()));
    }

    private int minutesRemaining() {
        return progressBar.getMaximum() - minutesEllapsed;
    }

    public void start() {

        final Timer progressBarTimer = new Timer();
        final Timer missionReminderTimer = new Timer();
        final Timer sessionEndReminderTimer = new Timer();

        if (sessionLength > 0) {
     
            //initialize time remaining label
            StringBuffer timeStatusText = new StringBuffer();
            timeStatusText.append(Integer.toString(sessionLength));

            timeRemainingLabel.setText(timeStatusText.toString());
            progressBar.setValue(0);

            //Required for inner class thread definition
            final JProgressBar innerProgressBar = progressBar;
            final JLabel innerTimeRemainingLabel = timeRemainingLabel;

            //Init progress bar to session length in minutes
            innerProgressBar.setMinimum(0);
            innerProgressBar.setMaximum(sessionLength);

            logger.info("Progress bar incrementer starting..");

            TimerTask progressBarTask = new TimerTask() {

                @Override
                public void run() {
                    if(stopped) {
                        this.cancel();
                        return;
                    }
                    //Assumes this is triggered every minute.
                    //Increments the progress bar by one minute.
                    if (!paused) {                        
                        minutesEllapsed = minutesEllapsed + 1;
                        int timeLeft = minutesRemaining();
                        innerTimeRemainingLabel.setText(Integer.toString(timeLeft));
                        innerProgressBar.setValue(minutesEllapsed);
                        if (minutesEllapsed == innerProgressBar.getMaximum()) {
                            missionReminderTimer.cancel();
                            sessionEndReminderTimer.cancel();
                            logger.info("Triggering session end and terminating reminder");
                            systemTrayManager.displayMessage(caption, sessionEndMessage);
                            controller.notifySessionEnded();
                            this.cancel();
                        }
                    }
                }
            };

            //Schedule progress bar update every minute
            progressBarTimer.schedule(progressBarTask, MINUTES_IN_MILLIS, MINUTES_IN_MILLIS);

        }

        //Configure mission reminder message if enabled
        if (missionReminderInterval > 0) {
            logger.info("Mission reminder starting..");
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    if(stopped) {
                        this.cancel();
                        return;
                    }
                    if (!paused) {
                        logger.info("Triggering mission reminder");
                        systemTrayManager.displayMessage(caption, missionReminderMessage);
                    }
                }
            };

            missionReminderTimer.schedule(task, 0, missionReminderInterval * MINUTES_IN_MILLIS);
        }

        //Configure session end reminder if enabled        
        if (sessionEndReminderInterval > 0 && sessionLength > 0) {
            logger.info("Session end reminder starting..");
            TimerTask task = new TimerTask() {
                
                @Override
                public void run() {
                    if (stopped) {
                        this.cancel();
                        return;
                    }
                    if (!paused) {
                        if(minutesRemaining() == sessionEndReminderInterval) {
                            logger.info("Triggering session end reminder");
                            try {
                                Thread.sleep(250); //Pause to ensure that this notification gets higher task tray priority (ST-23)
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            systemTrayManager.displayMessage(caption, sessionEndReminderMessage);
                            this.cancel();
                            return;
                        }
                    }
                }
            };

            //Schedule time remaining check every minute
            sessionEndReminderTimer.schedule(task, MINUTES_IN_MILLIS, MINUTES_IN_MILLIS);
        }

    }

    boolean isPaused() {
        return this.paused;
    }

    void pause() {
        paused = true;
    }

    void resume() {
        paused = false;
    }
}
