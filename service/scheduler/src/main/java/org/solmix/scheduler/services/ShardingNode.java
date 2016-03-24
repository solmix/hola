
package org.solmix.scheduler.services;

public class ShardingNode
{

   public static final String LEADER_SHARDING_ROOT = ElectionNode.ROOT + "/sharding";

   public static final String NECESSARY = LEADER_SHARDING_ROOT + "/necessary";

   public static final String PROCESSING = LEADER_SHARDING_ROOT + "/processing";

   private static final String SERVER_SHARDING = JobServerNode.ROOT + "/%s/sharding";

   public static String getShardingNode(final String ip) {
        return String.format(SERVER_SHARDING, ip);
    }
}
