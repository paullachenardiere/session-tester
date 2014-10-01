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

import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import org.jdesktop.application.ResourceMap;
import sessiontester.Configuration;
import sessiontester.PlatformUtils;
import sessiontester.client.ClientController.STATE;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Application client
 */
public class Client extends javax.swing.JFrame {

    private ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(Client.class);
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static final String TIMER_DEBUG_FLAG = "TIMER_DEBUG";
    private ClientController controller;
    private Configuration configuration;
    private NewSessionDialog newSessionDialog;

    //Workaround required to fix combo box not generating tags if the selected value is selected again
    //See ST-45
    PopupMenuListener listener = new PopupMenuListener() {

        boolean cancelled;

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            cancelled = true;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (cancelled) {
                return;
            }
            final JComboBox theComboBox = (JComboBox) e.getSource();

            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    addTag();
                }
            });
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            cancelled = false;
        }
    };

    public JProgressBar getProgressBar() {
        return jTimerProgressBar;
    }

    public JLabel getTimeStatusLabel() {
        return jTimeStatusLabel;
    }

    public void setTags(String[] tags) {
        jTagComboBox.setModel(new DefaultComboBoxModel(tags));
    }

    public JLabel getTesterLabel() {
        return jTesterLabel;
    }

    public String getEntryText() {
        return jEntryTextArea.getText();
    }

    public Client(Configuration configuration) throws Exception {
        this.configuration = configuration;
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        initComponents();

        controller = new ClientController(this);

        newSessionDialog = new NewSessionDialog(this, true, controller);

        //Sets shorter timer durations to allow for debugging of timer issues
        String timerDebugProperty = System.getProperty(TIMER_DEBUG_FLAG);
        if (timerDebugProperty != null && timerDebugProperty.equalsIgnoreCase("true")) {
            logger.info("Timer debug enabled..");
            controller.setTimerDebugFlag(true);
        }

        jTagComboBox.addPopupMenuListener(listener);

        URL url = Client.class.getResource("sessiontester_icon.png");
				Image image = Toolkit.getDefaultToolkit().
					getImage(url);
        setIconImage(image);

    }

    /**
     * Mac specific initialization code goes here
     */
    public void initializeMacSpecificPlatformTweaks() {
        //Redirect Exit and About items to the Apple Application menu
        jExitMenuItem.setVisible(false);
        jAboutMenuItem.setVisible(false);
        final Client frame = this;
        com.apple.eawt.Application.getApplication().addApplicationListener(new ApplicationAdapter() {

            @Override
            public void handleAbout(ApplicationEvent ae) {
                frame.controller.showAboutDialog();
                ae.setHandled(true);
            }

            @Override
            public void handleQuit(ApplicationEvent ae) {
                frame.controller.ExitApplication();
            }
        });

    }

    /**
     * Configures hot keys for button actions
     */
    public void initializeHotKeys() {

        //Disable button hotkeys on mac as this violates
        //Apple user interface guidelines
        if (PlatformUtils.isMac()) {
            return;
        }

        jTabbedPane.setMnemonicAt(0, 'e');
        jStartButton.setMnemonic('t');
        jPauseButton.setMnemonic('p');
        jStopButton.setMnemonic('t');
        jSaveButton.setMnemonic('s');
        jPrimingButton.setMnemonic('m');
    }

    public void resetForm() {
        jTesterLabel.setText(resourceMap.getString("Session Tester.welcome"));
        jEntryTextArea.setText(resourceMap.getString("entry.initial.text"));
    }

    public void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, title, message, JOptionPane.ERROR_MESSAGE);
    }

    public void showIOError() {
        showError(resourceMap.getString("file.io.error.title"), resourceMap.getString("file.io.error.text"));
    }



    public void setStatusBarLabels(String mission, String tester) {
        jMissionLabel.setText(mission);
        jTesterLabel.setText(tester);
    }

    public void refresh(STATE state) {

        if (state == STATE.NoSession) {
            if(newSessionDialog != null) {
                newSessionDialog.setVisible(false);
                newSessionDialog.reset();
            }
            resetForm();
            jMissionLabel.setVisible(false);
            jTagComboBox.setEnabled(false);
            jEntryTextArea.setEnabled(false);
            jStartButton.setEnabled(true);
            jPauseButton.setEnabled(false);
            jStopButton.setEnabled(false);
            jSaveButton.setEnabled(false);
            jSaveMenuItem.setEnabled(false);
            jExtendButton.setEnabled(false);

            jTimerProgressBar.setVisible(false);
            jTimeStatusLabel.setVisible(false);
            jTimeStatusUnitsLabel.setVisible(false);

        } else if (state == STATE.SessionStarted) {
            jMissionLabel.setVisible(true);
            jEntryTextArea.setEnabled(true);
            newSessionDialog.setVisible(false);
            jEntryTextArea.requestFocus();
            jTagComboBox.setEnabled(true);

            jStartButton.setEnabled(false);
            jPauseButton.setEnabled(true);
            jStopButton.setEnabled(true);
            jSaveButton.setEnabled(true);
            jSaveMenuItem.setEnabled(true);
            jExtendButton.setEnabled(true);

            jTimerProgressBar.setVisible(true);
            jTimeStatusLabel.setVisible(true);
            jTimeStatusUnitsLabel.setVisible(true);
        } else if (state == STATE.SessionEnded) {
            jTagComboBox.setEnabled(true);
            jEntryTextArea.setEnabled(true);
            jStartButton.setEnabled(true);
            jPauseButton.setEnabled(false);
            jStopButton.setEnabled(false);
            jExtendButton.setEnabled(false);
            jSaveButton.setEnabled(true);
            jSaveMenuItem.setEnabled(true);
        }

    }

    void setStopButtonText(String text) {
        jStopButton.setText(text);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jStopButton = new javax.swing.JButton();
        jTabbedPane = new javax.swing.JTabbedPane();
        jSessionPanel = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jEntryTextArea = new javax.swing.JTextArea();
        jTagComboBox = new javax.swing.JComboBox();
        jLabel22 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jMissionLabel = new javax.swing.JLabel();
        jStartButton = new javax.swing.JButton();
        jPauseButton = new javax.swing.JButton();
        jSaveButton = new javax.swing.JButton();
        jStatusPanel = new javax.swing.JPanel();
        jTimerProgressBar = new javax.swing.JProgressBar();
        jTimeStatusUnitsLabel = new javax.swing.JLabel();
        jTimeStatusLabel = new javax.swing.JLabel();
        jTesterLabel = new javax.swing.JLabel();
        jPrimingButton = new javax.swing.JButton();
        jExtendButton = new javax.swing.JButton();
        jMenuBar = new javax.swing.JMenuBar();
        jFileMenu = new javax.swing.JMenu();
        jSaveMenuItem = new javax.swing.JMenuItem();
        jPreferencesMenu = new javax.swing.JMenuItem();
        jExitMenuItem = new javax.swing.JMenuItem();
        jReportMenu = new javax.swing.JMenu();
        jGenerateWebReportItem = new javax.swing.JMenuItem();
        jHelpMenu = new javax.swing.JMenu();
        jCheatSheetMenuItem = new javax.swing.JMenuItem();
        jHelpMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jAboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusCycleRoot(false);
        setFocusTraversalPolicyProvider(true);
        setName("MainForm"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("sessiontester/client/resources/Client"); // NOI18N
        jStopButton.setText(bundle.getString("jStopButton.text")); // NOI18N
        jStopButton.setMaximumSize(new java.awt.Dimension(100, 30));
        jStopButton.setMinimumSize(new java.awt.Dimension(100, 30));
        jStopButton.setName("jStopButton"); // NOI18N
        jStopButton.setPreferredSize(new java.awt.Dimension(100, 30));
        jStopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStopButtonActionPerformed(evt);
            }
        });

        jTabbedPane.setName("jTabbedPane"); // NOI18N
        jTabbedPane.setRequestFocusEnabled(false);

        jSessionPanel.setName("jSessionPanel"); // NOI18N
        jSessionPanel.setOpaque(false);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jEntryTextArea.setColumns(20);
        jEntryTextArea.setLineWrap(true);
        jEntryTextArea.setRows(8);
        jEntryTextArea.setName("jEntryTextArea"); // NOI18N
        jScrollPane3.setViewportView(jEntryTextArea);

        jTagComboBox.setName("jTagComboBox"); // NOI18N

        jLabel22.setText(bundle.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        jLabel17.setText(bundle.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        jMissionLabel.setText(bundle.getString("jMissionLabel.text")); // NOI18N
        jMissionLabel.setName("jMissionLabel"); // NOI18N

        javax.swing.GroupLayout jSessionPanelLayout = new javax.swing.GroupLayout(jSessionPanel);
        jSessionPanel.setLayout(jSessionPanelLayout);
        jSessionPanelLayout.setHorizontalGroup(
            jSessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSessionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSessionPanelLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jMissionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel22)
                        .addGap(1, 1, 1)
                        .addComponent(jTagComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jSessionPanelLayout.setVerticalGroup(
            jSessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jSessionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jSessionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(jMissionLabel)
                    .addComponent(jLabel22)
                    .addComponent(jTagComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane.addTab("Session", jSessionPanel);

        jStartButton.setText(bundle.getString("jStartButton.text")); // NOI18N
        jStartButton.setEnabled(false);
        jStartButton.setMaximumSize(new java.awt.Dimension(100, 30));
        jStartButton.setMinimumSize(new java.awt.Dimension(100, 30));
        jStartButton.setName("jStartButton"); // NOI18N
        jStartButton.setPreferredSize(new java.awt.Dimension(100, 30));
        jStartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jStartButtonActionPerformed(evt);
            }
        });

        jPauseButton.setText(bundle.getString("jPauseButton.text")); // NOI18N
        jPauseButton.setMaximumSize(new java.awt.Dimension(100, 30));
        jPauseButton.setMinimumSize(new java.awt.Dimension(100, 30));
        jPauseButton.setName("jPauseButton"); // NOI18N
        jPauseButton.setPreferredSize(new java.awt.Dimension(100, 30));
        jPauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPauseButtonActionPerformed(evt);
            }
        });

        jSaveButton.setText(bundle.getString("jSaveButton.text")); // NOI18N
        jSaveButton.setMaximumSize(new java.awt.Dimension(75, 30));
        jSaveButton.setMinimumSize(new java.awt.Dimension(75, 30));
        jSaveButton.setName("jSaveButton"); // NOI18N
        jSaveButton.setPreferredSize(new java.awt.Dimension(100, 30));
        jSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveButtonActionPerformed(evt);
            }
        });

        jStatusPanel.setName("jStatusPanel"); // NOI18N
        jStatusPanel.setPreferredSize(new java.awt.Dimension(600, 85));

        jTimerProgressBar.setName("jTimerProgressBar"); // NOI18N

        jTimeStatusUnitsLabel.setText(bundle.getString("jTimeStatusUnitsLabel.text")); // NOI18N
        jTimeStatusUnitsLabel.setName("jTimeStatusUnitsLabel"); // NOI18N

        jTimeStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jTimeStatusLabel.setText(bundle.getString("jTimeStatusLabel.text")); // NOI18N
        jTimeStatusLabel.setName("jTimeStatusLabel"); // NOI18N

        jTesterLabel.setText(bundle.getString("jTesterLabel.text")); // NOI18N
        jTesterLabel.setName("jTesterLabel"); // NOI18N

        javax.swing.GroupLayout jStatusPanelLayout = new javax.swing.GroupLayout(jStatusPanel);
        jStatusPanel.setLayout(jStatusPanelLayout);
        jStatusPanelLayout.setHorizontalGroup(
            jStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStatusPanelLayout.createSequentialGroup()
                .addGroup(jStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jStatusPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jTesterLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jStatusPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTimeStatusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTimeStatusUnitsLabel))
                    .addGroup(jStatusPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTimerProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 822, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jStatusPanelLayout.setVerticalGroup(
            jStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jStatusPanelLayout.createSequentialGroup()
                .addComponent(jTesterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTimerProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTimeStatusLabel)
                    .addComponent(jTimeStatusUnitsLabel)))
        );

        jPrimingButton.setText(bundle.getString("jPrimingButton.text")); // NOI18N
        jPrimingButton.setMaximumSize(new java.awt.Dimension(105, 30));
        jPrimingButton.setMinimumSize(new java.awt.Dimension(105, 30));
        jPrimingButton.setName("jPrimingButton"); // NOI18N
        jPrimingButton.setPreferredSize(new java.awt.Dimension(105, 30));
        jPrimingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPrimingButtonActionPerformed(evt);
            }
        });

        jExtendButton.setText(bundle.getString("jExtendButton.text")); // NOI18N
        jExtendButton.setMaximumSize(new java.awt.Dimension(100, 30));
        jExtendButton.setMinimumSize(new java.awt.Dimension(100, 30));
        jExtendButton.setName("jExtendButton"); // NOI18N
        jExtendButton.setPreferredSize(new java.awt.Dimension(100, 30));
        jExtendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExtendButtonActionPerformed(evt);
            }
        });

        jMenuBar.setName("jMenuBar"); // NOI18N

        jFileMenu.setMnemonic('A');
        jFileMenu.setText(bundle.getString("jFileMenu.text")); // NOI18N
        jFileMenu.setName("jActionsMenu"); // NOI18N

        jSaveMenuItem.setText(bundle.getString("jSaveMenuItem")); // NOI18N
        jSaveMenuItem.setName("jSaveMenuItem"); // NOI18N
        jSaveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSaveMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jSaveMenuItem);

        jPreferencesMenu.setText(bundle.getString("jPreferencesMenuItem.text")); // NOI18N
        jPreferencesMenu.setName("jPreferencesMenuItem"); // NOI18N
        jPreferencesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPreferencesMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jPreferencesMenu);

        jExitMenuItem.setMnemonic('x');
        jExitMenuItem.setText(bundle.getString("jExitMenuItem.text")); // NOI18N
        jExitMenuItem.setName("jExitMenuItem"); // NOI18N
        jExitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jExitMenuItemActionPerformed(evt);
            }
        });
        jFileMenu.add(jExitMenuItem);

        jMenuBar.add(jFileMenu);

        jReportMenu.setText(bundle.getString("jReportMenu.text")); // NOI18N
        jReportMenu.setName("jReportMenu"); // NOI18N

        jGenerateWebReportItem.setMnemonic('w');
        jGenerateWebReportItem.setText(bundle.getString("jGenerateWebReportItem.text")); // NOI18N
        jGenerateWebReportItem.setName("jGenerateWebReportItem"); // NOI18N
        jGenerateWebReportItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGenerateWebReportItemActionPerformed(evt);
            }
        });
        jReportMenu.add(jGenerateWebReportItem);

        jMenuBar.add(jReportMenu);

        jHelpMenu.setMnemonic('H');
        jHelpMenu.setText(bundle.getString("jHelpMenu.text")); // NOI18N
        jHelpMenu.setName("jHelpMenu"); // NOI18N

        jCheatSheetMenuItem.setText(bundle.getString("jCheatSheetMenuItem.text")); // NOI18N
        jCheatSheetMenuItem.setName("jCheatSheetMenuItem"); // NOI18N
        jCheatSheetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheatSheetMenuItemActionPerformed(evt);
            }
        });
        jHelpMenu.add(jCheatSheetMenuItem);

        jHelpMenuItem.setText(bundle.getString("jHelpMenuItem.text")); // NOI18N
        jHelpMenuItem.setName("jHelpMenuItem"); // NOI18N
        jHelpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jHelpMenuItemActionPerformed(evt);
            }
        });
        jHelpMenu.add(jHelpMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jHelpMenu.add(jSeparator1);

        jAboutMenuItem.setMnemonic('A');
        jAboutMenuItem.setText(bundle.getString("jAboutMenuItem.text")); // NOI18N
        jAboutMenuItem.setName("jAboutMenuItem"); // NOI18N
        jAboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAboutMenuItemActionPerformed(evt);
            }
        });
        jHelpMenu.add(jAboutMenuItem);

        jMenuBar.add(jHelpMenu);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jStatusPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 858, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 828, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jStartButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPauseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jStopButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jExtendButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 190, Short.MAX_VALUE)
                        .addComponent(jPrimingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(22, 22, 22))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jStartButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPauseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jStopButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jExtendButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPrimingButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jPauseButton, jPrimingButton, jSaveButton, jStartButton, jStopButton});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveTestingSession() {
        try {
            controller.saveClicked();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred saving testing session", ex);
            showIOError();
        }
    }

    private void jStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStopButtonActionPerformed
        try {
            controller.stopClicked();
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).
                    log(Level.SEVERE, "Exception occured clicking stop button", ex);
            showIOError();
        }

}//GEN-LAST:event_jStopButtonActionPerformed

	private void jStartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jStartButtonActionPerformed
        controller.startClicked();
}//GEN-LAST:event_jStartButtonActionPerformed

	private void jExitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExitMenuItemActionPerformed
        controller.ExitApplication();
}//GEN-LAST:event_jExitMenuItemActionPerformed

    private void jAboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        controller.showAboutDialog();
    }

	private void jGenerateWebReportItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGenerateWebReportItemActionPerformed
        try {
            JOptionPane.showMessageDialog(this, resourceMap.getString("report.info.text"), resourceMap.getString("report.info.title"), JOptionPane.INFORMATION_MESSAGE);
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showOpenDialog(chooser);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File reportDirectory = chooser.getSelectedFile();
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                controller.publishWebReport(reportDirectory);
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception occurred generating report", ex);
            showIOError();
        } finally {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
	}//GEN-LAST:event_jGenerateWebReportItemActionPerformed

    private void addTag() {
        String selectedTag = (String) jTagComboBox.getModel().getSelectedItem();
        int currentPosition = jEntryTextArea.getCaretPosition();
        if (selectedTag.compareTo("@timestamp") == 0) {
            jEntryTextArea.insert("\n" + selectedTag + " [" + new Date().toString() + "]\n", currentPosition);
        } else {
            jEntryTextArea.insert("\n" + selectedTag + "\n", currentPosition);
        }

        jEntryTextArea.requestFocus();
    }

