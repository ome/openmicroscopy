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

from omeroweb.testlib import IWebTest
from omeroweb.testlib import get, post, get_json
import time
import pytest
import json

from django.core.urlresolvers import reverse


class TestScripts(IWebTest):
    """Test OMERO.scripts usage in the webclient."""

    def upload_script(self):
        """Upload script and return script ID."""
        root_client = self.new_client(system=True)
        scriptService = root_client.sf.getScriptService()
        uuid = self.uuid()

        script = """
import omero
from omero.rtypes import rstring, rlong, wrap
import omero.scripts as scripts
if __name__ == '__main__':
    client = scripts.client(
        'HelloWorld.py', 'Hello World example script',
        scripts.String('Greeting', default='Hello'),
        scripts.Bool('Do_Work', default=False),
        scripts.Int('Row_Count', default=12),
        scripts.List('Names', default=['A', 'B']),
        scripts.List('Channels',
                     default=[1L, 2L, 3L, 4L]).ofType(rlong(0))
    )
    params = client.getInputs(unwrap=True)
    for name, value in params.items():
        client.setOutput(name, wrap(value))
    client.setOutput('Message', wrap("Script Completed"))
    """

        script_id = scriptService.uploadOfficialScript(
            "/test/web/script%s.py" % uuid, script)
        assert script_id is not None
        return script_id


    def test_script_ui(self):
        """Test script UI html page."""
        script_id = self.upload_script()
        script_ui_url = reverse('script_ui', kwargs={'scriptId': script_id})
        rsp = get(self.django_client, script_ui_url)
        print rsp
        html = rsp.content
        assert 'value="1,2,3,4"' in html


    @pytest.mark.parametrize("inputs", [{},
                                        {'Greeting': 'Hello World',
                                         'Do_Work': True,
                                         'Row_Count': 6,
                                         'Names': ['One', 'Two', 'Three'],
                                         'Channels': [1,2]
                                        },
                                        {'Names': ['Single'],
                                         'Channels': ['not_a_number']
                                        },
        ])
    def test_script_inputs_outputs(self, inputs):
        """Test that inputs and outputs are passed to and from script."""
        script_id = self.upload_script()
        script_run_url = reverse('script_run', kwargs={'scriptId': script_id})

        data = inputs.copy()
        # Lists are submitted as comma-delimited strings
        if data.get('Names'):
            data['Names'] = ','.join(data['Names'])
            data['Channels'] = ','.join([str(c) for c in data['Channels']])
        rsp = post(self.django_client, script_run_url, data)
        rsp = json.loads(rsp.content)
        job_id = rsp['jobId']

        defaults = {
            'Greeting': 'Hello',
            'Do_Work': False,
            'Row_Count': 12,
            'Names': ['A', 'B'],
            'Channels': [1, 2, 3, 4]
        }

        # Any inputs we have will replace default values
        defaults.update(inputs)

        # Non numbers will get removed from Long list
        if defaults.get('Channels') == ['not_a_number']:
            defaults['Channels'] = []

        # Ping Activities until done...
        activities_url = reverse('activities_json')
        data = get_json(self.django_client, activities_url)

        # Keep polling activities until no jobs in progress
        while data['inprogress'] > 0:
            time.sleep(0.5)
            data = get_json(self.django_client, activities_url)

        # individual activities/jobs are returned as dicts within json data
        for k, o in data.items():
            # find dict of results from the script job
            if job_id in k:
                assert o['status'] == 'finished'
                assert o['job_name'] == 'HelloWorld'
                assert o['Message'] == 'Script Completed'
                # All inputs should be passed to outputs
                assert o['results'] == defaults
