import com.pocketparadox.game.Engine;
import com.pocketparadox.game.Engine.Level;
import com.pocketparadox.game.Engine.Piece;
import com.pocketparadox.game.Engine.Room;
import com.pocketparadox.game.Levels;
import java.util.Random;

/** Run with javac, then java -ea EngineCheck. No Android or test framework required. */
public final class EngineCheck {
    private static final Room OUTER = new Room(0, "Outside", new String[] {
        "#######", "#....@#", "#.....#", "#.....#", "#######"
    });
    private static final Room INSIDE = new Room(1, "Inside", new String[] {
        "#####", "#...#", ".....", "#...#", "#####"
    });

    private static Engine engine(Room[] rooms, Piece... pieces) {
        return new Engine(new Level("Check", "", rooms, pieces));
    }

    private static Piece p(int id, int room, int x, int y, int inside) {
        return new Piece(id, room, x, y, inside);
    }

    private static void location(Engine game, int id, int room, int x, int y) {
        Piece piece = game.at(room, x, y);
        assert piece != null && piece.id == id : "Wrong position for piece " + id;
    }

    private static void rejectSave(Engine game, String corrupt) {
        String before = game.save();
        boolean undo = game.canUndo();
        assert !game.restore(corrupt) : "Accepted invalid save";
        assert before.equals(game.save()) && undo == game.canUndo() : "Failed load mutated game";
    }

