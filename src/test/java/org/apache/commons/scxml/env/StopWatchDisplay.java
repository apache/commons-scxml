/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml.env;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Quick GUI to demonstrate the SCXML driven stopwatch.
 *
 * Separation of UI (this class) from behavior (StopWatch class).
 * UI serves merely as a front to relay user initiated events to StopWatch
 * object, which encapsulates all the behavior of a stopwatch.
 * Using SCXML makes the StopWatch class simplistic, and provides a direct
 * route from the UML model to the runtime.
 *
 * @see StopWatch
 */
public class StopWatchDisplay extends JFrame
        implements ActionListener {

    private static final long serialVersionUID = 1L;
    private StopWatch stopWatch;
    private Image watchImage, watchIcon;

    public StopWatchDisplay() {
        super("SCXML stopwatch");
        stopWatch = new StopWatch();
        setupUI();
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("START")) {
            if (start.getText().equals("Start")) {
                stopWatch.fireEvent(StopWatch.EVENT_START);
                start.setText("Stop");
                split.setEnabled(true);
            } else if (start.getText().equals("Stop")) {
                stopWatch.fireEvent(StopWatch.EVENT_STOP);
                start.setText("Reset");
                split.setEnabled(false);
            } else {
                stopWatch.fireEvent(StopWatch.EVENT_RESET);
                start.setText("Start");
                split.setText("Split");
            }
        } else if (command.equals("SPLIT")) {
            if (split.getText().equals("Split")) {
                stopWatch.fireEvent(StopWatch.EVENT_SPLIT);
                split.setText("Unsplit");
            } else {
                stopWatch.fireEvent(StopWatch.EVENT_UNSPLIT);
                split.setText("Split");
            }
        }
    }

    private void setupUI() {
        URL imageURL = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/stopwatch.gif");
        URL iconURL = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/stopwatchicon.gif");
        Toolkit kit = Toolkit.getDefaultToolkit();
        watchImage = kit.createImage(imageURL);
        watchIcon = kit.createImage(iconURL);
        WatchPanel panel = new WatchPanel();
        panel.setLayout(new BorderLayout());
        setContentPane(panel);
        display = new JLabel(stopWatch.getDisplay());
        panel.add(display, BorderLayout.PAGE_START);
        start = makeButton("START", "start, stop, reset", "Start");
        panel.add(start, BorderLayout.LINE_START);
        state = new JLabel();
        panel.add(state, BorderLayout.CENTER);
        split = makeButton("SPLIT", "split, unsplit", "Split");
        split.setEnabled(false);
        panel.add(split, BorderLayout.LINE_END);
        pack();
        setLocation(200,200);
        setIconImage(watchIcon);
        setResizable(false);
        setSize(300,125);
        show();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Timer displayTimer = new Timer();
        displayTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                display.setText(DISPLAY_PREFIX + stopWatch.getDisplay()
                    + DISPLAY_SUFFIX);
                state.setText(STATE_PREFIX + stopWatch.getCurrentState()
                    + STATE_SUFFIX);
            }
        }, 100, 100);
    }

    private JButton makeButton(final String actionCommand,
            final String toolTipText, final String altText) {
        JButton button = new JButton(altText);
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        button.setOpaque(false);
        return button;
    }

    class WatchPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        public void paintComponent(Graphics g) {
            if(watchImage != null) {
                g.drawImage(watchImage,0,0,this.getWidth(),this.getHeight(),this);
            }
        }
    }

    public static void main(String[] args) {
        StopWatchDisplay stopWatchDisplay = new StopWatchDisplay();
    }

    private JLabel display, state;
    private JButton start, split;
    // spaces :: GridBagConstraints ;-)
    private static final String
        DISPLAY_PREFIX = "<html><font face=\"Courier\" color=\"maroon\"" +
            " size=\"10\"><b>&nbsp;&nbsp;&nbsp;",
        DISPLAY_SUFFIX = "</b></font></html>",
        STATE_PREFIX = "<html><font color=\"blue\" size=\"4\"" +
            ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",
        STATE_SUFFIX = "</font></html>";

}

