genresrc  = properties["resrc.dest"]   
group     = properties["mvn.group"]   
artifact  = properties["mvn.artifact"]   
version   = properties["mvn.version"]   
basedir   = properties["basedir"]
sep=":"


repo = "repository/"
def run(pathname,element) {
    properties[pathname].split(sep).toList().collect( { x ->
      if ( x.contains("javax/transaction/jta") )
      {
          x = x.replaceAll("javax/transaction","geronimo-spec")
          x = x.replaceAll("jta","geronimo-spec-jta")
          x = x.replaceAll("1.0.1B","1.0.1B-rc4")
      }
      if ( x.contains(repo) )
      {
        path = x.substring(x.lastIndexOf(repo)+repo.length())
        return """\t<${element} name="${path}"/>"""
      } else {
        return null
      }

    }).findAll({it!=null}).sort().join("\n")
}


new File("${basedir}/classpath.xml").withOutputStream{ o ->
o << """

<!-- Code-generated ant classpath for ${artifact} component -->
<project name="${artifact}_classpath" default="classpath-generate" basedir=".">
  <property name="artifact.name" value="${artifact}"/>
  <property name="artifact.group" value="${group}"/>
  <property name="artifact.version" value="${version}"/>
  <property name="artifact.packaging" value="jar"/><!-- Default; override in component/build.xml -->
  <property name="artifact.final.name" value="${artifact}-${version}.\${artifact.packaging}"/>
  <property name="artifact.path" value="${group}/${artifact}/${version}/\${artifact.final.name}"/>

  <filelist id="generated.compile.filelist" dir="\${omero.repo.local}">
${run("compile.path","file")}
  </filelist>

  <patternset id="generated.compile.patternset">
${run("compile.path","include")}
  </patternset>

  <filelist id="generated.test.filelist" dir="\${omero.repo.local}">
${run("test.path","file")}
  </filelist>

  <patternset id="generated.test.patternset">
${run("test.path","include")}
  </patternset>

  <path id="generated.compile.classpath">
    <filelist refid="generated.compile.filelist"/>
  </path>

  <path id="generated.test.classpath">
    <filelist refid="generated.test.filelist"/>
  </path>

  <target name="dist-copy">
    <copy todir="\${dist.dir}/repository/">
      <fileset dir="\${omero.repo.local}">
        <patternset refid="generated.compile.patternset"/>
        <patternset refid="generated.test.patternset"/>
      </fileset>
    </copy>
  </target>

  <macrodef name="jars-copy">
    <attribute name="todir"/>
    <attribute name="type"/>
    <sequential>
    <copy todir="@{todir}">
      <fileset dir="\${omero.repo.local}">
        <patternset refid="generated.@{type}.patternset"/>
      </fileset>
      <flattenmapper/>
    </copy>
    </sequential>
  </macrodef>

</project>

"""

}
