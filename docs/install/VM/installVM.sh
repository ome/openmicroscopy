#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"SEPT1"}

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
