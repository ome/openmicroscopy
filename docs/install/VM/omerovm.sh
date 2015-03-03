#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}

export MEMORY=${MEMORY:-"1024"}
export SSH_PF=${SSH_PF:-"2222"}
export OMERO_PORT=${OMERO_PORT:-"4063"}
export OMERO_PF=${OMERO_PF:-"4063"}
export OMEROS_PORT=${OMEROS_PORT:-"4064"}
export OMEROS_PF=${OMEROS_PF:-"4064"}
export OMERO_JOB=${OMERO_JOB:-"OMERO-stable"}

set -e
set -u
set -x

VBOX="VBoxManage --nologo"
OS=`uname -s`
ATTEMPTS=0
MAXATTEMPTS=5
DELAY=2
NATADDR="10.0.2.15"

##################
##################
# SCRIPT FUNCTIONS
##################
##################

function checknet ()
{
	UP=$($VBOX guestproperty enumerate $VMNAME | grep "10.0.2.15") || true
	ATTEMPTS=$(($ATTEMPTS + 1))
}

function installvm ()
{
	ssh-keygen -R [localhost]:2222 -f ~/.ssh/known_hosts
	chmod 600 ./omerovmkey
	SCP="scp -2 -o NoHostAuthenticationForLocalhost=yes -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -o CheckHostIP=no -o PasswordAuthentication=no -o ChallengeResponseAuthentication=no -o PreferredAuthentications=publickey -i omerovmkey -P $SSH_PF"
	SSH="ssh -2 -o StrictHostKeyChecking=no -i omerovmkey -p $SSH_PF -t"
	echo "Copying scripts to VM"
	$SCP ../../../target/OMERO.server*zip omero@localhost:~/
	$SCP driver.sh omero@localhost:~/
	$SCP setup_userspace.sh omero@localhost:~/
	$SCP setup_postgres.sh omero@localhost:~/
	$SCP setup_environment.sh omero@localhost:~/
	$SCP setup_omero.sh omero@localhost:~/
	$SCP setup_nginx.sh omero@localhost:~/
	$SCP setup_omero_daemon.sh omero@localhost:~/
	$SCP omero-init.d omero@localhost:~/
	$SCP omero-web-init.d omero@localhost:~/
	$SCP virtualbox-network-fix-init.d omero@localhost:~/
  $SCP virtualbox_fix.sh omero@localhost:~/
  $SCP nginx-control.sh omero@localhost:~/
	echo "ssh : exec driver.sh"
	$SSH omero@localhost "export OMERO_JOB=$OMERO_JOB; bash /home/omero/driver.sh"
	sleep 10
	
	echo "ALL DONE!"
}

function failfast ()
{
	exit 1
}

function poweroffvm ()
{
	$VBOX list runningvms | grep "$VMNAME" && {
			VBoxManage controlvm "$VMNAME" poweroff && sleep 10
	} || true
}

function poweronvm ()
{
	$VBOX list runningvms | grep "$VMNAME" || {
	$VBOX startvm "$VMNAME" --type headless && sleep 60
	}
}

function rebootvm ()
{
	poweroffvm
	poweronvm
}

function killallvbox ()
{
	ps aux | grep [V]Box && {
	
		if [ "$OS" == "Darwin" ]; then
			killall -m [V]Box
		else [ "$OS" == "Linux" ];
			killall -r [V]Box
		fi
	} || true
	
	ps aux | grep [V]irtualBox && {
	
		if [ "$OS" == "Darwin" ]; then
			killall -m [V]irtualBox
		else [ "$OS" == "Linux" ];
			killall -r [V]irtualBox
		fi
	} || true
}

function checkhddfolder ()
{
	if test -e $HOME/Library/VirtualBox; then
	    export HARDDISKS=${HARDDISKS:-"$HOME/Library/VirtualBox/HardDisks/"}
	elif test -e $HOME/.VirtualBox; then
	    export HARDDISKS=${HARDDISKS:-"$HOME/.VirtualBox/HardDisks/"}
	else
	    echo "Cannot find harddisks! Trying setting HARDDISKS"
	    failfast
	fi
}

function deletevm ()
{
	poweroffvm
	
	$VBOX list vms | grep "$VMNAME" && {
		VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium none
		VBoxManage unregistervm "$VMNAME" --delete
		VBoxManage closemedium disk $HARDDISKS"$VMNAME".vdi --delete
	} || true
}

function createvm ()
{
		$VBOX list vms | grep "$VMNAME" || {
		VBoxManage clonehd "$OMERO_BASE_IMAGE" "$HARDDISKS$VMNAME.vdi"
		VBoxManage createvm --name "$VMNAME" --register --ostype "Ubuntu_64"
		VBoxManage storagectl "$VMNAME" --name "SATA CONTROLLER" --add sata
		VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium $HARDDISKS$VMNAME.vdi
			
		VBoxManage modifyvm "$VMNAME" --nic1 nat --nictype1 "82545EM"
		VBoxManage modifyvm "$VMNAME" --memory $MEMORY --acpi on
	
		VBoxManage modifyvm "$VMNAME" --natpf1 "ssh,tcp,127.0.0.1,2222,10.0.2.15,22"
		VBoxManage modifyvm "$VMNAME" --natpf1 "omero-unsec,tcp,127.0.0.1,4063,10.0.2.15,4063"
		VBoxManage modifyvm "$VMNAME" --natpf1 "omero-ssl,tcp,127.0.0.1,4064,10.0.2.15,4064"
		VBoxManage modifyvm "$VMNAME" --natpf1 "omero-web,tcp,127.0.0.1,8080,10.0.2.15,8080"
	}
}

####################
####################
# SCRIPT ENTRY POINT
####################
####################

checkhddfolder

killallvbox

deletevm

createvm

poweronvm

checknet

if [[ -z "$UP" ]]
then
	while [[ -z "$UP" && $ATTEMPTS -lt $MAXATTEMPTS ]]
	do
		rebootvm
	    checknet
	    sleep $DELAY
	done
	if [[ -z "$UP" ]]
	then
    	echo "No connection to x. Failure after $ATTEMPTS tries"
    	failfast
    fi
fi

echo "Network up after $ATTEMPTS tries"
installvm

bash export_ova.sh ${VMNAME}
