select currentversion || '__' || currentpatch as currentversion__currentpatch from dbpatch order by id desc limit 1;
