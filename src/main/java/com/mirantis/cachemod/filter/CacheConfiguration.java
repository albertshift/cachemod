/*
 * Copyright 2011 Mirantis Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mirantis.cachemod.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.FilterConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheConfiguration {

  private static final Log log = LogFactory.getLog(CacheConfiguration.class);
  
  private static final String DEF_CACHE_NAME = "cachemod-filter";
  private final static String ALREADY_FILTERED_KEY = "com.mirantis.cachemod.filter.";
  
  private String alreadyFilteredKey;

  public static enum FragmentType {
    YES, NO, AUTO;
  }
  
  private FragmentType fragment = FragmentType.AUTO;
  
  private Set<String> escapeMethods = new HashSet<String>();
  private boolean escapeSessionId = true;
  
  private int time = 60;
  
  public static enum LastModifiedType {
    ON, OFF, INITIAL;
  }

  private LastModifiedType lastModified = LastModifiedType.INITIAL;
  
  public static enum ExpiresType {
    ON, OFF, TIME;
  }
  
  private ExpiresType expires = ExpiresType.ON;
  
  public static enum MaxAgeType {
    NO_INIT, TIME, NUMBER;
  }
  
  private MaxAgeType maxAgeType = MaxAgeType.NUMBER;
  private long maxAge = 60;
  
  private CacheProvider cacheProvider = null;
  private KeyProvider cacheKeyProvider = null;
  private UserDataProvider userDataProvider = null;
  
  private String cacheName;
  
  public CacheConfiguration(FilterConfig config) {
    
    alreadyFilteredKey = ALREADY_FILTERED_KEY + config.getFilterName();

    String fragmentParam = config.getInitParameter("fragment");
    if (fragmentParam != null) {
        if ("no".equalsIgnoreCase(fragmentParam)) {
          fragment = FragmentType.NO;
        } else if ("yes".equalsIgnoreCase(fragmentParam)) {
          fragment = FragmentType.YES;
        } else if ("auto".equalsIgnoreCase(fragmentParam)) {
          fragment = FragmentType.AUTO;
        } else {
            log.error("CacheFilter: Wrong value '" + fragmentParam + "' for init parameter 'fragment', default is 'auto'.");
        }
    }    
    
    String escapeSessionIdParam = config.getInitParameter("escapeSessionId");
    if (escapeSessionIdParam != null) {
      if ("on".equalsIgnoreCase(escapeSessionIdParam)) {
        escapeSessionId = true;
      } else if ("off".equalsIgnoreCase(escapeSessionIdParam)) {
        escapeSessionId = false;
      } else {
        log.error("CacheFilter: Wrong value '" + escapeSessionIdParam + "' for init parameter 'escapeSessionId', default is 'no'.");
      }
    }
    
    String escapeMethodsParam = config.getInitParameter("escapeMethods");
    if (escapeMethodsParam != null && escapeMethodsParam.length() > 0) {
      StringTokenizer tokenizer = new StringTokenizer(escapeMethodsParam);
      while(tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken().trim();
        if (token != null && token.length() > 0) {
          escapeMethods.add(token.toUpperCase());
        }
      }
    }

    String timeParam = config.getInitParameter("time");
    if (timeParam != null) {
      try {
      time = Integer.parseInt(timeParam);
      } catch (NumberFormatException nfe) {
        log.error("CacheFilter: Unexpected value for the init parameter 'time', default is '60'. Message=" + nfe.getMessage());
      }
    }

    String lastModifiedParam = config.getInitParameter("lastModified");
    if (lastModifiedParam != null) {
      if ("on".equalsIgnoreCase(lastModifiedParam)) {
        lastModified = LastModifiedType.ON;
      }
      else if ("off".equalsIgnoreCase(lastModifiedParam)) {
        lastModified = LastModifiedType.OFF;
      }
      else if ("initial".equalsIgnoreCase(lastModifiedParam)) {
        lastModified = LastModifiedType.INITIAL;
      }
      else {
        log.error("CacheFilter: Invalid parameter 'lastModified'. Expected 'on', 'off' or 'initial'. Be default is 'initial'.");
      }
    }

    String expiresParam = config.getInitParameter("expires");
    if (expiresParam != null) {
      if ("on".equalsIgnoreCase(expiresParam)) {
        expires = ExpiresType.ON;
      }
      else if ("off".equalsIgnoreCase(expiresParam)) {
        expires = ExpiresType.OFF;
      }
      else if ("time".equalsIgnoreCase(expiresParam)) {
        expires = ExpiresType.TIME;
      }
      else {
        log.error("CacheFilter: Invalid parameter 'expires'. Expected 'on', 'off' or 'time'. Be default is 'on'.");
      }
    }
    
    String maxAgeParam = config.getInitParameter("max-age");
    if (maxAgeParam != null) {
      if (maxAgeParam.equalsIgnoreCase("no init")) {
        maxAgeType = MaxAgeType.NO_INIT;
      } else if (maxAgeParam.equalsIgnoreCase("time")) {
        maxAgeType = MaxAgeType.TIME;
      } else {
        try {
          maxAgeType = MaxAgeType.NUMBER;
          maxAge = Long.parseLong(maxAgeParam);
          if (maxAge < 0) {
            log.error("CacheFilter: 'max-age' parameter must be at least a positive integer, default is '60'.");
            maxAge = 60;
          }
        } catch (NumberFormatException nfe) {
          log.error("CacheFilter: Unexpected value for the init parameter 'max-age', default is '60'. Message=" + nfe.getMessage());
        }
      }
    }
    
    CacheProvider cacheProviderParam = (CacheProvider) initClass(config, "CacheProvider", CacheProvider.class);
    if (cacheProviderParam != null) {
      this.cacheProvider = cacheProviderParam;
    }
    else {
      this.cacheProvider = new LRUCacheProvider();
    }

    KeyProvider cacheKeyProviderParam = (KeyProvider) initClass(config, "KeyProvider", KeyProvider.class);
    if (cacheKeyProviderParam != null) {
      this.cacheKeyProvider = cacheKeyProviderParam;
    }
    else {
      this.cacheKeyProvider = new HashedKeyProvider();
    }

    UserDataProvider userDataProviderParam = (UserDataProvider) initClass(config, "UserDataProvider", UserDataProvider.class);
    if (userDataProviderParam != null) {
      this.userDataProvider = userDataProviderParam;
    }
    
    this.cacheName = config.getInitParameter("cacheName");
    if (cacheName == null) {
      this.cacheName = DEF_CACHE_NAME;
    }

  }
 
  private Object initClass(FilterConfig config, String classInitParam, Class interfaceClass) {
    String className = config.getInitParameter(classInitParam);
    if (className != null) {
      try {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
          classLoader = this.getClass().getClassLoader();
        }
        Class clazz = classLoader.loadClass(className);
        if (!interfaceClass.isAssignableFrom(clazz)) {
          log.error("CacheFilter: Specified class '" + className + "' does not implement" + interfaceClass.getName());
          return null;
        } else {
          return clazz.newInstance();
        }
      } catch (ClassNotFoundException e) {
        log.error("CacheFilter: Class '" + className + "' not found.", e);
      } catch (InstantiationException e) {
        log.error("CacheFilter: Class '" + className + "' could not be instantiated because it is not a concrete class.", e);
      } catch (IllegalAccessException e) {
        log.error("CacheFilter: Class '" + className + "' could not be instantiated because it is not public.", e);
      }
    }
    return null;
  }

  public String getAlreadyFilteredKey() {
    return alreadyFilteredKey;
  }

  public FragmentType getFragment() {
    return fragment;
  }

  public Set<String> getEscapeMethods() {
    return escapeMethods;
  }

  public boolean isEscapeSessionId() {
    return escapeSessionId;
  }

  public int getTime() {
    return time;
  }

  public LastModifiedType getLastModified() {
    return lastModified;
  }

  public ExpiresType getExpires() {
    return expires;
  }

  public MaxAgeType getMaxAgeType() {
    return maxAgeType;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public CacheProvider getCacheProvider() {
    return cacheProvider;
  }
  
  public KeyProvider getCacheKeyProvider() {
    return cacheKeyProvider;
  }

  public UserDataProvider getUserDataProvider() {
    return userDataProvider;
  }

  public String getCacheName() {
    return cacheName;
  }

  @Override
  public String toString() {
    return "CacheConfiguration [alreadyFilteredKey=" + alreadyFilteredKey + ", fragment=" + fragment + ", noCacheOnMethods=" + escapeMethods + ", noCacheWithSessionId="
        + escapeSessionId + ", time=" + time + ", lastModified=" + lastModified + ", expires=" + expires + ", maxAgeType=" + maxAgeType + ", maxAge=" + maxAge
        + ", cacheProvider=" + cacheProvider + ", cacheKeyProvider=" + cacheKeyProvider + ", userDataProvider=" + userDataProvider + "]";
  }
  
}
