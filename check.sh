#!/usr/bin/env sh
set -eu
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
javac_bin="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
java_bin="${JAVA_HOME:+$JAVA_HOME/bin/}java"
mkdir -p "$project_dir/build/checks"
"$javac_bin" --release 17 -d "$project_dir/build/checks" \
  "$project_dir/app/src/main/java/com/pocketparadox/game/Engine.java" \
  "$project_dir/app/src/main/java/com/pocketparadox/game/Levels.java" \
  "$project_dir/tests/EngineCheck.java" "$project_dir/tests/LevelCheck.java"
"$java_bin" -ea -cp "$project_dir/build/checks" EngineCheck
"$java_bin" -ea -cp "$project_dir/build/checks" com.pocketparadox.game.LevelCheck
