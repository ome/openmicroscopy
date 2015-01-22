#!/bin/bash

set -e -u -x

export PGPASSWORD=omero

FILE=.pgpass
sudo -u omero cat > ${FILE} << EOF
localhost:5432:omero:omero:omero
EOF
chmod 600 .pgpass
chown omero:omero .pgpass

echo "CREATE USER omero PASSWORD 'omero'" | sudo -u postgres psql
sudo -u postgres createdb -E UTF8 -O omero omero
sudo -u postgres createlang plpgsql omero || echo Already installed

echo `psql -h localhost -U omero -l`

sudo sed '/127.0.0.1/s/md5/trust/' /etc/postgresql/9.1/main/pg_hba.conf > pg_hba.conf && sudo mv pg_hba.conf /etc/postgresql/9.1/main/pg_hba.conf

sudo /etc/init.d/postgresql restart

echo `netstat -an | egrep '5432.*LISTEN'`
