package com.pocketparadox.game;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** Deterministic puzzle rules, independent of Android. Coordinates start at the top left. */
public final class Engine {
    private static final int MAX_MOVES = 1_000_000;

    public static final class Room {
        public final int id, width, height;
        public final String name;
        public final String[] tiles;

        public Room(int id, String name, String[] tiles) {
            if (id < 0 || name == null || tiles == null || tiles.length < 3 || tiles.length > 31
                    || tiles[0] == null || tiles[0].length() < 3 || tiles[0].length() > 31)
                throw new IllegalArgumentException("Invalid room");
            this.id = id;
            this.name = name;
            this.tiles = tiles.clone();
            width = tiles[0].length();
            height = tiles.length;
            for (String row : tiles) {
                if (row == null || row.length() != width)
                    throw new IllegalArgumentException("Room rows must have equal widths");
                for (int x = 0; x < width; x++)
                    if ("#.o@".indexOf(row.charAt(x)) < 0)
                        throw new IllegalArgumentException("Unknown tile");
            }
        }

        public boolean wall(int x, int y) {
            return x < 0 || y < 0 || x >= width || y >= height || tiles[y].charAt(x) == '#';
        }
    }

    public static final class Piece {
        public int id, room, x, y, inside;

        public Piece(int id, int room, int x, int y, int inside) {
            this.id = id;
            this.room = room;
            this.x = x;
            this.y = y;
            this.inside = inside;
        }

        private Piece(Piece other) {
            this(other.id, other.room, other.x, other.y, other.inside);
        }
    }

    public static final class Level {
        public final String title, hint;
        public final Room[] rooms;
        public final Piece[] pieces;

        public Level(String title, String hint, Room[] rooms, Piece[] pieces) {
            if (title == null || hint == null || rooms == null || pieces == null
                    || rooms.length < 1 || rooms.length > 64 || pieces.length < 1 || pieces.length > 256)
                throw new IllegalArgumentException("Invalid level");
            this.title = title;
            this.hint = hint;
            this.rooms = rooms.clone();
            this.pieces = new Piece[pieces.length];
            for (int i = 0; i < pieces.length; i++) {
                if (pieces[i] == null) throw new IllegalArgumentException("Missing piece");
                this.pieces[i] = new Piece(pieces[i]);
            }
        }
    }

    public final Level level;
    public final ArrayList<Piece> pieces = new ArrayList<>();
    public int moves;
    private final ArrayDeque<int[]> history = new ArrayDeque<>();
    private final String signature;

    public Engine(Level level) {
        if (level == null) throw new IllegalArgumentException("Missing level");
        this.level = level;
        Set<Integer> roomIds = new HashSet<>();
        int playerTargets = 0;
        for (Room room : level.rooms) {
            if (room == null || !roomIds.add(room.id)) throw new IllegalArgumentException("Duplicate room");
            for (String row : room.tiles)
                for (int x = 0; x < row.length(); x++) if (row.charAt(x) == '@') playerTargets++;
        }
        if (playerTargets != 1) throw new IllegalArgumentException("A level needs exactly one player target");
        for (Piece piece : level.pieces) pieces.add(new Piece(piece));
        if (!valid(pieces)) throw new IllegalArgumentException("Invalid starting pieces or containment");
        signature = signature();
    }

    /** Solver copy: mutable positions are independent; undo history starts empty. */
    public Engine(Engine other) {
        level = other.level;
        signature = other.signature;
        moves = other.moves;
        for (Piece piece : other.pieces) pieces.add(new Piece(piece));
    }

    public Piece player() {
        for (Piece piece : pieces) if (piece.id == 0) return piece;
        throw new IllegalStateException("Missing player");
    }

    public Room room(int id) {
        for (Room room : level.rooms) if (room.id == id) return room;
        return null;
    }

    public Piece at(int room, int x, int y) {
        for (Piece piece : pieces) if (piece.room == room && piece.x == x && piece.y == y) return piece;
        return null;
    }

