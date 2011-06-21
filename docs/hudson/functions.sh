#!/bin/bash
#
# Copyright 2011 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# Common functions used throughout the hudson scripts
#

git_short_rev() {
    git rev-parse HEAD | cut -b1-8
}

git_zip() {
    FILE=$1
    git archive --format=zip HEAD -o $FILE
}

git_info() {
    HEAD=`git rev-parse HEAD`
    git log --max-count=1 $HEAD
}
