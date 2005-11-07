#!/usr/bin/perl
use warnings;

my $files = $ARGV[0];

foreach my $data_file (split /:/, $files) {

  #my $data_file = 'gen-src/slice2java/ome/model/core/DatasetRemote.java';
  #my $insertion_file = 'gen-src/slice_exts/ome/model/core/DatasetRemote.ext';

  my $insertion_file = $data_file;

  $insertion_file =~ s/slice2java/slice_exts/;
  $insertion_file =~ s/java$/ext/;

  open DATA, "$data_file" or die "can't open $data_file $!";
  my @array_of_data = <DATA>;
  close (DATA);

  foreach my $line (reverse @array_of_data)
  {
    if ($line =~ /\}/)
    {
      $line="\n\n // inserting text \n\n";
      last;
    }
  }

  open INSR, "$insertion_file" or die "can't open $insertion_file $!";
  my @array_of_inserts = <INSR>;
  close (INSR);

  open(OUT,">$data_file") or die "can't open $data_file for writing $!";
  print OUT @array_of_data;
  print OUT @array_of_inserts;
  close(OUT);

}
