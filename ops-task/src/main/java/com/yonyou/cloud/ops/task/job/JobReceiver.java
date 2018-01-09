package com.yonyou.cloud.ops.task.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import com.yonyou.cloud.ops.task.entity.JobMsg;
import com.yonyou.cloud.ops.task.msg.TaskJob;
import com.yonyou.cloud.ops.task.service.AbstractJobRunner;
import com.yonyou.cloud.ops.task.utils.SpringUtil;
/**
 * 
 * @author daniell
 *
 */
@EnableBinding(TaskJob.class)
public class JobReceiver {

	private static Logger logger = LoggerFactory.getLogger(JobReceiver.class);

	@StreamListener(TaskJob.REVEIVE)
	public void receive(JobMsg job) {
		AbstractJobRunner runner = null;

		try {
			runner = (AbstractJobRunner) SpringUtil.getBean(job.getJobBean());
			runner.jobRun(job);
		} catch (NoSuchBeanDefinitionException e1) {
			logger.error("未找到对应的执行类");
		} catch (Exception e) {
			logger.error("执行失败",e);
		}

	}

}