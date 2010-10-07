#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"OMERO42"}

export SSH_PF=${SSH_PF:-"$2"}
export SSH_PF=${SSH_PF:-"2222"}

set -e
set -u
set -x

VBOX="VBoxManage --nologo"
SCP="scp -o StrictHostKeyChecking=no -i omerokey -P $SSH_PF"
SSH="ssh -o StrictHostKeyChecking=no -i omerokey -p $SSH_PF -t"

$VBOX list runningvms | grep "$VMNAME" || {
    # Under what conditions should we restart
    # echo "Stopping VM "
    # $VBOX controlvm "$VMNAME" poweroff && sleep 10
    echo "Starting VM "
    $VBOX startvm "$VMNAME" --type headless
    sleep 30
}


# In order to prompt password to connect to vm user must generate local RSA key by '$ ssh-keygen -t rsa'
# then copy them to the VM
#expect -c 'spawn ssh -p 2222 -t omero@localhost ls -al; expect assword ; send "ome\n" ; interact'
#expect -c 'spawn scp -P 2222 file omero@localhost:~/; expect assword; send "ome\n"; interact'

SCP_K="spawn scp -P $SSH_PF"
SSH_K="spawn ssh -p $SSH_PF -t"

[ -f omerokey.pub ] && {
    echo "Copying my RSA key"
    expect -c "$SCP_K omerokey.pub omero@localhost:~/; expect assword; send \"ome\n\"; interact"
    expect -c "$SCP_K setupkey.sh omero@localhost:~/; expect assword; send \"ome\n\"; interact"

    echo "Setup key"
    expect -c "$SSH_K omero@localhost sh /home/omero/setupkey.sh ; expect assword ; send \"ome\n\"; interact "
    
} || echo "Local RSAAuthentication key was not found. Use: $ ssh-keygen -t rsa"


echo "Copying ubuntu-root-install"
$SCP ubuntu-root-install.sh omero@localhost:~/
echo "Copying ubuntu-omero-install"
$SCP ubuntu-omero-install.sh omero@localhost:~/
echo "Copying omero.sh"
$SCP omero.sh omero@localhost:~/omero.sh
echo "Copying web-root-install and web-start.sh"
$SCP web-root-install.sh omero@localhost:~/
$SCP web-start.sh omero@localhost:~/
echo "Copying daemon-install.sh"
$SCP daemon-install.sh omero@localhost:~/
echo "ssh : exec ubuntu-root-install.sh"
$SSH omero@localhost 'yes ome | sudo -S sh /home/omero/ubuntu-root-install.sh'
echo "ssh : exec ubuntu-omero-install.sh"
$SSH omero@localhost 'sh /home/omero/ubuntu-omero-install.sh'
echo "ssh : exec web-root-install.sh"
$SSH omero@localhost 'yes ome | sudo -S bash -s < /home/omero/web-root-install.sh'
echo "ssh : exec daemon-install.sh"
$SSH omero@localhost 'yes ome | sudo -S sh /home/omero/daemon-install.sh'
