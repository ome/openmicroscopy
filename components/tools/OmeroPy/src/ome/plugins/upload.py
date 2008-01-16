
def do_upload(self, arg):
    """
    Syntax: upload <filename>
    Upload the given file name
    """
    client = self.client()
    client.upload(arg)

CLI.do_upload = do_upload
