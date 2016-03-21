
package org.solmix.scheduler.event;

import java.util.EventObject;

import org.solmix.runtime.event.Event;
import org.solmix.scheduler.JobContext;

public class JobEvent extends EventObject implements Event
{

    private final int type;

    private final JobContext container;

    public final static int BEFORE = 0x00000002;

    public final static int AFTER = 0x00000003;

    /**
     * @param source
     */
    public JobEvent(int type, Object source, JobContext container)
    {
        super(source);
        this.type = type;
        this.container = container;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6370021799067517907L;

    public int getType() {
        return type;
    }

    /**
     * @return the container
     */
    public JobContext getJobContext() {
        return container;
    }

}
