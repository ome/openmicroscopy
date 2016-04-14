from omero.plugins.duplicate import DuplicateControl
from test.integration.clitest.cli import CLITest
import pytest

object_types = ["Image", "Dataset", "Project", "Plate", "Screen"]
model = ["", "I"]


class TestDuplicate(CLITest):

    def setup_method(self, method):
        super(TestDuplicate, self).setup_method(method)
        self.cli.register("duplicate", DuplicateControl, "TEST")
        self.args += ["duplicate"]

    @pytest.mark.parametrize("model", model)
    @pytest.mark.parametrize("object_type", object_types)
    def testDuplicateCheckOriginalObject(self, object_type, model):
        oid = self.create_object(object_type)

        # Duplicate the object
        self.args += ['/%s%s:%s' % (object_type, model, oid)]
        self.cli.invoke(self.args, strict=True)

        # Check the original object is still present
        assert self.query.find(object_type, oid)
