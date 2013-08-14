#!/bin/bash

set -e -u -x

export PGPASSWORD=omero
echo localhost:5432:omero:omero:$PGPASSWORD > ~/.pgpass
chmod 600 ~/.pgpass

echo "CREATE USER omero PASSWORD '${PGPASSWORD}'" | sudo -u postgres psql
sudo -u postgres createdb -O omero omero

# Might be setup by default
set +e
sudo -u postgres createlang -l omero | grep '[^\w]plpgsql[^w]' > /dev/null
RET=$?
set -e
if [ $RET -ne 0 ]; then
	sudo -u postgres createlang plpgsql omero
fi

echo `psql -h localhost -U omero -l`

echo `netstat -an | egrep '5432.*LISTEN'`
