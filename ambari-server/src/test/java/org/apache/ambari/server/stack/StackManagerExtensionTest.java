/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.stack;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.apache.ambari.server.metadata.ActionMetadata;
import org.apache.ambari.server.orm.dao.ExtensionDAO;
import org.apache.ambari.server.orm.dao.ExtensionLinkDAO;
import org.apache.ambari.server.orm.dao.MetainfoDAO;
import org.apache.ambari.server.orm.dao.StackDAO;
import org.apache.ambari.server.orm.entities.StackEntity;
import org.apache.ambari.server.state.ExtensionInfo;
import org.apache.ambari.server.state.ServiceInfo;
import org.apache.ambari.server.state.StackInfo;
import org.apache.ambari.server.state.stack.OsFamily;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * StackManager extension unit tests.
 */
public class StackManagerExtensionTest  {

  @Test
  public void testExtensions() throws Exception {
    MetainfoDAO metaInfoDao = createNiceMock(MetainfoDAO.class);
    StackDAO stackDao = createNiceMock(StackDAO.class);
    ExtensionDAO extensionDao = createNiceMock(ExtensionDAO.class);
    ExtensionLinkDAO linkDao = createNiceMock(ExtensionLinkDAO.class);
    ActionMetadata actionMetadata = createNiceMock(ActionMetadata.class);
    OsFamily osFamily = createNiceMock(OsFamily.class);
    StackEntity stackEntity = createNiceMock(StackEntity.class);

    expect(
        stackDao.find(EasyMock.anyObject(String.class),
            EasyMock.anyObject(String.class))).andReturn(stackEntity).atLeastOnce();

    replay(actionMetadata, stackDao, metaInfoDao, osFamily);

    String stacks = ClassLoader.getSystemClassLoader().getResource("stacks_with_extensions").getPath();
    String common = ClassLoader.getSystemClassLoader().getResource("common-services").getPath();
    String extensions = ClassLoader.getSystemClassLoader().getResource("extensions").getPath();

    StackManager stackManager = new StackManager(new File(stacks),
        new File(common), new File(extensions), osFamily, metaInfoDao, actionMetadata, stackDao, extensionDao, linkDao);

    ExtensionInfo extension = stackManager.getExtension("EXT", "0.1");
    assertNull("EXT 0.1's parent: " + extension.getParentExtensionVersion(), extension.getParentExtensionVersion());
    assertNotNull(extension.getService("OOZIE2"));
    ServiceInfo oozie = extension.getService("OOZIE2");
    assertNotNull("Package dir is " + oozie.getServicePackageFolder(), oozie.getServicePackageFolder());
    assertTrue("Package dir is " + oozie.getServicePackageFolder(), oozie.getServicePackageFolder().contains("extensions/EXT/0.1/services/OOZIE2/package"));
    assertEquals(oozie.getVersion(), "3.2.0");

    extension = stackManager.getExtension("EXT", "0.2");
    assertNotNull("EXT 0.2's parent: " + extension.getParentExtensionVersion(), extension.getParentExtensionVersion());
    assertEquals("EXT 0.2's parent: " + extension.getParentExtensionVersion(), "0.1", extension.getParentExtensionVersion());
    assertNotNull(extension.getService("OOZIE2"));
    oozie = extension.getService("OOZIE2");
    assertNotNull("Package dir is " + oozie.getServicePackageFolder(), oozie.getServicePackageFolder());
    assertTrue("Package dir is " + oozie.getServicePackageFolder(), oozie.getServicePackageFolder().contains("extensions/EXT/0.1/services/OOZIE2/package"));
    assertEquals(oozie.getVersion(), "4.0.0");

    StackInfo stack = stackManager.getStack("HDP", "0.2");
    assertNotNull(stack.getService("OOZIE2"));
    oozie = stack.getService("OOZIE2");
    assertNotNull("Package dir is " + oozie.getServicePackageFolder(), oozie.getServicePackageFolder());
    assertTrue("Package dir is " + oozie.getServicePackageFolder(), oozie.getServicePackageFolder().contains("extensions/EXT/0.1/services/OOZIE2/package"));
    assertEquals(oozie.getVersion(), "4.0.0");

    assertTrue("Extensions found: " + stack.getExtensions().size(), stack.getExtensions().size() == 1);
    extension = stack.getExtensions().iterator().next();
    assertEquals("Extension name: " + extension.getName(), extension.getName(), "EXT");
    assertEquals("Extension version: " + extension.getVersion(), extension.getVersion(), "0.2");
  }

}
