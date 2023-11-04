package org.example;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioPlayer extends JFrame{
    private static AudioPlayer audioPlayer;
    private static final JPanel panel = new JPanel(new GridLayout(5,1));
    private static final JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
    private static final JPanel controlPanel = new JPanel(new GridLayout(3,2));
    private static final JLabel audioName = new JLabel("");
    private static final JFileChooser fileChooser = new JFileChooser();
    private static final FileFilter audioFilter = new FileNameExtensionFilter("Arquivos de Áudio", "wav");
    private static final JProgressBar progress = new JProgressBar(0, 100);
    private static final JButton chooseAudio = new JButton("Choose audio");
    private static final JButton play = new JButton("Play");
    private static final JButton stop = new JButton("Stop");
    private static final JButton reset = new JButton("Reset");
    private static final JButton quit = new JButton("Quit");
    private static File audioFile;
    private static Clip clip;
    private static final List<FloatControl> controls = new ArrayList<>();

    private static void start()
    {
        if(audioFile == null) audioPlayer = new AudioPlayer();
    }

    private AudioPlayer()
    {
        initialize();

        buttonPanel.add(play);
        buttonPanel.add(stop);
        buttonPanel.add(reset);
        buttonPanel.add(quit);

        panel.add(audioName);
        panel.add(progress);
        panel.add(buttonPanel);
        panel.add(chooseAudio);

        panel.add(controlPanel);

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        audioName.setFont(new Font("Arial", Font.BOLD, 18));
        audioName.setHorizontalAlignment(SwingConstants.CENTER);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(panel);
        setVisible(true);
        setTitle("Tocador de audio");
        setSize(400,400);
    }

    private void initialize()
    {
        fileChooser.setFileFilter(audioFilter);

        initializeEvents();

        updateProgressBar();
    }

    private void initializeEvents()
    {
        chooseAudio.addActionListener(e ->
        {
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                if(clip != null) clip.close();
                audioFile = fileChooser.getSelectedFile();
                String audioNameString = audioFile.getName();
                audioName.setText(audioNameString.substring(0, audioNameString.lastIndexOf(".")));
                setAudioFile(audioFile);
                initializeControlSliders();
                clip.start();
            }
        });

        play.addActionListener((e) -> clip.start());

        stop.addActionListener((e) -> clip.stop());

        reset.addActionListener((e) ->
        {
            clip.setMicrosecondPosition(0);
            progress.setValue(0);
        });

        quit.addActionListener(e ->
        {
            clip.close();
            dispose();
        });
    }

    private void initializeControlSliders()
    {
        for (FloatControl control : controls) {
            JSlider controlSlider = new JSlider(0, 100);
            controlSlider.addChangeListener(e ->
            {
                int value = controlSlider.getValue();
                float min = control.getMinimum();
                float max = control.getMaximum();
                float range = max - min;
                float adjustedValue = min + (range * (value / 100.0f));
                control.setValue(adjustedValue);
            });

            controlPanel.add(new JLabel(control.getType().toString()));
            controlPanel.add(controlSlider);
        }
    }

    private static void setAudioFile(File audio) {
        try {
            audioFile = audio;
            AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);

            clip = AudioSystem.getClip();
            clip.open(ais);

            audioName.setText(audioFile.getName().substring(0, audioFile.getName().lastIndexOf(".")));

            Control[] clipControls = clip.getControls();
            for (Control control : clipControls) {
                if (control instanceof FloatControl) {
                    controls.add((FloatControl) control);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updateProgressBar() {
        Thread progressBarUpdater = new Thread(() ->
        {
            while (true) {
                if(clip != null)
                {
                    if(clip.isRunning())
                    {
                        int position = (int) clip.getMicrosecondPosition();
                        int length = (int) clip.getMicrosecondLength();
                        int percentage = (int) ((position * 100.0) / length);
                        progress.setValue(percentage);
                    }
                }
            }
        });
        progressBarUpdater.start();
    }

    public static void main(String[] args)
    {
        start();
    }
}