package org.mhf.mhf.logic;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;


public class HuntController{

    @FXML
    private ImageView monsterView;

    @FXML
    private TextArea huntingText;

    @FXML
    private TextArea timer;

    @FXML
    private ProgressBar hunterHealth;

    @FXML
    private ProgressBar hunterStamina;

    @FXML
    private ProgressBar monsterHealth;

    @FXML
    private ProgressBar monsterStamina;

    private boolean charged = false;
    private boolean affected = false;
    private double monsterhp = 1;
    private double hunterhp = 1;
    private String[] monsterInfo;
    private final String musicLocation = "src/music/";
    private final String huntLocation = "src/hunts/";

    private Image newMonsterImage;
    private final Random random = new Random();

    public static String monsterToHunt;


    public HuntController(){
    }

    @FXML
    public void initialize(){
        try (Stream<String> monsterInfoStream = Files.lines(Path.of( huntLocation  + "monsterInfo.txt"))) {
            for(String s : monsterInfoStream.toList()){
                if(s.split(";")[0].equalsIgnoreCase(monsterToHunt)){
                    monsterInfo = s.split(";");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Platform.runLater(() -> hunterHealth.setProgress(100));
        Platform.runLater(() -> monsterHealth.setProgress(Integer.parseInt(monsterInfo[1])));
        startMonsterThread();
        startMusicThread(monsterInfo[3]);
    }

    private void startMusicThread(String musicName) {
        String[] musicInfo = {};
        try(Stream<String> musicInfoStream = Files.lines(Path.of(musicLocation + "musicInfo.txt"))){
            for(String s : musicInfoStream.toList()){
                System.out.println(s);
                if(s.split(";")[0].equalsIgnoreCase(musicName)){
                    musicInfo = s.split(";");
                }
            }
            File musicPath = new File(musicLocation + musicName + ".wav");
            if(musicPath.exists()){
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float volume = 0.5f;
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
                clip.loop(Integer.MAX_VALUE);
                int loopStartPoint = (clip.getFrameLength()/Integer.parseInt(musicInfo[1]))*Integer.parseInt(musicInfo[2]);
                int loopEndPoint = (clip.getFrameLength()/Integer.parseInt(musicInfo[1]))*Integer.parseInt(musicInfo[3]);
                clip.setLoopPoints(loopStartPoint, loopEndPoint);
                clip.start();
            }else{
                throw new IOException("Path not found!");
            }
        }catch(IOException | UnsupportedAudioFileException | LineUnavailableException e){
            e.printStackTrace();
        }
    }

    private void startMonsterThread() {
        Thread monsterThread = new Thread(() -> {
            Path path = Path.of(huntLocation + monsterToHunt + ".txt");
            Map<Integer, List<List<String>>> monsterText = new TreeMap<>();
            try (Stream<String> textStream = Files.lines(path)) {
                Integer key;
                for (String s : textStream.toList()) {
                    List<String> l = new ArrayList<>(Arrays.stream(s.split(";")).toList());
                    key = Integer.valueOf(l.get(0));
                    if (!monsterText.containsKey(key)) {
                        monsterText.put(key, new ArrayList<>());
                    }
                    monsterText.get(key).add(l.subList(1, l.size()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            List<String> command = getRandomCommand(monsterText);
            newMonsterImage = new Image(command.get(0));
            while(true){
                Platform.runLater(() -> hunterHealth.setProgress(hunterhp));
                Platform.runLater(() -> monsterHealth.setProgress(monsterhp));
                monsterView.setImage(newMonsterImage);
                int taskTime = 10;
                huntingText.clear();
                for(String s : command.subList(1,command.size())){
                    try{
                        taskTime = Integer.parseInt(s);
                    }catch(NumberFormatException nfe){
                        huntingText.appendText(s + "\n");
                    }
                }
                try {
                    startTimer(taskTime);
                    command = getRandomCommand(monsterText);
                    getNextImage(command.get(0));
                    Thread.sleep(taskTime*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        monsterThread.setDaemon(true);
        monsterThread.start();
    }

    private void getNextImage(String s) {
        Thread imageThread = new Thread(() -> newMonsterImage = new Image(s));
        imageThread.setDaemon(true);
        imageThread.start();
    }

    private List<String> getRandomCommand(Map<Integer, List<List<String>>> monsterText) {
        int rand = random.nextInt(10);
        if(rand <=0){
            if(charged){
                charged = false;
                return monsterText.get(2).get(monsterText.get(2).size()-1);
            }else{
                charged = true;
                return monsterText.get(2).get(random.nextInt(monsterText.get(2).size()-1));
            }
        }else if(rand <=2){
            if(affected){
                affected = false;
                return monsterText.get(1).get(monsterText.get(1).size()-1);
            }else{
                affected = true;
                return monsterText.get(1).get(random.nextInt(monsterText.get(1).size()-1));
            }
        }else if(rand <= 5){
            monsterhp-=0.1;
            return monsterText.get(3).get(random.nextInt(monsterText.get(3).size()));
        }else{
            hunterhp-=0.1;
            return monsterText.get(0).get(random.nextInt(monsterText.get(0).size()));
        }

    }


    private void startTimer(int taskTime) {
        Thread timerThread = new Thread(() -> {
           for(int i = 0; i < taskTime; i++){
               timer.clear();
               timer.appendText(Integer.toString(taskTime-i));
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }


}
