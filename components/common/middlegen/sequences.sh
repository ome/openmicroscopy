#pg_dump -s ome | \
#grep -A1 "CREATE TABLE"
#parse --> doIt <object> <sequence>
#sequences.pl (objects)
#
# Alternatively see grep setSequence under OME/src/perl
#

doIt(){
  hbm=~/code/omero/components/common/src/ome/model/$1.hbm.xml
  [ -f $hbm ] ||  echo "No file $hbm" 
  [ -f $hbm ] &&  perl -i -pe "s/this_seq_does_not_exist/$2/" $hbm
}

. sequences.txt
exit
