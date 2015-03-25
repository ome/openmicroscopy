Building an OMERO Virtual Appliance
===================================

The purpose of these scripts is to automate the process of building an OMERO 
virtual machine.

1. Prerequisites:
	- A Linux or Mac OS X operating system
	- A recent and working VirtualBox install

2. Retrieve the OMERO VM scripts:
	1. Using Git:

		``` 
		$ git archive --remote=git://git.openmicroscopy.org/ome.git 
		--format=tar HEAD:docs/install/VM > OMERO_VM.tar
		```

	2. If you have ssh access to the OMERO git repo then you can also use git as follows, replacing `USERNAME` with your user name as required:

		```
		$ git archive --remote=ssh://USERNAME@git.openmicroscopy.org/home/git/ome.git 
		--format=tar HEAD:docs/install/VM > OMERO_VM.tar
		```

	3.	Via HTTP by downloading the [scripts](../VM) manually and saving ALL of the contents of that directory to a suitable location on your machine. Note this is the least desirable option and requires more work on your part.
	
3. Environment Setup:

	See the [virtual appliance user documentation](
	http://www.openmicroscopy.org/site/support/omero5.1/users/virtual-appliance.html)
	
	Place the VDI in your VirtualBox harddisk directory. On Mac OS X this should be `$HOME/Library/VirtualBox/HardDisks/` and on Linux `$HOME/.VirtualBox/HardDisks/`
	
	The OMERO.VM script will look in these locations for the VDI to kick start the VM building process.

4. Run the VM build script to build a VM named omero-vm:

	```
	$ bash omerovm.sh omero-vm
	```

5. This should take roughly 8-15 minutes to complete depending upon your machine.

6. When you see the message "All Done!" you should be able to either:
 	1. Start an OMERO client such as OMERO.insight and connect to your VM
 	2. SSH into your VM using the IP address printed at the end of the build script
	3. Use the utility script connect.sh to open a shell into your vm named omero-vm:

		```
		$ bash connect.sh omero-vm
		```

Utility Scripts
===============

A number of utility scripts are included to enable you to easily start, stop, and connect to your VM:

- [connect.sh](connect.sh): To get a shell on a named OMERO VM, supplying the name of the VM that you wish to connect to:

  ```
  $ bash connect.sh omero-vm
  ```

- [start.sh](start.sh) - To start a stopped OMERO VM, supply the name of VM you wish to run:

  ```
  $ bash start.sh omero-vm
  ```

- [stop.sh](stop.sh) - To stop a started OMERO VM, supply the name of VM you wish to halt:

  ```
  $ bash stop.sh omero-vm
  ```


- [setup_port_forwarding.sh](setup_port_forwarding.sh) - To automatically set up port forwarding settings for the nominated VM in VirtualBox: 

  ```
  $ bash setup_port_forwarding.sh omero-vm
  ```
