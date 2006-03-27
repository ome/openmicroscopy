genresrc  = properties["resrc.dest"]   
group     = properties["mvn.group"]   
artifact  = properties["mvn.artifact"]   
version   = properties["mvn.version"]   
sep=":"

def run(pathname,closure) {
    properties[pathname].split(sep).toList().collect( { x ->
      if ( x.contains("javax/transaction/jta") )
      {
          x = x.replaceAll("javax/transaction","geronimo-spec")
          x = x.replaceAll("jta","geronimo-spec-jta")
          x = x.replaceAll("1.0.1B","1.0.1B-rc4")
      }
      return x

    }).collect(closure).findAll({it!=null}).sort().join("\n")
}


repo = "repository/"
def entry = { x ->
    if (x.contains(repo))  
    {
        path = x.substring(x.lastIndexOf(repo)+repo.length())
        return """\t<file name="${path}"/>"""
    }
}

def get = {|x|
    if (x.contains(repo))  {
        path = x.substring(x.lastIndexOf(repo)+repo.length())
        return """\t<get src="\${omero.repo.remote}/${path}" dest="\${omero.repo.local}/${path}" usetimestamp="true"/>"""
    }
}

def dist = {|x|
    if (x.contains(repo))  {
        path = x.substring(x.lastIndexOf(repo)+repo.length())
        return """\t<copy file="\${omero.repo.local}/${path}" todir="\${dist.dir}/repository/${path.substring(0,path.lastIndexOf("/"))}"/>"""
    }
}

def jars = {|x|
    if (x.contains(repo))  {
        path = x.substring(x.lastIndexOf(repo)+repo.length())
        return """\t<copy file="\${omero.repo.local}/${path}" todir="@{todir}"/>"""
    }
}

new File("${genresrc}/classpath.xml").withOutputStream{ o ->
o << """

<!-- Code-generated ant classpath for ${artifact} component -->
<project name="${artifact}_classpath" default="classpath-generate" basedir=".">
  <property name="artifact.name" value="${artifact}"/>
  <property name="artifact.group" value="${group}"/>
  <property name="artifact.version" value="${version}"/>
  <property name="artifact.packaging" value="jar"/><!-- Default; override in component/build.xml -->
  <property name="artifact.final.name" value="${artifact}-${version}.\${artifact.packaging}"/>
  <property name="artifact.path" value="${group}/${artifact}/${version}/\${artifact.final.name}"/>
  <path id="generated.compile.classpath">
    <filelist dir="\${omero.repo.local}">
${run("compile.path",entry)}
    </filelist>
  </path>
  <path id="generated.test.classpath">
    <filelist dir="\${omero.repo.local}">
${run("test.path",entry)}
    </filelist>
  </path>
  <target name="classpath-download">
${run("omero.path",get)}
  </target>
  <target name="dist-copy">
${run("omero.path",dist)}
  </target>
  <macrodef name="jars-copy">
    <attribute name="todir"/>
    <sequential>
${run("omero.path",jars)}
    </sequential>
  </macrodef>
</project>

"""

}
