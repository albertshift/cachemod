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
