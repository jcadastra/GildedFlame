package edu.cornell.cis3152.physics;

import com.badlogic.gdx.audio.Music;
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
    private int activeMusicIndex;

    private Random random;

    public SoundEngine() {
        registeredSoundEffects = new HashMap<>();
        registeredMusic = new HashMap<>();
        activeLoopingSounds = new HashMap<>();
        activeMusic = new PooledList<>();
        random = new Random();
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

    public void startMusicLoop(ArrayList<String> musicSet) {
        activeMusic.clear();
        for (String musicName : musicSet) {
            Music music = registeredMusic.get(musicName);
            if (music != null) {
                activeMusic.add(music);
            }
        }
        if (activeMusic.size() > 0) {
            activeMusic.get(0).play();
            activeMusicIndex = 0;
        }
    }

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
            footStep.setPitch(id, random.nextFloat(0.7f, 1.3f));
        }
    }

    public void jump() {
        SoundEffect jumpSound = registeredSoundEffects.get("jump");
        if (jumpSound != null) {
            jumpSound.play();
        }
    }

    public void throwTorch() {
        SoundEffect torchThrow = registeredSoundEffects.get("torchThrow");
        System.out.println("throwing");
        if (torchThrow != null) {
            torchThrow.play();
        }
    }

    public void dispose() {
        for (SoundEffect soundEffect : registeredSoundEffects.values()) {
            soundEffect.dispose();
        }
        for (Music music : registeredMusic.values()) {
            music.dispose();
        }
    }
}
