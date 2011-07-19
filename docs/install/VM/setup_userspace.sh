#!/bin/bash

set -e -u -x

sudo -S echo "deb http://ftp.uk.debian.org/debian/ squeeze contrib" >> /etc/apt/sources.list
sudo -S echo "deb-src http://ftp.uk.debian.org/debian/ squeeze contrib" >> /etc/apt/sources.list
sudo -S echo "deb http://ftp.uk.debian.org/debian/ squeeze non-free" >> /etc/apt/sources.list
sudo -S echo "deb-src http://ftp.uk.debian.org/debian/ squeeze non-free" >> /etc/apt/sources.list

cat /etc/apt/sources.list

sudo -S aptitude --assume-yes update
sudo -S aptitude --assume-yes dist-upgrade
sudo -S aptitude --assume-yes install build-essential
sudo -S aptitude --assume-yes install openssh-server
sudo -S aptitude --assume-yes install bash
sudo -S aptitude --assume-yes install less
sudo -S aptitude --assume-yes install vim
sudo -S aptitude --assume-yes install unzip
sudo -S aptitude --assume-yes install bzip2
sudo -S aptitude --assume-yes install gzip
sudo -S aptitude --assume-yes install git
sudo -S echo sun-java6-jdk shared/accepted-sun-dlj-v1-1 select true | /usr/bin/debconf-set-selections
sudo -S echo sun-java6-jre shared/accepted-sun-dlj-v1-1 select true | /usr/bin/debconf-set-selections
sudo -S aptitude --assume-yes install java-package sun-java6-bin sun-java6-demo sun-java6-fonts sun-java6-jdk sun-java6-plugin s$
sudo -S aptitude --assume-yes install cython python-numpy python-scipy python-setuptools python-matplotlib python$
sudo -S aptitude --assume-yes install python-distutils-extra
sudo -S aptitude --assume-yes install python-tables
sudo -S aptitude --assume-yes install python-imaging
sudo -S aptitude --assume-yes install zeroc-ice33
sudo -S aptitude --assume-yes install postgresql

export PGPASSWORD=omero

FILE=.pgpass
sudo -u omero cat > ${FILE} << EOF
localhost:5432:omero:omero:omero
EOF
chmod 600 .pgpass

echo "CREATE USER omero PASSWORD 'omero'" | sudo -u postgres psql
sudo -u postgres createdb -O omero omero
sudo -u postgres createlang plpgsql omero

echo `psql -h localhost -U omero -l`

sudo sed '/127.0.0.1/s/md5/trust/' /etc/postgresql/8.4/main/pg_hba.conf > pg_hba.conf && sudo mv pg_hba.conf /etc/postgresql/8.4/main/pg_hba.conf

sudo /etc/init.d/postgresql restart

echo `netstat -an | egrep '5432.*LISTEN'`
