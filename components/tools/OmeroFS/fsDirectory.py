#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    OMERO.fs Directory module

    The Directory module contains classes to represent 
    a snaphot of a directory tree.

    Copyright 2009 University of Dundee. All rights reserved.
    Use is subject to license terms supplied in LICENSE.txt

"""

import logging
from time import localtime, strftime

# Third party path package. It provides much of the 
# functionality of os.path but without the complexity.
# Imported as pathModule to avoid potential clashes.
import path as pathModule

import fsLists


class Directory(object):
    """
        A class representing a directory as a tree from a given point in
        the file system.

        :group Constructor: __init__
        :group Accessors: getPath, getWhitelist, getBlacklist
        :group Query Methods: onWhitelist, onBlacklist, isSubdirectory
        :group Tree manipulation: patchTree
        :group Tree comparison: getExtraFilesFromTree, getChangedFilesFromTree
        :group Other Methods: getFile, pruneZeroFiles, getChangedFiles
        :group String Representation: __repr__

    """

    def __init__(self, pathString, whitelist=None, pathMode='Flat'):
        """
            Build a tree of the directory given its path.
            
            If no pathString argument is given use the current working directory.
            
            :Parameters:
                pathString : string
                    A full path to the directory of interest.         
                whitelist : Whitelist
                    A list of extensions of interest.                    
                blacklist : Blacklist
                    A list of subdirectories to be ignored.                                               

        """

        #: path as a string 
        self.pathString = pathString
        
        #: path as a pathModule path type
        self.path = pathModule.path(self.pathString)
        
        # Use an empty whitelist if necessary.
        if whitelist == None:
            whitelist = []
        #: whitelist of extensions
        self.whitelist = fsLists.Whitelist(whitelist)

        self.pathMode = pathMode

        #: path of parent, ie where walking starts from.
        self.parent = self.path.parent
        
        #: the root node for this subdirectory
        self.root = DirNode(self.path, self, self.pathMode)
        
        #: the root node for a subdirectory
        self.subRoot = None

    def __repr__(self):
        """
            Return the parent path and the representation of the root node.
        
            :return: String representation of the object.
            :rtype: string

        """
        reprStr = str(self.parent) + '\n'
        reprStr += str(self.root)
        return reprStr

    def getWhitelist(self):
        """
            Getter for the whitelist.
        
            :return: String representation whitelist.
            :rtype: list<string>

        """
        return self.whitelist.asList()


    def getPath(self):
        """
            Return the path, ie the root
            
            :return: Path of interest.
            :rtype: pathModule.path

        """
        return self.path
        
        
    def onWhitelist(self, ext):
        """
            Return true if extension is on whitelist of extensions.
            
            :Parameters:
                ext : string
                    A file extension.         

            :return: Status of extension on whitelist.
            :rtype: boolean

        """
        return self.whitelist.onList(ext)
        
        
    def isSubdirectory(self, pathString):
        """
            Return true if pathString is a subdirectory of the current Directory
            
            :todo: Check and return a value is pathString is a subdirectory 
                of the current Directory's path. This should be used when
                first creating a snapshot to confirm that the blacklist
                contains valid subdirectories.
            
            :return: Status of subdirectory as part of directory.
            :rtype: boolean

        """
        return (pathString != str(self.path) and pathString.find(self.path) == 1)
   

    def getFile(self, pathString):
        """
            Get the file info for a file given a path.
            
            :Parameters:
                pathString : string
                    A full path to the file of interest.
                    
            :return: The file node at the end of pathString.
            :rtype: FileNode
            
        """
        file = None
        path = pathModule.path(pathString)
        nodes = self.path.relpathto(path).splitall()[1:]
        file = self.root.getFile(nodes)

        return file
        
#     def getArchivedSubTree(self, pathString):
#         """
#             Return a subtree from the archived file system starting at pathString.
#             
#             This method is essentially only used for test purposes.
#             
#         """
#         subTree = None
#         path = pathModule.path(pathString)
#         subPath = self.getPath().relpathto(path)
#         if not self.onBlacklist(subPath):
#             subTree = self.root
#             subParts = subPath.splitall()
#             for part in subParts[1:]:
#                 subTree = subTree.getSubDir(part)
# 
#         return subTree        
        
    def patchTree(self, pathString):
        """
            Replace a subtree from the snaphot starting at pathString with new subtree.
            
            For the given pathString, if it isn't on the blacklist, create a new tree
            from the live file system. 
            Then, find the node in the snapshot representing the archive of that path.
            Replace the old snapshot with the new.
            Return both trees so that they can be compared against each other.
            
            :Parameters:
                pathString : string
                    A full path to the subdirectory of interest.
                    
            :return: A tuple containing new and old snaphots of the subdirectory of interest.
            :rtype: tuple<DirNode>
                       
        """
        newTree = None
        oldTree = None
        
        path = pathModule.path(pathString)
        subPath = self.getPath().relpathto(path)
        
        # Generate a snapshot from the live file system/
        newTree = DirNode(path, self, self.pathMode)

        # Find the old tree in the full snapshot.
        oldTree = self.root
        parent = None
        subParts = subPath.splitall()
        for part in subParts[1:]:
            parent = oldTree
            oldTree = oldTree.getSubDir(part)
        
        # Patch in the new tree (which may be the whole tree)
        if parent == None:
            self.root = newTree
        else:
            parent.replaceSubDir(part, newTree)

        return (newTree, oldTree)        
        

    def getExtraFilesFromTree(self, bigTree, littleTree):
        """
            Return a list of files that appear in bigTree but not littleTree.
            
            This can be used for finding both deleted files and new files by
            reversing the supplied parameters.
            
            :Parameters:
                bigTree : DirNode
                    A directory snapshot.
                littleTree : DirNode
                    A directory snapshot.
                    
            :return: A list containing files as full path strings.
            :rtype: list<string>
            
        """
        fileList = []
        if isinstance(littleTree, DirNode) and isinstance(bigTree, DirNode)  :
            for childName, childNode in bigTree.getChildren().items():
                if childName not in littleTree.getChildren().keys():
                    fileList.extend(childNode.getAllFiles())
                else:
                    if self.pathMode == 'Follow' and isinstance(childNode, DirNode):
                        fileList.extend(self.getExtraFilesFromTree(childNode, 
                                littleTree.getChildren()[childName]))     
        
        return fileList

    def getChangedFilesFromTree(self, bigTree, littleTree, compare=('SIZE')):
        """
            Return a list of files that appear in both trees but differ in size.
        
            This should be extended to use mtime, ctime, etc...

            :Parameters:
                bigTree : DirNode
                    A directory snapshot.
                littleTree : DirNode
                    A directory snapshot.
                compare : list<string>
                    A list of what file attribute should be compared.
                    
            :return: A list containing files as full path strings.
            :rtype: list<string>
            
        """
        fileList = []
        if isinstance(littleTree, DirNode) and isinstance(bigTree, DirNode)  :
            for childName, childNode in bigTree.getChildren().items():
                if childName in littleTree.getChildren().keys():                
                    if isinstance(childNode, FileNode):
                        if 'SIZE' in compare:
                            if (childNode.getSize() != 
                                    littleTree.getChildren()[childName].getSize()):
                                fileList.extend(childNode.getAllFiles())
                         # if 'CTIME'...
                         # if 'MTIME'...
                     
                    else:
                        if self.pathMode == 'Follow' and isinstance(childNode, DirNode):
                            fileList.extend(self.getChangedFilesFromTree(childNode, 
                                littleTree.getChildren()[childName], compare))     
        
        return fileList

    def pruneZeroFiles(self, fileList):
        """
            Prune out zero-sized files.
            
            Return a list of those files in the argument list that are
            not zero-sized.
            
            :Parameters:
                fileList : list<string>
                    A list containing files as full path strings.
                    
            :return: A list containing files as full path strings.
            :rtype: list<string>     
            
        """
        newFileList = []
        for fileName in fileList:
            try:
                if self.getFile(fileName).getSize() > 0:
                    newFileList.append(fileName)
            except Exception, e:
                raise
                
        return newFileList
        
    def pruneDirectories(self, fileList):
        """
            Prune out directories.
            
            Return a list of those files in the argument list that are
            files.
            
            :Parameters:
                fileList : list<string>
                    A list containing files as full path strings.
                    
            :return: A list containing files as full path strings.
            :rtype: list<string>     
            
        """
        newFileList = []
        for fileName in fileList:
            try:
                if self.getFile(fileName).isFile():
                    newFileList.append(fileName)
            except Exception, e:
                raise
                
        return newFileList
            

            
    def getChangedFiles(self, pathString, compare=('SIZE')):
        """
            Return lists of new, deleted and changed files in a subtree.
            
            Co-ordinate the action of patching the snapshot and comparing
            files in the new and old trees. It is possible that both new 
            and old trees will be None if a blacklisted directory has been 
            the cause of an event, in this case empty lists are returned.
            
            :Parameters:
                pathString : string
                    A list containing files as full path strings.
                compare : list<string>
                    A list of what file attribute should be compared.
                    
            :return: A tuple containing lists containing files as full path strings.
            :rtype: tuple<list<string>>
   
            
        """   
        newTree, oldTree = self.patchTree(pathString)
        
        newFileList = []
        delFileList = []
        chgFileList = []
        
        if newTree != None or oldTree!= None:
            newFileList = self.getExtraFilesFromTree(newTree, oldTree)
            delFileList = self.getExtraFilesFromTree(oldTree, newTree)
            chgFileList = self.getChangedFilesFromTree(newTree, oldTree, compare)
        
        return newFileList, delFileList, chgFileList     
    

