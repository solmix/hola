package org.solmix.scheduler.job;

import org.solmix.commons.exec.Execute;
import org.solmix.commons.util.StringUtils;

import com.google.common.base.Preconditions;

public class ScriptJob extends AbstractDistributingJob {

	@Override
	protected void executeJob(JobExecutionShardingContext shardingContext) {

		String scriptCommandLine = getServicesManager().getScriptCommandLine();
		Preconditions.checkArgument(!StringUtils.isEmpty(scriptCommandLine),
				"Cannot find script command line.");
		Execute executor = new Execute();
		try {
			executor.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
