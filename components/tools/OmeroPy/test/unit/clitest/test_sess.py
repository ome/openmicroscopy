#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Test of the sessions plugin

   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import pytest
import Glacier2
import omero_ext.uuid as uuid  # see ticket:3774

from path import path
from omero.cli import CLI, NonZeroReturnCode
from omero.util import get_user
from omero.util.sessions import SessionsStore
from omero.util.temp_files import create_path
from omero.plugins.sessions import SessionsControl

omeroDir = path(os.getcwd()) / "build"

testsess = "testsess"
testuser = "testuser"
testhost = "testhost"


class MyStore(SessionsStore):

    def __init__(self, *args, **kwargs):
        SessionsStore.__init__(self, *args, **kwargs)
        self.clients = []
        self.exceptions = []

    def create(self, name, pasw, props, new=True, set_current=True, sudo=None):

        if not isinstance(props, dict):
            raise Exception("Bad type")

        if self.exceptions:
            raise self.exceptions.pop(0)

        cb = getattr(self, "create_callback", None)
        if cb:
            cb()
            self.create_callback = None
        return_tuple, add_tuple, should_be_new = self.clients.pop(0)

        assert should_be_new == new, ("should_be_new=%s wasn't!"
                                      % should_be_new)

        if new:
            self.add(*add_tuple)

        if set_current:
            self.set_current(*add_tuple)

        return return_tuple

    def __del__(self):
        assert len(self.clients) == 0, ("clients not empty! %s"
                                        % self.clients)
        assert len(self.exceptions) == 0, ("exceptions not empty! %s"
                                           % self.exceptions)


class MyClient(object):

    def __init__(self, user, group, props):
        self.sf = self
        self.userName = user
        self.groupName = group
        self.props = {"omero.port": "4064"}  # Fix after #3883
        self.props.update(props)

    def __del__(self, *args):
        pass

    def enableKeepAlive(self, *args):
        pass

    def getSession(self):
        return self

    def keepAlive(self, prx):
        pass

    def closeSession(self):
        pass

    def getAdminService(self):
        return self

    def getEventContext(self):
        return self

    def getProperty(self, key):
        return self.props[key]


class MyCLI(CLI):

    def __init__(self, *args, **kwargs):
        CLI.__init__(self, *args, **kwargs)
        self.DIR = create_path(folder=True)
        self.REQRESP = {}
        self.STORE = MyStore(self.DIR)
        self.STORE.clear(testhost, testuser)
        self.register("s", SessionsControl, "TEST")
        self.controls["s"].FACTORY = lambda ignore: self.STORE
        assert self.STORE.count(testhost, testuser) == 0

    def __del__(self):
        del self.STORE

    def creates_client(self, name="testuser", host="testhost", sess="sess_id",
                       port=None, group=None, new=True):
        props = dict()
        if port:
            props["omero.port"] = port
        if group:
            props["omero.group"] = group
        else:
            group = "mygroup"  # For use via IAdmin.EventContext

        # props = {"omero.group":group, "omero.port":port}
        return_tuple = (MyClient(name, group, {"omero.host": host}), sess, 0,
                        0)
        add_tuple = (host, name, sess, props)
        self.STORE.clients.append((return_tuple, add_tuple, new))

    def throw_on_create(self, e):
        self.STORE.exceptions.append(e)

    def requests_host(self, host="testhost", port="4064"):
        self.REQRESP["Server: [localhost:%s]" % port] = host

    def requests_user(self, user='testuser'):
        self.REQRESP["Username: [%s]" % get_user("Unknown")] = user

    def requests_pass(self, pasw="pasw"):
        self.REQRESP["Password:"] = pasw

    def requests_size(self):
        return len(self.REQRESP)

    def assertReqSize(self, test, size):
        assert size == self.requests_size(), "size!=%s: %s" % (
            size, self.REQRESP)

    def input(self, prompt, hidden=False, required=False):
        if prompt not in self.REQRESP:
            raise Exception("Missing prompt: '%s'" % prompt)
        return self.REQRESP.pop(prompt)

    def invoke(self, *args):
        CLI.invoke(self, *args, strict=True)


