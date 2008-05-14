README (OMERO Import Test Client)

Last Updated: December 5th, 2006

Please note that portions of this software are copyrighted under the GNU Lesser General Public License. Details for this license can be found at in the lglp.txt file included with this software, or from http://www.gnu.org/licenses/lgpl.html.

-----

The current implementation of the OMERO import engine is a client side application which allows you to import microscopy imaging data into an OMERO server environment. Technically, the client application will allow you to import any file format into OMERO, however in its current state, only DeltaVision (.dv) and Metamorph (.stk), and single plane Tiff (.tif) files are supported.

Please note that this application has limited functionality and is for testing purposes only. While it has been tested in a few live environments, it is by no means well tested, and at some point you will probably find a bug which is unaccounted for - Caveat Emptor!

To use this applicaton, you must have access to an existing and fully functional OMERO server, and you must know your username, password, URL, and port for logging into it. After logging into the importer, files can be imported by going to the "file viewer" tab, locating your .dv/.stk file(s) for import and adding them to the queue (which will prompt you to choose an OMERO project/dataset to import them into). By selecting more then one file in the file chooser you may also add multiple files to the queue at the same time. Once you are happy with your queue, click import. After the import has started, you may continue to add files to the import queue, (but you cannot delete them). Once the import is started, you will need to click the import button again to restart the import for any new files.

Please note: this software requires that you use Java version 1.5.0 or higher.

-----

NEW FEATURES:

(Changeset Version 1115)
- Fixed a few minor bugs to improve UI usability

(Changeset Version 1095)
- Added a debug messaging system that allows you to send us bugs when the importer crashes.
- Added a comment feedback system that allows you to send us comments from the help menu.
- The import dialog now memorizes what options you select for file naming (so you don't have to keep changing it)
- The archive feature has been temporarily disabled until archive file retrieval is added to Shoola
- Fixed a login bug that didn't allow you to log out and back in correctly
- Fixed an import bug that didn't allow you to import the same image twice.

(Changeset Version 1072)
- In anticipation of our RC1 release, this build addresses a number of minor bug fixes and improves on feedback to the user when or if they run into trouble. Expect to see more popup messages and the like when something goes wrong.

(Changeset Version 1066)
- Added the abilty during import to limit the length of the file path. You may now choose to use the single file name (such as "myimage.dv"), the full file path (such as "\users\swedlow\images\myimage.dv") or any combination of file name + partial path (such as "\images\myimage.dv" -- leaving out the "\users\swedlow" at the front).
- You can now archive the original file on the server during the import process by selecting the archive file option (this is turned on by default). NOTE: The shoola client does not handle file retrieval yet, so HANG ONTO YOUR ORIGINAL FILES!

-----

KNOWN BUGS: 
(Changeset Version 1072)
- There is a bug when importing an image twice. To fix this problem, shut down the importer before attempting the second import. If you do not, the image will overwrite the first and you will only see one image imported into OMERO.

(Changeset Version 1066)
- No known bugs *crosses fingers*

(Changeset Version 1048)
 - Added support for single plane tiff images. NOTE: You CANNOT currently import multi-plane tiffs as this will trigger a debug exception and stop the importer (if this occurs you will need to restart the application).

(Changeset Version 1008)
 - While importing, you cannot delete files from the file queue
 - For very large files, there is a small pause just before the import begins that could be mistaken as the importing hanging. Give it time and it should start importing after a few moments.
 - Currently, nothing is stopping you from shutting down the importer in the middle of an import will. This will result in an incomplete import. Don't do this!

(Changeset Version 900)
 - Changed the order of the buttons used by the import dialog to reflect the order used in the directory chooser
 - Stripped out any unused file formats (now only displays .dv and .stk files).
 - The file chooser now memorizes the last directory you used, (or defaults to your home directory).
 - Added funcionality to handle a rounding error in GobalMin and GlobalMax
 - Updated the functionality of the login/logoff dialog. This dialog can now be dismissed in case of an error so you may view the application windows below.
 - The application now correctly handles errors in the login process and allows you to relog in by selecting the File/Login option.
 - You may now logoff from the File/Logoff menu option without exiting the application.

(Changeset Version 893)

 - There have been a number of UI and functional changes to the importer. The most important addition is the ability to import metamorph .STK files.
 - The login bug from the pervious version has also been fixed. You can now log in after a login error without restarting the application.


(Changeset Version 818)

 - Currently, there is no way to create a new dataset from the test client, so your OMERO server must already have a dataset in place for you to import.
 - Due to an server-side implementation bug, you will have to restart the test client if you fail to log into the server and want to try to log in again.

