#!/bin/bash

# Yes this script uses eval - use at your own risk!
# provided a new release version number it attempts to update the recipes automatically by renaming them and updating the sha256sum


PV="$1"

if [ -z "$PV" ] || [ $# -ne 1 ]
then
    printf "I need a single argument, the new version number (e.g. 1.24.8).\n"
    exit 1
fi

recipes="
gstreamer1.0
gstreamer1.0-libav
gstreamer1.0-plugins-bad
gstreamer1.0-plugins-base
gstreamer1.0-plugins-good
gstreamer1.0-plugins-ugly
gstreamer1.0-python
gstreamer1.0-rtsp-server
gstreamer1.0-vaapi
"

recipes_path="meta/recipes-multimedia/gstreamer"

export PV

for r in $recipes
do
    recipe="$(ls $recipes_path/${r}_*.bb)"
    src_uri=$(cat $recipe | grep "SRC_URI = " | grep  -Eo 'https://[^ >]+' | sed s/\"$//)
    PNREAL=$(cat $recipe | sed -nr 's/^PNREAL = "(.*)"$/\1/p')
    src_uri=$(eval echo $src_uri)
    sha256sum=$(curl -so - $src_uri.sha256sum | cut -f1 -d" " | grep -E '^[[:xdigit:]]{64}$')
    if [ -z "$sha256sum" ]
    then
        printf "Failed retreiving sha256sum for %s\n" "$r"
        exit 1
    fi
    
    new_filname="$recipes_path/${r}_${PV}.bb"
    echo "sha256 for new recipe $new_filname: $sha256sum"

    git mv $recipe $new_filname
    sed -i -r  "s/^SRC_URI\[sha256sum\] = \"[[:xdigit:]]{64}\"\$/SRC_URI[sha256sum] = \"$sha256sum\"/" $new_filname

done

git branch feature/${PV}
git checkout feature/${PV}
git add .
git commit -m "Bump to ${PV}"