/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.forge.arquillian.archive.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.arquillian.services.LocalServices;
import org.jboss.forge.furnace.lifecycle.AddonLifecycleProvider;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.impl.base.container.ContainerBase;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class AddonArchiveImpl extends ContainerBase<AddonArchive> implements AddonArchive
{
   // -------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(AddonArchiveImpl.class.getName());

   /**
    * Path to the web inside of the Archive.
    */
   private static final ArchivePath PATH_ROOT = ArchivePaths.root();

   /**
    * Path to the classes inside of the Archive.
    */
   private static final ArchivePath PATH_CLASSES = ArchivePaths.create(PATH_ROOT, "");

   /**
    * Path to the libraries inside of the Archive.
    */
   private static final ArchivePath PATH_LIBRARY = ArchivePaths.create(PATH_ROOT, "lib");

   /**
    * Path to the manifests inside of the Archive.
    */
   private static final ArchivePath PATH_MANIFEST = ArchivePaths.create("META-INF");

   /**
    * Path to the forge XML config file inside of the Archive.
    */
   private static final ArchivePath PATH_FORGE_XML = ArchivePaths.create("META-INF/forge.xml");

   /**
    * Path to web archive service providers.
    */
   private static final ArchivePath PATH_SERVICE_PROVIDERS = ArchivePaths.create(PATH_CLASSES, "META-INF/services");

   private List<AddonDependencyEntry> addonDependencies = new ArrayList<AddonDependencyEntry>();

   private String repository;

   private int deploymentTimeout = 10000;

   private TimeUnit deploymentTimeoutUnit;

   // -------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   // -------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * Create a new {@link AddonArchive} with any type storage engine as backing.
    * 
    * @param delegate The storage backing.
    */
   public AddonArchiveImpl(final Archive<?> delegate)
   {
      super(AddonArchive.class, delegate);
   }

   // -------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * 
    * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getManifestPath()
    */
   @Override
   protected ArchivePath getManifestPath()
   {
      return PATH_MANIFEST;
   }

   protected ArchivePath getForgeXMLPath()
   {
      return PATH_FORGE_XML;
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getClassesPath()
    */
   @Override
   protected ArchivePath getClassesPath()
   {
      return PATH_CLASSES;
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.jboss.shrinkwrap.impl.base.container.ContainerBase#getLibraryPath()
    */
   @Override
   protected ArchivePath getLibraryPath()
   {
      return PATH_LIBRARY;
   }

   protected ArchivePath getServiceProvidersPath()
   {
      return PATH_SERVICE_PROVIDERS;
   }

   @Override
   protected ArchivePath getResourcePath()
   {
      return PATH_CLASSES;
   }

   @Override
   public AddonArchive addAsAddonDependencies(AddonDependencyEntry... dependencies)
   {
      if (dependencies != null)
         addonDependencies.addAll(Arrays.asList(dependencies));
      return this;
   }

   @Override
   public List<AddonDependencyEntry> getAddonDependencies()
   {
      return addonDependencies;
   }

   @Override
   public AddonArchive addBeansXML()
   {
      addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
      return this;
   }

   @Override
   public AddonArchive addBeansXML(Asset resource)
   {
      addAsManifestResource(resource, ArchivePaths.create("beans.xml"));
      return this;
   }

   @Override
   public String getAddonRepository()
   {
      return repository;
   }

   @Override
   public AddonArchive setAddonRepository(String repository)
   {
      this.repository = repository;
      return this;
   }

   private static final String SERVICE_REGISTRATION_FILE_NAME = "org.jboss.forge.furnace.services.Exported";

   @Override
   public AddonArchive addAsLocalServices(Class<?>... serviceTypes)
   {
      addAsServiceProvider(AddonLifecycleProvider.class, LocalServices.class);
      addPackages(true, LocalServices.class.getPackage());
      Set<String> typeNames = new LinkedHashSet<String>();
      for (Class<?> type : serviceTypes)
      {
         typeNames.add(type.getName());
      }

      addAsServiceProvider(SERVICE_REGISTRATION_FILE_NAME,
               typeNames.toArray(new String[typeNames.size()]));
      return this;
   }

   @Override
   public AddonArchive setDeploymentTimeoutQuantity(int quantity)
   {
      this.deploymentTimeout = quantity;
      return this;
   }

   @Override
   public int getDeploymentTimeoutQuantity()
   {
      return this.deploymentTimeout;
   }

   @Override
   public AddonArchive setDeploymentTimeoutUnit(TimeUnit unit)
   {
      this.deploymentTimeoutUnit = unit;
      return this;
   }

   @Override
   public TimeUnit getDeploymentTimeoutUnit()
   {
      return deploymentTimeoutUnit == null ? TimeUnit.MILLISECONDS : deploymentTimeoutUnit;
   }
}
