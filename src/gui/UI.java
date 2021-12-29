package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UI extends JFrame {
    private JTextArea log;
    private JButton addNewProcessButton;
    private JPanel leftPanel;
    private JPanel originPanel;
    private JPanel rightPanel;
    private JButton killProcess;
    private JTextField processId;
    private JTextArea uiLog;

    private int currentPort = 9099;
    private String currentPorts = currentPort+" ";
    private Map<Long, Integer> processIdAndPortMap = new HashMap<>();
    private List<Process> processList = new ArrayList<>();
    public UI(){

        setContentPane(originPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 800);
        setVisible(true);
        addNewProcessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread thread = new Thread(() -> {
                    try {
                        Process p = Runtime.getRuntime().exec("java -jar bully-algorithm.jar " + currentPorts);
                        uiLog.append("process created, pid: "+p.pid()+" port: "+currentPort+"\n");
                        processList.add(p);
                        processIdAndPortMap.put(p.pid(), currentPort);
                        currentPort++;
                        currentPorts += currentPort+" ";
                        InputStream lsOut = p.getInputStream();
                        InputStreamReader r = new InputStreamReader(lsOut);
                        BufferedReader in = new BufferedReader(r);

                        String line;
                        while ((line = in.readLine()) != null) {
                            log.append(line+"\n");
                        }
                    } catch (IOException ex) {
                        uiLog.append(ex.getMessage()+"\n");
                    }
                });
                thread.start();
            }
        });
        uiLog.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                uiLog.setText("");
                for (Process p : processList) {
                    if(p.isAlive()) {
                        uiLog.append("process "+p.pid()+" alive\n");
                    } else {
                        uiLog.append("process "+p.pid()+" died\n");
                    }
                }
            }
        });
        killProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (processId.getText() != null) {
                    long pId = Long.parseLong(processId.getText());
                    for (Process p : processList) {
                        if (p.pid() == pId) {
                            Thread thread = new Thread(() -> {
                                try {
                                    Runtime.getRuntime().exec("taskkill /F /PID " +pId);
                                    Integer removedPort = processIdAndPortMap.get(p.pid());
                                    currentPorts = currentPorts.replace(removedPort+" ", "");
                                } catch (IOException ex) {
                                    uiLog.append(ex.getMessage()+"\n");
                                }
                            });
                            thread.start();
                            uiLog.append("killed process with id: "+pId+"\n");
                            processList.remove(p);
                            break;
                        }
                    }
                }
            }
        });
    }
}
