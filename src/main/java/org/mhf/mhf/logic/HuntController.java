package org.mhf.mhf.logic;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class HuntController{

    @FXML
    private ImageView monsterView;

    @FXML
    private TextArea huntingText;

    @FXML
    private TextArea timer;

    @FXML
    private ProgressBar hunterHealthProgressBar;

    @FXML
    private ProgressBar hunterStaminaProgressBar;

    @FXML
    private ProgressBar monsterHealthProgressBar;

    @FXML
    private ProgressBar monsterStaminaProgressBar;

    private boolean monsterTurn = true;
    private boolean monsterCharged = false;
    private boolean statusEffectActive = false;
    private int monsterChargeDurationLeft, statusDurationLeft;
    private double monsterhp, hunterhp, monsterStamina, hunterStamina;
    private double monsterMaxHp, hunterMaxHp, monsterMaxStamina, hunterMaxStamina;
    private String[] monsterInfo;
    private final String musicLocation = "src/music/";
    private final String huntLocation = "src/hunts/";

    private List<List<String>> monsterAttacks = new ArrayList<>();
    private List<List<String>> monsterCharges = new ArrayList<>();
    private List<List<String>> monsterStatusEffects = new ArrayList<>();
    private List<List<String>> hunterAttacks = new ArrayList<>();
    private List<List<String>> hunterCarts = new ArrayList<>();
    private List<List<String>> hunterRest = new ArrayList<>();
    private List<List<String>> monsterRest = new ArrayList<>();

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

        monsterMaxHp = Integer.parseInt(monsterInfo[1]);
        monsterhp = monsterMaxHp;
        hunterMaxHp = 100;
        hunterhp = hunterMaxHp;
        monsterMaxStamina = Integer.parseInt(monsterInfo[2]);
        monsterStamina = monsterMaxStamina;
        hunterMaxStamina = 50;
        hunterStamina = hunterMaxStamina;

        loadMonster();
        startMonsterThread();
        startMusicThread(monsterInfo[3]);
    }

    private void loadMonster() {
        Path path = Path.of(huntLocation + monsterToHunt + ".txt");
        try (Stream<String> textStream = Files.lines(path)) {
            for (String s : textStream.toList()) {
                List<String> l = new ArrayList<>(Arrays.stream(s.split(";")).toList());
                switch (l.get(0)){
                    case "ma": monsterAttacks.add(l.subList(1,l.size()));
                        break;
                    case "mc": monsterCharges.add(l.subList(1,l.size()));
                        break;
                    case "ms": monsterStatusEffects.add(l.subList(1,l.size()));
                        break;
                    case "ha": hunterAttacks.add(l.subList(1,l.size()));
                        break;
                    case "hc": hunterCarts.add(l.subList(1, l.size()));
                        break;
                    case "hr": hunterRest.add(l.subList(1, l.size()));
                        break;
                    case "mr": monsterRest.add(l.subList(1, l.size()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMusicThread(String musicName) {
        String[] musicInfo = {};
        try(Stream<String> musicInfoStream = Files.lines(Path.of(musicLocation + "musicInfo.txt"))){
            for(String s : musicInfoStream.toList()){
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
            List<String> command = getRandomCommand();
            newMonsterImage = new Image(command.get(0));
            while(monsterhp > 0){
                Platform.runLater(() -> hunterHealthProgressBar.setProgress(hunterhp/hunterMaxHp));
                Platform.runLater(() -> monsterHealthProgressBar.setProgress(monsterhp/monsterMaxHp));
                Platform.runLater(() -> hunterStaminaProgressBar.setProgress(hunterStamina/hunterMaxStamina));
                Platform.runLater(() -> monsterStaminaProgressBar.setProgress(monsterStamina/monsterMaxStamina));
                monsterView.setImage(newMonsterImage);
                int taskTime = 10;
                huntingText.clear();
                for(String s : command.subList(1,command.size())){
                    try{
                        taskTime = Integer.parseInt(s);
                        break;
                    }catch(NumberFormatException nfe){
                        huntingText.appendText(s + "\n");
                    }
                }
                try {
                    startTimer(taskTime);
                    command = getRandomCommand();
                    getNextImage(command.get(0));
                    Thread.sleep(taskTime* 1000L);
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

    private List<String> getRandomCommand() {
        if(hunterhp <= 0){
            hunterhp = hunterMaxHp;
            return hunterCarts.get(random.nextInt(hunterCarts.size()));
        }
        if(monsterCharged && monsterChargeDurationLeft <= 0){
            return monsterCharges.get(monsterCharges.size()-1);
        }
        if(statusEffectActive && statusDurationLeft <= 0){
            return monsterStatusEffects.get(monsterStatusEffects.size()-1);
        }

        List<List<String>> availableAttacks;
        List<String> chosenAttack;

        if(monsterTurn){
            if(random.nextInt(3) == 1){
                if(random.nextInt(2) == 1){
                    chosenAttack = monsterCharges.get(random.nextInt(monsterCharges.size()-1));
                    monsterStamina -= Integer.parseInt(chosenAttack.get(chosenAttack.size()-1));
                }else{
                    chosenAttack = monsterStatusEffects.get(random.nextInt(monsterStatusEffects.size()-1));
                    monsterStamina -= Integer.parseInt(chosenAttack.get(chosenAttack.size()-1));
                }
            }else{
                availableAttacks = monsterAttacks.stream().filter(n -> Integer.parseInt(n.get(n.size()-1)) <= monsterStamina)
                        .collect(Collectors.toList());
                if(monsterStamina <= 5){
                    availableAttacks.addAll(monsterRest);
                }
                chosenAttack = availableAttacks.get(random.nextInt(availableAttacks.size()));
                int staminaChange = Integer.parseInt(chosenAttack.get(chosenAttack.size()-1));
                if(staminaChange>=0){
                    monsterStamina -= Integer.parseInt(chosenAttack.get(chosenAttack.size()-1));
                }else{
                    if(monsterStamina - staminaChange > monsterMaxStamina){
                        monsterStamina = monsterMaxStamina;
                    }else{
                        monsterStamina -= staminaChange;
                    }
                }

                hunterhp -= 1;
            }
        }else{
            availableAttacks = hunterAttacks.stream().filter(n -> Integer.parseInt(n.get(n.size()-1)) <= hunterStamina)
                    .collect(Collectors.toList());
            if(hunterStamina <= 5){
                availableAttacks.addAll(hunterRest);
            }
            chosenAttack = availableAttacks.get(random.nextInt(availableAttacks.size()));
            int staminaChange = Integer.parseInt(chosenAttack.get(chosenAttack.size()-1));
            if(staminaChange>=0){
                hunterStamina -= Integer.parseInt(chosenAttack.get(chosenAttack.size()-1));
            }else{
                if(hunterStamina - staminaChange > hunterMaxStamina){
                    hunterStamina = hunterMaxStamina;
                }else{
                    hunterStamina -= staminaChange;
                }
            }

            monsterhp -= 1;
        }
        if(monsterCharged) monsterChargeDurationLeft-=1;
        if(statusEffectActive) statusDurationLeft-=1;
        monsterTurn = !monsterTurn;
        return chosenAttack.subList(0, chosenAttack.size()-1);
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
