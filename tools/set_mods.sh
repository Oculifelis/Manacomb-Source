#!/usr/bin/env bash
set -eu
cd "$(dirname "$0")/.."
RUN=$1; MODE=${2:-}
mkdir -p "$RUN/mods"
rm -f "$RUN"/mods/*.jar
cp libs/complicated_bees-1.21.1-3.6.2.jar libs/runtime/botania-*.jar libs/runtime/Patchouli-*.jar libs/runtime/curios-*.jar "$RUN/mods/"
if [ "$MODE" = client ]; then
  cp libs/runtime/jei-*.jar libs/Jade-*.jar "$RUN/mods/"
fi
ls "$RUN/mods"
