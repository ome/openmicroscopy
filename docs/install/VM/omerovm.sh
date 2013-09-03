#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}

export MEMORY=${MEMORY:-"1024"}
export SSH_PF=${SSH_PF:-"2222"}

export OMERO_JOB=${OMERO_JOB:-"OMERO-stable-ice34"}
export OMERO_BASE_IMAGE=${OMERO_BASE_IMAGE:-"Debian-7.1.0-amd64-omerobase.vdi"}
export OMERO_POST_INSTALL_SCRIPTS=${OMERO_POST_INSTALL_SCRIPTS:-""}

export DELETE_BUILD_VM=${DELETE_BUILD_VM:-"1"}
export KILL_VBOX=${KILL_VBOX:-"1"}

set -e -u -x

VBOX="VBoxManage --nologo"
OS=`uname -s`
ATTEMPTS=0
MAXATTEMPTS=20
DELAY=60
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
	chmod 600 ./omerovmkey
        SSH_ARGS="-2 -o NoHostAuthenticationForLocalhost=yes -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -o CheckHostIP=no -o PasswordAuthentication=no -o ChallengeResponseAuthentication=no -o PreferredAuthentications=publickey -i omerovmkey"
        SCP="scp $SSH_ARGS -P $SSH_PF"
        SSH="ssh $SSH_ARGS -p $SSH_PF -t"

        echo "Copying scripts to VM"
        $SSH omero@localhost "mkdir install"
        $SCP \
            driver.sh \
            cleanup.sh \
            omero_guest_settings.sh \
            setup_environment.sh \
            setup_nginx.sh \
            setup_postgres.sh \
            setup_omero.sh \
            setup_omero_daemon.sh \
            omero-init.d \
            omero-web-init.d \
            no_processor_8266.sh \
            omero@localhost:install

        if [ -n "$OMERO_POST_INSTALL_SCRIPTS" ]; then
            $SSH omero@localhost "mkdir install/post"
            $SCP $OMERO_POST_INSTALL_SCRIPTS omero@localhost:install/post
        fi

        echo "ssh : exec driver.sh"
        $SSH omero@localhost "export OMERO_JOB=$OMERO_JOB; cd install; bash driver.sh"
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
    	$VBOX startvm "$VMNAME" --type headless && sleep 45
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

function checkbaseimage ()
{
    # Check the old locations in case anyone is still relying on them
    # TODO: Remove during the next refactoring?
    FILE="$OMERO_BASE_IMAGE"
    if [ ! -f "$FILE" ]; then
        FILE="$HOME/Library/VirtualBox/HardDisks/$OMERO_BASE_IMAGE"
    fi
    if [ ! -f "$FILE" ]; then
        FILE="$HOME/.VirtualBox/HardDisks/$OMERO_BASE_IMAGE"
    fi
    if [ ! -f "$FILE" ]; then
        echo "$OMERO_BASE_IMAGE not found, try specifying the full path"
        failfast
    fi

    export OMERO_BASE_IMAGE="$FILE"
}

function deletevm ()
{
    poweroffvm

    $VBOX list vms | grep "$VMNAME" && {
        # Try this first because it should delete everything including whereas
        # deleting the disks separately seems to leave some log files behind
        VBoxManage unregistervm "$VMNAME" --delete
        if [ $? -ne 0 ]; then
            VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium none
            VBoxManage unregistervm "$VMNAME" --delete
            VBoxManage closemedium disk "$VMNAME.vdi" --delete
        fi
    } || true
}

function createvm ()
{
		$VBOX list vms | grep "$VMNAME" || {
		VBoxManage clonehd "$OMERO_BASE_IMAGE" "$VMNAME.vdi"
		VBoxManage createvm --name "$VMNAME" --register --ostype "Debian"
		VBoxManage storagectl "$VMNAME" --name "SATA CONTROLLER" --add sata
		VBoxManage storageattach "$VMNAME" --storagectl "SATA CONTROLLER" --port 0 --device 0 --type hdd --medium "$VMNAME.vdi"
			
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

checkbaseimage

if [ "$KILL_VBOX" -eq 1 ]; then
    killallvbox
fi

deletevm

createvm

poweronvm

checknet

if [[ -z "$UP" ]]
then
	while [[ -z "$UP" && $ATTEMPTS -lt $MAXATTEMPTS ]]
	do
	    #rebootvm
	    sleep $DELAY
	    checknet
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
if [ "$DELETE_BUILD_VM" -eq 1 ]; then
    deletevm
fi
