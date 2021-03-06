/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.furnace.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.forge.furnace.exception.ContainerException;

/**
 * Utility class for executing fragments of code within a specific {@link Thread#getContextClassLoader()}
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ClassLoaders
{
   private static Logger log = Logger.getLogger(ClassLoaders.class.getName());

   /**
    * Execute the given {@link Callable} in the {@link ClassLoader} provided. Return the result, if any.
    */
   public static <T> T executeIn(ClassLoader loader, Callable<T> task) throws Exception
   {
      if (task == null)
         return null;

      if (log.isLoggable(Level.FINE))
      {
         log.fine("ClassLoader [" + loader + "] task began.");
      }
      ClassLoader original = SecurityActions.getContextClassLoader();
      try
      {
         SecurityActions.setContextClassLoader(loader);
         return task.call();
      }
      finally
      {
         SecurityActions.setContextClassLoader(original);
         if (log.isLoggable(Level.FINE))
         {
            log.fine("ClassLoader [" + loader + "] task ended.");
         }
      }
   }

   /**
    * Execute the given {@link Callable} by creating an {@link URLClassLoader} from the given {@link URL} array.
    * <p/>
    * Return the result, if any.
    */
   public static <T> T executeIn(URL[] urls, Callable<T> callable) throws Exception
   {
      ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
      URLClassLoader newClassLoader = null;
      try
      {
         newClassLoader = new URLClassLoader(urls, savedClassLoader);
         return executeIn(newClassLoader, callable);
      }
      finally
      {
         Streams.closeQuietly(newClassLoader);
      }
   }

   public static boolean containsClass(ClassLoader loader, Class<?> type)
   {
      if (loader == null)
         throw new IllegalArgumentException("Class loader to inspect must not be null.");
      if (type == null)
         throw new IllegalArgumentException("Class to find must not be null.");

      try
      {
         return loader.loadClass(type.getName()) == type;
      }
      catch (ClassNotFoundException | LinkageError e)
      {
         return false;
      }
   }

   public static boolean containsClass(ClassLoader loader, String type)
   {
      if (loader == null)
         throw new IllegalArgumentException("Class loader to inspect must not be null.");
      if (type == null)
         throw new IllegalArgumentException("Class to find must not be null.");

      try
      {
         loader.loadClass(type);
         return true;
      }
      catch (ClassNotFoundException | LinkageError e)
      {
         return false;
      }
   }

   public static Class<?> loadClass(ClassLoader loader, String typeName)
   {
      if (loader == null)
         throw new IllegalArgumentException("Class loader to inspect must not be null.");
      if (typeName == null)
         throw new IllegalArgumentException("Class name to find must not be null.");

      try
      {
         return loader.loadClass(typeName);
      }
      catch (ClassNotFoundException | LinkageError e)
      {
         throw new ContainerException("Could not locate class [" + typeName + "] in Loader [" + loader + "]", e);
      }
   }

   public static Class<?> loadClass(ClassLoader loader, Class<?> type)
   {
      if (loader == null)
         throw new IllegalArgumentException("Class loader to inspect must not be null.");
      if (type == null)
         throw new IllegalArgumentException("Class to find must not be null.");

      try
      {
         return loader.loadClass(type.getName());
      }
      catch (ClassNotFoundException | LinkageError e)
      {
         throw new ContainerException("Could not locate class [" + type.getName() + "] in Loader [" + loader + "]", e);
      }
   }

   public static boolean ownsClass(ClassLoader loader, Class<?> type)
   {
      if (loader == null)
         throw new IllegalArgumentException("Class loader to inspect must not be null.");
      if (type == null)
         throw new IllegalArgumentException("Class to find must not be null.");

      return loader.equals(type.getClassLoader());
   }

   public static Throwable getClassLoadingExceptionFor(ClassLoader loader, String typeName)
   {
      try
      {
         loader.loadClass(typeName);
         return null;
      }
      catch (ClassNotFoundException | LinkageError e)
      {
         return e;
      }
   }
}
