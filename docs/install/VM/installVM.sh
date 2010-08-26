#!/bin/bash
#
if [ -n $VMNAME ]
then 
    if [ -n $1 ]
    then
        export VMNAME="OMEROTEST"
    else
        export VMNAME=$1
    fi
fi
if [ -n $PASSWORD ]
then 
    if [ -n $2 ] 
    then
        export PASSWORD="ome"
    else
        export PASSWORD=$2
    fi
fi
if [ -n $SSH_PF ]
then
    if [ -n $3 ] 
    then
        export SSH_PF="2222"
    else
        export SSH_PF=$3
    fi
fi
VBoxManage controlvm "$VMNAME" poweroff
VBoxManage startvm "$VMNAME" --type headless
scp -P $SSH_PF ubuntu-install.sh omero@localhost:~/
scp -P $SSH_PF omero.sh omero@localhost:~/omero
scp -P $SSH_PF installDaemon.sh omero@localhost:~/
export DISPLAY=:0
export SSH_ASKPASS=`pwd`/fakepass.sh
ssh -l omero -p $SSH_PF localhost 'echo "$PASSWORD" | sudo -S sh /home/omero/ubuntu-install.sh'
ssh -l omero -p $SSH_PF localhost 'echo "$PASSWORD" | sudo -S sh /home/omero/installDaemon.sh'
