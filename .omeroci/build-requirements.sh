#!/usr/bin/env bash

set -e
set -u

BUILD_STYLE=$1
BUILD_REPO=$2
BUILD_DIR=$3
COMPONENTS_BRANCH=${COMPONENTS_BRANCH:-master}
COMPONENTS_FORK=${COMPONENTS_FORK:-ome}
BUILD_STYLE=${BUILD_STYLE:-latest}

if [ "$BUILD_STYLE" == "release" ]; then
   echo Release build. No build of ${BUILD_REPO} needed.
   exit 0
fi

set -x
cd ${BUILD_DIR}
git clone -b ${COMPONENTS_BRANCH} git://github.com/${COMPONENTS_FORK}/${BUILD_REPO} ${BUILD_REPO}
cd ${BUILD_REPO}

if [ "$BUILD_STYLE" == "latest" ]; then

  git submodule update --init

elif [ "$BUILD_STYLE" == "merge" ]; then

   echo Merging components...
   git config user.name automated
   git config user.email build@localhost
   scc merge -vvv --no-ask --reset --shallow -S success-only ${COMPONENTS_BRANCH}
   git submodule sync
   git submodule update --init --remote --recursive
   # TBD: use repositories.yml here
   # TBD: update versions
   git submodule foreach git config user.name automated
   git submodule foreach git config user.email build@localhost
   scc merge -vvv --no-ask --reset           -S success-only ${COMPONENTS_BRANCH}


else

   echo "Unknown build style: ${BUILD_STYLE}"
   exit 1

fi

./build.sh