class TestStore(object):

    def store(self, store_path=None):
        if not store_path:
            store_path = create_path(folder=True)
        return MyStore(store_path)

    def testReport(self):
        s = self.store()
        s.report()

    @pytest.mark.parametrize('port', [None, '4064', '14064'])
    @pytest.mark.parametrize('sudo', [None, 'root'])
    def testAdd(self, port, sudo, tmpdir):
        s = self.store(tmpdir)
        props = {}
        if port:
            props["omero.port"] = port
        s.add("srv", "usr", "uuid", props, sudo=sudo)
        assert 1 == len(s.available("srv", "usr"))
        session_dir = tmpdir / "omero" / "sessions"
        session_file = session_dir / "srv" / "usr" / "uuid"
        session_file_content = session_file.read()
        assert "omero.sess=uuid\n" in session_file_content
        assert "omero.user=usr\n" in session_file_content
        assert "omero.host=srv\n" in session_file_content
        if sudo:
            assert "omero.sudo=%s\n" % sudo in session_file_content
        if port:
            assert "omero.port=%s\n" % port in session_file_content

    def testDefaults(self):
        s = self.store()
        assert "localhost" == s.last_host()
        assert "4064" == s.last_port()

    @pytest.mark.parametrize('name', [None, 'usr'])
    @pytest.mark.parametrize('key', [None, 'uuid'])
    @pytest.mark.parametrize('port', [None, '4064', '14064'])
    def testSetCurrent(self, name, key, port, tmpdir):
        s = self.store(tmpdir)
        props = {}
        if port:
            props["omero.port"] = port
        s.set_current("srv", name=name, uuid=key, props=props)
        session_dir = tmpdir / "omero" / "sessions"

        # Using last_* methods
        assert (session_dir / "._LASTHOST_").exists()
        assert "srv" == s.last_host()
        assert (session_dir / "._LASTPORT_").exists()
        assert (port or '4064') == s.last_port()

        # Using helpers
        assert "srv" == s.host_file().text().strip()
        if name:
            assert name == s.user_file("srv").text().strip()
        if key and name:
            assert key == s.sess_file("srv", "usr").text().strip()

        # Using get_current
        lasthost, lastname, lastkey, lastport = s.get_current()
        assert lasthost == "srv"
        assert lastport == (port or '4064')
        assert lastname == name
        if name:
            assert lastkey == key
        else:
            assert lastkey is None

    def testContents(self):
        s = self.store()
        s.add("a", "a", "a", {})
        s.add("b", "b", "b", {})
        rv = s.contents()
        assert 2 == len(rv)
        assert "a" in rv
        assert "a" in rv["a"]
        assert "a" in rv["a"]["a"]
        assert "b" in rv
        assert "b" in rv["b"]
        assert "b" in rv["b"]["b"]

    def testCount(self):
        s = self.store()
        assert 0 == s.count()
        s.add("a", "a", "a", {})
        assert 1 == s.count()
        s.remove("a", "a", "a")
        assert 0 == s.count()

    def testGet(self):
        s = self.store()
        s.add("a", "b", "c", {"foo": "1"})
        rv = s.get("a", "b", "c")
        expect = {
            "foo": "1",
            "omero.host": "a",
            "omero.user": "b",
            "omero.sess": "c"}
        assert expect == rv

    @pytest.mark.parametrize("ignore_nulls", [True, False])
    def testGroupConflicts(self, ignore_nulls):
        s = self.store()
        s.add("a", "b", "c", {"omero.group": "1"})
        conflicts = s.conflicts("a", "b", "c", {}, ignore_nulls=ignore_nulls)
        if ignore_nulls:
            assert conflicts == ''
        else:
            assert conflicts == 'omero.group: 1!=None'

        conflicts = s.conflicts(
            "a", "b", "c", {"omero.group": "2"}, ignore_nulls=ignore_nulls)
        assert conflicts == 'omero.group: 1!=2'

    @pytest.mark.parametrize("ignore_nulls", [True, False])
    def testMultiConflicts(self, ignore_nulls):
        s = self.store()
        s.add("a", "b", "c", {"omero.group": "1"})
        conflicts = s.conflicts("a", "b", "c", {
            "omero.group": "2", "omero.port": "14064"},
            ignore_nulls=ignore_nulls)
        assert conflicts == 'omero.port: None!=14064; omero.group: 1!=2'


