// dir = properties["import.dir"]
// this.evaluate(new File("${dir}/groovy/common.groovy"))
// println test

src      = properties["src.dir"]	
resrc    = properties["resrc.dir"]	
gensrc   = properties["src.dest"]	
genresrc = properties["resrc.dest"]	
test     = properties["test.dir"]
classrel = properties["classes.rel"]
basedir  = properties["basedir"]	

new File("${basedir}/.classpath").withOutputStream{|o|

    def entryMaker = {|extras|
        return {|path|
            path = path.substring(basedir.size()+1,path.size())
            f = new File(path)
            if (f.isDirectory())
                o << """\t<classpathentry excluding="**/.svn" kind="src" path="${path}" ${extras}/>\n"""
            }
    }

    o << "<classpath>\n"
    o << """\t<classpathentry kind="output" path="${classrel}"/>\n"""
    o << """\t<classpathentry kind="var" rootpath="JRE_SRCROOT" path="JRE_LIB" sourcepath="JRE_SRC"/>\n"""

    [src,resrc,gensrc,genresrc].each(entryMaker(""))
    [test].each(entryMaker("output=\"target/test-classes\""))

    paths = []
    properties["omero.path"].split(":").each{ |x|
        repo = "repository/"
        if (x.contains(repo))  {
        if ( x.contains("javax/transaction/jta") )
        {
            x = x.replaceAll("javax/transaction","geronimo-spec")
            x = x.replaceAll("jta","geronimo-spec-jta")
            x = x.replaceAll("1.0.1B","1.0.1B-rc4")
        }
          paths << x.substring(x.lastIndexOf(repo)+repo.length())
        }
    }

    paths.sort().each{|path|            
        o << """\t<classpathentry kind="var" path="M2_REPO/${path}"/>\n"""
    }

    o << "</classpath>\n"

}
