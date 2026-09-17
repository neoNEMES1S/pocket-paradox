package com.pocketparadox.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import java.util.HashSet;
import java.util.Set;

/** Short original chimes; no loops, delayed playback, or background audio. */
final class GameAudio {
    static final int STEP = 0, PUSH = 1, BLOCKED = 2, ENTER = 3, EXIT = 4, GOAL = 5, COMPLETE = 6, UNDO = 7;
    private final AudioManager manager;
    private final SoundPool pool;
    private final AudioFocusRequest focus;
    private final Set<Integer> loaded = new HashSet<>();
    private final int[] sounds = new int[8];
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable endFocus = this::pause;
    private boolean focused;

    GameAudio(Context context) {
        manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        pool = new SoundPool.Builder().setMaxStreams(3).setAudioAttributes(attributes).build();
        focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes).setOnAudioFocusChangeListener(change -> {
                    focused = change == AudioManager.AUDIOFOCUS_GAIN;
                    if (!focused) pool.autoPause();
                }).build();
        pool.setOnLoadCompleteListener((p, id, status) -> { if (status == 0) loaded.add(id); });
        int[] resources = {R.raw.sfx_step, R.raw.sfx_push, R.raw.sfx_blocked, R.raw.sfx_enter,
                R.raw.sfx_exit, R.raw.sfx_goal, R.raw.sfx_complete, R.raw.sfx_undo};
        for (int i = 0; i < resources.length; i++) sounds[i] = pool.load(context, resources[i], 1);
    }

    void play(int effect) {
        if (!loaded.contains(sounds[effect])) return;
        if (!focused) focused = manager.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        if (focused) {
            pool.play(sounds[effect], .45f, .45f, 1, 0, 1);
            handler.removeCallbacks(endFocus); handler.postDelayed(endFocus, 550);
        }
    }

    void pause() { handler.removeCallbacks(endFocus); pool.autoPause(); manager.abandonAudioFocusRequest(focus); focused = false; }
    void release() { pause(); pool.setOnLoadCompleteListener(null); pool.release(); }
}
