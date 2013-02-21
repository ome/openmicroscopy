#!/bin/bash

set -e
set -u

VENV_URL=${VENV_URL:-https://raw.github.com/pypa/virtualenv/master/virtualenv.py}
TABLES_GIT=${TABLES_GIT:-git+https://github.com/PyTables/PyTables.git@master}
if [[ "${GIT_SSL_NO_VERIFY-}" == "1" ]]; then
    CURL="curl ${CURL_OPTS-} --insecure -O"
else
    CURL="curl ${CURL_OPTS-} -O"
fi

###################################################################
# BREW & PIP BASE SYSTEMS
###################################################################

# Python virtualenv/pip support ===================================
if (pip --version)
then
    PIP_DIR="$(dirname $(dirname $(which pip)))"
    echo "Using existing pip installed in $PIP_DIR"

    # Move to PIP_DIR for the rest of this script
    # so that "bin/EXECUTABLE" will pick up the
    # intended executable.
    cd "$PIP_DIR"
else
    rm -rf virtualenv.py
    $CURL "$VENV_URL"
    python virtualenv.py --no-site-packages .
fi


###################################################################
# PIP INSTALLS
###################################################################

installed(){
    PKG=$1; shift
    bin/pip freeze "$@" | grep -q "^$PKG==" && {
        echo $PKG installed.
    } || {
        return 1
    }
}

# Python requirements =============================================
installed numpy  || bin/pip install numpy
installed PIL || bin/pip install PIL
installed scipy || bin/pip install scipy
#
# Various issues with matplotlib. See the following if you have problems:
# -----------------------------------------------------------------------
# http://superuser.com/questions/242190/how-to-install-matplotlib-on-os-x
# http://jholewinski.org/blog/installing-matplotlib-on-os-x-10-7-with-homebrew/
#
export LDFLAGS="-L/usr/X11/lib"
export CFLAGS="-I/usr/X11/include -I/usr/X11/include/freetype2 -I/usr/X11/include/libpng12"
installed matplotlib || bin/pip install matplotlib

# PyTables requirements ===========================================
export HDF5_DIR=`pwd`
installed Cython || bin/pip install Cython
installed numexpr || bin/pip install numexpr
bin/pip freeze | grep -q tables-dev || bin/pip install -e $TABLES_GIT#egg=tables

echo "Done."

