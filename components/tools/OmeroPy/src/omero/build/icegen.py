from distutils.cmd import Command
from distutils.errors import *
from distutils import log
import pkg_resources
import os
import commands

class icegen(Command):
    description = 'Generate python classes and modules from slice definitions'
    user_options = [
        ('dist-dir=', 'd', 'OMERO distribution directory to be use'),
        ('copy-dir=', 'c', 'Copy of OMERO source files where needed'),
        ]

    def initialize_options(self):
        self.dist_dir = "../../../dist"
        self.copy_dir = "target/temp"

    def finalize_options(self):
        log.info("ok")

    def run(self):
        log.info(os.path.abspath(self.dist_dir));
        log.info(os.path.abspath(self.copy_dir));
        log.info(commands.getoutput("which slice2py"))
        log.info(commands.getoutput("slice2py --help"))
        (out,rv) = commands.get("slice2py 

