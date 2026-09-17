"""Generate Pocket Paradox's original, short sine chimes using only the standard library.

Run: python3 tools/make_sounds.py
The generated WAV files are source assets, not downloaded recordings.
"""
from array import array
from math import sin, pi
from pathlib import Path
import sys
import wave

SOUNDS = {
    "step": (0.065, [440]),
    "push": (0.10, [220, 330]),
    "blocked": (0.07, [147]),
    "enter": (0.18, [392, 523, 659]),
    "exit": (0.18, [659, 523, 392]),
    "goal": (0.23, [523, 659, 784]),
    "complete": (0.42, [523, 659, 784, 1047]),
    "undo": (0.10, [440, 349]),
}


def samples(duration, notes):
    rate = 22050
    count = int(rate * duration)
    data = array("h")
    for i in range(count):
        time = i / rate
        value = 0
        for n, frequency in enumerate(notes):
            local = time - n * duration * 0.15
            if local >= 0:
                envelope = min(1, local / 0.006) * max(0, 1 - time / duration) ** 2
                value += sin(2 * pi * frequency * local) * envelope
        data.append(round(value * 7000 / len(notes)))
    assert len(data) == count and max(abs(v) for v in data) <= 7000
    if sys.byteorder != "little":
        data.byteswap()
    return data.tobytes()


if __name__ == "__main__":
    target = Path(__file__).resolve().parents[1] / "app/src/main/res/raw"
    target.mkdir(parents=True, exist_ok=True)
    for name, (duration, notes) in SOUNDS.items():
        with wave.open(str(target / f"sfx_{name}.wav"), "wb") as output:
            output.setparams((1, 2, 22050, 0, "NONE", "not compressed"))
            output.writeframes(samples(duration, notes))
    print(f"Generated {len(SOUNDS)} original sound effects.")
