#!/bin/bash
# Main Homebrew installation script

set -e
set -u
set -x

export VENV_DIR=${VENV_DIR:-/usr/local/virtualenv}
export PSQL_DIR=${PSQL_DIR:-/usr/local/var/postgres}
export OMERO_DATA_DIR=${OMERO_DATA_DIR:-/tmp/var/OMERO.data}
export BREW_OPTS=${BREW_OPTS:-}
export SCRIPT_NAME=${SCRIPT_NAME:-OMERO.sql}
VENV_URL=${VENV_URL:-https://raw.github.com/pypa/virtualenv/master/virtualenv.py}

if [[ "${GIT_SSL_NO_VERIFY-}" == "1" ]]; then
    CURL="curl ${CURL_OPTS-} --insecure -O"
else
    CURL="curl ${CURL_OPTS-} -O"
fi

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
ruby -e "$(curl -fsSL https://raw.github.com/mxcl/homebrew/go)"
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

# Python virtualenv/pip support
rm -rf virtualenv.py
$CURL "$VENV_URL"
/usr/bin/python virtualenv.py --no-site-packages $VENV_DIR

# Activate the virtual environment
set +u
source $VENV_DIR/bin/activate
set -u

# Tap ome-alt library
bin/brew tap ome/alt || echo "Already tapped"

if [ $TESTING_MODE ]; then
    # Install scc tools
    $VENV_DIR/bin/pip install -U scc || echo "scc installed"

    # Merge homebrew-alt PRs
    cd Library/Taps/ome-alt
    $VENV_DIR/bin/scc merge master
fi

cd /usr/local

# Install Genshi (OMERO and Bio-Formats requirement)
bin/brew install python
bin/pip install -U genshi

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

# Install additional Python dependencies
bash bin/omero_python_deps

# Set environment variables
ICE_VERSION=$(bin/brew deps omero $BREW_OPTS | grep ice)
export ICE_CONFIG=$(bin/brew --prefix omero)/etc/ice.config
export ICE_HOME=$(bin/brew --prefix $ICE_VERSION)
export PYTHONPATH=$(bin/brew --prefix omero)/lib/python:$ICE_HOME/python
export PATH=$(bin/brew --prefix)/bin:$(bin/brew --prefix)/sbin:/usr/local/lib/node_modules:$ICE_HOME/bin:$PATH
export DYLD_LIBRARY_PATH=$ICE_HOME/lib:$ICE_HOME/python:${DYLD_LIBRARY_PATH-}


# Create database
if [ -d "$PSQL_DIR" ]; then
    rm -rf $PSQL_DIR
fi
bin/initdb $PSQL_DIR
bin/brew services restart postgresql
bin/pg_ctl -D $PSQL_DIR -l $PSQL_DIR/server.log start


# Set database
bin/omero config set omero.db.name omero_database
bin/omero config set omero.db.user db_user
bin/omero config set omero.db.pass db_password

# Create user and databse
bin/createuser -w -D -R -S db_user
bin/createdb -O db_user omero_database
bin/psql -h localhost -U db_user -l

bin/omero db script "" "" root_password -f $SCRIPT_NAME
bin/psql -h localhost -U db_user omero_database < $SCRIPT_NAME
rm $SCRIPT_NAME

# Set up the data directory
mkdir -p $OMERO_DATA_DIR
bin/omero config set omero.data.dir $OMERO_DATA_DIR

# Start the server
bin/omero admin start