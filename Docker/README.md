Docker Build for OMERO
----------------------

Quickstart:
 * Build OMERO from source with: `ant release-src release-docker`
 * Create a container with the built code: `docker run --name=mydistro omero-dist true`
 * Launch omero-in-a-box with that code: `docker run --volumes-from mydistro omedocker/omero-in-a-box`

What you'll see:
```
2014-07-10 09:36:42,662 CRIT Set uid to user 0
2014-07-10 09:36:42,665 INFO supervisord started with pid 1
2014-07-10 09:36:43,667 INFO spawned: 'pgsql' with pid 7
2014-07-10 09:36:43,668 INFO spawned: 'ssh' with pid 8
2014-07-10 09:36:43,670 INFO spawned: 'nginx' with pid 9
2014-07-10 09:36:43,671 INFO spawned: 'omero' with pid 10
2014-07-10 09:36:43,672 INFO spawned: 'web' with pid 11
2014-07-10 09:36:44,723 INFO success: pgsql entered RUNNING state, process has stayed up for > than 1 seconds (startsecs)
2014-07-10 09:36:44,723 INFO success: ssh entered RUNNING state, process has stayed up for > than 1 seconds (startsecs)
2014-07-10 09:36:44,723 INFO success: nginx entered RUNNING state, process has stayed up for > than 1 seconds (startsecs)
2014-07-10 09:36:44,723 INFO success: omero entered RUNNING state, process has stayed up for > than 1 seconds (startsecs)
2014-07-10 09:36:44,724 INFO success: web entered RUNNING state, process has stayed up for > than 1 seconds (startsecs)
```

This is the output from supervisord starting up the following:

 * a PostgreSQL database with a fresh DB,
 * a sshd daemon which you can log in to as root,
 * a nginx instance which serves OMERO.web,
 * and the OMERO server itself.