    public boolean move(int dx, int dy) {
        if (!((dx == -1 || dx == 1) && dy == 0 || (dy == -1 || dy == 1) && dx == 0)
                || moves >= MAX_MOVES) return false;
        int[] before = snapshot();
        if (!advance(player(), dx, dy, new HashSet<>()) || !valid(pieces)) {
            apply(before);
            return false;
        }
        history.push(before);
        moves++;
        return true;
    }

    public boolean canUndo() { return !history.isEmpty(); }

    public boolean undo() {
        if (history.isEmpty()) return false;
        apply(history.pop());
        return true;
    }

    public boolean won() {
        for (Room room : level.rooms) for (int y = 0; y < room.height; y++)
            for (int x = 0; x < room.width; x++) {
                char tile = room.tiles[y].charAt(x);
                if (tile != 'o' && tile != '@') continue;
                Piece occupant = at(room.id, x, y);
                if (occupant == null || (tile == '@') != (occupant.id == 0)) return false;
            }
        return true;
    }

    /** Positions and containment only; move count is deliberately excluded for search. */
    public String stateKey() {
        StringBuilder result = new StringBuilder();
        for (Piece piece : pieces) result.append(piece.id).append(',').append(piece.room).append(',')
                .append(piece.x).append(',').append(piece.y).append(',').append(piece.inside).append(';');
        return result.toString();
    }

    /** Versioned, level-specific session state. Undo history is intentionally session-local. */
    public String save() {
        StringBuilder result = new StringBuilder("PP1\n").append(signature).append('\n').append(moves);
        for (Piece piece : pieces) result.append('\n').append(piece.id).append(',').append(piece.room)
                .append(',').append(piece.x).append(',').append(piece.y).append(',').append(piece.inside);
        return result.toString();
    }

