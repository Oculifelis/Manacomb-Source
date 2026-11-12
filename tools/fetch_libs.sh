#!/usr/bin/env bash
set -eu
cd "$(dirname "$0")/.."
mkdir -p libs/runtime

get() {  # get <dest> <url>
  [ -s "$1" ] && return 0
  echo "fetching $1"
  curl -sSfL -o "$1.part" "$2" && mv "$1.part" "$1"
}

MR=https://cdn.modrinth.com/data
get libs/complicated_bees-1.21.1-3.6.2.jar "$MR/uhqEKIQS/versions/mgIcHfld/complicated_bees-1.21.1-3.6.2.jar"
get libs/Jade-1.21.1-NeoForge-15.10.3.jar "$(curl -sSf 'https://api.modrinth.com/v2/project/jade/version?loaders=%5B%22neoforge%22%5D&game_versions=%5B%221.21.1%22%5D' \
  | python -c "import sys,json;print(next(f['url'] for v in json.load(sys.stdin) if '15.10.3' in v['version_number'] for f in v['files'] if f['primary']))")"

BJ=https://maven.blamejared.com
SNAP=$(curl -sSf "$BJ/vazkii/botania/botania-neoforge-1.21.1/457-SNAPSHOT/maven-metadata.xml" | sed -n 's:.*<value>\(.*\)</value>.*:\1:p' | head -1)
get libs/runtime/botania-neoforge-1.21.1-457-SNAPSHOT.jar "$BJ/vazkii/botania/botania-neoforge-1.21.1/457-SNAPSHOT/botania-neoforge-1.21.1-$SNAP.jar"
get libs/runtime/Patchouli-1.21.1-92-NEOFORGE.jar "$BJ/vazkii/patchouli/Patchouli/1.21.1-92-NEOFORGE/Patchouli-1.21.1-92-NEOFORGE.jar"
get libs/runtime/curios-neoforge-9.5.1+1.21.1.jar "https://maven.theillusivec4.top/top/theillusivec4/curios/curios-neoforge/9.5.1+1.21.1/curios-neoforge-9.5.1+1.21.1.jar"
get libs/runtime/jei-1.21.1-neoforge-19.21.2.313.jar "$BJ/mezz/jei/jei-1.21.1-neoforge/19.21.2.313/jei-1.21.1-neoforge-19.21.2.313.jar"
echo "libs ready"
