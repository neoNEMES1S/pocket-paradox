package com.pocketparadox.game;

/** An original twelve-puzzle introduction to finite nested worlds. */
public final class Levels {
    private Levels() {}

    public static final String[] CHAPTERS = {"First moves", "Worlds within", "Moving worlds"};

    private static Engine.Room r(int id, String name, String... tiles) {
        return new Engine.Room(id, name, tiles);
    }

    private static Engine.Piece p(int id, int room, int x, int y, int inside) {
        return new Engine.Piece(id, room, x, y, inside);
    }

    public static final Engine.Level[] ALL = {
        new Engine.Level("A little nudge", "Push the crate onto the ring, then stand on the coral cross.",
            new Engine.Room[] {r(0, "Courtyard", "#####", "#...#", "#..o#", "#@..#", "#####")},
            new Engine.Piece[] {p(0, 0, 1, 2, -1), p(1, 0, 2, 2, -1)}),

        new Engine.Level("Around the corner", "You can push, but never pull. Leave room to change direction.",
            new Engine.Room[] {r(0, "Courtyard", "#######", "#..o..#", "#.#...#", "#.....#", "#..#..#", "#@....#", "#######")},
            new Engine.Piece[] {p(0, 0, 1, 3, -1), p(1, 0, 3, 3, -1)}),

        new Engine.Level("Together", "A line of crates moves together when there is space beyond it.",
            new Engine.Room[] {r(0, "Courtyard", "#######", "#.....#", "#...oo#", "#..@..#", "#######")},
            new Engine.Piece[] {p(0, 0, 1, 2, -1), p(1, 0, 2, 2, -1), p(2, 0, 3, 2, -1)}),

        new Engine.Level("Room to turn", "Plan where you will stand for the next push. Undo is always available.",
            new Engine.Room[] {r(0, "Courtyard", "#######", "#...o.#", "#.....#", "#..#..#", "#....o#", "#@....#", "#######")},
            new Engine.Piece[] {p(0, 0, 1, 5, -1), p(1, 0, 3, 2, -1), p(2, 0, 3, 4, -1)}),

        new Engine.Level("Step inside", "When a world cannot move, walk into its open doorway.",
            new Engine.Room[] {
                r(0, "Courtyard", "#######", "#.....#", "#.....#", "#.....#", "#.....#", "#.....#", "#######"),
                r(1, "Little room", "#####", "#...#", "...o#", "#.@.#", "#####")},
            new Engine.Piece[] {p(0, 0, 2, 3, -1), p(1, 0, 5, 3, 1), p(2, 1, 2, 2, -1)}),

        new Engine.Level("Back outside", "Push cargo through an open edge. Find another way out.",
            new Engine.Room[] {
                r(0, "Courtyard", "#######", "#@....#", "#.....#", "##.o..#", "###...#", "#.....#", "#######"),
                r(1, "Passage", "##.##", "#...#", "#....", "#...#", "#####")},
            new Engine.Piece[] {p(0, 1, 2, 2, -1), p(1, 0, 2, 3, 1), p(2, 1, 3, 2, -1)}),

        new Engine.Level("The other doorway", "Look at the room's edges. This world opens from the right.",
            new Engine.Room[] {
                r(0, "Courtyard", "#######", "#.....#", "#.....#", "#o....#", "#.....#", "#.....#", "#######"),
                r(1, "East room", "#####", "#.#@#", "#.#..", "#.#.#", "#####")},
            new Engine.Piece[] {p(0, 0, 1, 3, -1), p(1, 0, 3, 3, 1)}),

        new Engine.Level("One more world", "A world can hold another world. Follow the open center doorways.",
            new Engine.Room[] {
                r(0, "Courtyard", "#######", "#.....#", "#.....#", "#.....#", "#.....#", "#.....#", "#######"),
                r(1, "Middle room", "#####", "#...#", "....#", "#...#", "#####"),
                r(2, "Inner room", "#####", "#...#", "...o#", "#.@.#", "#####")},
            new Engine.Piece[] {p(0, 0, 1, 3, -1), p(1, 0, 5, 3, 1), p(2, 1, 3, 2, 2), p(3, 2, 2, 2, -1)}),

        new Engine.Level("Special delivery", "Crates fit through doorways too. Bring this one inside.",
            new Engine.Room[] {
                r(0, "Courtyard", "#######", "#.....#", "#.....#", "#.....#", "#.....#", "#@....#", "#######"),
                r(1, "Receiving room", "#####", "#...#", "..o.#", "#...#", "#####")},
            new Engine.Piece[] {p(0, 0, 2, 3, -1), p(1, 0, 5, 3, 1), p(2, 0, 3, 3, -1)}),

        new Engine.Level("Carry the room", "Moving a world carries everything inside. Line up its lower exit.",
            new Engine.Room[] {
                r(0, "Courtyard", "#######", "#.....#", "#...#.#", "#....##", "#.##o##", "#@....#", "#######"),
                r(1, "Cargo room", "#####", "#...#", "....#", "#...#", "##.##")},
            new Engine.Piece[] {p(0, 0, 1, 3, -1), p(1, 0, 2, 3, 1), p(2, 1, 2, 2, -1)}),

        new Engine.Level("Two deliveries", "One crate belongs inside; the other belongs outside. Make space first.",
            new Engine.Room[] {
                r(0, "Courtyard", "#######", "#.....#", "#....##", "#.....#", "#.###o#", "#@....#", "#######"),
                r(1, "Sorting room", "#####", "#.o.#", "....#", "#...#", "##.##")},
            new Engine.Piece[] {p(0, 0, 1, 3, -1), p(1, 0, 4, 3, 1), p(2, 0, 2, 3, -1), p(3, 1, 2, 3, -1)}),

        new Engine.Level("A long way home", "Move the outer world, then guide its cargo out through both rooms.",
            new Engine.Room[] {
                r(0, "Courtyard", "#######", "#.....#", "#.....#", "#.....#", "#####o#", "#######", "#######"),
                r(1, "Middle room", "#######", "#..#..#", "#...#.#", "......#", "#.....#", "#@....#", "###.###"),
                r(2, "Cargo room", "#####", "#...#", "....#", "#...#", "##.##")},
            new Engine.Piece[] {p(0, 0, 1, 3, -1), p(1, 0, 4, 3, 1), p(2, 1, 3, 2, 2), p(3, 2, 2, 2, -1)})
    };

    /** Recorded winning moves, replayed by tests/LevelCheck.java. */
    public static final String[] SOLUTIONS = {
        "RDL",
        "RRURRDDLUURULDDDDLLL",
        "RRD",
        "UUURRLDDRRUUDDDLLL",
        "RRRRRD",
        "RRLULUUUL",
        "URRRDLLLLU",
        "RRRRRRRRRD",
        "RRRRLLDDLLL",
        "RRRRURDDDUULLLLLDD",
        "URRDRRRRDULLLULLLDRRRRRDRDUULLLLLLDD",
        "RRRRRURRRURDDDDDDDULL"
    };
}
