README (OMERO Import Test Client)

Last Updated: July 31st, 2006

Please note that portions of this software are copyrighted under the GNU Lesser General Public License. Details for this license can be found at in the lglp.txt file included with this software, or from http://www.gnu.org/licenses/lgpl.html.

-----

The current implementation of the OMERO import engine is a client side application which allows you to import microscopy imaging data into an OMERO server environment. Technically, the client application will allow you to import any file format into OMERO, however in its current state, only DeltaVision (.dv) files are supported.

Please note that this application has limited functionality and is for testing purposes only. While it has been tested in a few live environments, it is by no means well tested, and at some point you will probably find a bug which is unaccounted for - Caveat Emptor!

To use this applicaton, you must have access to an existing and fully functional OMERO server, and you must know your username, password, URL, and port for logging into it. After entering this data into the test client, files may be imported into OMERO by selecting the import option into the client, locating your .dv file for import, and choosing a OMERO dataset to import them into. By selecting more then one file in the file chooser you may also import multiple files at the same time.

-----

KNOWN BUGS: 

(Changeset Version 818)

 - Currently, there is no way to create a new dataset from the test client, so your OMERO server must already have a dataset in place for you to import.
 - Due to an server-side implementation bug, you will have to restart the test client if you fail to log into the server and want to try to log in again.

