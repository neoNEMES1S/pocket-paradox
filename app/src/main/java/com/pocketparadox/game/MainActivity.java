package com.pocketparadox.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

/** Native UI: the puzzle rules live in the independently runnable Engine. */
public final class MainActivity extends Activity {
    static final int BG = Color.rgb(16, 24, 32), SURFACE = Color.rgb(26, 39, 51);
    static final int FLOOR = Color.rgb(34, 49, 63), WALL = Color.rgb(56, 76, 94);
    static final int INK = Color.rgb(244, 246, 249), MUTED = Color.rgb(178, 191, 203);
    static final int MINT = Color.rgb(159, 227, 194), CORAL = Color.rgb(255, 148, 127);
    static final int VIOLET = Color.rgb(175, 167, 237), GOLD = Color.rgb(255, 213, 143);
    private SharedPreferences prefs;
    private Engine game;
    private int levelIndex;
    private String screen = "home";
    private LinearLayout shell, directionPad;
    private TextView movesLabel, roomLabel, hintLabel;
    private Button undoButton, nextButton;
    private Board board;
    private boolean haptics;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        prefs = getSharedPreferences("pocket-paradox-v1", MODE_PRIVATE);
        haptics = prefs.getBoolean("haptics", true);
        levelIndex = Math.max(0, Math.min(prefs.getInt("sessionLevel", 0), unlocked()));
        Object retained = getLastNonConfigurationInstance();
        if (retained instanceof Engine) {
            game = (Engine) retained;
            for (int i = 0; i < Levels.ALL.length; i++) if (Levels.ALL[i] == game.level) levelIndex = i;
        } else {
            game = new Engine(Levels.ALL[levelIndex]);
            game.restore(prefs.getString("session", ""));
        }
        if (android.os.Build.VERSION.SDK_INT >= 33)
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::navigateBack);
        String page = saved == null ? "home" : saved.getString("screen", "home");
        if (page.equals("play")) showPlay();
        else if (page.equals("levels")) showLevels();
        else if (page.equals("settings")) showSettings();
        else showHome();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putString("screen", screen);
        persist();
        super.onSaveInstanceState(out);
    }

    @Override protected void onPause() { persist(); super.onPause(); }
    @Override public Object onRetainNonConfigurationInstance() { return game; }

    private void persist() {
        if (game != null) prefs.edit().putInt("sessionLevel", levelIndex)
                .putString("session", game.save()).apply();
    }

    private int dp(float n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private int unlocked() { return Math.min(Levels.ALL.length - 1, Math.max(0, prefs.getInt("unlocked", 0))); }
    private boolean complete(int i) { return prefs.getBoolean("complete." + i, false); }
    private int completed() { int count = 0; for (int i = 0; i < Levels.ALL.length; i++) if (complete(i)) count++; return count; }
    private String number(int n) { return String.format(java.util.Locale.ROOT, "%02d", n); }

    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private LinearLayout row() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.HORIZONTAL); v.setGravity(Gravity.CENTER_VERTICAL); return v; }
    private void gap(LinearLayout v, int size) { View spacer = new View(this); v.addView(spacer, new LinearLayout.LayoutParams(1, dp(size))); }

    private TextView text(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setFontFeatureSettings("kern"); t.setLineSpacing(dp(2), 1); return t;
    }

    private TextView eyebrow(String s) {
        TextView t = text(s, 11, MINT); t.setLetterSpacing(.17f);
        t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); return t;
    }

    private TextView heading(String s, int size) {
        TextView t = text(s, size, INK); t.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        if (android.os.Build.VERSION.SDK_INT >= 28) t.setAccessibilityHeading(true); return t;
    }

    private Button button(String s, boolean primary, Runnable action) {
        Button b = new Button(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false);
        b.setTextColor(primary ? BG : INK); b.setBackgroundTintList(ColorStateList.valueOf(primary ? MINT : SURFACE));
        b.setMinHeight(dp(52)); b.setMinimumHeight(dp(52)); b.setPadding(dp(16), dp(8), dp(16), dp(8));
        b.setStateListAnimator(null); b.setOnClickListener(v -> action.run()); return b;
    }

    private void fullButton(LinearLayout into, Button b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.topMargin = dp(6); into.addView(b, p);
    }

    private LinearLayout page(String name, boolean scroll) {
        screen = name;
        shell = column(); shell.setBackgroundColor(BG);
        shell.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });
        setContentView(shell);
        LinearLayout content = column(); content.setPadding(dp(24), dp(16), dp(24), dp(20));
        if (scroll) {
            ScrollView scroller = new ScrollView(this); scroller.setFillViewport(true);
            scroller.addView(content); shell.addView(scroller, new LinearLayout.LayoutParams(-1, -1));
        } else shell.addView(content, new LinearLayout.LayoutParams(-1, -1));
        shell.requestApplyInsets(); return content;
    }

    private void navigation(LinearLayout content, String label, Runnable back) {
        LinearLayout line = row();
        Button b = button("‹", false, back); b.setTextSize(28); b.setContentDescription("Back");
        line.addView(b, new LinearLayout.LayoutParams(dp(52), dp(52)));
        TextView title = eyebrow(label); title.setPadding(dp(16), 0, 0, 0);
        line.addView(title, new LinearLayout.LayoutParams(0, -2, 1)); content.addView(line); gap(content, 14);
    }

    private void showHome() {
        LinearLayout content = page("home", true);
        LinearLayout top = row(); top.addView(eyebrow("A SMALL WORLD OF POSSIBILITIES"), new LinearLayout.LayoutParams(0, -2, 1));
        content.addView(top); gap(content, 20);
        View emblem = new View(this) {
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(Canvas c) {
                float size = Math.min(getWidth(), getHeight()) * .84f;
                c.save(); c.translate(getWidth() / 2f, getHeight() / 2f); c.rotate(-8);
                paint.setColor(SURFACE); c.drawRoundRect(-size/2-10, -size/2-10, size/2+10, size/2+10, dp(24), dp(24), paint);
                int[] colors = {MINT, BG, VIOLET, BG, CORAL};
                for (int i = 0; i < colors.length; i++) {
                    float s = size * (1 - i * .18f); paint.setColor(colors[i]);
                    c.drawRoundRect(-s/2, -s/2, s/2, s/2, dp(i == 4 ? 7 : 12), dp(i == 4 ? 7 : 12), paint);
                }
                paint.setColor(BG); c.drawCircle(-size*.04f, -size*.02f, size*.012f, paint);
                c.drawCircle(size*.04f, -size*.02f, size*.012f, paint); c.restore();
            }
        };
        emblem.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        content.addView(emblem, new LinearLayout.LayoutParams(-1, dp(190))); gap(content, 18);
        content.addView(heading("Pocket\nParadox", 48)); gap(content, 10);
        content.addView(text("Think outside the box.\nThen step inside it.", 17, MUTED)); gap(content, 28);
        LinearLayout progress = row(); progress.addView(text("YOUR JOURNEY", 11, MUTED), new LinearLayout.LayoutParams(0, -2, 1));
        progress.addView(text(completed() + " / " + Levels.ALL.length + " solved", 13, INK)); content.addView(progress); gap(content, 10);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(Levels.ALL.length); bar.setProgress(completed()); bar.setProgressTintList(ColorStateList.valueOf(MINT));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(SURFACE));
        bar.setContentDescription(completed() + " of " + Levels.ALL.length + " puzzles solved");
        content.addView(bar, new LinearLayout.LayoutParams(-1, dp(5))); gap(content, 20);
        fullButton(content, button(completed() == Levels.ALL.length ? "Revisit the worlds  →" : completed() == 0 && game.moves == 0 ? "Begin exploring  →" : "Continue exploring  →", true, this::continueGame));
        fullButton(content, button("Choose a puzzle", false, this::showLevels));
        LinearLayout links = row();
        links.addView(button("How to play", false, this::help), new LinearLayout.LayoutParams(0, -2, 1));
        links.addView(button("Settings", false, this::showSettings), new LinearLayout.LayoutParams(0, -2, 1));
        content.addView(links); gap(content, 20);
        TextView footer = text("12 puzzles · 3 chapters · entirely offline", 12, MUTED); footer.setGravity(Gravity.CENTER); content.addView(footer);
    }

    private void continueGame() {
        if (game.won()) {
            int next = 0;
            while (next < Levels.ALL.length - 1 && complete(next)) next++;
            startLevel(next);
        } else showPlay();
    }

    private void showLevels() {
        LinearLayout content = page("levels", true); navigation(content, "YOUR JOURNEY", this::showHome);
        content.addView(heading("Little worlds.\nBig ideas.", 35)); gap(content, 10);
        content.addView(text("Solve a puzzle to open the next.", 15, MUTED)); gap(content, 24);
        for (int chapter = 0; chapter < Levels.CHAPTERS.length; chapter++) {
            content.addView(eyebrow("CHAPTER " + number(chapter + 1))); gap(content, 5);
            content.addView(heading(Levels.CHAPTERS[chapter], 25)); gap(content, 10);
            for (int offset = 0; offset < 4; offset++) {
                final int i = chapter * 4 + offset;
                if (i >= Levels.ALL.length) break;
                boolean locked = i > unlocked(), done = complete(i);
                String best = done ? "  ·  best " + prefs.getInt("best." + i, 0) + " moves" : "";
                Button b = button(number(i + 1) + "   " + Levels.ALL[i].title + "\n" + (locked ? "Locked" : done ? "✓ Solved" + best : "Ready to explore"), false, () -> {
                    if (i == levelIndex && !game.won()) showPlay(); else startLevel(i);
                });
                b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL); b.setMinHeight(dp(76)); b.setTextSize(14);
                b.setEnabled(!locked); b.setAlpha(locked ? .42f : 1);
                b.setContentDescription("Puzzle " + (i+1) + ", " + Levels.ALL[i].title + ", " + (locked ? "locked" : done ? "solved" : "available"));
                fullButton(content, b);
            }
            gap(content, 26);
        }
    }

    private void startLevel(int index) {
        if (index < 0 || index > unlocked()) return;
        levelIndex = index; game = new Engine(Levels.ALL[index]); persist(); showPlay();
    }

    private void showPlay() {
        boolean landscape = getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels;
        LinearLayout content = page("play", !landscape);
        LinearLayout top = row();
        Button back = button("‹", false, this::showLevels); back.setTextSize(28); back.setContentDescription("Puzzle selection");
        top.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView progress = eyebrow("PUZZLE " + number(levelIndex + 1) + " / " + Levels.ALL.length);
        progress.setPadding(dp(14), 0, 0, 0); top.addView(progress, new LinearLayout.LayoutParams(0, -2, 1));
        Button how = button("?", false, this::help); how.setContentDescription("How to play");
        top.addView(how, new LinearLayout.LayoutParams(dp(48), dp(48))); content.addView(top); gap(content, 10);
        content.addView(heading(game.level.title, 27)); gap(content, 4);
        roomLabel = text("", 12, MINT); content.addView(roomLabel); gap(content, 8);
        LinearLayout main = landscape ? row() : column();
        content.addView(main, new LinearLayout.LayoutParams(-1, landscape ? 0 : -2, landscape ? 1 : 0));
        board = new Board();
        if (landscape) main.addView(board, new LinearLayout.LayoutParams(0, -1, 1));
        else main.addView(board, new LinearLayout.LayoutParams(-1,
                Math.max(dp(220), Math.min(dp(440), getResources().getDisplayMetrics().heightPixels - dp(480)))));
        LinearLayout controls = column();
        if (landscape) {
            ScrollView scroller = new ScrollView(this); controls.setPadding(dp(18), 0, 0, 0);
            scroller.addView(controls); main.addView(scroller, new LinearLayout.LayoutParams(dp(280), -1));
        } else main.addView(controls, new LinearLayout.LayoutParams(-1, -2));
        movesLabel = text("", 12, MUTED); movesLabel.setGravity(Gravity.CENTER); controls.addView(movesLabel);
        hintLabel = text(game.level.hint, 13, MUTED); hintLabel.setGravity(Gravity.CENTER);
        hintLabel.setPadding(0, dp(5), 0, dp(5)); hintLabel.setMinHeight(dp(40)); controls.addView(hintLabel);
        LinearLayout actions = row();
        undoButton = button("↶  Undo", false, () -> { if (game.undo()) { persist(); updateBoard(); } });
        actions.addView(undoButton, new LinearLayout.LayoutParams(0, -2, 1));
        actions.addView(button("↻  Restart", false, this::restart), new LinearLayout.LayoutParams(0, -2, 1));
        controls.addView(actions);
        directionPad = landscape ? row() : column(); directionPad.setGravity(Gravity.CENTER);
        if (landscape) {
            directionPad.addView(direction("←", "Move left", -1, 0), new LinearLayout.LayoutParams(0, dp(54), 1));
            directionPad.addView(direction("↑", "Move up", 0, -1), new LinearLayout.LayoutParams(0, dp(54), 1));
            directionPad.addView(direction("↓", "Move down", 0, 1), new LinearLayout.LayoutParams(0, dp(54), 1));
            directionPad.addView(direction("→", "Move right", 1, 0), new LinearLayout.LayoutParams(0, dp(54), 1));
        } else {
            gap(directionPad, 3);
            directionPad.addView(direction("↑", "Move up", 0, -1), new LinearLayout.LayoutParams(dp(64), dp(54)));
            LinearLayout middle = row(); middle.setGravity(Gravity.CENTER);
            middle.addView(direction("←", "Move left", -1, 0), new LinearLayout.LayoutParams(dp(64), dp(54)));
            TextView center = text("✦", 23, CORAL); center.setGravity(Gravity.CENTER); center.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            middle.addView(center, new LinearLayout.LayoutParams(dp(64), dp(54)));
            middle.addView(direction("→", "Move right", 1, 0), new LinearLayout.LayoutParams(dp(64), dp(54)));
            directionPad.addView(middle);
            directionPad.addView(direction("↓", "Move down", 0, 1), new LinearLayout.LayoutParams(dp(64), dp(54)));
        }
        controls.addView(directionPad);
        nextButton = button(levelIndex == Levels.ALL.length - 1 ? "Journey complete  →" : "Next puzzle  →", true, () -> {
            if (levelIndex + 1 < Levels.ALL.length) startLevel(levelIndex + 1); else showHome();
        });
        fullButton(controls, nextButton);
        updateBoard();
        board.requestFocus();
    }

    private Button direction(String glyph, String description, int dx, int dy) {
        Button b = button(glyph, false, () -> move(dx, dy));
        b.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); b.setContentDescription(description); return b;
    }

    private void restart() {
        if (game.moves == 0) return;
        new AlertDialog.Builder(this).setTitle("Restart this puzzle?")
                .setMessage("Your best score and completed puzzles stay saved.")
                .setNegativeButton("Keep playing", null).setPositiveButton("Restart", (d,w) -> startLevel(levelIndex)).show();
    }

    private void move(int dx, int dy) {
        if (!screen.equals("play") || game.won()) return;
        if (!game.move(dx, dy)) {
            hintLabel.setText("That way is blocked. Try another direction, or undo.");
            hintLabel.announceForAccessibility("Blocked"); return;
        }
        if (haptics) board.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        if (game.won()) {
            int best = prefs.getInt("best." + levelIndex, Integer.MAX_VALUE);
            prefs.edit().putBoolean("complete." + levelIndex, true)
                    .putInt("best." + levelIndex, Math.min(best, game.moves))
                    .putInt("unlocked", Math.max(unlocked(), Math.min(Levels.ALL.length - 1, levelIndex + 1))).apply();
        }
        persist(); updateBoard();
        board.announceForAccessibility(game.won() ? "Puzzle solved in " + game.moves + " moves" : board.getContentDescription());
    }

    private Engine.Piece owner(int room) {
        for (Engine.Piece p : game.pieces) if (p.inside == room) return p; return null;
    }

    private String breadcrumb() {
        int id = game.player().room; String path = game.room(id).name;
        for (int i = 0; i < game.level.rooms.length; i++) {
            Engine.Piece p = owner(id); if (p == null) break;
            id = p.room; path = game.room(id).name + "  ›  " + path;
        }
        return path;
    }

    private void updateBoard() {
        roomLabel.setText(breadcrumb());
        int best = prefs.getInt("best." + levelIndex, 0);
        movesLabel.setText(game.moves + (game.moves == 1 ? " move" : " moves") + (best > 0 ? "   ·   best " + best : "   ·   take your time"));
        hintLabel.setText(game.won() ? "Beautifully done. A little world, figured out." : game.level.hint);
        hintLabel.setTextColor(game.won() ? MINT : MUTED);
        undoButton.setEnabled(game.canUndo()); undoButton.setAlpha(game.canUndo() ? 1 : .42f);
        nextButton.setVisibility(game.won() ? View.VISIBLE : View.GONE);
        directionPad.setVisibility(game.won() ? View.GONE : View.VISIBLE);
        board.setContentDescription(boardDescription()); board.invalidate();
    }

    private String boardDescription() {
        Engine.Piece player = game.player(); Engine.Room r = game.room(player.room);
        StringBuilder s = new StringBuilder(r.name + ". Player column " + (player.x+1) + ", row " + (player.y+1) + ". ");
        for (Engine.Piece p : game.pieces) if (p.room == r.id && p.id != 0)
            s.append(p.inside < 0 ? "Crate" : "Room box").append(" at column ").append(p.x+1).append(", row ").append(p.y+1).append(". ");
        for (int y = 0; y < r.height; y++) for (int x = 0; x < r.width; x++) {
            char tile = r.tiles[y].charAt(x);
            if (tile == '@' || tile == 'o') s.append(tile == '@' ? "Player goal" : "Box goal").append(" at column ").append(x+1).append(", row ").append(y+1).append(". ");
        }
        return s.toString();
    }

    private void help() {
        new AlertDialog.Builder(this).setTitle("Small boxes. Bigger possibilities.")
                .setMessage("YOU ARE THE CORAL EXPLORER\nUse the arrows or swipe anywhere on the board. A keyboard's arrows or WASD work too.\n\nFIND YOUR PLACE\nPut a crate or room box on every square-ring goal. Finish on the coral cross goal yourself.\n\nSTEP INTO A WORLD\nMint boxes contain rooms. You push a box first. If it cannot move, you can enter through the center of its facing edge, when that tile is open.\n\nSTEP BACK OUT\nWalk through an open edge to emerge beside the room's box in the outside world. The small overview shows that outside world.\n\nTAKE THE WORLD WITH YOU\nPush room boxes to move everything inside them. Crates can travel into and out of rooms too.\n\nROOM TO EXPERIMENT\nUndo as often as you need during play, or restart. Progress saves automatically; undo history starts fresh when the app reopens.\n\nThis first campaign explores finite nesting. Self-containing worlds and infinity puzzles are future mechanics.")
                .setPositiveButton("Let's explore", null).show();
    }

    private void showSettings() {
        LinearLayout content = page("settings", true); navigation(content, "MAKE YOURSELF AT HOME", this::showHome);
        content.addView(heading("Your kind\nof quiet.", 38)); gap(content, 24);
        Switch feedback = new Switch(this); feedback.setText("Touch feedback"); feedback.setTextSize(17); feedback.setTextColor(INK);
        feedback.setMinHeight(dp(56)); feedback.setChecked(haptics);
        feedback.setOnCheckedChangeListener((b, on) -> { haptics = on; prefs.edit().putBoolean("haptics", on).apply(); }); content.addView(feedback);
        content.addView(text("A gentle tap after each move. Follows your device's vibration settings.", 14, MUTED)); gap(content, 28);
        fullButton(content, button("How to play", false, this::help)); gap(content, 24);
        content.addView(eyebrow("ABOUT POCKET PARADOX")); gap(content, 10);
        content.addView(text("An original pocket-sized puzzle game about worlds inside movable boxes.\n\n12 original puzzles. No accounts, ads, tracking, or internet connection.\n\nFoundation build · 0.1.0\nInspired by the recursive puzzle idea explored in Patrick's Parabox. This is an independent project, with original code, puzzles, and artwork.", 14, MUTED));
        gap(content, 30); fullButton(content, button("Reset all progress", false, () -> new AlertDialog.Builder(this)
                .setTitle("Reset your journey?").setMessage("This erases solved puzzles, best scores, and the current game on this device.")
                .setNegativeButton("Keep progress", null).setPositiveButton("Reset everything", (d,w) -> {
                    prefs.edit().clear().putBoolean("haptics", haptics).apply(); levelIndex = 0; game = new Engine(Levels.ALL[0]); persist(); showHome();
                }).show()));
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (screen.equals("play") && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_W) { move(0,-1); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_S) { move(0,1); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_A) { move(-1,0); return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_D) { move(1,0); return true; }
            if (keyCode == KeyEvent.KEYCODE_Z && game.undo()) { persist(); updateBoard(); return true; }
        }
        return super.dispatchKeyEvent(event);
    }

    private void navigateBack() {
        if (screen.equals("play")) showLevels(); else if (!screen.equals("home")) showHome(); else finish();
    }
    // API 33+ uses the platform callback registered in onCreate; this covers older devices.
    @SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { navigateBack(); }

    private final class Board extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private float downX, downY;
        private int pointerId = -1;

        Board() {
            super(MainActivity.this); setFocusableInTouchMode(true); setClickable(true);
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        private void rounded(Canvas c, float x, float y, float w, float h, float radius, int color) {
            paint.setColor(color); paint.setStyle(Paint.Style.FILL);
            rect.set(x, y, x+w, y+h); c.drawRoundRect(rect, radius, radius, paint);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (getWidth() <= 0 || getHeight() <= 0) return;
            Engine.Room active = game.room(game.player().room);
            Engine.Piece parent = owner(active.id);
            float parentArea = parent != null && getHeight() > dp(250) ? dp(88) : 0;
            float size = Math.max(0, Math.min(getWidth() - dp(8), getHeight() - parentArea - dp(16)));
            if (parentArea > 0) {
                Engine.Room outer = game.room(parent.room);
                drawRoom(canvas, outer, (getWidth()-dp(76))/2f, 0, dp(76), 0, false);
                paint.setColor(MUTED); paint.setTextAlign(Paint.Align.LEFT); paint.setTextSize(dp(10));
                canvas.drawText("OUTSIDE", Math.max(0, (getWidth()-dp(76))/2f-dp(62)), dp(40), paint);
            }
            float left = (getWidth()-size)/2f, top = parentArea + (getHeight()-parentArea-size)/2f;
            rounded(canvas, left-dp(4), top-dp(4), size+dp(8), size+dp(8), dp(14), SURFACE);
            drawRoom(canvas, active, left, top, size, 0, true);
        }

        private void drawRoom(Canvas c, Engine.Room r, float left, float top, float size, int depth, boolean main) {
            float cell = size / Math.max(r.width, r.height);
            float x0 = left + (size-r.width*cell)/2, y0 = top + (size-r.height*cell)/2;
            rounded(c, left, top, size, size, cell*.12f, BG);
            for (int y=0; y<r.height; y++) for (int x=0; x<r.width; x++) {
                float px=x0+x*cell, py=y0+y*cell, gap=cell*.035f;
                boolean wall=r.wall(x,y);
                rounded(c, px+gap, py+gap, cell-2*gap, cell-2*gap, cell*.07f, wall ? WALL : FLOOR);
                if (wall) continue;
                char tile=r.tiles[y].charAt(x);
                if (tile=='o') {
                    paint.setColor(GOLD); paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(Math.max(1, cell*.045f));
                    rect.set(px+cell*.22f,py+cell*.22f,px+cell*.78f,py+cell*.78f);
                    c.drawRoundRect(rect,cell*.08f,cell*.08f,paint); paint.setStyle(Paint.Style.FILL);
                } else if (tile=='@') {
                    paint.setColor(CORAL); paint.setStrokeWidth(Math.max(1,cell*.05f));
                    c.drawLine(px+cell*.34f,py+cell*.5f,px+cell*.66f,py+cell*.5f,paint);
                    c.drawLine(px+cell*.5f,py+cell*.34f,px+cell*.5f,py+cell*.66f,paint);
                }
            }
            for (Engine.Piece p:game.pieces) if (p.room==r.id) {
                float px=x0+p.x*cell,py=y0+p.y*cell;
                if (p.id==0) {
                    rounded(c,px+cell*.15f,py+cell*.15f,cell*.7f,cell*.7f,cell*.2f,CORAL);
                    paint.setColor(BG);
                    c.drawCircle(px+cell*.38f,py+cell*.43f,cell*.045f,paint);
                    c.drawCircle(px+cell*.62f,py+cell*.43f,cell*.045f,paint);
                    paint.setStrokeWidth(cell*.032f); c.drawLine(px+cell*.42f,py+cell*.65f,px+cell*.58f,py+cell*.65f,paint);
                } else if (p.inside>=0) {
                    rounded(c,px+cell*.08f,py+cell*.08f,cell*.84f,cell*.84f,cell*.1f,MINT);
                    // ponytail: draw three preview depths; a zoom camera can replace this if deeper levels need it.
                    if (depth<3 && cell>10) drawRoom(c,game.room(p.inside),px+cell*.18f,py+cell*.18f,cell*.64f,depth+1,false);
                } else {
                    rounded(c,px+cell*.13f,py+cell*.13f,cell*.74f,cell*.74f,cell*.1f,VIOLET);
                    paint.setColor(BG); paint.setStrokeWidth(cell*.035f);
                    c.drawLine(px+cell*.32f,py+cell*.32f,px+cell*.68f,py+cell*.68f,paint);
                    c.drawLine(px+cell*.68f,py+cell*.32f,px+cell*.32f,py+cell*.68f,paint);
                }
                if (r.tiles[p.y].charAt(p.x)=='o' && p.id!=0) {
                    paint.setColor(GOLD); c.drawCircle(px+cell*.81f,py+cell*.19f,cell*.07f,paint);
                }
            }
            if (main) {
                paint.setColor(MINT); paint.setStrokeWidth(Math.max(2,cell*.045f));
                if (!r.wall(0,r.height/2)) c.drawLine(x0,y0+(r.height/2+.32f)*cell,x0,y0+(r.height/2+.68f)*cell,paint);
                if (!r.wall(r.width-1,r.height/2)) c.drawLine(x0+r.width*cell,y0+(r.height/2+.32f)*cell,x0+r.width*cell,y0+(r.height/2+.68f)*cell,paint);
                if (!r.wall(r.width/2,0)) c.drawLine(x0+(r.width/2+.32f)*cell,y0,x0+(r.width/2+.68f)*cell,y0,paint);
                if (!r.wall(r.width/2,r.height-1)) c.drawLine(x0+(r.width/2+.32f)*cell,y0+r.height*cell,x0+(r.width/2+.68f)*cell,y0+r.height*cell,paint);
            }
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getActionMasked()==MotionEvent.ACTION_DOWN) {
                getParent().requestDisallowInterceptTouchEvent(true);
                pointerId=e.getPointerId(0); downX=e.getX(); downY=e.getY(); return true;
            }
            if (e.getActionMasked()==MotionEvent.ACTION_CANCEL || e.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN) {
                getParent().requestDisallowInterceptTouchEvent(false);
                pointerId=-1; return true;
            }
            if (e.getActionMasked()==MotionEvent.ACTION_UP && pointerId==e.getPointerId(0)) {
                getParent().requestDisallowInterceptTouchEvent(false);
                float dx=e.getX()-downX,dy=e.getY()-downY; pointerId=-1;
                if (Math.max(Math.abs(dx),Math.abs(dy))>Math.max(dp(24),ViewConfiguration.get(getContext()).getScaledTouchSlop()*2)) {
                    if (Math.abs(dx)>Math.abs(dy)) move(dx>0?1:-1,0); else move(0,dy>0?1:-1);
                } else performClick();
                return true;
            }
            return true;
        }

        @Override public boolean performClick() { super.performClick(); return true; }
    }
}
