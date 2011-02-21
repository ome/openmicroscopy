This directory maintains logs and notes about branch synchronizations. As of
Feb. 2011, three branches are actively being synchronized:

 * develop (4.3) is the mainline of production where most commits
   are added. New features, which do not need to be backported include:

   o "Big Images"

 * dev_4_2 (4.2) is the latest stable release and intermediate
   releases are provided to some users for extended testing. Many commits,
   especially of bug fixes, can be cherry-picked directly from develop.

 * dev_4_1_custom (4.1) is an combination of the dev_4_1 branch
   with specific directories from the develop branch. Since only the portions
   of changesets which affect those directories are backported, "git
   format-patch --relative" is used and not "git cherry-pick". Further, some of
   the commits from this branch, are forward-ported to develop (from where they
   may also be applied to the dev_4_2 branch.

Synchronization takes place manually on some periodic schedule (roughly
weekly). The developer who will be performing the synchronization checks the
appropriate file in this directory. The last time the file was committed to
marks the last time that that particular synchronization took place on *both*
branches.

Let's take an example. Assuming that the file "4.3-to-4.2.txt" is used
to record synchronizations between 4.3 and 4.2 (as above).

The first step is to get the last commits to these branches. For example:

 $ git log -n 1 --oneline develop -- docs/backporting/4.3-to-4.2.txt
 0423ade Synchronizing 4.3 to 4.2

 $ git log -n 1 --oneline dev_4_2 -- docs/backporting/4.3-to-4.2.txt
 f425941 Synchronizing 4.3 to 4.2

Then, to find all the commits which were made. When using cherry-picking,

 $ git cherry -v dev_4_2 develop 0423ade

shows any commits which are candidates for backporting will be prefixed with a
"+". Those starting with "-" have already been cleanly backported. Alternatively,
a simple "git log" or "git rev-list" can be used to show the commits:

 $ git rev-list 0423ade..develop
