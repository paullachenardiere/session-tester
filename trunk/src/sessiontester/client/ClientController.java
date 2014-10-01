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

import java.awt.Dialog.ModalExclusionType;
import java.io.File;
import java.util.Date;
import java.util.logging.Logger;
import javax.swing.JDialog;
import org.jdesktop.application.ResourceMap;
import sessiontester.FileUtils;
import sessiontester.Parser;
import sessiontester.Persistor;
import sessiontester.PlatformUtils;
import sessiontester.TestingSession;
import sessiontester.publisher.WebPublisher;

/**
 *
 * User actions and control flow are represented in this class.
 *
 * This allows easier interpretation and management of UI flow
 * without being overwhelmed by Swing glue code.
 *
 */
public class ClientController {

    public enum STATE {
        NoSession, SessionStarted, SessionEnded
    }

    private STATE state;
    private SystemTrayManager systemTrayManager;
    private Reminder reminder;
    private ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(Client.class);
    private File testingSessionFile;
    private boolean timerDebugFlag;
    private Client view;
    private Date startDate;
    private static final Logger logger = Logger.getLogger(ClientController.class.getName());
    private static int FIELD_MAX_LENGTH = 200;

    public ClientController(Client view) {
        this.view = view;

        systemTrayManager = new SystemTrayManager(this);

        if(PlatformUtils.isMac()) {
            view.initializeMacSpecificPlatformTweaks();
        }
        view.initializeHotKeys();
        view.setTags(Parser.TAGS);
        view.resetForm();
        setState(STATE.NoSession);
    }

    private void setState(STATE state) {
        this.state = state;
        view.refresh(state);
    }

    public STATE getState() {
        return state;
    }

    public boolean isTimerDebugFlag() {
        return timerDebugFlag;
    }

    public void setTimerDebugFlag(boolean timerDebugFlag) {
        this.timerDebugFlag = timerDebugFlag;
    }

    private TestingSession buildTestSession() {
        Parser parser = new Parser();
        Date endDate = new Date();
        TestingSession session = parser.parse(view.getEntryText());
        session.mission = maxLength(view.getNewSessionDialog().getMissionText(), FIELD_MAX_LENGTH);
        session.start = startDate.toString();
        session.end = endDate.toString();
        session.tester = maxLength(view.getNewSessionDialog().getTesterText(), FIELD_MAX_LENGTH);
        long difference = endDate.getTime() - startDate.getTime();
        int minutes = (int) (difference / 1000 / 60);
        session.duration = minutes + " min(s)";
        return session;
    }

    private String maxLength(String input, int maxLength) {
        if(input.length() > maxLength) {
            return input.substring(0, maxLength - 1);
        }

        return input;
    }

    private void saveTestingSession() throws Exception {
        Persistor.save(testingSessionFile, buildTestSession());
        String fileName = "\n" + testingSessionFile.toString();
        systemTrayManager.displayMessage(resourceMap.getString("MainForm.title"), resourceMap.getString("SysTray.SessionSaved") + fileName);
    }

    public void saveClicked() throws Exception{
        saveTestingSession();
    }

    public void stopClicked() throws Exception {
        reminder.stop();
        saveTestingSession();
        setState(STATE.SessionEnded);
    }

    public void startClicked() {
        setState(STATE.NoSession);
        view.showNewSessionDialog();
    }

    void startNewSessionDialogCancelled() {
        setState(STATE.NoSession);
    }

    public void startNewSessionDialogCompleted() {
        NewSessionDialog dialogView = view.getNewSessionDialog();
        
        reminder = new Reminder(this, view.getProgressBar(), systemTrayManager, view.getTimeStatusLabel(), resourceMap.getString("MainForm.title"));
        //Validate entry fields
        if (dialogView.getTesterText().trim().equals("") || dialogView.getMissionText().trim().equals("")) {
            dialogView.showError(resourceMap.getString("validation.error.text"), resourceMap.getString("validation.error.title"));
            dialogView.focusOnTesterField();
        } else {
            //Commence session
            testingSessionFile = FileUtils.buildTestingSessionFileFromCurrentTime();
            startDate = new Date(); //Track start time for saving later
            view.setStatusBarLabels(maxLength(dialogView.getMissionText(), FIELD_MAX_LENGTH), maxLength(dialogView.getTesterText(), FIELD_MAX_LENGTH));

            int sessionLength;
            int missionReminderInterval;
            int sessionEndReminderInterval;

            //Sets timers to hardcoded, short settings if debug is set
            if (timerDebugFlag) {
                logger.info("SessionLength = 4; MissionReminderInterval = 2; SessionEndReminderInterval = 2");
                sessionLength = 4;
                missionReminderInterval = 2;
                sessionEndReminderInterval = 2;

            } else {
                sessionLength = dialogView.getSessionLength();
                missionReminderInterval = dialogView.getMissionReminderInterval();
                sessionEndReminderInterval = dialogView.getSessionEndReminderInterval();
            }

            if (dialogView.isMissionReminderChecked()) {
                reminder.setMissionReminder(missionReminderInterval, dialogView.getMissionText());
            }
            if (dialogView.isSessionEndReminderChecked()) {
                reminder.setSessionEndReminder(sessionEndReminderInterval, resourceMap.getString("SysTray.SessionEndReminder"));
            }

            reminder.setSessionLength(sessionLength, resourceMap.getString("SysTray.SessionEnd"));
            reminder.start();
            setState(STATE.SessionStarted);
        }

    }

    public void extendSessionClicked() {
        reminder.extendSession(5);
        view.refresh(state);
        systemTrayManager.displayMessage(resourceMap.getString("sessionextended.info.title"), resourceMap.getString("sessionextended.info.text"));
    }

    public void publishWebReport(File reportDirectory) throws Exception {
        WebPublisher publisher = new WebPublisher();
        publisher.publishTo(reportDirectory);
        String directoryName = reportDirectory.toString();
        //TODO ajw: On mac, this looks like crap. Swing on mac has fixed size alert window.
        systemTrayManager.displayMessage(resourceMap.getString("MainForm.title"), resourceMap.getString("SysTray.WebReportGenerated") + directoryName);
    }

    public void pauseClicked() {
        if (reminder.isPaused()) {
            view.setPauseButtonText(resourceMap.getString("jPauseButton.text"));
            reminder.resume();
        } else {
            view.setPauseButtonText(resourceMap.getString("jPauseButton.resume.text"));
            reminder.pause();
        }
    }

    public void notifySessionEnded() {
        view.setStopButtonText(resourceMap.getString("jStopButton.restart.text"));
        setState(STATE.SessionEnded);
    }

    public void ExitApplication() {
        System.exit(0);
    }

    public void showAboutDialog() {
        JDialog aboutBox = new AboutBox(view);
        aboutBox.setVisible(true);
    }

    public void showSessionTester() {
        view.setVisible(true);
    }

    public void showPreferencesDialog() {
        JDialog dlgPreferences = new PreferencesDialog(view);
        dlgPreferences.setVisible(true);
    }
}
