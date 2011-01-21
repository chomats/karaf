/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.karaf.main;

import java.io.File;
import java.util.HashSet;

import junit.framework.Assert;
import org.junit.Test;
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import static org.ops4j.pax.swissbox.tinybundles.core.TinyBundles.withBnd;

public class MainStartTest {

	/** 
	 * Start two bundles at level one and two.
	 * At level one start mvn handler service and at level 2 use mvn handler service.
	 * @throws Exception cannot start karaf.
	 */
	@Test
    public void testAutoStart() throws Exception {
		resetProperty();
		File basedir = new File(getClass().getClassLoader().getResource("foo").getPath()).getParentFile();
        File home = new File(basedir, "test-karaf-home");
        File data = new File(home, "data-autostart");

        Utils.deleteDirectory(data);

		String[] args = new String[0];
		String fileMVNbundle = new File(home, "bundles/pax-url-mvn.jar").toURI().toURL().toExternalForm();
		String mvnUrl = "mvn:org.osgi/org.osgi.compendium/4.2.0";
		System.setProperty("karaf.home", home.toString());
		System.setProperty("karaf.data", data.toString());
		System.setProperty("karaf.auto.start.1", "\""+fileMVNbundle+"|unused\"");
		System.setProperty("karaf.auto.start.2", "\""+mvnUrl+"|unused\"");
		System.setProperty("karaf.maven.convert", "false");
		

		Main main = new Main(args);
		main.launch();
		Thread.sleep(1000);
		Framework framework = main.getFramework();
		Bundle[] bundles = framework.getBundleContext().getBundles();
		Assert.assertEquals(3, bundles.length);
		Assert.assertEquals(fileMVNbundle, bundles[1].getLocation());
		Assert.assertEquals(mvnUrl, bundles[2].getLocation());
		Assert.assertEquals(Bundle.ACTIVE, bundles[1].getState());
		Assert.assertEquals(Bundle.ACTIVE, bundles[2].getState());
		main.destroy();
	}
	
	void resetProperty() {
		for(Object o: new HashSet<Object>(System.getProperties().keySet())) {
			String k = (String) o;
			if (k.startsWith("karaf."))	{
				System.clearProperty(k);
			}
		}
	}
	/** 
	 * Start two bundles at level 5 and 10.
	 * At level 5 start mvn handler service and at level 10 use mvn handler service.
	 * @throws Exception cannot start karaf.
	 */
	@Test
    public void testAutoStart2() throws Exception {
		resetProperty();
		File basedir = new File(getClass().getClassLoader().getResource("foo").getPath()).getParentFile();
        File home = new File(basedir, "test-karaf-home");
        File data = new File(home, "data-autostart2");

        Utils.deleteDirectory(data);

		String[] args = new String[0];
		String fileMVNbundle = new File(home, "bundles/pax-url-mvn.jar").toURI().toURL().toExternalForm();
		String mvnUrl = "mvn:org.osgi/org.osgi.compendium/4.2.0";
		System.setProperty("karaf.home", home.toString());
		System.setProperty("karaf.data", data.toString());
		System.setProperty("karaf.auto.start.5", "\""+fileMVNbundle+"|unused\"");
		System.setProperty("karaf.auto.start.10", "\""+mvnUrl+"|unused\"");
		System.setProperty("karaf.maven.convert", "false");

		Main main = new Main(args);
		main.launch();
		Thread.sleep(2000);
		Framework framework = main.getFramework();
		Bundle[] bundles = framework.getBundleContext().getBundles();
		Assert.assertEquals(3, bundles.length);
		Assert.assertEquals(fileMVNbundle, bundles[1].getLocation());
		Assert.assertEquals(mvnUrl, bundles[2].getLocation());
		Assert.assertEquals(Bundle.ACTIVE, bundles[1].getState());
		Assert.assertEquals(Bundle.ACTIVE, bundles[2].getState());
		main.destroy();
	}

    @Test
    public void testStopWithTimeout() throws Exception {
    	resetProperty();
		File basedir = new File(getClass().getClassLoader().getResource("foo").getPath()).getParentFile();
        File home = new File(basedir, "test-karaf-home");
        File data = new File(home, "data-timeout");

        Utils.deleteDirectory(data);

		String[] args = new String[0];
		System.setProperty("karaf.home", home.toString());
		System.setProperty("karaf.data", data.toString());
        System.setProperty("karaf.framework.factory", "org.apache.felix.framework.FrameworkFactory");


        Main main = new Main(args);
        main.launch();
        Thread.sleep(1000);
        Framework framework = main.getFramework();
        String activatorName = TimeoutShutdownActivator.class.getName().replace('.', '/') + ".class";
        Bundle bundle = framework.getBundleContext().installBundle("foo",
                TinyBundles.newBundle()
                    .set( Constants.BUNDLE_ACTIVATOR, TimeoutShutdownActivator.class.getName() )
                    .add( activatorName, getClass().getClassLoader().getResourceAsStream( activatorName ) )
                    .build( withBnd() )
        );
        bundle.start();
        long t0 = System.nanoTime();
        main.destroy();
        long t1 = System.nanoTime();
        StringBuilder sb = new StringBuilder("Shutdown duration: ");
        sb.append((((double)(t1 - t0))/1000000)).append(" ms");
        Assert.assertTrue(sb.toString(), ((t1 - t0)/1000000) > TimeoutShutdownActivator.TIMEOUT / 2);
    }
}