    public static void main(String[] args) {
        boolean assertions = false;
        assert assertions = true;
        if (!assertions) throw new IllegalStateException("Run with -ea");

        Room goals = new Room(0, "Goals", new String[] {
            "#######", "#....@#", "#..oo.#", "#.....#", "#######"
        });
        Engine chain = engine(new Room[] {goals}, p(0, 0, 1, 2, -1), p(1, 0, 2, 2, -1), p(2, 0, 3, 2, -1));
        String initial = chain.stateKey();
        assert chain.move(1, 0) && chain.moves == 1;
        location(chain, 1, 0, 3, 2);
        location(chain, 2, 0, 4, 2);
        assert chain.move(1, 0);
        String blocked = chain.save();
        assert !chain.move(1, 0) && blocked.equals(chain.save());
        assert !chain.move(1, 1) && !chain.move(Integer.MIN_VALUE, 0);
        assert chain.undo() && chain.moves == 1;
        assert chain.undo() && chain.moves == 0 && initial.equals(chain.stateKey());
        assert !chain.undo() && !chain.canUndo();
        assert chain.move(1, 0) && chain.move(0, -1);
        assert chain.move(1, 0) && chain.move(1, 0) && chain.move(1, 0);
        assert chain.won() : "Both crate goals and player target must be satisfied";

        Engine entry = engine(new Room[] {OUTER, INSIDE}, p(0, 0, 4, 2, -1), p(1, 0, 5, 2, 1));
        assert entry.move(1, 0);
        location(entry, 0, 1, 0, 2);
        assert entry.move(1, 0) && entry.undo();
        assert entry.move(-1, 0);
        location(entry, 0, 0, 4, 2);
        assert entry.undo();
        location(entry, 0, 1, 0, 2);
        Engine copy = new Engine(entry);
        assert !copy.canUndo();
        assert copy.move(1, 0) && !copy.stateKey().equals(entry.stateKey());
        assert copy.undo() && copy.stateKey().equals(entry.stateKey());

        Engine pushFirst = engine(new Room[] {OUTER, INSIDE}, p(0, 0, 2, 2, -1), p(1, 0, 3, 2, 1));
        assert pushFirst.move(1, 0);
        location(pushFirst, 0, 0, 3, 2);
        location(pushFirst, 1, 0, 4, 2);

        Room edges = new Room(1, "Edges", new String[] {".....", ".....", ".....", ".....", "....."});
        Engine center = engine(new Room[] {OUTER, edges}, p(0, 1, 0, 1, -1), p(1, 0, 5, 2, 1));
        assert !center.move(-1, 0) : "Exits require the center edge";
        assert center.move(0, 1) && center.move(-1, 0);
        location(center, 0, 0, 4, 2);

        Engine crateEntry = engine(new Room[] {OUTER, INSIDE},
                p(0, 0, 3, 2, -1), p(1, 0, 4, 2, -1), p(2, 0, 5, 2, 1));
        assert crateEntry.move(1, 0);
        location(crateEntry, 1, 1, 0, 2);
        location(crateEntry, 0, 0, 4, 2);
        assert crateEntry.move(1, 0);
        location(crateEntry, 1, 1, 1, 2);
        location(crateEntry, 0, 1, 0, 2);

        Engine crateExit = engine(new Room[] {OUTER, INSIDE},
                p(0, 1, 3, 2, -1), p(1, 1, 4, 2, -1), p(2, 0, 3, 2, 1), p(3, 0, 4, 2, -1));
        assert crateExit.move(1, 0);
        location(crateExit, 0, 1, 4, 2);
        location(crateExit, 1, 0, 4, 2);
        location(crateExit, 3, 0, 5, 2);
        blocked = crateExit.save();
        assert !crateExit.move(1, 0) && blocked.equals(crateExit.save());

        Room closed = new Room(1, "Closed", new String[] {
            "#####", "#####", ".####", "#####", "#####"
        });
        Engine blockedEntry = engine(new Room[] {OUTER, closed},
                p(0, 0, 3, 2, -1), p(1, 0, 4, 2, -1), p(2, 0, 5, 2, 1), p(3, 1, 0, 2, -1));
        blocked = blockedEntry.save();
        assert !blockedEntry.move(1, 0) && blocked.equals(blockedEntry.save());
        assert blockedEntry.moves == 0 && !blockedEntry.canUndo();

        Room sibling = new Room(2, "Sibling", INSIDE.tiles);
        Room shortOuter = new Room(0, "Outside", new String[] {
            "#######", "#....@#", "#....##", "#.....#", "#######"
        });
        Engine nested = engine(new Room[] {shortOuter, INSIDE, sibling},
                p(0, 0, 2, 2, -1), p(1, 0, 3, 2, 1), p(2, 0, 4, 2, 2), p(3, 1, 2, 2, -1));
        assert nested.move(1, 0);
        location(nested, 1, 2, 0, 2);
        location(nested, 3, 1, 2, 2);
        assert nested.undo();
        location(nested, 1, 0, 3, 2);

        String saved = crateEntry.save();
        Engine restored = new Engine(crateEntry.level);
        assert restored.restore(saved) && restored.moves == crateEntry.moves;
        assert restored.stateKey().equals(crateEntry.stateKey()) && !restored.canUndo();
        assert restored.move(1, 0);
        rejectSave(restored, null);
        rejectSave(restored, saved + "\n");
        rejectSave(restored, saved.replace("PP1", "PP2"));
        rejectSave(restored, saved.replace("0,1,0,2,-1", "0,9,0,2,-1"));
        rejectSave(restored, saved.replace("0,1,0,2,-1", "0,1,0,0,-1"));
        rejectSave(restored, saved.replace("0,1,0,2,-1", "0,1,1,2,-1"));
        rejectSave(restored, saved.replace("1,1,1,2,-1", "1,1,1,2,1"));
        rejectSave(restored, saved.replace("2,0,5,2,1", "2,1,3,2,1"));
        rejectSave(restored, saved.replace("\n2\n", "\n-1\n"));
        rejectSave(restored, saved.replace("\n2\n", "\n1000001\n"));
        rejectSave(restored, saved.replace("\n2\n", "\n999999999999999999\n"));
        rejectSave(nested, nested.save().replace("1,0,3,2,1", "1,2,3,2,1")
                .replace("2,0,4,2,2", "2,1,4,2,2"));
        rejectSave(restored, entry.save());
        assert new Engine(crateEntry.level).moves == 0 : "New engine resets the level";

        Random random = new Random(0x50415241444f58L);
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (Level level : Levels.ALL) {
            Engine game = new Engine(level), verifier = new Engine(level);
            for (int step = 0; step < 2048; step++) {
                if (step % 256 == 0) game = new Engine(level);
                int[] direction = directions[random.nextInt(4)];
                String before = game.save();
                boolean couldUndo = game.canUndo();
                if (game.move(direction[0], direction[1])) {
                    String after = game.save();
                    assert verifier.restore(after) && after.equals(verifier.save()) : level.title;
                    assert game.undo() && before.equals(game.save()) : "Undo: " + level.title;
                    assert game.move(direction[0], direction[1]) && after.equals(game.save()) : "Replay: " + level.title;
                } else {
                    assert before.equals(game.save()) && couldUndo == game.canUndo() : "Rollback: " + level.title;
                }
            }
        }
        System.out.println("Engine checks passed: chains, rollback, doors, nesting, undo, goals, persistence; "
                + (2048 * Levels.ALL.length) + " deterministic campaign fuzz moves.");
    }
}
