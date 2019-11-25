#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

"""Test OMERO.scripts usage in the webclient."""

from builtins import str
from omeroweb.testlib import IWebTest
from omeroweb.testlib import get, post, get_json
import time
import pytest
import json

from django.core.urlresolvers import reverse


class TestScripts(IWebTest):
    """Test OMERO.scripts usage in the webclient."""

    # Values that are included in the script below
    default_param_values = {
        'Greeting': 'Hello',
        'Do_Work': False,
        'Row_Count': 12,
        'Names': ['A', 'B'],
        'Channels': [1, 2, 3, 4],
        'Data_Type': 'Image',
    }

    def upload_script(self):
        """Upload script and return script ID."""
        root_client = self.new_client(system=True)
        scriptService = root_client.sf.getScriptService()
        uuid = self.uuid()

        script = """
import omero
from omero.rtypes import rint, rlong, robject, rstring, wrap
import omero.scripts as scripts
if __name__ == '__main__':
    client = scripts.client(
        'HelloWorld.py', 'Hello World example script',
        scripts.String('Data_Type', default='Image'),
        scripts.List('IDs').ofType(rlong(0)),
        scripts.String('Greeting', default='Hello'),
        scripts.Bool('Do_Work', default=False),
        scripts.Int('Row_Count', default=12),
        scripts.List('Names', default=['A', 'B']),
        scripts.List('Channels',
                     default=[1, 2, 3, 4]).ofType(rint(0))
    )
    params = client.getInputs(unwrap=True)
    print(params)
    if params.get('IDs'):
        dtype = params.get('Data_Type')
        print('get %s' % dtype)
        qs = client.sf.getQueryService()
        obj = qs.get(dtype, params.get('IDs')[0])
        print(obj.id.val)
        client.setOutput(dtype, robject(obj))
    for name, value in params.items():
        client.setOutput(name, wrap(value))
    client.setOutput('Message', wrap("Script Completed"))
    """

        script_id = scriptService.uploadOfficialScript(
            "/test/web/script%s.py" % uuid, script)
        assert script_id is not None
        return script_id

    def test_script_ui_defaults(self):
        """Test script UI html page includes default values."""
        script_id = self.upload_script()
        script_ui_url = reverse('script_ui', kwargs={'scriptId': script_id})
        rsp = get(self.django_client, script_ui_url)
        html = rsp.content.decode("utf-8")
        defaults = self.default_param_values
        expected_values = [
            defaults['Greeting'],
            str(defaults['Row_Count']),
            ','.join([str(c) for c in defaults['Channels']]),
            ','.join(defaults['Names'])
        ]
        for v in expected_values:
            assert ('value="%s"' % v) in html

    @pytest.mark.parametrize("inputs", [{},
                                        {'Greeting': 'Hello World',
                                         'Do_Work': True,
                                         'Row_Count': 6,
                                         'Names': ['One', 'Two', 'Three'],
                                         'Channels': [1, 2]},
                                        {'Names': ['Single'],
                                         'Channels': ['not_a_number']}])
    def test_script_inputs_outputs(self, inputs):
        """Test that inputs and outputs are passed to and from script."""
        script_id = self.upload_script()
        script_run_url = reverse('script_run', kwargs={'scriptId': script_id})

        data = inputs.copy()
        # script basically passes inputs/defaults to outputs
        results = self.default_param_values.copy()

        # Create Image and add IDs to inputs
        image = self.make_image("test_script_inputs_outputs")
        data["IDs"] = str(image.id.val)
        # We expect to get Image returned (and IDs are passed through too)
        results['IDs'] = [image.id.val]
        results['Image'] = {
            'id': image.id.val,
            'type': 'Image',
            'browse_url': "/webclient/userdata/?show=image-%s" % image.id.val,
            'name': "test_script_inputs_outputs"
        }

        # Lists are submitted as comma-delimited strings
        if data.get('Names'):
            data['Names'] = ','.join(data['Names'])
            data['Channels'] = ','.join([str(c) for c in data['Channels']])
        rsp = post(self.django_client, script_run_url, data)
        rsp = json.loads(rsp.content)
        job_id = rsp['jobId']

        # Any inputs we have will replace default values
        results.update(inputs)

        # Non numbers will get removed from Long list
        if results.get('Channels') == ['not_a_number']:
            results['Channels'] = []

        # Ping Activities until done...
        activities_url = reverse('activities_json')
        data = get_json(self.django_client, activities_url)

        # Keep polling activities until no jobs in progress
        while data['inprogress'] > 0:
            time.sleep(0.5)
            data = get_json(self.django_client, activities_url)

        print(data)
        # individual activities/jobs are returned as dicts within json data
        for k, o in data.items():
            # find dict of results from the script job
            if job_id in k:
                assert o['job_type'] == 'script'
                assert o['status'] == 'finished'
                assert o['job_name'] == 'HelloWorld'
                assert o['Message'] == 'Script Completed'
                assert o['stdout'] > 0
                # All inputs should be passed to outputs
                assert o['results'] == results

        # check Activities html
        rsp = get(self.django_client, reverse('activities'))
        html = rsp.content.decode("utf-8")
        # Links to Image
        assert "Open Image in Viewer" in html
        # check message
        assert "Script Completed" in html
        # stdout button
        assert "Show additional info generated by the script" in html


class TestFigureScripts(IWebTest):
    """Figure scripts run from webclient with custom dialog UI."""

    @pytest.mark.parametrize("script_name", ['SplitView', 'Thumbnail',
                                             'MakeMovie'])
    def test_figure_script_dialog(self, script_name):
        # Need at least one multi-dimensional image, in a Dataset
        image = self.create_test_image(size_c=2, size_t=5)
        image_id = image.id.val
        dataset = self.make_dataset(client=self.root)
        self.link(dataset, image, client=self.root)
        script_ui_url = reverse('figure_script',
                                kwargs={'scriptName': script_name})
        rsp = get(self.django_root_client, script_ui_url,
                  data={'Image': image_id})
        html = rsp.content.decode("utf-8")

        titles = {'SplitView': 'Create Split View Figure',
                  'Thumbnail': 'Create Thumbnail Figure',
                  'MakeMovie': 'Make Movie'}
        # Basic check that we have some html
        assert "script_form" in html
        assert titles[script_name] in html
