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
package com.linkedin.helix;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.linkedin.helix.PropertyPathConfig;
import com.linkedin.helix.PropertyType;


@Test
public class TestPropertyPathConfig
{
  @Test
  public void testGetPath()
  {
    String actual;
    actual = PropertyPathConfig.getPath(PropertyType.IDEALSTATES, "test_cluster");
    AssertJUnit.assertEquals(actual, "/test_cluster/IDEALSTATES");
    actual = PropertyPathConfig.getPath(PropertyType.IDEALSTATES, "test_cluster","resource");
    AssertJUnit.assertEquals(actual, "/test_cluster/IDEALSTATES/resource");

    
    actual = PropertyPathConfig.getPath(PropertyType.INSTANCES, "test_cluster","instanceName1");
    AssertJUnit.assertEquals(actual, "/test_cluster/INSTANCES/instanceName1");

    actual = PropertyPathConfig.getPath(PropertyType.CURRENTSTATES, "test_cluster","instanceName1");
    AssertJUnit.assertEquals(actual, "/test_cluster/INSTANCES/instanceName1/CURRENTSTATES");
    actual = PropertyPathConfig.getPath(PropertyType.CURRENTSTATES, "test_cluster","instanceName1","sessionId");
    AssertJUnit.assertEquals(actual, "/test_cluster/INSTANCES/instanceName1/CURRENTSTATES/sessionId");
    
    actual = PropertyPathConfig.getPath(PropertyType.CONTROLLER, "test_cluster");
    AssertJUnit.assertEquals(actual, "/test_cluster/CONTROLLER");
    actual = PropertyPathConfig.getPath(PropertyType.MESSAGES_CONTROLLER, "test_cluster");
    AssertJUnit.assertEquals(actual, "/test_cluster/CONTROLLER/MESSAGES");

    
  }
}
