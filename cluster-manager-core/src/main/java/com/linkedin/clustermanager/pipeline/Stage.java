package com.linkedin.clustermanager.pipeline;

import com.linkedin.clustermanager.controller.stages.ClusterEvent;

public interface Stage
{

  void init(StageContext context);
  
  void preProcess();
  
  public void process(ClusterEvent event) throws Exception;
  
  void postProcess();
  
  void release();
}
