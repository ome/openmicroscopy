-- List all the users who have an empty password.
--
-- For each of the active users, logging in with
-- ANY password will work.

-- Output is of the form:
--
--  id  |   case   |   omename    |   firstname   |  lastname   |              email
-- -----+----------+--------------+---------------+-------------+----------------------------------
--   10 |          | a            | A             | Q           | aq@example.com
--   11 | inactive | a            | A             | V           | av@example.com
--
-- "inactive" users would not be able to login
-- since they are not in the "user" group.
--
-- The "guest" account is exempted since it is
-- intended to have an empty password.
--

select e.id,
       case when g.id is null then 'inactive' else '' end as active,
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
