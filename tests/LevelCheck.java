package com.pocketparadox.game;

/** Run with assertions enabled after compiling alongside Engine.java and Levels.java. */
public final class LevelCheck {
    public static void main(String[] args) {
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (!assertionsEnabled) throw new IllegalStateException("Run this check with java -ea");
        assert Levels.ALL.length == 12 : "Expected twelve teaching puzzles";
        assert Levels.SOLUTIONS.length == Levels.ALL.length;
        assert Levels.CHAPTERS.length == 3;
        assert Levels.CHAPTER_STARTS.length == Levels.CHAPTERS.length;
        assert Levels.CHAPTER_STARTS[0] == 0;
        for (int i = 1; i < Levels.CHAPTER_STARTS.length; i++)
            assert Levels.CHAPTER_STARTS[i] > Levels.CHAPTER_STARTS[i-1]
                    && Levels.CHAPTER_STARTS[i] < Levels.ALL.length;
        assert Levels.TEACHING.length == Levels.ALL.length;
        for (int i = 0; i < Levels.ALL.length; i++) {
            Engine game = new Engine(Levels.ALL[i]);
            for (int step = 1; step <= 3; step++) assert !Levels.hint(i, step).isBlank();
            assert !game.won() : "Puzzle starts solved: " + i;
            String moves = Levels.SOLUTIONS[i];
            assert !moves.isEmpty() && moves.length() < 60 : "Missing or overly long solution: " + i;
            for (char move : moves.toCharArray()) {
                int direction = "UDLR".indexOf(move);
                assert direction >= 0 : "Unknown move: " + move;
                int dx = new int[] {0, 0, -1, 1}[direction];
                int dy = new int[] {-1, 1, 0, 0}[direction];
                boolean moved = game.move(dx, dy);
                assert moved : "Blocked move in puzzle " + (i + 1) + ": " + moves;
            }
            assert game.won() : "Unsolved puzzle " + (i + 1) + ": " + Levels.ALL[i].title;
            assert !new Engine(Levels.ALL[i]).won() : "Replay mutated the level template";
            System.out.println((i + 1) + ". " + Levels.ALL[i].title + " — " + moves.length() + " moves");
        }
        System.out.println("All twelve original puzzles have verified winning solutions.");
    }
}
