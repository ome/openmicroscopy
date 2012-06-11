The two *.blob files were created by the main method of BlobShareStore
using different Ice jar versions (3.3.1 and 3.4.2). These are read in
by the BlobShareStore test to make sure that we remain compatible.
