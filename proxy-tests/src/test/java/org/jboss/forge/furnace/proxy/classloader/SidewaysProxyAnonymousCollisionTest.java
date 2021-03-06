/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.proxy.classloader;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.arquillian.services.LocalServices;
import org.jboss.forge.classloader.mock.sidewaysproxy.AbstractExtra;
import org.jboss.forge.classloader.mock.sidewaysproxy.Action;
import org.jboss.forge.classloader.mock.sidewaysproxy.Action1;
import org.jboss.forge.classloader.mock.sidewaysproxy.Context;
import org.jboss.forge.classloader.mock.sidewaysproxy.ContextImpl;
import org.jboss.forge.classloader.mock.sidewaysproxy.ContextValue;
import org.jboss.forge.classloader.mock.sidewaysproxy.ContextValueImpl;
import org.jboss.forge.classloader.mock.sidewaysproxy.Extra;
import org.jboss.forge.classloader.mock.sidewaysproxy.Payload;
import org.jboss.forge.classloader.mock.sidewaysproxy.Payload1;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.proxy.ClassLoaderAdapterBuilder;
import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SidewaysProxyAnonymousCollisionTest
{
   @Deployment(order = 3)
   public static AddonArchive getDeploymentA()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML()
               .addClasses(Context.class, ContextImpl.class, ContextValue.class, Action.class, Action1.class,
                        Payload.class, Payload1.class, Extra.class, AbstractExtra.class, ContextValueImpl.class)
               .addAsLocalServices(SidewaysProxyAnonymousCollisionTest.class);

      return archive;
   }

   @Deployment(name = "B,1", testable = false, order = 2)
   public static AddonArchive getDeploymentB()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(Action1.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("D", "1")
               );

      return archive;
   }

   @Deployment(name = "C,1", testable = false, order = 1)
   public static AddonArchive getDeploymentC()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(Payload.class, Payload1.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("E", "1")
               );

      return archive;
   }

   @Deployment(name = "D,1", testable = false, order = 1)
   public static AddonArchive getDeploymentD()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(Context.class, Action.class, ContextImpl.class, ContextValue.class);

      return archive;
   }

   @Deployment(name = "E,1", testable = false, order = 1)
   public static AddonArchive getDeploymentE()
   {
      AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
               .addClasses(Extra.class, AbstractExtra.class);

      return archive;
   }

   @Test
   public void testSidewaysCollision() throws Exception
   {
      AddonRegistry registry = LocalServices.getFurnace(getClass().getClassLoader())
               .getAddonRegistry();
      ClassLoader A = this.getClass().getClassLoader();
      ClassLoader B = registry.getAddon(AddonId.from("B", "1")).getClassLoader();
      ClassLoader C = registry.getAddon(AddonId.from("C", "1")).getClassLoader();

      Class<?> typeAction1 = B.loadClass(Action1.class.getName());
      Action action1 = getProxiedInstance(A, B, typeAction1);

      Class<?> typePayload1 = C.loadClass(Payload1.class.getName());
      Payload payload1 = getProxiedInstance(A, C, typePayload1);

      Context context = new ContextImpl();
      ContextValue<Payload> value = new ContextValueImpl<Payload>();
      value.set(payload1);
      context.set(value);

      action1.handle(context);
   }

   @SuppressWarnings("unchecked")
   private <T> T getProxiedInstance(ClassLoader A, ClassLoader B, Class<?> type)
            throws InstantiationException,
            IllegalAccessException
   {
      Object delegate = type.newInstance();
      T enhanced = (T) ClassLoaderAdapterBuilder.callingLoader(A).delegateLoader(B).enhance(delegate);
      Assert.assertTrue(Proxies.isForgeProxy(enhanced));
      return enhanced;
   }
}
