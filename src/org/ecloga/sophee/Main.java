package org.ecloga.sophee;

import javazoom.jlgui.basicplayer.*;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Main extends JFrame implements NativeKeyListener {

    private BasicPlayer player;
    private BasicController control;
    private boolean paused;
    private JLabel lblStatus;

    public Main() {
        setTitle("Sophee");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int frameWidth = (int) (screenSize.width * 0.2);
        int frameHeight = (int) (screenSize.width * 0.1);

        int buttonWidth = (int) (frameWidth * 0.25);
        int buttonHeigth = (int) (frameHeight * 0.25);

        setSize(frameWidth, frameHeight);
        setLocation(0, screenSize.height - frameHeight);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JButton btnStart = new JButton("Play some sh*t");
        btnStart.setPreferredSize(new Dimension(buttonWidth, buttonHeigth));
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(control != null) {
                    try {
                        control.stop();
                    }catch(BasicPlayerException e1) {
                        JOptionPane.showMessageDialog(null, e1.getMessage());
                    }
                }

                paused = true;
                setStatus();

                getFile();
            }
        });

        lblStatus = new JLabel("STATUS");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblAuthor = new JLabel("Made with <3 by Ecloga Apps");
        lblAuthor.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(btnStart, BorderLayout.NORTH);
        panel.add(lblStatus, BorderLayout.CENTER);
        panel.add(lblAuthor, BorderLayout.SOUTH);

        setContentPane(panel);

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        GlobalScreen.getInstance().addNativeKeyListener(this);
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }

    private void getFile() {
        String userDir = System.getProperty("user.home");
        JFileChooser fileChooser = new JFileChooser(userDir + "/Desktop");

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".mp3");
            }

            @Override
            public String getDescription() {
                return "*.mp3";
            }
        };

        fileChooser.addChoosableFileFilter(filter);

        int result = fileChooser.showOpenDialog(null);

        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();

            if(!path.substring(path.length() - 3).equals("mp3")) {
                JOptionPane.showMessageDialog(null, "Only .mp3 files are allowed");
            }

            play(selectedFile);
        }
    }

    public void play(File file) {
        player = new BasicPlayer();
        control = (BasicController) player;

        try {
            control.open(file);
            control.play();
        }catch(BasicPlayerException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {}
    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {}

    private void setStatus() {
        if(paused) {
            lblStatus.setText("PAUSED");
        }else {
            lblStatus.setText("PLAYING");
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        if(paused) {
            try {
                control.resume();
                paused = false;
                setStatus();
            } catch (BasicPlayerException e) {
                e.printStackTrace();
            }

            for(int i = 3; i >= 0; i--) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(i == 0) {
                    try {
                        control.pause();
                        paused = true;
                        setStatus();
                    }catch(BasicPlayerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
