These scripts are used to generate the content at https://www.openmicroscopy.org/site/products/omero4/downloads

Regular usage steps consist of:

  * Push the tag and the branch to github
    git push origin v.4.4.4 develop

  * After the OMERO-trunk and OMERO-trunk-ice34
    builds pass, promote them with "RELEASE".
    This will transfer the files under
    omero/releases/OMERO-trunk*/<BUILDNUMBER>

  * Create a directory as an omedev with the
    Release number and chmod

  * Run prep.sh as hudson. This:

    * Rename the OVA to include the version number
        (e.g. 4.4.4) and be sure to modify the MD5
        file to use the new name.

    * Symlink OMERO-trunk/<BUILDNUMBER> to
        omero/4.4.4

  * Run gen.py 4.4.4 <BUILDNUMBER> and save
    the output as the download page on plone
