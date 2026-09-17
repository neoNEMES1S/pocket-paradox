"""Emulator-only UI checks. Resets this game's test data; requires explicit --reset-test-data.

python3 tests/android_smoke.py /path/to/adb emulator-5580 --reset-test-data
"""
import atexit
import os
import pathlib
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

assert len(sys.argv) == 4 and sys.argv[2].startswith("emulator-") and sys.argv[3] == "--reset-test-data", __doc__
adb, serial = sys.argv[1:3]
package = os.environ.get("POCKET_TEST_PACKAGE", "com.pocketparadox.game")
assert package in ("com.pocketparadox.game", "com.pocketparadox.game.polishtest")
root = pathlib.Path(__file__).resolve().parents[1]
shots = root / "dist" / "screenshots"
shots.mkdir(parents=True, exist_ok=True)


def run(*args, binary=False):
    return subprocess.check_output([adb, "-s", serial, *args], text=not binary, timeout=30)


def nodes():
    run("shell", "uiautomator", "dump", "/sdcard/pocket-window.xml")
    return list(ET.fromstring(run("shell", "cat", "/sdcard/pocket-window.xml")).iter("node"))


def bounds(n):
    return list(map(int, re.findall(r"\d+", n.get("bounds"))))


def find(label, field="text", scroll=False, clickable=False):
    for attempt in range(7 if scroll else 3):
        ui = nodes()
        for n in ui:
            if n.get(field, "").casefold().startswith(label.casefold()) and (not clickable or n.get("clickable") == "true"):
                return n
        if scroll:
            area = next(n for n in ui if n.get("scrollable") == "true")
            x1, y1, x2, y2 = bounds(area)
            run("shell", "input", "swipe", str(x1+8), str(y2-60), str(x1+8), str(y1+60), "250")
    raise AssertionError(f"UI element missing: {label}. Texts: {[n.get('text') for n in ui if n.get('text')]}")


def tap(label, field="text", scroll=False):
    n = find(label, field, scroll, clickable=True)
    assert n.get("enabled") == "true", label
    x1, y1, x2, y2 = bounds(n)
    run("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
    time.sleep(.2)


def screenshot(name):
    time.sleep(.55)  # Capture the settled board, not the first frame of a room zoom.
    (shots / f"{name}.png").write_bytes(run("exec-out", "screencap", "-p", binary=True))


def launch():
    run("shell", "input", "keyevent", "224")
    run("shell", "wm", "dismiss-keyguard")
    run("shell", "am", "start", "-W", "-n", package + "/com.pocketparadox.game.MainActivity")


def keys(moves):
    run("shell", "input", "keyevent", *[str({"U":19, "D":20, "L":21, "R":22}[m]) for m in moves])


def reset_display():
    run("shell", "settings", "put", "system", "font_scale", "1.0")
    run("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    run("shell", "settings", "put", "system", "user_rotation", "0")
    run("shell", "wm", "size", "reset")
    run("shell", "wm", "density", "reset")


atexit.register(reset_display)
reset_display()
run("shell", "pm", "clear", package)
launch()
find("0 / 12 solved")
screenshot("01-home")
tap("Settings")
tap("Touch feedback")
assert find("Touch feedback").get("checked") == "false"
tap("Sound effects")
assert find("Sound effects").get("checked") == "false"
tap("Reduced motion", scroll=True)
assert find("Reduced motion").get("checked") == "true"
tap("Reduced motion")
run("shell", "input", "keyevent", "4")
tap("How to play")
tap("Let's explore")
tap("Begin exploring")
find("0 moves")
screenshot("02-first-puzzle")
tap("Need a hint?", "content-desc", scroll=True)
find("There are two kinds of goal.")
tap("More help", "content-desc", scroll=True)
tap("More help", "content-desc", scroll=True)
find("Push right once.")
keys("R")
find("1 move")
run("shell", "settings", "put", "system", "accelerometer_rotation", "0")
run("shell", "settings", "put", "system", "user_rotation", "1")
time.sleep(1)
find("1 move")
assert find("↶  Undo").get("enabled") == "true"
screenshot("03-landscape")
run("shell", "settings", "put", "system", "user_rotation", "0")
time.sleep(1)
run("shell", "input", "keyevent", "4")
find("Little worlds.")
tap("Puzzle 1,", "content-desc")
find("1 move")
tap("↶  Undo")
find("0 moves")
board = next(n for n in nodes() if ". Player column " in n.get("content-desc", ""))
x1, y1, x2, y2 = bounds(board)
run("shell", "input", "swipe", str((x1+x2)//2-100), str((y1+y2)//2), str((x1+x2)//2+100), str((y1+y2)//2), "180")
find("1 move")
tap("↶  Undo")
find("0 moves")
source = (root / "app/src/main/java/com/pocketparadox/game/Levels.java").read_text()
solutions = re.findall(r'"([UDLR]+)"', source.split("String[] SOLUTIONS =", 1)[1])
for i, solution in enumerate(solutions):
    find(f"PUZZLE {i+1:02d}")
    if i == 1:
        keys(solution[:2])
        find("2 moves")
        run("shell", "am", "force-stop", package)
        launch()
        find("1 / 12 solved")
        tap("Continue exploring")
        find("2 moves")
        assert find("↶  Undo").get("enabled") == "false"
        tap("↻  Restart")
        tap("Restart", scroll=False)
        find("0 moves")
    if i == 7:
        keys(solution[:7])
        # Undo across both room boundaries while transitions can still be running.
        run("shell", "input", "keyevent", "54", "54", "54", "54", "54")
        keys(solution[2:7])
        screenshot("04-nested-worlds")
        keys(solution[7:])
    else:
        keys(solution)
    find("Beautifully done.")
    print(f"Android puzzle {i+1}/12 solved", flush=True)
    if i < len(solutions)-1:
        tap("Next puzzle", scroll=True)
    else:
        screenshot("05-campaign-complete")
        tap("Journey complete", scroll=True)
find("12 / 12 solved")
tap("Settings")
assert find("Sound effects").get("checked") == "false"
assert find("Touch feedback").get("checked") == "false"
tap("Reduced motion", scroll=True)
run("shell", "input", "keyevent", "4")
tap("Choose a puzzle")
screenshot("06-level-selection")
tap("Puzzle 1,", "content-desc")
run("shell", "wm", "size", "720x1136")
run("shell", "wm", "density", "320")
run("shell", "settings", "put", "system", "font_scale", "2.0")
time.sleep(1)
tap("Move right", "content-desc", scroll=True)
screenshot("07-large-text-small-screen")
run("shell", "settings", "put", "system", "font_scale", "1.0")
run("shell", "wm", "size", "reset")
run("shell", "wm", "density", "reset")
run("shell", "am", "force-stop", package)
launch()
find("12 / 12 solved")
tap("Settings")
assert find("Reduced motion", scroll=True).get("checked") == "true"
print("Android UI checks passed: controls, back, rotation+undo, resume, restart, all 12 levels, persistence, enlarged text.", flush=True)