class Node(object):
    """
        An abstract class representing a node in a tree that represents a file hierarchy.

        :group Constructor: __init__
        :group Accessors: getName, getPathString, getSize, getMTime, getCTime, getOwner
        :group Abstract methods: getAllFiles
        :group String Representation: __repr__

    """

    def __init__(self, path):
        """
            Set initial general attributes.
            
            These are the attributes all files and directories possess.

            :Parameters:
                path : pathModule.path object
                    The path of this node.
                    
        """
        self.pathString = str(path)
        self.name = path.name
        self.owner = path.owner
        self.size = path.size
        self.ctime = path.ctime
        self.mtime = path.mtime

    def __repr__(self):
        """
            Return a string containing the attributes.
        
            :return: String representation of the object.
            :rtype: str

        """
        ctimeStr = strftime('%d/%m/%y %H:%M:%S',localtime(self.ctime))
        mtimeStr = strftime('%d/%m/%y %H:%M:%S',localtime(self.mtime))
        return '%s : (%s %d created %s modified %s)' % (self.name,
                                                        self.owner,
                                                        self.size,
                                                        ctimeStr,
                                                        mtimeStr)


    def getAllFiles(self):
        """
            Return a list form of the full filenames under the current node with 
            each of its children recursively if a directory node.
            
            This method must be overridden by the subclass.
            
            :return: List representation of the object.
            :rtype: list
            
        """
        return []
        
    def getName(self):
        """
            Return the file name.
            
            :return: File name
            :rtype: string
            
        """
        return self.name

    def getPathString(self):
        """
            Return the full path name.
            
            :return: Path name
            :rtype: string
            
        """
        return self.pathString

    def getSize(self):
        """
            Return the file size.
            
            :return: File name
            :rtype: int
            
        """
        return self.size

    def getMTime(self):
        """
            Return the mtime.
            
            :return: File mtime
            :rtype: string
            
        """
        return self.mtime
        
    def getCTime(self):
        """
            Return the ctime.
            
            :return: File ctime
            :rtype: string
            
        """
        return self.ctime
        
    def getOwner(self):
        """
            Return the owner.
            
            :return: File owner
            :rtype: string
            
        """
        return self.owner
        
    def isFile(self):
        pass
        

