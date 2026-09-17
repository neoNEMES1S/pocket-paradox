package com.pocketparadox.game;

/** Regression checks for rapid input, exact undo, room crossings, and static rendering. */
public final class MotionCheck {
    public static void main(String[] args) {
        Engine game = new Engine(Levels.ALL[0]);
        BoardMotion motion = new BoardMotion(game);
        Engine before = new Engine(game);
        assert game.move(1, 0);
        motion.begin(before, game, true);
        assert motion.x(0) == 1 && motion.x(1) == 2;
        assert motion.pushed && motion.goalActivated;
        motion.elapsed = 50;
        float displayed = motion.x(0);
        assert displayed > 1 && displayed < 2;
        before = new Engine(game);
        assert game.move(0, 1);
        motion.begin(before, game, true);
        assert Math.abs(motion.x(0) - displayed) < .0001 : "Rapid input snapped to target";
        assert !motion.goalActivated : "Stationary goal replayed its effect";
        motion.finish();
        assert motion.x(0) == game.player().x && motion.y(0) == game.player().y;
        before = new Engine(game);
        assert game.undo();
        motion.begin(before, game, false);
        assert motion.y(0) == game.player().y;
        String state = game.save();
        motion.elapsed = 20;
        motion.x(0);
        assert state.equals(game.save()) : "Rendering mutated rules";

        game = new Engine(Levels.ALL[4]);
        motion = new BoardMotion(game);
        for (int i = 0; i < 3; i++) {
            before = new Engine(game);
            assert game.move(1, 0);
            motion.begin(before, game, true);
            motion.finish();
        }
        assert motion.roomChanged && game.player().room == 1;
        motion.elapsed = 30;
        assert motion.x(0) == game.player().x : "Cross-room coordinates were interpolated";
        before = new Engine(game);
        assert game.undo();
        motion.begin(before, game, true);
        assert motion.roomChanged && motion.after.player().room == 0;
        motion.finish();
        assert motion.x(0) == game.player().x;
        assert BoardMotion.ease(-1) == 0 && BoardMotion.ease(2) == 1;
        assert BoardMotion.portalStrength(0) == 0 && BoardMotion.portalStrength(1) == 0;
        assert BoardMotion.portalStrength(.2f) == 1 : "Portal should visibly peak during entry";
        assert BoardMotion.portalStrength(.7f) > 0 && BoardMotion.portalStrength(.7f) < 1;
        assert BoardMotion.portalStrength(2) == 0 : "Settled portal must stop drawing";
        System.out.println("Motion checks passed: rapid input, goal events, undo, room crossings, reduced motion.");
    }
}
