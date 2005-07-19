#!/usr/bin/perl -wn

sub objectify{
  $word=shift;
  @a = map(ucfirst,split '_',$word);
  $a[-1] =~ s/(.*?)ies$/$1y/;
  $a[-1] =~ s/(.*?)s$/$1/;
  return join("",@a);
}


/^doIt\s(.+)\s(.+)$/;
print "doIt ".objectify($1)." ".$2. "\n";