class DirNode(Node):
    """
        A directory node.

        A directory node builds and maintains a dictionary of its children.
        The children are nodes of either type.
        
        :group Constructor: __init__
        :group Accessors: getBase
        :group Other methods: addChild, getChildren, getSubDir, replaceSubDir, 
            getFile, getAllFiles
        :group String Representation: __repr__

    """

    def __init__(self, path, base, mode):
        """
            Create the tree for this node.

            :Parameters:
                path : pathModule.path
                    The path of this directory node. 
                base : string
                    The base path name of this directory node. 
                    
        """
        Node.__init__(self, path)
        self.log = logging.getLogger("fsserver."+__name__)
        #: children is a dictionary of child paths keyed by name
        self.children = {} 
        self.base = base
        self.mode = mode
        
        # add each item in the directory to the dictionary
        #
        #   At the moment all files and directories are added. Some files, and possibly directories,
        #   should probably be ignored. For instance each directory on a Mac has a .DS_Store file
        #   that could be omitted. 
        #
        for d in path.listdir('*'):
            self.addChild(d)

    def __repr__(self, padding=''):
        """
            Return a string form of current node concatenated with 
            each of its children recursively.
            
            :Parameters:
                padding : string
                    On optional padding string to give a degree
                    of nested output.
        
            :return: String representation of the object.
            :rtype: string
            
        """
        reprStr = padding + Node.__repr__(self) + '/\n'
        if len(self.children) > 0:
            for child in self.children.values():
                reprStr += child.__repr__(padding=padding+'  ')
        return reprStr        

    def getBase(self):
        """
            Return the base directory name.
            
            :return: Path name
            :rtype: string
            
        """
        return self.base

    def addChild(self, path):
        """
            Add a file or directory node as a child to the current node.
            
            If it is a directory then this has the effect of recursively 
            adding children to the tree.

            :Parameters:
                path : pathModule.path 
                    The path of the child node to add.

            :return: No explicit return value.
            
        """
        if path.isfile():
            if self.base.onWhitelist(path.ext): 
                self.children[path.name] = FileNode(path)
        elif path.isdir():
            if self.mode == 'Flat':
                self.children[path.name] = DirStub(path)
            elif self.mode == 'Follow':
                self.children[path.name] = DirNode(path, self.base, self.mode)
        else:
            self.log.info('Not a file or directory. Ignoring ' + path.abspath())

    def getAllFiles(self):
        """
            Return a list form of the full filenames under the current node with 
            each of its children recursively.
        
            :return: List representation of the object.
            :rtype: list
            
        """
        fileList = [self.pathString]
        if len(self.children) > 0:
            for child in self.children.values():
                fileList.extend(child.getAllFiles())
                
        return fileList        

    def getChildren(self):
        """
            Return dictionary of children.
            
            :return: Dictionary containing all child nodes.
            :rtype: dict<string,Node>
            
        """
        return self.children
        
    def getSubDir(self, dirName):
        """
            Return the directory node with name dirName
        
            :Parameters:
                dirName : string
                    The name of a directory node.

            :return: A directory node.
            :rtype: DirNode
        """
        return self.children[dirName]
        
    def replaceSubDir(self, dirName, subTree):
        """
            Replace a directory node with name dirName with a subTree

            :Parameters:
                dirName : string
                    The name of a directory node.
                subTree : DirNode
                    A directory node.

            :return: No explicit return value.
            
        """
        self.children[dirName] = subTree
        
    def getFile(self, pathList):
        """
            Return the file at the end of a path list recursively.
            
            Return the immediate child if the path list only
            has one item or ask the child to return the file at the
            end of the list shortened by one.

            :Parameters:
                pathList : list<string>
                    The path of this directory node as an ordered list of strings.
                    
            :return: The file node at the end of the path list.
            :rtype: FileNode
                    
        """
        file = None
        try:
            if len(pathList) == 1:
                file = self.children[pathList[0]]
            else:
                file = self.children[pathList[0]].getFile(pathList[1:])
        except KeyError:
            pass
        
        return file

    def isFile(self):
        return False