class TestSessions(object):

    CONNECTION_TYPES = ["string", "prefixed_string", "options"]
    ALL_PORTS = [None, 4064, 14064]
    ALL_GROUPS = [None, "mygroup", "mygroup2"]
    # The following attributes define list of tuples for testing session
    # re-attachment.
    # The first element of each tuple correspond to the value stored in the
    # session file (where None means the property is not stored). The second
    # element corresponds to the value passed via the connection arguments.
    MATCHING_PORTS = [(None, 4064), (4064, None)]
    CONFLICTING_PORTS = [(None, 14064), (14064, None), (4064, 14064)]
    DIFFERENT_GROUPS = [
        (None, "mygroup"), ("mygroup", None), ("mygroup", "mygroup2")]

    def get_conflict_message(self):
        return "Failed to join session %s due to property conflicts: "

    def get_conn_args(self, conn_type, host="testhost", name="testuser",
                      port=None, group=None):
        """Generated various forms of connection arguments"""

        if name:
            if conn_type == "string":
                pattern = "%s@%s"
            elif conn_type == "prefixed_string":
                pattern = "-s %s@%s"
            else:
                pattern = "-u %s -s %s"
            args = pattern % (name, host)
        else:
            if conn_type == "string":
                pattern = "%s"
            elif conn_type == "prefixed_string":
                pattern = "-s %s"
            else:
                pattern = "-s %s"
            args = pattern % host

        if port:
            if conn_type == "options":
                args += " -p %s" % port
            else:
                args += ":%s" % port

        # Add group login argument
        if group:
            args += " -g %s" % group

        return args.split()

    def testLoginWithNoArgumentsRequests(self):
        cli = MyCLI()
        cli.requests_host()
        cli.requests_user()
        cli.requests_pass()
        cli.creates_client()
        cli.invoke(["s", "login"])
        assert 0 == cli.rv

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testLoginWithConnectionArguments(self, connection):
        cli = MyCLI()
        cli.requests_pass()
        cli.creates_client(name="testuser")
        conn_args = self.get_conn_args(connection)
        cli.invoke(["s", "login"] + conn_args)
        assert 0 == cli.rv

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testPasswordArgument(self, connection):
        cli = MyCLI()
        cli.creates_client(name="testuser")
        conn_args = self.get_conn_args(connection)
        cli.invoke(["s", "login", "-w", "pasw"] + conn_args)
        assert 0 == cli.rv

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testKeyArgument(self, connection):
        cli = MyCLI()
        cli.STORE.add("testhost", "testuser", "key", {})
        cli.creates_client(sess="key", new=False)
        conn_args = self.get_conn_args(connection, name=None)
        cli.invoke(["s", "login", "-k", "key"] + conn_args)
        assert 0 == cli.rv

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testReuseWorks(self, connection):
        """
        Test session reattachment without key
        """
        cli = MyCLI()
        cli.STORE.add("testhost", "testuser", "testsessid", {})
        cli.creates_client(new=False)
        conn_args = self.get_conn_args(connection)
        cli.invoke(["s", "login"] + conn_args)
        assert cli.get_client() is not None

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testReuseFromDifferentGroupDoesntWork(self, connection):
        """
        Test session reattachment without key with different group
        """
        cli = MyCLI()
        cli.STORE.add("testhost", "testuser", "testsessid", {})
        cli.requests_pass()
        cli.assertReqSize(self, 1)
        cli.creates_client(group="mygroup2")
        conn_args = self.get_conn_args(connection, group="mygroup2")
        cli.invoke(["s", "login"] + conn_args)
        cli.assertReqSize(self, 0)

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testReuseFromSameGroupDoesWork(self, connection):
        """
        Test session reattachment without key with same group
        """
        cli = MyCLI()
        cli.STORE.add("testhost", "testuser", "testsessid",
                      {"omero.group": "mygroup"})
        cli.assertReqSize(self, 0)
        cli.creates_client(group="mygroup", new=False)
        conn_args = self.get_conn_args(connection, group="mygroup")
        cli.invoke(["s", "login"] + conn_args)
        cli.assertReqSize(self, 0)

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testReuseFromDifferentPortDoesntWork(self, connection):
        """
        Test session reattachment without key with mismatching port
        """
        cli = MyCLI()
        cli.STORE.add("testhost", "testuser", "testsessid", {})
        cli.requests_pass()
        cli.assertReqSize(self, 1)
        cli.creates_client(port="4444")
        conn_args = self.get_conn_args(connection, port="4444")
        cli.invoke(["s", "login"] + conn_args)
        cli.assertReqSize(self, 0)

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testReuseFromSamePortDoesWork(self, connection):
        """
        Test session reattachment with matching ports
        """
        cli = MyCLI()
        cli.STORE.add("testhost", "testuser", "testsessid",
                      {"omero.port": "4444"})
        cli.assertReqSize(self, 0)
        cli.creates_client(port="4444", new=False)
        conn_args = self.get_conn_args(connection, port="4444")
        cli.invoke(["s", "login"] + conn_args)
        cli.assertReqSize(self, 0)

    def testInteractiveLoginNonDefaultPort(self):
        cli = MyCLI()
        cli.STORE.add("testhost", "testuser", "testsessid",
                      {"omero.port": "4444"})
        cli.assertReqSize(self, 0)
        cli.creates_client(port="4444", new=False)
        cli.requests_host()
        cli.requests_user("testuser")
        with pytest.raises(NonZeroReturnCode):
            cli.invoke("s login".split())
        cli.requests_host("testhost:4444", port="4064")
        cli.requests_user("testuser")
        cli.invoke("s login".split())
        cli.assertReqSize(self, 0)

    def testLogicOfConflictsOnNoLocalhostRequested(self):
        cli = MyCLI()
        cli.creates_client()
        cli.invoke("-s testhost -u testuser -w testpass s login")
        cli.invoke("s login")  # Should work. No conflict
        cli.invoke("-p 4444 s login")

    def testPortThenNothingShouldReuse(self):
        cli = MyCLI()
        cli.creates_client(port="4444")
        cli.requests_host(port="4444")
        cli.requests_user()
        cli.requests_pass()
        cli.invoke("-p 4444 s login")
        cli.assertReqSize(self, 0)  # All were requested
        cli.set_client(None)  # Forcing new instance
        cli.creates_client(port="4444", new=False)
        cli.invoke("s login")  # Should work. No conflict
        del cli

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testBadSessionKeyDies(self, connection):
        """
        As seen in ticket 4223, when a bad session is
        provided, a password shouldn't be asked for.
        """
        cli = MyCLI()

        MOCKKEY = "MOCKKEY"

        # First, successful login
        cli.creates_client(sess=MOCKKEY)
        cli.requests_pass()
        conn_args = self.get_conn_args(connection)
        cli.invoke(["s", "login"] + conn_args)
        cli.assertReqSize(self, 0)  # All were requested
        cli.set_client(None)  # Forcing new instance

        # Now try with session when it's still available
        cli.creates_client(sess=MOCKKEY, new=False)
        key_conn_args = self.get_conn_args(connection, name=None)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)
        cli.set_client(None)  # Forcing new instance

        # Don't do creates_client, so the session key
        # is now bad.
        cli.throw_on_create(
            Glacier2.PermissionDeniedException("MOCKKEY EXPIRED"))
        try:
            cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)
            assert False, "This must throw 'Bad session key'"
        except NonZeroReturnCode:
            pass
        cli.set_client(None)  # Forcing new instance

        del cli

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    @pytest.mark.parametrize('port', ALL_PORTS)
    @pytest.mark.parametrize('group', ALL_GROUPS)
    def testSessionReattachSameArguments(self, connection, port, group):
        """
        Test session re-attachment works with the same login arguments
        """
        cli = MyCLI()
        MOCKKEY = "%s" % uuid.uuid4()

        # Connect using the session key (create a local session file)
        cli.creates_client(sess=MOCKKEY, port=port, group=group)
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port, group=group)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

        # Force new CLI instance using the same connection arguments
        cli.set_client(None)
        cli.creates_client(sess=MOCKKEY, new=False)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    @pytest.mark.parametrize('port', MATCHING_PORTS)
    @pytest.mark.parametrize('group', ALL_GROUPS)
    def testSessionReattachDefaultPort(self, connection, port, group):
        """
        Test session re-attachment by key with matching ports
        """
        cli = MyCLI()
        MOCKKEY = "%s" % uuid.uuid4()

        # Connect using the session key (create a local session file)
        cli.creates_client(sess=MOCKKEY, port=port[0], group=group)
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port[0], group=group)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

        # Force new CLI instance using the same connection arguments
        cli.set_client(None)
        cli.creates_client(sess=MOCKKEY, new=False)
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port[1], group=group)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    @pytest.mark.parametrize('port', ALL_PORTS)
    @pytest.mark.parametrize('group', ALL_GROUPS)
    def testSessionReattachServerMismatch(self, connection, port, group):
        """
        Test session re-attachment by key with mismatching hostnames
        """
        cli = MyCLI()
        MOCKKEY = "%s" % uuid.uuid4()

        # Connect using the session key (create a local session file)
        cli.creates_client(sess=MOCKKEY, port=port, group=group)
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port, group=group)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

        # Force new CLI instance using the same connection arguments
        cli.set_client(None)
        cli.creates_client(sess=MOCKKEY, new=False)
        key_conn_args = self.get_conn_args(
            connection, host="wrongserver",  name=None, port=port, group=group)
        with pytest.raises(NonZeroReturnCode):
            cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    @pytest.mark.parametrize('port', CONFLICTING_PORTS)
    @pytest.mark.parametrize('group', ALL_GROUPS)
    def testSessionReattachConflictingPort(
            self, connection, port, group, capsys):
        """
        Test session re-attachment fails with mismatching ports
        """
        cli = MyCLI()
        MOCKKEY = "%s" % uuid.uuid4()

        # Connect using the session key (create a local session file)
        cli.creates_client(sess=MOCKKEY, port=port[0], group=group)
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port[0], group=group)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

        # Force new CLI instance using the same connection arguments
        cli.set_client(None)
        cli.creates_client(sess=MOCKKEY, new=False)
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port[1], group=group)
        with pytest.raises(NonZeroReturnCode):
            cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)
        out, err = capsys.readouterr()
        msg = self.get_conflict_message() + 'omero.port: %s!=%s'
        assert err.splitlines()[-2] == msg % (MOCKKEY, port[0], port[1])

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    @pytest.mark.parametrize('port', ALL_PORTS)
    @pytest.mark.parametrize('group', DIFFERENT_GROUPS)
    def testSessionReattachDifferentGroup(self, connection, port, group):
        """
        Test session re-attachment by key with different groups
        """
        cli = MyCLI()
        MOCKKEY = "%s" % uuid.uuid4()

        # Connect using the session key (create a local session file)
        cli.creates_client(sess=MOCKKEY, port=port, group=group[0])
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port, group=group[0])
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

        # Force new CLI instance using the same connection arguments
        cli.set_client(None)
        cli.creates_client(sess=MOCKKEY, new=False)
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port, group=group[1])
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    @pytest.mark.parametrize('port', CONFLICTING_PORTS)
    @pytest.mark.parametrize('group', DIFFERENT_GROUPS)
    def testSessionReattachFailsPortGroup(self, connection, port, group,
                                          capsys):
        """
        Test session re-attachment by key with mismatching ports and different
        groups
        """
        cli = MyCLI()
        MOCKKEY = "%s" % uuid.uuid4()

        # Connect using the session key (create a local session file)
        cli.creates_client(sess=MOCKKEY, port=port[0], group=group[0])
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port[0], group=group[0])
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)

        # Force new CLI instance using the same connection arguments
        cli.set_client(None)
        cli.creates_client(sess=MOCKKEY, new=False)
        key_conn_args = self.get_conn_args(
            connection, name=None, port=port[1], group=group[1])
        with pytest.raises(NonZeroReturnCode):
            cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + key_conn_args)
        out, err = capsys.readouterr()
        msg = (self.get_conflict_message() + 'omero.port: %s!=%s')
        assert err.splitlines()[-2] == msg % (MOCKKEY, port[0], port[1])

    @pytest.mark.parametrize('port', ALL_PORTS)
    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def testCopiedSessionWorks(self, connection, port):
        """
        Found by Colin while using a session key from a non-CLI-source.
        """
        cli = MyCLI()

        MOCKKEY = "MOCKKEY%s" % uuid.uuid4()

        # Try with session when it's still available
        cli.creates_client(sess=MOCKKEY, new=True, port=port)

        conn_args = self.get_conn_args(connection, name=None, port=port)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + conn_args)
        cli.set_client(None)  # Forcing new instance
        cli.creates_client(sess=MOCKKEY, new=False)
        conn_args = self.get_conn_args(connection, port=port)
        cli.invoke(["s", "login", "-k", "%s" % MOCKKEY] + conn_args)

    @pytest.mark.parametrize('connection', CONNECTION_TYPES)
    def test5975(self, connection):
        """
        Runs various tests which try to force the stored user name
        to be a session uuid (which should never happen)
        """
        cli = MyCLI()
        key = "%s" % uuid.uuid4()
        cli.creates_client(sess=key, new=True)
        conn_args = self.get_conn_args(connection, name=None)
        cli.invoke(["s", "login", "-k", "%s" % key] + conn_args)
        current = cli.STORE.get_current()
        assert key != current[1]

        cli.invoke("s logout")
        current = cli.STORE.get_current()
        assert key != current[1]


