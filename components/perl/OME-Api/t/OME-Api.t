# Before `make install' is performed this script should be runnable with
# `make test'. After `make install' it should work as `perl OME-Api.t'

#########################

# change 'tests => 1' to 'tests => last_test_to_print';

use Data::Dumper;
use Test::More tests => 7;
BEGIN { use_ok('OME::Api') };
BEGIN { use_ok('OME::Api::ServiceFactory') };
BEGIN { use_ok('OME::Api::PojoService') };

#########################

# see man page ( perldoc Test::More ) for help writing this test script.

ok(defined(OME::Api::ServiceFactory->getHierarchyBrowsingService()), "Autoload good");
ok(!defined(OME::Api::ServiceFactory->getUnknown()),"Autoload bad");

my $service = new OME::Api::PojoService();
ok(defined($service));


# TESTING CLASS
my $class = java::lang::Class->forName("org.openmicroscopy.omero.model.Project");
my $result = $service->loadPDIHierarchy($class,1);
ok(defined($result));
diag(Dumper($result));
$iter =
Inline::Java::cast('java.util.Iterator',$result->getDatasets()->iterator());
while ($iter->hasNext()){
  diag(Dumper($iter->next()));
}
diag(Dumper(OME::Api::dump($result)));



# TESTING SETS
#my $ids = ints2set([1]);
#my $result = $service->findImageAnnotations($ids);
#ok(defined($result));

diag("NEED TO CREATE CLASSES AS NEED SO WE DON'T HAVE TO USE
'DUPLICATE'");

1;