class FileNode(Node):
    """
        A file node.

        A file node cannot have children. It can have an extension.

        :group Constructor: __init__
        :group Acessor methods: getExt
        :group Other methods: getAllFiles
        :group String Representation: __repr__

    """

    def __init__(self, path):
        """
            Set specific file attributes.

            :Parameters:
                path : pathModule.path object
                    The path of this file node.
                    
        """
        Node.__init__(self, path)
        self.ext = path.ext
       
    def __repr__(self,  padding=''):
        """
            Return a string form of current file node.
        
            :Parameters:
                padding : string
                    On optional padding string to give a degree
                    of nested output.
                    
            :return: String representation of the object.
            :rtype: str
            
        """
        return padding + Node.__repr__(self) + '\n'
        
    def getExt(self):
        """
            Return the file extensiom.
            
            :return: File extension
            :rtype: string
            
        """
        return self.ext

    def getAllFiles(self):
        """
            Return a list form of the full filename of the current node
        
            :return: List containing the path string.
            :rtype: list<string>
            
        """
        return [self.pathString]        

    def isFile(self):
        return True


class DirStub(Node):
    """
        A direectory stub node.

        A direectory stub cannot have children. It can have an extension.

        :group Constructor: __init__
        :group Other methods: getAllFiles
        :group String Representation: __repr__

    """

    def __init__(self, path):
        """
            Set specific file attributes.

            :Parameters:
                path : pathModule.path object
                    The path of this file node.

        """
        Node.__init__(self, path)
        self.ext = path.ext

    def __repr__(self,  padding=''):
        """
            Return a string form of current file node.

            :Parameters:
                padding : string
                    On optional padding string to give a degree
                    of nested output.

            :return: String representation of the object.
            :rtype: str

        """
        return padding + Node.__repr__(self) + '/X\n'


    def getAllFiles(self):
        """
            Return a list form of the full filename of the current node

            :return: List containing the path string.
            :rtype: list<string>

        """
        return [self.pathString]        

    def isFile(self):
        return False
