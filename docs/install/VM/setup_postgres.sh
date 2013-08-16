#!/bin/bash

set -e -u -x

# hostname:port:database:username:password
echo "localhost:5432:*:$OMERO_DB_USER:$OMERO_DB_PASS" > ~/.pgpass
chmod 600 ~/.pgpass

echo "CREATE USER $OMERO_DB_USER PASSWORD '$OMERO_DB_PASS'" | \
    sudo -u postgres psql
sudo -u postgres createdb -O "$OMERO_DB_USER" "$OMERO_DB_NAME"

# Might be setup by default
set +e
sudo -u postgres createlang -l "$OMERO_DB_NAME" | grep '[^\w]plpgsql[^w]' > /dev/null
RET=$?
set -e
if [ $RET -ne 0 ]; then
	sudo -u postgres createlang plpgsql "$OMERO_DB_NAME"
fi

echo `psql -h localhost -U "$OMERO_DB_USER" -l`

echo `netstat -an | egrep '5432.*LISTEN'`
