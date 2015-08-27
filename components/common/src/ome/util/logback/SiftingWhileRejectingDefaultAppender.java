package ome.util.logback;

import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SiftingWhileRejectingDefaultAppender extends SiftingAppender {

    @Override
    protected void append(ILoggingEvent event) {
        String discriminatingValue = getDiscriminator().getDiscriminatingValue(event);
        if (!discriminatingValue.equals("default")) {
            super.append(event);
        }
    }
}