private void jPauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPauseButtonActionPerformed
    controller.pauseClicked();
}//GEN-LAST:event_jPauseButtonActionPerformed

private void jSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveButtonActionPerformed
    saveTestingSession();
}//GEN-LAST:event_jSaveButtonActionPerformed

private void jPrimingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPrimingButtonActionPerformed
    List<String> primingIdeas = configuration.getPrimingList();

    if (primingIdeas == null || primingIdeas.isEmpty()) {
        logger.log(Level.SEVERE, "priming.txt file is empty. Could not display priming message.");
        showError(resourceMap.getString("priming.title"), resourceMap.getString("priming.error"));
        return;
    }

    int randomNth = new Random().nextInt(primingIdeas.size());
    String idea = primingIdeas.get(randomNth);
    String[] options = {resourceMap.getString("close.text"), resourceMap.getString("another.text")};
    if (JOptionPane.showOptionDialog(this, idea, resourceMap.getString("priming.title"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]) == 1) {
        jPrimingButtonActionPerformed(evt);
    }
}//GEN-LAST:event_jPrimingButtonActionPerformed

private void jCheatSheetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheatSheetMenuItemActionPerformed
    openBrowserToResource("/Resources/testheuristicscheatsheetv1.pdf");
}//GEN-LAST:event_jCheatSheetMenuItemActionPerformed

