# INSTALL #

1) Install VirtualBox from http://www.virtualbox.org/wiki/Downloads
2) Copy vdi file from smb://squig.openmicroscopy.org.uk/ome/team/VM/OMERO-SSH.vdi to /YOUR_PATH/VirtualBox/HardDisks/OMERO-SSH.vdi
3) Check out scripts 'co svn+ssh://your_username@svn.openmicroscopy.org.uk/home/svn/omero/trunk/docs/install/VM installVM'
4) Run by the following commands:

cd installVM

run sh createVM.sh
run sh installVM.sh

5) If successful 

host: 'localhost'
port: '4064'
username: 'root'
password: 'ome'

OMERO.webadmin is available on http://localhost:8080/webadmin.
OMERO.web is available on http://localhost:8080/webclient


# Start/Stop OMERO #

1) To start please use the following command:

VBoxManage --nologo startvm SEPT1 --type headless

OMERO.webadmin is available on http://localhost:8080/webadmin. OMERO.web is available on http://localhost:8080/webclient

2) To stop please use the following command:

VBoxManage controlvm SEPT1 poweroff


# DOCUMENTATION #
If you want to modify user doc is here: https://www.openmicroscopy.org.uk/site/support/omero4/getting-started/demo