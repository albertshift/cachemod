package com.mirantis.cachemod.filter;

import javax.servlet.http.HttpServletRequest;

public interface KeyProvider {

  public String createKey(HttpServletRequest httpRequest);
  
}