private void jHelpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jHelpMenuItemActionPerformed
    openBrowserToResource("/Resources/sessiontester_user_guide.html");
}//GEN-LAST:event_jHelpMenuItemActionPerformed

private void jExtendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jExtendButtonActionPerformed
    controller.extendSessionClicked();
}//GEN-LAST:event_jExtendButtonActionPerformed

private void jPreferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPreferencesMenuItemActionPerformed
    controller.showPreferencesDialog();
}//GEN-LAST:event_jPreferencesMenuItemActionPerformed

private void jSaveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSaveMenuItemActionPerformed
    saveTestingSession();
}//GEN-LAST:event_jSaveMenuItemActionPerformed


    private void openBrowserToResource(String resourceFileName) {
        try {
            if (PlatformUtils.isMac()) {
                //Append path into application bundle
                //Unfortunately, this will cause the menu options to not work
                //when testing in NetBeans.
                File file = new File(System.getProperty("user.dir") + "/SessionTester.app/Contents/Resources/Java" + resourceFileName);
                Desktop.getDesktop().browse(file.toURI());
                return;
            }

            File file = new File(System.getProperty("user.dir") + resourceFileName);

            if (PlatformUtils.isWindows()) {
                //Work around for crashing JVM process on shell exec on some Windows XP configurations
                //on Desktop.getDesktop invoke
                Runtime.getRuntime().exec("cmd /c start " + file.toURI());
                return;
            }

            //All other platforms..
            Desktop.getDesktop().browse(file.toURI());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not launch browser.", e);
        }
    }

    public void setPauseButtonText(String text) {
        jPauseButton.setText(text);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    logger.info("Starting session tester client.");
                    PlatformUtils.initializeMacPlatformProperties();
                    Configuration configuration = Configuration.BuildConfiguation();
                    Client client = new Client(configuration);
                    
                    client.setVisible(true);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "A serious error has occurred launching the session tester client", ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jAboutMenuItem;
    private javax.swing.JMenuItem jCheatSheetMenuItem;
    private javax.swing.JTextArea jEntryTextArea;
    private javax.swing.JMenuItem jExitMenuItem;
    private javax.swing.JButton jExtendButton;
    private javax.swing.JMenu jFileMenu;
    private javax.swing.JMenuItem jGenerateWebReportItem;
    private javax.swing.JMenu jHelpMenu;
    private javax.swing.JMenuItem jHelpMenuItem;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JLabel jMissionLabel;
    private javax.swing.JButton jPauseButton;
    private javax.swing.JMenuItem jPreferencesMenu;
    private javax.swing.JButton jPrimingButton;
    private javax.swing.JMenu jReportMenu;
    private javax.swing.JButton jSaveButton;
    private javax.swing.JMenuItem jSaveMenuItem;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel jSessionPanel;
    private javax.swing.JButton jStartButton;
    private javax.swing.JPanel jStatusPanel;
    private javax.swing.JButton jStopButton;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JComboBox jTagComboBox;
    private javax.swing.JLabel jTesterLabel;
    private javax.swing.JLabel jTimeStatusLabel;
    private javax.swing.JLabel jTimeStatusUnitsLabel;
    private javax.swing.JProgressBar jTimerProgressBar;
    // End of variables declaration//GEN-END:variables

    public void showNewSessionDialog() {
        newSessionDialog.setModal(true);
        newSessionDialog.setVisible(true);
    }
    
    public NewSessionDialog getNewSessionDialog() {
        return newSessionDialog;
    }

}
