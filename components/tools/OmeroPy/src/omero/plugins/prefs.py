
def do_prefs(self, arg):
    import omero.java, shlex
    print omero.java.run(["prefs"]+shlex.split(arg))

CLI.do_prefs = do_prefs

def help_prefs(self):
    print "syntax: prefs",
    print "-- access to java properties"
