################################################
# Java configuration
################################################
package main;

use strict;
use warnings;

my @imports =
  [
  'ome.client.ServiceFactory',
  'java.util.HashSet',
  'java.util.Set',
  'java.util.Iterator',
  'java.lang.Integer',
  'java.lang.Class',
  ];

use Inline (
  Java => 'STUDY',
  J2SDK => "/home/josh/lib/jdk1.4",
  JNI => 1,
  STUDY => [],
  DEBUG => 0,
  WARN_METHOD_SELECT => 1,
  AUTOSTUDY => 1,
  DIRECTORY => "/home/josh/tmp/_Inline",
) ;
use Inline::Java qw(study_classes caught cast) ;
study_classes(@imports,'main');
################################################

package OME::Api;

use 5.008004;
use strict;
use warnings;

require Exporter;

our @ISA = qw(Exporter);

# Items to export into callers namespace by default. Note: do not export
# names by default without a very good reason. Use EXPORT_OK instead.
# Do not simply export all your public functions/methods/constants.

# This allows declaration	use OME::Api ':all';
# If you do not need this, moving things directly into @EXPORT or @EXPORT_OK
# will save memory.
our %EXPORT_TAGS = ( 'all' => [ qw(
	
) ] );

our @EXPORT_OK = ( @{ $EXPORT_TAGS{'all'} } );

our @EXPORT = qw(
 ints2set	
);

our 
$VERSION = '0.02';


# Preloaded methods go here.

###############
# TESTING
###############

sub isModel {
  my $ref = shift;
  my $type = ref $ref;
  return $type =~ /model/ ? 1 : undef;
}

sub isSet {
  my $ref = shift;
  my $type = ref $ref;
  return $type =~ /java::util::HashSet/ ? 1 : undef;
}
###############
# CONVERSION
###############

sub ints2set {
  my ($arr) = @_;
  my $set = new java::util::HashSet();
  for my $item (@{$arr}){
    my $i = newInteger($item);
    $set->add(newInteger(1));#$i);
  }
  return asSet($set);
}

sub asObj {
  my $obj = shift;
  return Inline::Java::cast('java.lang.Object', $obj);
}

sub asSet {
  my $set = shift;
  return Inline::Java::cast('java.util.Set',$set);
}

sub asInt {
  my $int = shift;
  return Inline::Java::coerce('int',$int);
}

sub newInteger {
  my $int = shift;
  return new java::lang::Integer($int);
}

sub iter {
  my $coll = shift;
  return Inline::Java::cast('java.util.Iterator',$coll->iterator());
}

###############
# PRINTING
###############

sub dump {
  no strict;
  my $ref   = shift;
  my $cache = shift; 
  my $class = ref $ref;
  my %syms  = %{$class."::"}; 
  
  # Make new cache if necessary
  $cache = new java::util::HashSet()
    unless $cache;
  
  # Defining structure
  my %walk;
  $walk{self} = $ref;
  for my $key (keys %syms){
    if ($key =~ /^get(\w+)/){
      my $field = $1;
      
      my $value = $ref->$key();      
      my $type  = ref $value;
      if ( OME::Api::isModel($value) ) {
        if ($cache->contains($value) ) {
          $walk{$field} = { self => $value , DUPLICATE => 1 };
        } else {
          $cache->add($value); 
          $walk{$field} = OME::Api::dump($value,$cache);
        }
      } elsif (OME::Api::isSet($value)) {
        if ($cache->contains($value)) {
          $walk{$field} = { self => $value , DUPLICATE => 1 };
        } else {
          $cache->add($value);
          my $iter = OME::Api::iter($value);
	  my @arr = ();
          while ($iter->hasNext()) {
	   my $obj = $iter->next();
	   push @arr,OME::Api::dump($obj, $cache);
	  } 
          $walk{$field} = {self => $value , ARRAY => \@arr };
 	}
      } else {
        $walk{$field} = $value;
      }
    }
  }
  
  return \%walk;

}

1;
__END__
# Below is stub documentation for your module. You'd better edit it!

=head1 NAME

OME::Api - Perl extension for blah blah blah

=head1 SYNOPSIS

  use OME::Api;
  use OME::Api::PojoService;
  my $service = new OME::Api::PojoService();
  my @images  = $service->getImage(p2j([1,2]));

=head1 DESCRIPTION

Entry point to the Perl-Java bridge. OME::Api prepares the runtime
environment and exports functions needed for working with the API.

=head2 EXPORT

  p2j - Converts perl objects to java as best as possible.
  j2p - Strike that, reverse it.

=head1 SEE ALSO

=head1 AUTHOR

Josh Moore, E<lt>josh.moore@gmx.deE<gt>

=head1 COPYRIGHT AND LICENSE

Copyright (C) 2005 by Josh Moore

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself, either Perl version 5.8.4 or,
at your option, any later version of Perl 5 you may have available.


=cut
