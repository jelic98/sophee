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

public class Sophee extends JFrame implements NativeKeyListener {

    private BasicController control;
    private JLabel lblStatus;
    private VolumeDecreaser decreaser;

    private enum PlayerStatus {
        PLAYING,
        PAUSED,
        NO_TRACK,
        READY
    }

    public Sophee() {
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

        JButton btnOpen = new JButton("Open");
        btnOpen.setPreferredSize(new Dimension(buttonWidth, buttonHeigth));
        btnOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (control != null) {
                    try {
                        control.stop();
                    } catch (BasicPlayerException e1) {
                        JOptionPane.showMessageDialog(null, e1.getMessage());
                    }
                }

                getFile();
            }
        });

        lblStatus = new JLabel();
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblAuthor = new JLabel("Made with <3 by Ecloga Apps");
        lblAuthor.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(btnOpen, BorderLayout.NORTH);
        panel.add(lblStatus, BorderLayout.CENTER);
        panel.add(lblAuthor, BorderLayout.SOUTH);

        setContentPane(panel);

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        GlobalScreen.getInstance().addNativeKeyListener(this);

        setStatus(PlayerStatus.NO_TRACK);
    }

    public static void main(String[] args) {
        new Sophee().setVisible(true);
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

    private void setStatus(PlayerStatus status) {
        lblStatus.setText(status.name());
    }

    private void play(File file) {
        control = new BasicPlayer();

        try {
            control.open(file);
            control.play();
            control.pause();

            setStatus(PlayerStatus.READY);
        }catch(BasicPlayerException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {}
    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
        if(decreaser != null && decreaser.isRunning()) {
            decreaser.reset();
        }else {
            try {
                control.setGain(0.0);
            } catch (BasicPlayerException e) {
                e.printStackTrace();
            }

            new VolumeIncreaser().start();
        }

        try {
            control.resume();
            setStatus(PlayerStatus.PLAYING);
        } catch (BasicPlayerException e) {
            e.printStackTrace();
        }
    }

    private void decreaseVolume() {
        decreaser = new VolumeDecreaser();
        decreaser.start();
    }

    private class VolumeIncreaser extends CustomTimer {

        @Override
        public void run() {
            decreaseVolume();

            double currentGain = 0.0;

            for (int i = 20; i >= 0; i--) {
                currentGain += 0.05;

                try {
                    control.setGain(currentGain);
                } catch (BasicPlayerException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                control.setGain(1.0);
            } catch (BasicPlayerException e) {
                e.printStackTrace();
            }

            decreaser.start();

            finish();
        }
    }

    private class VolumeDecreaser extends CustomTimer {

        @Override
        public void run() {
            double currentGain = 1.0;

            for(int i = 20; i >= 0; i--) {
                if(!isRunning()) {
                    break;
                }

                currentGain -= 0.05;

                try {
                    control.setGain(currentGain);
                } catch (BasicPlayerException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(i == 0) {
                    try {
                        control.pause();
                        setStatus(PlayerStatus.PAUSED);
                    }catch(BasicPlayerException e) {
                        e.printStackTrace();
                    }

                    try {
                        control.setGain(0.0);
                    } catch (BasicPlayerException e) {
                        e.printStackTrace();
                    }
                }
            }

            finish();
        }

        public void reset() {
            finish();
            start();
        }
    }

    private abstract class CustomTimer extends Thread {

        private boolean running;

        @Override
        public synchronized void start() {
            super.start();

            running = true;
        }

        protected void finish() {
            running = false;
        }

        protected boolean isRunning() {
            return running;
        }

        @Override
        public abstract void run();
    }
}