    /** Reject malformed saves without changing the running game. */
    public boolean restore(String serialized) {
        if (serialized == null || serialized.length() > 64_000) return false;
        String[] lines = serialized.split("\n", -1);
        if (lines.length != pieces.size() + 3 || !lines[0].equals("PP1") || !lines[1].equals(signature))
            return false;
        try {
            int count = Integer.parseInt(lines[2]);
            if (count < 0 || count > MAX_MOVES) return false;
            ArrayList<Piece> candidate = new ArrayList<>();
            for (int i = 0; i < pieces.size(); i++) {
                String[] values = lines[i + 3].split(",", -1);
                if (values.length != 5) return false;
                Piece piece = new Piece(Integer.parseInt(values[0]), Integer.parseInt(values[1]),
                        Integer.parseInt(values[2]), Integer.parseInt(values[3]), Integer.parseInt(values[4]));
                Piece original = level.pieces[i];
                if (piece.id != original.id || piece.inside != original.inside) return false;
                candidate.add(piece);
            }
            if (!valid(candidate)) return false;
            for (int i = 0; i < pieces.size(); i++) {
                pieces.get(i).room = candidate.get(i).room;
                pieces.get(i).x = candidate.get(i).x;
                pieces.get(i).y = candidate.get(i).y;
            }
            moves = count;
            history.clear();
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean advance(Piece piece, int dx, int dy, Set<Integer> active) {
        if (!active.add(piece.id)) return false;
        try {
            return enter(piece, piece.room, piece.x + dx, piece.y + dy, dx, dy, active);
        } finally {
            active.remove(piece.id);
        }
    }

    private boolean enter(Piece piece, int roomId, int x, int y, int dx, int dy, Set<Integer> active) {
        Room room = room(roomId);
        if (x < 0 || y < 0 || x >= room.width || y >= room.height) {
            if (dx != 0 && y != room.height / 2 || dy != 0 && x != room.width / 2) return false;
            Piece enclosing = owner(pieces, roomId);
            return enclosing != null && enter(piece, enclosing.room, enclosing.x + dx,
                    enclosing.y + dy, dx, dy, active);
        }
        if (room.wall(x, y) || wouldContainItself(piece, roomId)) return false;
        Piece blocker = at(roomId, x, y);
        if (blocker == piece) return false;
        if (blocker != null) {
            int[] before = snapshot();
            if (advance(blocker, dx, dy, active) && at(roomId, x, y) == null
                    && !wouldContainItself(piece, roomId)) {
                piece.room = roomId;
                piece.x = x;
                piece.y = y;
                return true;
            }
            apply(before);
            if (blocker.inside < 0) return false;
            Room interior = room(blocker.inside);
            int entryX = dx == 0 ? interior.width / 2 : dx > 0 ? 0 : interior.width - 1;
            int entryY = dy == 0 ? interior.height / 2 : dy > 0 ? 0 : interior.height - 1;
            if (enter(piece, interior.id, entryX, entryY, dx, dy, active)) return true;
            apply(before);
            return false;
        }
        piece.room = roomId;
        piece.x = x;
        piece.y = y;
        return true;
    }

    private boolean wouldContainItself(Piece piece, int destination) {
        if (piece.inside < 0) return false;
        // ponytail: finite room trees; self-eating/infinite worlds need graph-based spatial rules.
        for (int i = 0; i <= level.rooms.length; i++) {
            if (destination == piece.inside) return true;
            Piece parent = owner(pieces, destination);
            if (parent == null) return false;
            destination = parent.room;
        }
        return true;
    }

    private static Piece owner(ArrayList<Piece> state, int roomId) {
        for (Piece piece : state) if (piece.inside == roomId) return piece;
        return null;
    }

    private boolean valid(ArrayList<Piece> state) {
        Set<Integer> ids = new HashSet<>(), interiors = new HashSet<>();
        Set<String> occupied = new HashSet<>();
        boolean hasPlayer = false;
        for (Piece piece : state) {
            Room room = room(piece.room);
            if (piece.id < 0 || !ids.add(piece.id) || room == null || room.wall(piece.x, piece.y)
                    || !occupied.add(piece.room + "," + piece.x + "," + piece.y)) return false;
            if (piece.id == 0) {
                if (piece.inside != -1) return false;
                hasPlayer = true;
            }
            if (piece.inside < -1 || piece.inside >= 0 && (room(piece.inside) == null
                    || piece.inside == level.rooms[0].id || !interiors.add(piece.inside))) return false;
        }
        if (!hasPlayer || interiors.size() != level.rooms.length - 1) return false;
        for (Room room : level.rooms) {
            Set<Integer> ancestors = new HashSet<>();
            int current = room.id;
            while (true) {
                if (!ancestors.add(current)) return false;
                Piece parent = owner(state, current);
                if (parent == null) {
                    if (current != level.rooms[0].id) return false;
                    break;
                }
                current = parent.room;
            }
        }
        return true;
    }

    private int[] snapshot() {
        int[] state = new int[1 + pieces.size() * 3];
        state[0] = moves;
        for (int i = 0; i < pieces.size(); i++) {
            state[1 + i * 3] = pieces.get(i).room;
            state[2 + i * 3] = pieces.get(i).x;
            state[3 + i * 3] = pieces.get(i).y;
        }
        return state;
    }

    private void apply(int[] state) {
        moves = state[0];
        for (int i = 0; i < pieces.size(); i++) {
            pieces.get(i).room = state[1 + i * 3];
            pieces.get(i).x = state[2 + i * 3];
            pieces.get(i).y = state[3 + i * 3];
        }
    }

    private String signature() {
        StringBuilder source = new StringBuilder(level.title).append('\n');
        for (Room room : level.rooms) {
            source.append(room.id).append(':');
            for (String row : room.tiles) source.append(row).append('\n');
        }
        source.append(stateKey());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : digest) result.append(Character.forDigit((value & 255) >> 4, 16))
                    .append(Character.forDigit(value & 15, 16));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new AssertionError(exception);
        }
    }
}
