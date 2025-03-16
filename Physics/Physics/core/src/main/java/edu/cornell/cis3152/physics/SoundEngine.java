package edu.cornell.cis3152.physics;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Sort;
import edu.cornell.gdiac.assets.MusicParser;
import edu.cornell.gdiac.audio.MusicQueue;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.audio.SoundEffectManager;
import edu.cornell.gdiac.util.PooledList;
import java.util.ArrayList;
import java.util.HashMap;

public class SoundEngine {

    private HashMap<String, SoundEffect> registeredSoundEffect;
    private HashMap<String, Music> registeredMusic;
    private PooledList<SoundEffect> activeSoundEffects;
    private PooledList<Music> activeMusic;
    private int activeMusicIndex;

    public SoundEngine () {
        registeredSoundEffect = new HashMap<>();
        registeredMusic = new HashMap<>();
        activeSoundEffects = new PooledList<>();
        activeMusic = new PooledList<>();
    }

    public void registerSoundEffect(String name, SoundEffect soundEffect) {
        registeredSoundEffect.put(name, soundEffect);
    }

    public void registerMusic (String name, Music music) {
        registeredMusic.put(name, music);
    }

    public void playSoundEffects(String name) {
        for (SoundEffect se : activeSoundEffects) {
            if (!se.isPlaying(0)) {
                activeSoundEffects.remove(se);
            }
        }

        SoundEffect sound = registeredSoundEffect.get(name);
        activeSoundEffects.add(sound);
        sound.play();
    }

    public void playMusic(String name) {
        for (Music music : activeMusic) {
            if (!music.isPlaying()) {
                activeMusic.remove(music);
            }
        }

        Music sound = registeredMusic.get(name);
        activeMusic.add(sound);
        sound.play();
    }

    public void tendToMusicLoop() {
        if (!activeMusic.get(activeMusicIndex).isPlaying()) {
            activeMusicIndex = (activeMusicIndex + 1) % activeMusic.size();
            activeMusic.get(activeMusicIndex).play();
        } else {
            System.out.println(activeMusic.get(activeMusicIndex).getPosition());
        }
    }
    public void startMusicLoop(ArrayList<String> musicSet) {
        for (String musicName : musicSet) {
            activeMusic.add(registeredMusic.get(musicName));
        }
        activeMusic.get(0).play();
        activeMusicIndex = 0;
    }

    public void pauseSoundEffects() {
        for (SoundEffect soundEffect : activeSoundEffects) {
            soundEffect.pause();
        }
    }

    public void resumeSoundEffects() {
        for (SoundEffect soundEffect : activeSoundEffects) {
            soundEffect.play();
        }
    }

    public void dispose() {
        for (SoundEffect soundEffect : activeSoundEffects) {
            soundEffect.dispose();
        }
        for (Music music : activeMusic) {
            music.dispose();
        }
    }
}
