#!/bin/bash

export VMNAME=${VMNAME:-"$1"}
export VMNAME=${VMNAME:-"omerovm"}

VBOX="VBoxManage --nologo"

set -e -u -x

$VBOX guestproperty enumerate $VMNAME | grep IP
