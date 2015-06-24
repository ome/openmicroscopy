/*
 *   Copyright 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ome.services.blitz.test.AbstractServantTest;
import ome.services.util.Executor;
import ome.services.mail.MailUtil;
import ome.system.ServiceFactory;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.Request;
import omero.cmd.Response;
import omero.cmd.SendEmailResponse;
import omero.cmd.Status;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class SendEmailRequestTest extends AbstractServantTest {

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected SendEmailResponse assertRequest(final SendEmailRequestI req,
            Map<String, String> ctx) {

        final Status status = new Status();

        @SuppressWarnings("unchecked")
        List<Object> rv = (List<Object>) user.ex.execute(ctx, user
                .getPrincipal(), new Executor.SimpleWork(this, "testRequest") {
            @Transactional(readOnly = true)
            public List<Object> doWork(Session session, ServiceFactory sf) {

                // from HandleI.steps()
                List<Object> rv = new ArrayList<Object>();

                Helper helper = new Helper((Request) req, status,
                        getSqlAction(), session, sf);
                req.init(helper);

                int j = 0;
                while (j < status.steps) {
                    try {
                        status.currentStep = j;
                        rv.add(req.step(j));
                    } catch (Cancel c) {
                        throw c;
                    } catch (Throwable t) {
                        throw helper.cancel(new ERR(), t, "bad-step", "step",
                                "" + j);
                    }
                    j++;
                }

                return rv;
            }
        });

        // Post-process
        for (int step = 0; step < status.steps; step++) {
            Object obj = rv.get(step);
            req.buildResponse(step, obj);
        }

        Response rsp = req.getResponse();
        if (rsp instanceof ERR) {
            fail(rsp.toString());
        }

        return (SendEmailResponse) rsp;
    }

    @Test
    public void testSendEmail() throws Exception {

        SendEmailRequestI req = new SendEmailRequestI(
                (MailUtil) user.ctx.getBean("mailUtil"));
        req.userIds = new ArrayList<Long>(Arrays.asList(0L));
        req.groupIds = new ArrayList<Long>(Arrays.asList(0L));
        req.extra = new ArrayList<String>(Arrays.asList("user@mail"));
        req.subject = "topic";
        req.body = "text";

        SendEmailResponse rsp = assertRequest(req, null);
    }

}
