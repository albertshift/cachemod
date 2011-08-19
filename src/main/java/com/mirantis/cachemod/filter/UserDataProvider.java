package com.mirantis.cachemod.filter;

import javax.servlet.http.HttpServletRequest;

public interface UserDataProvider {

  public void createIndexes(Object cache);
  
  public Object createUserData(HttpServletRequest httpRequest);

}
