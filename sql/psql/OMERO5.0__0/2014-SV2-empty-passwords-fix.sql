BEGIN;

--
-- See:
-- https://www.openmicroscopy.org/secvuln/2014-sv2-empty-passwords
--
-- Fix all the users who have an empty password.
--
-- First, the same query as in list-empty-passwords.sql
-- will be performed for logging purposes.
--
-- Then, each of the listed users will have their
-- password set to null so that they will no longer
-- be able to login.
--

select e.id,
       case when g.id is null then 'inactive' else '' end as inactive,
       omename, firstname, lastname, email
  from password, experimenter e
  left outer join groupexperimentermap g on (
           g.child = e.id
       and g.parent in (
             select id from experimentergroup where name = 'user'
           )
       )
 where password.experimenter_id = e.id
   and password.hash = ''
   and omename <> 'guest'
 order by omename asc;

update password set hash = null
 where experimenter_id in (
select id
  from experimenter, password
 where password.experimenter_id = experimenter.id
   and password.hash = ''
   and omename <> 'guest'
 order by omename asc
);

COMMIT;
