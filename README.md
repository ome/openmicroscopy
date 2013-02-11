OMERO Release
=============

These scripts are used to generate the content at
https://www.openmicroscopy.org/site/products/omero4/downloads
for each release.

Preparation:
------------

  * Merge all PRs related to the current release
    (here, e.g. 4.4.4)

  * Check out origin/develop and merge any modified
    submodules. See merge_all.sh

  * Tag: ``git tag -u "Josh Moore (Glencoe Software, Inc.) <you@example.com>" -m "Release version 4.4.4" v.4.4.4``

  * Push the tag and the branch to github
    git push origin v.4.4.4 develop

Regular usage steps consist of:
-------------------------------

  * After the OMERO-trunk and OMERO-trunk-ice34
    builds pass, promote them with "RELEASE".
    This will transfer the files under
    omero/releases/OMERO-trunk*/<BUILDNUMBER>

  * Create a directory as an omedev with the
    release number and chmod a+w

  * Run prep.sh as hudson. This performs the following:

     - Rename the OVA to include the version number
         (e.g. 4.4.4) and be sure to modify the MD5
         file to use the new name.

     - Symlink OMERO-trunk/<BUILDNUMBER> to
         omero/4.4.4

  * Manually copy OMERO-4.4.6.pdf into release dir.

  * Run gen.py 4.4.4 <BUILDNUMBER> and save
    the output as the download page on plone.
    If testing (i.e. staging) use:
    ``STAGING=1 ./gen.py 4.4.4 b3097 | mail you@example.com``

These instructions should be unified with ReleaseProcess.

Bio-Formats Release
===================

Similar to the OMERO release, a BF release requires:

 * Copying all the artifacts to ../bioformats/VERSION/

 * Then executing `./bfgen.py VERSION` from inside of the **omero** directory.

 * STAGING and other flags are respected.
