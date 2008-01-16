import shlex

def do_upload(self, arg):
    """
    Syntax: upload <filename>
    Upload the given file name
    """

    files = shlex.split(arg)
    print arg + " = " + str(files)
    client = self.client()
    for f in files:
        obj = client.upload(f)
        print "Uploaded %s as " % f + str(obj.id.val)

CLI.do_upload = do_upload
