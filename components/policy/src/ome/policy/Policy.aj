package ome.policy;

public aspect Policy
{

    pointcut threading() : 
        call(* java.lang.Thread+.start(..))
        || call(java.lang.Thread+.new(..));
    
    pointcut hibernateUsage() : 
        call(* org.hibernate.*.*(..)) 
        || call(org.hibernate.*.new(..))
        || call(* org.hibernate.*.*.*(..)) 
        || call(org.hibernate.*.*.new(..))
        || call(* org.hibernate.*.*.*.*(..)) 
        || call(org.hibernate.*.*.*.new(..));
    
    pointcut springOrmUsage() :
        call(*  org.springframework.orm.hibernate3.*.*(..)) 
        || call(org.springframework.orm.hibernate3.*.new(..))
        || call(* org.springframework.orm.hibernate3.support.*.*(..)) 
        || call(  org.springframework.orm.hibernate3.support.*.new(..));

    pointcut privilegedCode() :
        /*
         * Only code we _want_ to be working with these apis.
         */
        withincode(* ome.logic.UpdateImpl.*(..)) 
        || withincode(* ome.logic.QueryImpl.*(..))
        || withincode(* ome.resurrect.Omero2Connector.*(..)) // TODO
        || withincode(* ome.dynamic.SessionUpdater.*(..)) // TODO

        /*
         * Exceptions due to anonymous inner classes
         */
        || withincode(* org.springframework.orm.hibernate3.HibernateCallback+.*(..))
        || withincode(org.springframework.orm.hibernate3.HibernateCallback+.new(..))

        /*
         * Known packges
         */
        || within(ome.tools.hibernate.*)

        /* 
         * Hibernate tests should be explicitly listed 
         */
        //|| within(ome.server.itests.update.AbstractQueryTest+)
        || within(ome.server.itests.update.AbstractUpdateTest+)
        || within(ome.server.itests.hibernate.HotSwapTest)
        || within(ome.server.itests.update.FilterTest);
    
    pointcut notAllowed() : 
        threading() 
        || hibernateUsage() 
        || springOrmUsage();
    
    pointcut blocked() : notAllowed() && ! privilegedCode();
    
    declare error : blocked() : 
        "Accessing this code is blocked from unprivileged packages.";
    
}
