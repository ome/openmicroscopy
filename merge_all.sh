#!/bin/bash

set -e
set -u

merge(){

    path=$1
    name=$2
    head=$3

    cd "$path"
    git fetch origin
    git checkout origin/"$head"
    cd -
    # Test if there are any modifications
    test -n "$( git diff HEAD --name-only $path )"
        git commit -m "$name: moved to latest $head" "$path"
}

git fetch origin
git merge --ff-only origin/develop
merge docs/sphinx docs/sphinx master
merge components/tools/OmeroPy/scripts scripts master
merge components/bioformats bioformats develop
