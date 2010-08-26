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
echo "deb http://archive.canonical.com/ubuntu lucid partner" | sudo tee -a /etc/apt/sources.list
echo "deb-src http://archive.canonical.com/ubuntu lucid partner" | sudo tee -a /etc/apt/sources.list
aptitude --assume-yes update
aptitude --assume-yes dist-upgrade
aptitude --assume-yes install build-essential
aptitude --assume-yes install subversion
echo sun-java6-jdk shared/accepted-sun-dlj-v1-1 select true | /usr/bin/debconf-set-selections
echo sun-java6-jre shared/accepted-sun-dlj-v1-1 select true | /usr/bin/debconf-set-selections
aptitude --assume-yes install java-package sun-java6-bin sun-java6-demo sun-java6-fonts sun-java6-jdk sun-java6-plugin sun-java6-jre sun-java6-source
echo 'JAVA_HOME="/usr/lib/jvm/java-6-sun"' | sudo tee -a /etc/environment
echo 'LD_LIBRARY_PATH="/usr/lib/jvm/java-6-sun/jre/lib/amd64/server"' | sudo tee -a /etc/bash.bashrc
echo "export LD_LIBRARY_PATH" | sudo tee -a /etc/bash.bashrc
aptitude --assume-yes install cython python-numpy python-scipy python-setuptools python-numeric python-matplotlib python-wxgtk2.8 python-decorator python-mysqldb python-nose python-dev
apt-get --assume-yes install python-distutils-extra
apt-get --assume-yes install zeroc-ice33
apt-get --assume-yes install postgresql
apt-get --assume-yes install gdm
apt-get --assume-yes install xinit
mkdir /OMERO
mkdir /Client
mkdir /Server
yes "p" | sudo svn co https://svnrepos.broadinstitute.org/CellProfiler/trunk/CellProfiler/ /Client/cellprofiler
svn co http://svn.openmicroscopy.org.uk/svn/omero/trunk /Server/omero
svn co http://svn.openmicroscopy.org.uk/svn/shoola/trunk /Client/Insight
chown -R omero $OMERODIR
chown -R omero /Client
chown -R omero /Server
cd /Server/omero
sudo -u omero python build.py
sleep 10
cd dist
echo "CREATE USER $OMERO_DB_USER PASSWORD '$PGPASSWORD'" | sudo -u postgres psql 
sleep 10
sudo -u postgres createdb -O  $OMERO_DB_USER $OMERO_DB_NAME
sudo -u postgres createlang plpgsql $OMERO_DB_NAME
sudo -u omero bin/omero config set omero.db.name $OMERO_DB_NAME
sudo -u omero bin/omero config set omero.db.user $OMERO_DB_USER
sudo -u omero bin/omero config set omero.db.pass $PGPASSWORD
sudo -u omero bin/omero config set omero.data.dir $OMERODIR
sudo -u omero bin/omero db script $OMERO_VERSION $OMERO_PATCH $PGPASSWORD
psql -h localhost -U omero omero < $OMERO_VERSION"__"$OMERO_PATCH".sql"
echo "# OMERODIR /OMERO vboxsf user=omero,rw" | sudo tee -a /etc/fstab

#webclient
cd var
mkdir lib
FILE=custom_settings.py
cat > ${FILE} << EOF
# custom_settings.py

SERVER_LIST = (
    ('localhost', 4064, 'omero'),
)

#ADMINS = (
#    ('Aleksandra Tarkowska', 'A.Tarkowska@dundee.ac.uk'),
#)

SERVER_EMAIL = 'omero@localhost'
EMAIL_HOST = 'localhost'

APPLICATION_HOST='http://localhost:8000/' 

EMDB_USER = ('emdb', 'ome')
EOF

sudo -u omero bin/omero web syncmedia
