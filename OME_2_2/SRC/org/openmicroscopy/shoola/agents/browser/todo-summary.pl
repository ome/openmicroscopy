#!/usr/bin/perl

use strict;

use File::Find::Rule;

my @files = File::Find::Rule->file()
	->name('*.java')
	->in(".");

while(@files)
{
	my $file = shift(@files);
	open(F,"<$file");
	my $linecount = 0;
	my $printout = undef;
	my @statements = ();
	while(<F>)
	{
		$linecount++;
		if(m/TODO/)
		{
			$printout = 1;
			my @halves = split(':',$_);
			my $todo = $halves[1];
			chomp $todo;
			push (@statements,"Line $linecount: $todo\n");
		}
	}
	close(F);
	if($printout)
	{
		print STDOUT "$file\n";
		for(my $i=0;$i<length($file);$i++)
		{
			print STDOUT "-";
		}
		print STDOUT "\n";
		for(my $i=0;$i<=$#statements;$i++)
		{
			print STDOUT $statements[$i];
		}
		print STDOUT "\n";
	}
}
