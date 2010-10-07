#!/bin/bash

OMERO_VERSION=${OMERO_VERSION:-"$1"}
OMERO_VERSION=${OMERO_VERSION:-"OMERO4.2"}

OMERO_PATCH=${OMERO_PATCH:-"$2"}
OMERO_PATCH=${OMERO_PATCH:-"0"}

OMERO_DB_USER=${OMERO_DB_USER:-"$3"}
OMERO_DB_USER=${OMERO_DB_USER:-"omero"}

OMERO_DB_NAME=${OMERO_DB_NAME:-"$4"}
OMERO_DB_NAME=${OMERO_DB_NAME:-"omero"}

PASSWORD=${PASSWORD:-"$5"}
PASSWORD=${PASSWORD:-"ome"}

PGPASSWORD=${PGPASSWORD:-"$PASSWORD"}

OMERODIR=${OMERODIR:-"$6"}
OMERODIR=${OMERODIR:-"/OMERO"}

REVISION=${REVISION:-"$7"}
REVISION=${REVISION:-"HEAD"}

set -e
set -u
set -x

# If NOBUILD exists, then we don't refresh
# the OMERO installations in the guest,
# assuming they are still valid.
if test ! -e "$HOME/NOBUILD";
then

    ##
    # Checkout Insight
    #
    if test -e /Client/Insight;
    then
        cd /Client/Insight
        svn up
        cd build
        java build clean
    else
        svn co http://svn.openmicroscopy.org.uk/svn/shoola/trunk /Client/Insight
    fi
    cd /Client/Insight/build
    java build
    cd $HOME

    ##
    # Check out OMERO
    #
    if test -e /Server/omero;
    then
        cd /Server/omero
        if test -e dist;
        then
            dist/bin/omero admin stop
        fi
        svn up
        ./build.py clean
    else
        svn co http://svn.openmicroscopy.org.uk/svn/omero/trunk /Server/omero
    fi
    cd /Server/omero
    python build.py
    cd $HOME

    ##
    # Install CellProfiler
    #

    if test -e /Client/cellprofiler;
    then
        cd /Client/cellprofiler
        svn up
    else
        yes "p" | svn co https://svnrepos.broadinstitute.org/CellProfiler/trunk/CellProfiler/ /Client/cellprofiler
    fi

fi


##
# Set up server
#

#cd /home/omero
#FILE=.pgpass
#sudo -u omero cat > ${FILE} << EOF
#localhost:5432:$OMERO_DB_NAME:$OMERO_DB_USER:$PGPASSWORD
#EOF

cd /Server/omero/dist

bin/omero config set omero.data.dir $OMERODIR

export PGPASSWORD=$PGPASSWORD

echo "CREATE USER $OMERO_DB_USER PASSWORD '$PGPASSWORD'" | sudo -u postgres psql

###
### FREEZES SOMEWHERE HERE
###

sudo -u postgres createdb -O  $OMERO_DB_USER $OMERO_DB_NAME && {
    sudo -u postgres createlang plpgsql $OMERO_DB_NAME
    bin/omero config set omero.db.name $OMERO_DB_NAME
    bin/omero config set omero.db.user $OMERO_DB_USER
    bin/omero config set omero.db.pass $PGPASSWORD
    bin/omero db script -f DB.sql $OMERO_VERSION $OMERO_PATCH $PGPASSWORD
    psql -h localhost -U $OMERO_DB_USER $OMERO_DB_NAME < DB.sql
} || echo DB Exists

bin/omero admin start
