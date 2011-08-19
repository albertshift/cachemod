package com.mirantis.cachemod.filter;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

public class HashedKeyProvider extends AbstractKeyProvider {

  @Override
  protected String shortQueryString(String queryString) {
    return Base64.encodeBase64String(digest(queryString)).replace('/', '_');
  }

  private byte[] digest(String str) {
    try {
      java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
      return digest.digest(str.getBytes());
    } catch (NoSuchAlgorithmException e) {
      // ignore exception
      return str.getBytes();
    }

  }
  
}
