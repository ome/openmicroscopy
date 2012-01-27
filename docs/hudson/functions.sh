#!/bin/bash
#
# Copyright 2011 Glencoe Software, Inc. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# Common functions used throughout the hudson scripts
#

git_info() {
    HEAD=`git rev-parse HEAD`
    git log --max-count=1 $HEAD
}
