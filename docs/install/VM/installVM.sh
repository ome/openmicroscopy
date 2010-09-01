#!/bin/bash
#
if [ -n $VMNAME ]
then 
    if [ -n $1 ]
    then
        export VMNAME="SEPT1"
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
echo "Stopping VM " 
VBoxManage controlvm "$VMNAME" poweroff
sleep 10
echo "Starting VM " 
VBoxManage startvm "$VMNAME" --type headless
sleep 30
echo "Copying ubuntu-install" 
scp -P $SSH_PF ubuntu-install.sh omero@localhost:~/
echo "Copying omero.sh" 
scp -P $SSH_PF omero.sh omero@localhost:~/omero
echo "Copying installDaemon.sh" 
scp -P $SSH_PF installDaemon.sh omero@localhost:~/
echo "Copying ubuntu-install" 
export DISPLAY=:0
export SSH_ASKPASS=`pwd`/fakepass.sh
echo "ssh : exec ubuntu-install.sh"
ssh -l omero -p $SSH_PF localhost 'echo "$PASSWORD" | sudo -S sh /home/omero/ubuntu-install.sh'
echo "ssh : exec installDaemon.sh"
ssh -l omero -p $SSH_PF localhost 'echo "$PASSWORD" | sudo -S sh /home/omero/installDaemon.sh'