class TestParseConn(object):

    @pytest.mark.parametrize('default_user', [None, 'default_user'])
    def testEmptyString(self, default_user):
        out_server, out_name, out_port = SessionsControl._parse_conn(
            '', default_user)

        assert not out_server
        assert out_name == default_user
        assert not out_port

    @pytest.mark.parametrize('default_user', [None, 'default_user'])
    @pytest.mark.parametrize('port', ['bad_port', '12323414232'])
    def testBadPort(self, default_user, port):
        out_server, out_name, out_port = SessionsControl._parse_conn(
            'localhost:%s' % port, default_user)

        assert out_server == 'localhost:%s' % port
        assert out_name == default_user
        assert not out_port

    @pytest.mark.parametrize('default_user', [None, 'default_user'])
    @pytest.mark.parametrize(
        'user_prefix', ['', 'user@', 'üser-1£@', 'user:4@email@'])
    @pytest.mark.parametrize('server', ['localhost', 'server.domain'])
    @pytest.mark.parametrize('port_suffix', ['', ':4064', ':14064'])
    def testConnectionString(self, default_user, user_prefix, server,
                             port_suffix):
        in_server = '%s%s%s' % (user_prefix, server, port_suffix)
        out_server, out_name, out_port = SessionsControl._parse_conn(
            in_server, default_user)
        assert out_server == server
        if user_prefix:
            assert out_name == user_prefix[:-1]
        else:
            assert out_name == default_user
        if port_suffix:
            assert out_port == port_suffix[1:]
        else:
            assert not out_port
