                genresrc  = properties["resrc.dest"]   
                group     = properties["mvn.group"]   
                artifact  = properties["mvn.artifact"]   
                version   = properties["mvn.version"]   
                packaging = properties["mvn.packaging"]   
                new File("${genresrc}/classpath.xml").withOutputStream{|o|

		  repo = "repository/"
		  def entry = {|x|
		    if (x.contains(repo))  {
		      path = x.substring(x.lastIndexOf(repo)+repo.length())
		      o << """\n\t<file name="${path}"/>"""
                    }
		  }

		  def get = {|x|
		    if (x.contains(repo))  {
		      path = x.substring(x.lastIndexOf(repo)+repo.length())
		      o << """\n\t<get src="\${omero.repo.remote}/${path}" dest="\${omero.repo.local}/${path}" usetimestamp="true"/>"""
                    }
		  }

		  def dist = {|x|
		    if (x.contains(repo))  {
		      path = x.substring(x.lastIndexOf(repo)+repo.length())
		      o << """
	<copy file="\${omero.repo.local}/${path}" todir="\${dist.dir}/repository/${path.substring(0,path.lastIndexOf("/"))}"/>"""
                    }
		  }

		  o << """
<!-- Code-generated ant classpath for ${artifact} component -->
<project name="${artifact}_classpath" default="classpath-generate" basedir=".">
  <property name="artifact.name" value="${artifact}"/>
  <property name="artifact.group" value="${group}"/>
  <property name="artifact.version" value="${version}"/>
  <property name="artifact.packaging" value="${packaging}"/>
  <property name="artifact.final.name" value="${artifact}-${version}.${packaging}"/>
  <property name="artifact.path" value="${group}/${artifact}/${version}/\${artifact.final.name}"/>
  <path id="generated.compile.classpath">
    <filelist dir="\${omero.repo.local}">"""

		  properties["compile.path"].split(":").toList().sort().each(entry)

		  o << """
    </filelist>
  </path>
  <path id="generated.test.classpath">
    <filelist dir="\${omero.repo.local}">"""

		  properties["test.path"].split(":").toList().sort().each(entry)

		  o << """
    </filelist>
  </path>

  <target name="classpath-download">"""

		  properties["omero.path"].split(":").toList().sort().each(get)

		o << """
  </target>

  <target name="dist-copy">"""

		  properties["omero.path"].split(":").toList().sort().each(dist)

		o << """
  </target>
</project>"""
		}
