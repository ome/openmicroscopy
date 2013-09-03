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
	http://www.openmicroscopy.org/site/support/omero4/users/virtual-appliance.html)
	
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


- [setup_port_forwarding.sh](setup_port_forwarding.sh) - This should already be setup, if these settings are lost you can run:

  ```
  $ bash setup_port_forwarding.sh omero-vm
  ```


Rebuilding the Base Image
=========================

The base image is created using [Veewee](https://github.com/jedi4ever/veewee).
To rebuild the base image from scratch install [RVM](https://rvm.io/rvm/install)
or [rbenv](https://github.com/sstephenson/rbenv) and activate Ruby 1.9.2
(later versions may also work).
For example:

        # Install RVM to ~/.rvm
        curl -L https://get.rvm.io | bash -s stable
        # Enable rvm
        source ~/.rvm/scripts/rvm
        rvm install 1.9.2

Clone the `ome-veewee` repository:

        git clone https://github.com/manics/ome-veewee.git

And run

        ./build_base_image.sh Debian-7.1.0-amd64-omerobase

Read the comments in
[`build_base_image.sh`](https://github.com/manics/ome-veewee/blob/master/build_base_image.sh)
for more information.

Note that Veewee doesn't return a error code if it fails, so it is necessary to
either parse the output of Veewee, or to check the expected outputs have been
created or deleted.

Creating a new base image
=========================

Note if you want to use the development version of Veewee follow the
[Veewee installation instructions](https://github.com/jedi4ever/veewee/blob/master/README.md)
and copy or symlink the `definitions` directory from
[`ome-veewee`](https://github.com/manics/ome-veewee.git).
Depending on how your Ruby/Veewee environment is setup you may have to run
`bundle exec veewee` instead of `veewee`.

To create a new definition list the existing templates:

        veewee vbox templates

Select a template (this will create a copy under `definitions`):

        veewee vbox define NEW_BOX_NAME TEMPLATE_NAME

Edit the files under `definitions/NEW_BOX_NAME` to change how the base image is
created.
Finally build the image:

        veewee vbox build NEW_BOX_NAME --nogui

At this point you can explore the built VM to help with testing or debugging
(obviously avoid this when creating the final VM):

        veewee vbox ssh NEW_BOX_NAME

Finally shutdown the VM:

        veewee vbox halt NEW_BOX_NAME

And clone/copy the hard disk image, which should be under your VirtualBox
directory, for example `$HOME/VirtualBox VMs/NEW_BOX_NAME/NEW_BOX_NAME.vdi`.


Common errors
=============

VBoxManage: error: Cannot register the hard disk ... already exists
-------------------------------------------------------------------

Every VirtualBox disk image (VDI) contains a unique UUID.
If a VDI file is copied and the original VDI is still present and registered to
VirtualBox on the same machine you may see this error when you try to use the
copied VDI. To avoid this use `VBoxManage clonehd` instead of copy, or delete
the original virtual machine.
The build script occasionally fails to delete a virtual machine, leading to
this error.
If this occurs you will need to manually delete the Virtual Machine.

Network timeouts or no connection errors
----------------------------------------

VirtualBox occasionally fails to bring up the guest network for unknown reasons.
[`omerovm.sh`](omerovm.sh) attempts to handle this by waiting and retrying.
If this doesn't work there isn't much you can do other than retrying.

Corrupt files in the VirtualBox image
-------------------------------------

Occasionally file corruption occurs, which can cause startup failures in
PostgreSQL or OMERO.
The underlying cause is unclear, one possibility is that `VBoxManage` exits
before its worker programs such as `VBoxHeadless` has finished writing the disk
image.
Time delays have been inserted into [`omerovm.sh`](omerovm.sh) to try to work
around this.
If this continues to be a problem it may be worth explicitly checking whether
`VBoxHeadless` has exited.
See also
[`build_base_image.sh`](https://github.com/manics/ome-veewee/blob/master/build_base_image.sh).

