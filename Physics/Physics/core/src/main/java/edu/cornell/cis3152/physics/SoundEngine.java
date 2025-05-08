package edu.cornell.cis3152.physics;

import com.badlogic.gdx.audio.Music;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.util.PooledList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SoundEngine {

    private HashMap<String, SoundEffect> registeredSoundEffects;
    private HashMap<String, Music> registeredMusic;

    private HashMap<String, Long> activeLoopingSounds;

    private PooledList<Music> activeMusic;
    private ArrayList<String> trackList = new ArrayList<>();
    private int activeMusicIndex;
    private AssetDirectory directory;
    private Random random;

    public SoundEngine() {
        registeredSoundEffects = new HashMap<>();
        registeredMusic = new HashMap<>();
        activeLoopingSounds = new HashMap<>();
        activeMusic = new PooledList<>();
        random = new Random();

    }

    public boolean needsPopulating() {return directory == null;}

    public void populateSoundEngine(AssetDirectory directory) {
        if (this.directory == null && directory != null) {
            this.directory = directory;
            System.out.println("Populating sound engine");

            registerSoundEffect("clickSound", directory.getEntry("clickSound", SoundEffect.class));
            registerSoundEffect("doorOpening", directory.getEntry("door_opening", SoundEffect.class));
            registerSoundEffect("jump", directory.getEntry("platform-jump", SoundEffect.class));
            registerSoundEffect("pew", directory.getEntry("platform-pew", SoundEffect.class));
            registerSoundEffect("plop", directory.getEntry("platform-plop", SoundEffect.class));
            registerSoundEffect("dirtFootStep",
                directory.getEntry("dirtFootStep", SoundEffect.class));
            registerSoundEffect("torchThrow", directory.getEntry("torchThrow", SoundEffect.class));
            registerSoundEffect("torchLanding",
                directory.getEntry("torch_landing", SoundEffect.class));
            registerSoundEffect("landing", directory.getEntry("jumper_landing", SoundEffect.class));
            registerSoundEffect("totemTurn", directory.getEntry("turn_around", SoundEffect.class));
            registerSoundEffect("platformMoving",
                directory.getEntry("moving_platform", SoundEffect.class));
            registerSoundEffect("mothAttack", directory.getEntry("moth_attack", SoundEffect.class));
            registerSoundEffect("mothCharging", directory.getEntry("moth_charging", SoundEffect.class));
            registerSoundEffect("mothSmother", directory.getEntry("moth_fluttering", SoundEffect.class));
            registerSoundEffect("successSound", directory.getEntry("success_sound", SoundEffect.class));
            registerSoundEffect("failureSound", directory.getEntry("fire_extinguished", SoundEffect.class));
            registerMusic("menu_music", directory.getEntry("menu_music", Music.class));
            registerMusic("in_game", directory.getEntry("in_game_music", Music.class));
        }
    }

    public void registerSoundEffect(String name, SoundEffect soundEffect) {
        registeredSoundEffects.put(name, soundEffect);
    }

    public void registerMusic(String name, Music music) {
        registeredMusic.put(name, music);
    }

    public void playMusic(String name) {
        activeMusic.removeIf(music -> !music.isPlaying());
        Music music = registeredMusic.get(name);
        if (music != null) {
            activeMusic.add(music);
            music.play();
        }
    }

    public void tendToMusicLoop() {
        if (activeMusic.size() == 0) return;
        if (!activeMusic.get(activeMusicIndex).isPlaying()) {
            activeMusicIndex = (activeMusicIndex + 1) % activeMusic.size();
            activeMusic.get(activeMusicIndex).play();
        }
    }

    public void startMusicLoop(ArrayList<String> trackList) {
        if (!this.trackList.toString().equals(trackList.toString())) {
            stopMusicLoop();
            this.trackList = trackList;
            for (String musicName : trackList) {
                Music music = registeredMusic.get(musicName);
                if (music != null) {
                    activeMusic.add(music);
                }
            }
        }
        if (!activeMusic.isEmpty()) {
            activeMusic.get(0).play();
            activeMusicIndex = 0;
        }
    }

    private void stopMusicLoop() {
        for (Music music : activeMusic) {
            music.stop();
        }
        activeMusic.clear();
        trackList.clear();
        activeMusicIndex = 0;
    }

    public ArrayList<String> getMusicLoop() {return trackList;}

    public void pauseLoopingSoundEffects() {
        for (String name : activeLoopingSounds.keySet()) {
            SoundEffect sound = registeredSoundEffects.get(name);
            long soundId = activeLoopingSounds.get(name);
            if (sound != null) {
                sound.pause(soundId);
            }
        }
    }

    public void resumeLoopingSoundEffects() {
        for (String name : activeLoopingSounds.keySet()) {
            SoundEffect sound = registeredSoundEffects.get(name);
            long oldId = activeLoopingSounds.get(name);
            sound.stop(oldId);
            long newId = sound.loop();
            activeLoopingSounds.put(name, newId);
        }
    }

    public void avatarWalking(float avatarHorizontal, boolean grounded) {
        String soundName = "dirtFootStep";
        SoundEffect footStep = registeredSoundEffects.get(soundName);
        if (footStep == null) return;

        if (avatarHorizontal == 0 || !grounded) {
            if (activeLoopingSounds.containsKey(soundName)) {
                long id = activeLoopingSounds.get(soundName);
                footStep.stop(id);
                activeLoopingSounds.remove(soundName);
            }
        } else {
            if (!activeLoopingSounds.containsKey(soundName)) {
                long id = footStep.loop();
                activeLoopingSounds.put(soundName, id);
            }
            long id = activeLoopingSounds.get(soundName);
            footStep.setPitch(id, 0.7f + random.nextFloat() * (1.3f - 0.7f));
        }
    }

    public void jump() {
        SoundEffect jumpSound = registeredSoundEffects.get("jump");
        if (jumpSound != null) {
            jumpSound.play();
        }
    }

    public void playSoundEffect(String key) {
        SoundEffect sound = registeredSoundEffects.get(key);
        if (sound != null) {
            System.out.println("Playing sound: " + key);
            sound.play();
        } else {
            System.out.println("Sound not found: " + key);
        }
    }

    public void stopSoundEffect(String key) {
        SoundEffect sound = registeredSoundEffects.get(key);
        if (sound != null) {
            sound.stop();
        }
    }

    public void failureSound() {
        SoundEffect failureSound = registeredSoundEffects.get("failureSound");
        if (failureSound != null) {
            failureSound.play();
        }
    }

    public void successSound() {
        SoundEffect successSound = registeredSoundEffects.get("successSound");
        if (successSound != null) {
            successSound.play();
        }
    }

    public void stopAllSoundEffects() {
        for (SoundEffect sound : registeredSoundEffects.values()) {
            if (sound != null) {
                sound.stop();
            }
        }
    }

    public void platformMoving(boolean moving) {
        SoundEffect platformMoving = registeredSoundEffects.get("platformMoving");
        if (platformMoving == null) return;
        if (!moving) {
            if (activeLoopingSounds.containsKey("platformMoving")) {
                long id = activeLoopingSounds.get("platformMoving");
                platformMoving.stop(id);
                activeLoopingSounds.remove("platformMoving");
            }
        } else {
            if (!activeLoopingSounds.containsKey("platformMoving")) {
                long id = platformMoving.loop();
                activeLoopingSounds.put("platformMoving", id);
            }
        }
    }

    public void dispose() {
        for (SoundEffect soundEffect : registeredSoundEffects.values()) {
            if (soundEffect != null) {
                soundEffect.dispose();
            }
        }
        for (Music music : registeredMusic.values()) {
            if (music != null) {
                music.dispose();
            }
        }
    }
}
