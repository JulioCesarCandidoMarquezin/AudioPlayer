package org.example;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Objects;

public class AudioPlayer {
    private static File audioFile;
    private static Clip clip;
    private static FloatControl volumeControl;
    private static FloatControl balanceControl;

    public static void main(String[] args) {
        setAudioFile(new File(Objects.requireNonNull(AudioPlayer.class.getResource("/SnapInsta.io-Hino-Nacional-com-letra-_128-kbps_.wav")).getFile()));

        screen();
    }

    private static void setAudioFile(File audio) {
        try {
            audioFile = audio;
            AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);

            clip = AudioSystem.getClip();
            clip.open(ais);

            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            balanceControl = (FloatControl) clip.getControl(FloatControl.Type.BALANCE);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void screen()
    {
        JFrame frame = new JFrame("Tocador de audio");
        JPanel panel = new JPanel(new GridLayout(5,1)), buttonPanel = new JPanel(new GridLayout(1, 4));
        JLabel audioName = new JLabel(audioFile.getName().substring(0, audioFile.getName().lastIndexOf(".")));
        JFileChooser fileChooser = new JFileChooser();
        JProgressBar progress = new JProgressBar(0, 100);
        JButton chooseAudio = new JButton("Choose audio"), play = new JButton("Play"), stop = new JButton("Stop"), reset = new JButton("Reset"), quit = new JButton("Quit");

        JSlider volumeSlider = new JSlider(0, 100);
        JSlider balanceSlider = new JSlider(-100, 100);

        volumeSlider.addChangeListener(e -> {
            int value = volumeSlider.getValue();
            float gain = (float) Math.log10(value / 100.0) * 20;
            volumeControl.setValue(gain);
        });

        balanceSlider.addChangeListener(e -> {
            int value = balanceSlider.getValue();
            balanceControl.setValue(value / 100.0f);
        });

        FileFilter audioFilter = new FileNameExtensionFilter("Arquivos de Áudio", "wav");
        fileChooser.setFileFilter(audioFilter);

        updateProgressBar(progress);

        chooseAudio.addActionListener(e -> {
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                clip.close();
                audioFile = fileChooser.getSelectedFile();
                String audioNameString = audioFile.getName();
                audioName.setText(audioNameString.substring(0, audioNameString.lastIndexOf(".")));
                setAudioFile(audioFile);
                clip.start();
            }
        });
        play.addActionListener((e) -> clip.start());
        stop.addActionListener((e) -> clip.stop());
        reset.addActionListener((e) -> {
            clip.setMicrosecondPosition(0);
            progress.setValue(0);
        });
        quit.addActionListener(e -> {
            clip.close();
            frame.dispose();
        });

        buttonPanel.add(play);
        buttonPanel.add(stop);
        buttonPanel.add(reset);
        buttonPanel.add(quit);

        panel.add(audioName);
        panel.add(progress);
        panel.add(buttonPanel);
        panel.add(chooseAudio);

        JPanel controlPanel = new JPanel(new GridLayout(2, 2));
        controlPanel.add(new JLabel("Volume:"));
        controlPanel.add(volumeSlider);
        controlPanel.add(new JLabel("Balanço:"));
        controlPanel.add(balanceSlider);

        panel.add(controlPanel);

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        audioName.setFont(new Font("Arial", Font.BOLD, 18));
        audioName.setHorizontalAlignment(SwingConstants.CENTER);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.setSize(400,400);
    }

    private static void updateProgressBar(JProgressBar progress) {
        Thread progressBarUpdater = new Thread(() -> {
            while (true) {
                if(clip.isRunning()) {
                    int position = (int) clip.getMicrosecondPosition();
                    int length = (int) clip.getMicrosecondLength();
                    int percentage = (int) ((position * 100.0) / length);
                    progress.setValue(percentage);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        progressBarUpdater.start();
    }
}