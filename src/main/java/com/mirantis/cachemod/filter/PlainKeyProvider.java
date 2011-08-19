package com.mirantis.cachemod.filter;


public class PlainKeyProvider extends AbstractKeyProvider {

  @Override
  protected String shortQueryString(String queryString) {
    return queryString;
  }

}
