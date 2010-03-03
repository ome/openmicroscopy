#!/usr/bin/env python
"""
   upoad plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import Arguments, BaseControl
import omero.util.originalfileutils;
import omero;
import omero.rtypes
from omero.rtypes import rlong
from omero.rtypes import rint
from omero.rtypes import rstring
from omero.rtypes import rdouble
from omero.rtypes import rfloat


try: 
	import hashlib 
 	hash_sha1 = hashlib.sha1 
except: 
	import sha 
	hash_sha1 = sha.new 

class UploadControl(BaseControl):

    def help(self, args = None):
        return \
            """
Syntax: %(program_name)s upload <filename> [1..n]
        Upload the given files to omero.

Syntax: %(program_name)s upload script
        Upload default scripts defined in default scripts to the server.

Syntax: %(program_name)s upload pytable <filename> [1..n]
        Upload the given files to pytables in omero.
        
    """
    SCRIPT_ARG='scripts';
    PYTABLE_ARG='pytable';
    FILE_ARG='files';

    def calcSha1(self, filename):
        fileHandle = open(filename)
        h = hash_sha1()
        h.update(fileHandle.read())
        hash = h.hexdigest()
        fileHandle.close()
        return hash;

    def createOriginalFile(self, id, name, filename):
        file = open(filename, 'rb')
        if(id != None):
            ofile = omero.model.OriginalFileI(rlong(id))
        else:
            ofile = omero.model.OriginalFileI();
        try:
            size = os.path.getsize(file.name)
            ofile.size = rlong(size)
            ofile.sha1 = rstring(self.calcSha1(file.name))
            ofile.name = rstring(name)
            ofile.path = rstring(os.path.abspath(file.name));
            fmt = omero.util.originalfileutils.getFormat(filename);
            ofile.format =  omero.model.FormatI();
            ofile.format.value = rstring(fmt[1]);
            up = self.client.getSession().getUpdateService()
            ofile = up.saveAndReturnObject(ofile)
        finally:
            file.close();
        return ofile;

    def uploadFile(self, filename, originalFile = None):
        format = omero.util.originalfileutils.getFormat(filename);
        omeroFormat = format[1];    
        if(format[0]==omero.util.originalfileutils.IMPORTER):
            self.ctx.out("This file should be imported using omero import");
        return self.client.upload(filename, filename, filename, omeroFormat, originalFile)
    
    def uploadFromString(self, string, originalFile):
        prx = self.client.getSession().createRawFileStore()
        prx.setFileId(originalFile.id.val)
        strlen = len(string);
        prx.write(string, 0, strlen)
        prx.close()
        
    def readCommandArgs(self, commandline):
        script = False;
        pytable = False;
        files = list();
        for arg in commandline: 
            if arg in (self.SCRIPT_ARG):
                script = True;
            elif arg in (self.PYTABLE_ARG): 
                pytable = True;	
            else:
                files.append(arg);
        return {self.SCRIPT_ARG:script, self.PYTABLE_ARG:pytable, self.FILE_ARG:files}	         

    def returnSource(self, filename):
        if(filename[len(filename)-3:] == 'pyc'):
            return filename[:len(filename)-1]
        return filename;
    
    def uploadDefaultScripts(self, args):
        import defaultscripts;
        scripts = defaultscripts.defaultscripts; 
        for id in scripts:
            script = scripts[id];
            filename = "";
            try:
                importedScript = __import__(script);
                filename = self.returnSource(importedScript.__file__);
            except:
                raise Exception("Script: " + script + " does not exist");
            if not filename:
                raise Exception("Non-null filename must be provided")

            if not os.path.exists(filename):
                raise Exception("File does not exist: " + filename)
            
            originalFile = self.createOriginalFile(None, script, filename);
            self.uploadFile(filename, originalFile);

    def uploadFromCommandline(self, commandline):
        fileList = commandline[self.FILE_ARG];
        for file in fileList:
            obj = self.uploadFile(file);
            self.ctx.out("Uploaded %s as " % file + str(obj.id.val))
            self.ctx.set("last.upload.id", obj.id.val)

    def __call__(self, *args):
        args = Arguments(args)
        self.client = self.ctx.conn(args)
        argMap = self.readCommandArgs(args);
        # if(argMap[self.SCRIPT_ARG]==True): # and argMap[self.PYTABLE_ARG] == True):
        #   raise ClientError("upload can only be used with %s or %s arguments" % (self.SCRIPT_ARG, self.PYTABLE_ARG));

        if(argMap[self.SCRIPT_ARG]):
            self.uploadDefaultScripts(argMap);
        else:
            self.uploadFromCommandline(argMap);
       
try:
    register("upload", UploadControl)
except NameError:
    UploadControl()._main()
