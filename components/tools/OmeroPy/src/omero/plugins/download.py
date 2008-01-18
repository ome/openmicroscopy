
def do_download(self, arg):
    """
    Syntax: download <id> <filename>
    Download the given file id to the given file name
    """
    
    id = 1
    file = "foo"

    client = self.client()
    session = client.getSession()
    filePrx = session.createRawFileStore()
    filePrx.setFileId(id)
    fileSize = filePrx.getSize()
    
    

CLI.do_download = do_download
