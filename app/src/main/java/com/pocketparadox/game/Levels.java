package com.pocketparadox.game;

/** An original twelve-puzzle introduction to finite nested worlds. */
public final class Levels {
    private Levels() {}

    public static final String[] CHAPTERS = {"First moves", "Worlds within", "Moving worlds"};
    public static final int[] CHAPTER_STARTS = {0, 4, 8};

    public static final String[] TEACHING = {
        "You’re the coral explorer. Swipe the board or use the arrows. Push the crate onto the gold ring.",
        "You can push, but never pull. Undo lets you try another approach.",
        "Crates can push other crates. Look for space at the far end.",
        "Take your time. Work out where you need to stand before pushing.",
        "Mint boxes hold rooms. Push one until it cannot move, then enter its centered doorway.",
        "Open edges lead outside. The small preview shows the room around you.",
        "Each room has its own doorways. Look closely at the miniature inside the mint frame.",
        "Rooms can hold other rooms. Follow the breadcrumb to keep your bearings.",
        "Crates fit through room doorways too. Push them just as you push yourself inside.",
        "Moving a mint box carries everything inside it. Its position changes where exits lead.",
        "Goals can be in different rooms. Use the outside preview to plan both deliveries.",
        "Everything you’ve learned fits together here. Undo and hints are always available."
    };
    private static final String[] NUDGES = {
        "There are two kinds of goal. The crate and explorer each have a place.",
        "Think about which side of the crate must face the goal.",
        "You don’t have to separate this pair of crates.",
        "Solve one delivery without blocking your route to the other.",
        "A room box behaves like a crate until the wall stops it.",
        "A doorway can carry cargo as well as the explorer.",
        "The visible goal is outside, but your own goal is somewhere else.",
        "Keep moving toward the doorway, even after the view changes.",
        "Put the cargo between yourself and the room’s entrance.",
        "Before moving cargo, look at where the bottom doorway will emerge.",
        "There is room for one delivery inside and one outside.",
        "Line up the worlds before guiding the cargo home."
    };
    private static final String[] GUIDANCE = {
        "Push right once. Walk down, then left to the coral cross.",
        "Get below the crate and push it to the top row. Then go around its right side to push it left onto the goal.",
        "Push right twice to place both crates. Step down onto your cross.",
        "Push the upper crate right once to line it up with its ring. Push the lower crate right twice, then approach the upper crate from below and push it up twice.",
        "Push the mint box right against the wall, then keep walking right through its doorway. Push the inner crate onto the ring and step down.",
        "Push the crate right twice to send it outside. Return left, then leave through the top opening and walk to the cross.",
        "Go above the box and around to its right side. Push it left onto the ring; keep moving left to enter, then go up to your cross.",
        "Keep walking right: push the outer room to the wall, enter, push the inner room to its wall, and enter again. Fill the ring, then step down.",
        "Push right four times to deliver the crate into the room. Backtrack left twice, down twice, then left three times to your cross.",
        "Push the room right twice, then enter it. Get above the cargo and push it through the bottom opening onto the outside ring.",
        "Move the outside crate into the room and push it up onto the inner ring. Then take the other crate out through the bottom doorway toward the outside ring.",
        "Push the outer room right twice, enter it, then push the inner room right. Enter the inner room, get above its crate, and push down through both aligned exits."
    };

    public static String hint(int level, int step) {
        return step <= 1 ? NUDGES[level] : step == 2 ? ALL[level].hint : GUIDANCE[level];
    }

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
