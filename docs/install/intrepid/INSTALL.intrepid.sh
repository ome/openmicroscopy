#
# Kindly provided by Martin Kielhorn
# http://lists.openmicroscopy.org.uk/pipermail/ome-users/2009-May/001762.html
#
sudo apt-get install postgresql zeroc-ice33

sudo -u postgres createuser -P -D -R -S omero
sudo -u postgres createdb -O omero omero             # passwd set to omero
sudo -u postgres createlang plpgsql omero
mkdir /sda5/omero-data
chown -R martin /sda5/omero-data/
cd /sda5/omero
wget http://cvs.openmicroscopy.org.uk/snapshots/omero/omero-Beta4.0.3.tar.bz2
tar xjf omero-Beta4.0.3.tar.bz2
cd omero_dist
bin/omero config set omero.data.dir /sda5/omero-data
bin/omero db script
# set password manually
psql -h localhost -U omero omero < OMERO4__0.sql
bin/omero admin start
bin/omero admin ice
server enable Web
server start Web
exit
