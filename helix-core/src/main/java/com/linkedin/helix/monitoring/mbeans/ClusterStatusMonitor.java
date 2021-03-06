/**
 * Copyright (C) 2012 LinkedIn Inc <opensource@linkedin.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.helix.monitoring.mbeans;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.linkedin.helix.ExternalViewChangeListener;
import com.linkedin.helix.LiveInstanceChangeListener;
import com.linkedin.helix.NotificationContext;
import com.linkedin.helix.model.ExternalView;
import com.linkedin.helix.model.IdealState;
import com.linkedin.helix.model.LiveInstance;


public class ClusterStatusMonitor
  implements ClusterStatusMonitorMBean
{
  private static final Logger LOG = Logger.getLogger(ClusterStatusMonitor.class);

  private final MBeanServer _beanServer;

  private int _numOfLiveInstances = 0;
  private int _numOfInstances = 0;
  private final ConcurrentHashMap<String, ResourceMonitor> _resourceMbeanMap
    = new ConcurrentHashMap<String, ResourceMonitor>();
  private String _clusterName = "";

  private int _numOfDisabledInstances = 0;
  private int _numOfDisabledPartitions = 0;

  public ClusterStatusMonitor(String clusterName)
  {
    _clusterName = clusterName;
    _beanServer = ManagementFactory.getPlatformMBeanServer();
    try
    {
      register(this, getObjectName("cluster="+_clusterName));
    }
    catch(Exception e)
    {
      LOG.error("Register self failed.", e);
    }
  }

  public ObjectName getObjectName(String name) throws MalformedObjectNameException
  {
    return new ObjectName("ClusterStatus: "+name);
  }

  // Used by other external JMX consumers like ingraph
  public String getBeanName()
  {
    return "ClusterStatus "+_clusterName;
  }

  @Override
  public long getDownInstanceGauge()
  {
    return _numOfInstances - _numOfLiveInstances;
  }

  @Override
  public long getInstancesGauge()
  {
    return _numOfInstances;
  }

  private void register(Object bean, ObjectName name)
  {
    try
    {
      _beanServer.unregisterMBean(name);
    }
    catch (Exception e1)
    {
      // Swallow silently
    }

    try
    {
      LOG.info("Registering " + name.toString());
      _beanServer.registerMBean(bean, name);
    }
    catch (Exception e)
    {
      LOG.warn("Could not register MBean", e);
    }
  }
  
  private void unregister(ObjectName name)
  {
    try
    {
      LOG.info("Unregistering " + name.toString());
      _beanServer.unregisterMBean(name);
    }
    catch (Exception e)
    {
      LOG.warn("Could not unregister MBean", e);
    }
  }

  public void setClusterStatusCounters(int numberLiveInstances, int numberOfInstances, int disabledInstances, int disabledPartitions)
  {
    _numOfInstances = numberOfInstances;
    _numOfLiveInstances = numberLiveInstances;
    _numOfDisabledInstances  = disabledInstances;
    _numOfDisabledPartitions = disabledPartitions;
  }

  public void onExternalViewChange(ExternalView externalView, IdealState idealState)
  {
    try
    {
        String resourceName = externalView.getId();
        if(!_resourceMbeanMap.containsKey(resourceName))
        {
          synchronized(this)
          {
            if(!_resourceMbeanMap.containsKey(resourceName))
            {
              ResourceMonitor bean = new ResourceMonitor(_clusterName, resourceName);
              String beanName = "Cluster=" + _clusterName + ",resourceName=" + resourceName;
              register(bean, getObjectName(beanName));
              _resourceMbeanMap.put(resourceName, bean);
            }
          }
        }
        _resourceMbeanMap.get(resourceName).updateExternalView(externalView, idealState);
    }
    catch(Exception e)
    {
      LOG.warn(e);
    }
  }

  public void reset()
  {
    LOG.info("Resetting ClusterStatusMonitor");
    try
    {
      for(String resourceName : _resourceMbeanMap.keySet())
      {
        String beanName = "Cluster=" + _clusterName + ",resourceName=" + resourceName;
        unregister(getObjectName(beanName));
      }
      _resourceMbeanMap.clear();
      unregister(getObjectName("cluster="+_clusterName));
    }
    catch(Exception e)
    {
      LOG.error("unregister self failed.", e);
    }
  }
  
  public String getSensorName()
  {
    return "ClusterStatus"+"_" + _clusterName;
  }

  @Override
  public long getDisabledInstancesGauge()
  {
    return _numOfDisabledInstances;
  }

  @Override
  public long getDisabledPartitionsGauge()
  {
    return _numOfDisabledPartitions;
  }
}
