#!/bin/bash
if [ -n $OMERO_VERSION ]
then
    if [ -n $1 ] 
    then
        export OMERO_VERSION="OMERO4.2"
    else
        export OMERO_VERSION=$1
    fi
fi
if [ -n $OMERO_PATCH ]
then 
    if [ -n $2 ] 
    then
        export OMERO_PATCH="0"
    else
        export OMERO_PATCH=$2
    fi
fi
if [ -n $OMERO_DB_USER ]
then 
    if [ -n $3 ] 
    then
        export OMERO_DB_USER="omero"
    else
        export OMERO_DB_USER=$3
    fi
fi
if [ -n $OMERO_DB_NAME ]
then 
    if [ -n $4 ] 
    then
        export OMERO_DB_NAME="omero"
    else
        export OMERO_DB_NAME=$4
    fi
fi
if [ -n $PASSWORD ]
then 
    if [ -n $5 ] 
    then
        export PASSWORD="ome"
    else
        export PASSWORD=$5
    fi
fi
if [ -n $PGPASSWORD ]
then 
    export PGPASSWORD=$PASSWORD
fi
if [ -n $OMERODIR ]
then 
    if [ -n $6 ] 
    then
        export OMERODIR="/OMERO"
    else
        export OMERODIR=$6
    fi
fi
if [ -n $REVISION ]
then 
    if [ -n $6 ] 
    then
        export REVISION="HEAD"
    else
        export REVISION=$6
    fi
fi
##
# Setup Java sources in Ubuntu
#
echo "deb http://archive.canonical.com/ubuntu lucid partner" | sudo tee -a /etc/apt/sources.list
echo "deb-src http://archive.canonical.com/ubuntu lucid partner" | sudo tee -a /etc/apt/sources.list

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
mkdir /OMERO
mkdir /Client
mkdir /Server

##
# Checkout Insight
#
svn co http://svn.openmicroscopy.org.uk/svn/shoola/trunk /Client/Insight

##
# Check out OMERO
#
svn co http://svn.openmicroscopy.org.uk/svn/omero/trunk /Server/omero

chown -R omero $OMERODIR
chown -R omero /Client
chown -R omero /Server

##
# Build OMERO Server
# 
cd /Server/omero
sudo -u omero python build.py
sleep 10

##
# Set up server
#
cd dist
echo "CREATE USER $OMERO_DB_USER PASSWORD '$PGPASSWORD'" | sudo -u postgres psql 
sleep 10
###
### FREEZES SOMEWHERE HERE 
###
sudo -u postgres createdb -O  $OMERO_DB_USER $OMERO_DB_NAME
sudo -u postgres createlang plpgsql $OMERO_DB_NAME
sudo -u omero bin/omero config set omero.db.name $OMERO_DB_NAME
sudo -u omero bin/omero config set omero.db.user $OMERO_DB_USER
sudo -u omero bin/omero config set omero.db.pass $PGPASSWORD
sudo -u omero bin/omero config set omero.data.dir $OMERODIR
sudo -u omero bin/omero db script $OMERO_VERSION $OMERO_PATCH $PGPASSWORD
sleep 20
psql -h localhost -U omero omero < $OMERO_VERSION"__"$OMERO_PATCH".sql"
echo "# OMERODIR /OMERO vboxsf user=omero,rw" | sudo tee -a /etc/fstab


## 
# Set up apache
#
mkdir /Server/omero/logs/weblog
sudo chown -R www-data:www-data /Server/logs/weblog/
cd /etc/apache2/sites-available
sudo sed 's/<\/VirtualHost>/\t<Location \/> \n\t\tSetHandler python-program\n\t\tPythonHandler django.core.handlers.modpython\n\t\tSetEnv DJANGO_SETTINGS_MODULE omeroweb.setting\n\t\tSetEnv MPLCONFIGDIR \/Server\/logs\/matplotlib \n\t\tPythonDebug On\n\t\tPythonPath "['\''\/Server\/omero\/dist\/lib\/python'\'', '\''\/Server\/omero\/dist\/var\/lib'\'', '\''\/Server\/omero\/dist\/lib\/python\/omeroweb'\''] + sys.path"\n\t<\/Location>\n<\/VirtualHost>/' < default > default

##
# Setup Matplotlib 
#
mkdir /Server/logs/matplotlib
sudo echo "backend: Agg" > /Server/logs/matplotlib/matplotlibrc
sudo chown -R www-data:www-data /Server/logs/matplotlib/

##
# Setup Webclient
#
mkdir /Server/omero/dist/var
mkdir /Server/omero/dist/var/lib
chmod +rx /Server/omero/dist/var/
cd /Server/omero/dist/var/lib

FILE=custom_settings.py
cat > ${FILE} << EOF
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

##
# Install CellProfiler
#
yes "p" | sudo svn co https://svnrepos.broadinstitute.org/CellProfiler/trunk/CellProfiler/ /Client/cellprofiler

