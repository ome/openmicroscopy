package OME::Api::PojoService;

use 5.008004;
use strict;
use warnings;

our $VERSION = '0.01';

# Api
use OME::Api;
use OME::Api::ServiceFactory;

# Preloaded methods go here.

sub new {
    my($class) = @_;      
    my $self = 
	{ proxy => OME::Api::ServiceFactory->getHierarchyBrowsingService() };
    bless($self, $class);       
    return $self;
}

sub AUTOLOAD {
    no strict;
    my $name = our $AUTOLOAD;
    $name =~ s/.*:://;  # trim package name
    *$AUTOLOAD = sub { 
        my $self = shift;
 	my $result;
        eval {
		 $result = $self->{proxy}->$name(@_);
 	};
	if ($@){
	  	die;
	}
	return $result;
    };
    goto &$AUTOLOAD;    # Restart the new routine.
} 

1;
__END__
# Below is stub documentation for your module. You'd better edit it!

=head1 NAME

OME::Api::PojosService - Basic Pojo-based service.

=head1 SYNOPSIS

  use OME::Api;
  use OME::Api::PojosService;
  OME::Api::ServiceFactory->getSomeInterface();

=head1 DESCRIPTION

OME::Api::ServiceFactory delegates to the Java ServiceFactory. All
methods available from that Java object will be available by AUTOLOAD.
If an error occurs, undef will be returned.

This module returns Java proxies. This is probably not what you want. 
Use the methods defined on OME::Api to get references to
OME::Api::*Service objects.

=head2 EXPORT

None by default.

=head1 SEE ALSO

TODO link to Javadoc.

=head1 AUTHOR

Josh Moore, E<lt>josh.moore@gmx.deE<gt>

=head1 COPYRIGHT AND LICENSE

Copyright (C) 2005 by Josh Moore

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself, either Perl version 5.8.4 or,
at your option, any later version of Perl 5 you may have available.


=cut
