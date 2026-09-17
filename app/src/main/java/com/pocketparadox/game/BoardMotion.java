package com.pocketparadox.game;

/** Presentation snapshots only: animation never delays or mutates the rules engine. */
final class BoardMotion {
    Engine before, after;
    float elapsed = 500;
    float[] fromX, fromY;
    boolean[] moved, goal;
    boolean roomChanged, pushed, goalActivated;

    BoardMotion(Engine game) { begin(game, game, false); }

    void begin(Engine previous, Engine current, boolean animate) {
        int count = current.pieces.size();
        float[] x = new float[count], y = new float[count];
        for (int i = 0; i < count; i++) {
            Engine.Piece p = previous.pieces.get(i);
            boolean continuous = after != null && after.level == previous.level
                    && after.pieces.get(i).room == p.room;
            x[i] = continuous ? x(i) : p.x;
            y[i] = continuous ? y(i) : p.y;
        }
        before = new Engine(previous); after = new Engine(current);
        fromX = x; fromY = y;
        moved = new boolean[count]; goal = new boolean[count];
        roomChanged = before.player().room != after.player().room;
        pushed = false; goalActivated = false;
        for (int i = 0; i < count; i++) {
            Engine.Piece a = before.pieces.get(i), b = after.pieces.get(i);
            moved[i] = a.room != b.room || a.x != b.x || a.y != b.y;
            pushed |= b.id != 0 && moved[i];
            Engine.Piece occupant = before.at(b.room, b.x, b.y);
            goal[i] = b.id != 0 && after.room(b.room).tiles[b.y].charAt(b.x) == 'o'
                    && (occupant == null || occupant.id == 0);
            goalActivated |= goal[i];
        }
        elapsed = animate ? 0 : 500;
    }

    float progress() { return ease(elapsed / (roomChanged ? 210f : 130f)); }
    static float ease(float t) { t = Math.max(0, Math.min(1, t)); return 1 - (1-t)*(1-t)*(1-t); }
    static float portalStrength(float t) {
        if (t <= 0 || t >= 1) return 0;
        return Math.min(1, t / .16f) * (1 - ease(Math.max(0, (t - .35f) / .65f)));
    }
    float x(int i) { return before.pieces.get(i).room != after.pieces.get(i).room ? after.pieces.get(i).x
            : fromX[i] + (after.pieces.get(i).x - fromX[i]) * progress(); }
    float y(int i) { return before.pieces.get(i).room != after.pieces.get(i).room ? after.pieces.get(i).y
            : fromY[i] + (after.pieces.get(i).y - fromY[i]) * progress(); }
    void finish() { elapsed = 500; }
}
