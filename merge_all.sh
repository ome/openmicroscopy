#!/bin/bash

set -e
set -u


BRANCH=${BRANCH:-dev_4_4}

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
git merge --ff-only origin/$BRANCH
merge docs/sphinx docs/sphinx $BRANCH
merge components/tools/OmeroPy/scripts scripts $BRANCH
merge components/bioformats bioformats $BRANCH
