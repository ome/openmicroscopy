#!/bin/bash

OMERO_VERSION=${OMERO_VERSION:-"$1"}
OMERO_VERSION=${OMERO_VERSION:-"OMERO4.2"}

OMERO_PATH=${OMERO_PATCH:-"$2"}
OMERO_PATH=${OMERO_PATCH:-"0"}

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

##
# Setup Java sources in Ubuntu
#
add_sources(){
    echo "$1" | sudo tee -a /etc/apt/sources.list
}
add_sources "deb http://archive.canonical.com/ubuntu lucid partner"
add_sources "deb-src http://archive.canonical.com/ubuntu lucid partner"

##
# Update core system and apt-cache
#
aptitude --assume-yes update
aptitude --assume-yes dist-upgrade

##
# Install Core applications
#
aptitude --assume-yes install build-essential
aptitude --assume-yes install subversion

##
# Intall Java
#
echo sun-java6-jdk shared/accepted-sun-dlj-v1-1 select true | /usr/bin/debconf-set-selections
echo sun-java6-jre shared/accepted-sun-dlj-v1-1 select true | /usr/bin/debconf-set-selections
aptitude --assume-yes install java-package sun-java6-bin sun-java6-demo sun-java6-fonts sun-java6-jdk sun-java6-plugin sun-java6-jre sun-java6-source
echo 'JAVA_HOME="/usr/lib/jvm/java-6-sun"' | sudo tee -a /etc/environment
echo 'LD_LIBRARY_PATH="/usr/lib/jvm/java-6-sun/jre/lib/amd64/server"' | sudo tee -a /etc/bash.bashrc
echo "export LD_LIBRARY_PATH" | sudo tee -a /etc/bash.bashrc


##
# Install Python
#
aptitude --assume-yes install cython python-numpy python-scipy python-setuptools python-numeric python-matplotlib python-wxgtk2.8 python-decorator python-mysqldb python-nose python-dev
apt-get --assume-yes install python-distutils-extra
apt-get --assume-yes install python-tables
apt-get --assume-yes install python-imaging

##
# Install Ice
#
apt-get --assume-yes install zeroc-ice33

##
# Install Postgres
#
apt-get --assume-yes install postgresql

##
# Install X and Gnome
#
apt-get --assume-yes install gdm
apt-get --assume-yes install xinit

##
# Install Apache, Django and webclient apps
#
apt-get --assume-yes install apache2
apt-get --assume-yes install python-django
apt-get --assume-yes install libapache2-mod-python

##
# Install OMERO
#


##
# Set up omero
#
mkdir -p $OMERODIR
mkdir -p /Client
mkdir -p /Server

chown -R omero $OMERODIR
chown -R omero /Client
chown -R omero /Server

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
        sudo -u omero svn up
        cd build
        sudo -u omero java build clean
    else
        sudo -u omero svn co http://svn.openmicroscopy.org.uk/svn/shoola/trunk /Client/Insight
        sudo -u omero java build
    fi
    cd $HOME

    ##
    # Check out OMERO
    #
    if test -e /Server/omero;
    then
        cd /Server/omero
        if test -e dist;
        then
            sudo -u omero dist/bin/omero admin stop
        fi
        sudo -u omero svn up
        sudo -u omero ./build.py clean
    else
        sudo -u omero svn co http://svn.openmicroscopy.org.uk/svn/omero/trunk /Server/omero
    fi
    cd /Server/omero
    sudo -u omero python build.py
    cd $HOME

    ##
    # Install CellProfiler
    #

    if test -e /Client/cellprofiler;
    then
        cd /Client/cellprofiler
        sudo -u omero svn up
    else
        yes "p" | sudo -u omero svn co https://svnrepos.broadinstitute.org/CellProfiler/trunk/CellProfiler/ /Client/cellprofiler
    fi

fi


##
# Set up server
#
cd /Server/omero/dist

sudo -u omero bin/omero config set omero.data.dir $OMERODIR
echo "# OMERODIR $OMERODIR vboxsf user=omero,rw" | sudo tee -a /etc/fstab

echo "CREATE USER $OMERO_DB_USER PASSWORD '$PGPASSWORD'" | sudo -u postgres psql
###
### FREEZES SOMEWHERE HERE
###
sudo -u postgres createdb -O  $OMERO_DB_USER $OMERO_DB_NAME && {
    sudo -u postgres createlang plpgsql $OMERO_DB_NAME
    sudo -u omero bin/omero config set omero.db.name $OMERO_DB_NAME
    sudo -u omero bin/omero config set omero.db.user $OMERO_DB_USER
    sudo -u omero bin/omero config set omero.db.pass $PGPASSWORD
    sudo -u omero bin/omero db script -f DB.sql $OMERO_VERSION $OMERO_PATCH $PGPASSWORD
    psql -h localhost -U $OMERO_DB_USER $OMERO_DB_NAME < DB.sql
} || echo DB Exists


##
# Set up apache
#
mkdir -p /Server/logs/weblog
chown -R www-data:www-data /Server/logs/weblog/
chmod -R 770 /Server/logs/weblog
cd /etc/apache2/sites-available
sudo sed 's/<\/VirtualHost>/\t<Location \/> \n\t\tSetHandler python-program\n\t\tPythonHandler django.core.handlers.modpython\n\t\tSetEnv DJANGO_SETTINGS_MODULE omeroweb.setting\n\t\tSetEnv MPLCONFIGDIR \/Server\/logs\/matplotlib \n\t\tPythonDebug On\n\t\tPythonPath "['\''\/Server\/omero\/dist\/lib\/python'\'', '\''\/Server\/omero\/dist\/var\/lib'\'', '\''\/Server\/omero\/dist\/lib\/python\/omeroweb'\''] + sys.path"\n\t<\/Location>\n<\/VirtualHost>/' < default > default

##
# Setup Matplotlib
#
mkdir -p /Server/logs/matplotlib
sudo echo "backend: Agg" > /Server/logs/matplotlib/matplotlibrc
sudo chown -R www-data:www-data /Server/logs/matplotlib/

##
# Setup Webclient
#
sudo -u omero mkdir -p /Server/omero/dist/var
sudo -u omero mkdir -p /Server/omero/dist/var/lib
sudo -u omero chmod +rx /Server/omero/dist/var/
cd /Server/omero/dist/var/lib

FILE=custom_settings.py
sudo -u omero cat > ${FILE} << EOF
# custom_settings.py
LOGDIR = '/Server/logs/weblog/'
SERVER_LIST = (
    ('localhost', 4064, 'omero'),
)

#ADMINS = (
#    ('username', 'emailaddress'),
#)

SERVER_EMAIL = 'omero@localhost'
EMAIL_HOST = 'localhost'

APPLICATION_HOST='http://localhost:8000/'

EMDB_USER = ('emdb', 'ome')
EOF

cd /Server/omero/dist
sudo -u omero bin/omero web syncmedia
sudo /etc/init.d/apache2 restart

