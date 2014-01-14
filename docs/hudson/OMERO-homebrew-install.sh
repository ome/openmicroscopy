#!/bin/bash
# Main Homebrew installation script

set -e
set -u
set -x

export PSQL_DIR=${PSQL_DIR:-/usr/local/var/postgres}
export OMERO_DATA_DIR=${OMERO_DATA_DIR:-/tmp/var/OMERO.data}
export BREW_OPTS=${BREW_OPTS:-}
export SCRIPT_NAME=${SCRIPT_NAME:-OMERO.sql}

# Test whether this script is run in a job environment
JOB_NAME=${JOB_NAME:-}
if [[ -n $JOB_NAME ]]; then
    DEFAULT_TESTING_MODE=true
else
    DEFAULT_TESTING_MODE=false
fi
TESTING_MODE=${TESTING_MODE:-$DEFAULT_TESTING_MODE}

###################################################################
# Homebrew installation
###################################################################

# Install Homebrew in /usr/local
ruby -e "$(curl -fsSL https://raw.github.com/mxcl/homebrew/go/install)"
cd /usr/local

# Install git if not already installed
bin/brew list | grep "\bgit\b" || bin/brew install git

# Update Homebrew
bin/brew update

# Run brew doctor
export PATH=$(bin/brew --prefix)/bin:$PATH
bin/brew doctor

###################################################################
# Python pip installation
###################################################################

# Install Homebrew python
# Alternately, the system Python can be used but installing Python
# dependencies may require sudo
bin/brew install python

# Install Genshi (OMERO and Bio-Formats requirement)
bin/pip install -U genshi

# Tap homebrew-science library (HDF5)
bin/brew tap homebrew/science || echo "Already tapped"

# Tap ome-alt library
bin/brew tap ome/alt || echo "Already tapped"

if [ $TESTING_MODE ]; then
    # Install scc tools
    bin/pip install -U scc || echo "scc installed"

    # Merge homebrew-alt PRs
    cd Library/Taps/ome-alt
    /usr/local/bin/scc merge master
fi

cd /usr/local

###################################################################
# Bio-Formats installation
###################################################################

# Install Bio-Formats
bin/brew install bioformats $BREW_OPTS
showinf -version

###################################################################
# OMERO installation
###################################################################

# Install PostgreSQL and OMERO
bin/brew install omero $BREW_OPTS
bin/brew install postgres

# Install OMERO Python dependencies
bash bin/omero_python_deps

# Set environment variables
ICE_VERSION=$(bin/brew deps omero $BREW_OPTS | grep ice)
export ICE_CONFIG=$(bin/brew --prefix omero)/etc/ice.config
export ICE_HOME=$(bin/brew --prefix $ICE_VERSION)
export PYTHONPATH=$(bin/brew --prefix omero)/lib/python:$ICE_HOME/python
export PATH=$(bin/brew --prefix)/bin:$(bin/brew --prefix)/sbin:/usr/local/lib/node_modules:$ICE_HOME/bin:$PATH
export DYLD_LIBRARY_PATH=$ICE_HOME/lib:$ICE_HOME/python:${DYLD_LIBRARY_PATH-}

# Note: If postgres startup fails it's probably because there was an old
# process still running.
# Create PostgreSQL database
if [ -d "$PSQL_DIR" ]; then
    rm -rf $PSQL_DIR
fi
bin/initdb $PSQL_DIR
bin/pg_ctl -D $PSQL_DIR -l $PSQL_DIR/server.log -w start

# Create user and database
bin/createuser -w -D -R -S db_user
bin/createdb -O db_user omero_database
bin/psql -h localhost -U db_user -l

# Set database
bin/omero config set omero.db.name omero_database
bin/omero config set omero.db.user db_user
bin/omero config set omero.db.pass db_password

# Run DB script
bin/omero db script "" "" root_password -f $SCRIPT_NAME
bin/psql -h localhost -U db_user omero_database < $SCRIPT_NAME
rm $SCRIPT_NAME

# Set up the data directory
mkdir -p $OMERO_DATA_DIR
bin/omero config set omero.data.dir $OMERO_DATA_DIR

# Start the server
bin/omero admin start