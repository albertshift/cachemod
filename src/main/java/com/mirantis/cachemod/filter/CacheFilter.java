package com.mirantis.cachemod.filter;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheFilter implements Filter {

  private static final Log log = LogFactory.getLog(CacheFilter.class);

  private CacheConfiguration conf = null;

  public void destroy() {
  }

  private boolean isCacheable(ServletRequest request) {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      if (conf.getEscapeMethods().contains(httpRequest.getMethod())) {
        return false;
      }
      if (conf.isEscapeSessionId() && httpRequest.isRequestedSessionIdFromURL()) {
        return false;
      }
    }
    return true;
  }

  public boolean isFragment(HttpServletRequest request) {

    switch(conf.getFragment()) {
    case AUTO:
      return request.getAttribute("javax.servlet.include.request_uri") != null;
    case NO:
      return false;
    case YES:
      return true;
    }
    
    return request.getAttribute("javax.servlet.include.request_uri") != null;
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

    if (request.getAttribute(conf.getAlreadyFilteredKey()) != null || !isCacheable(request)) {
      
      /*
       *  This request is not Cacheable
       */
      
      chain.doFilter(request, response);
    } else {
      request.setAttribute(conf.getAlreadyFilteredKey(), Boolean.TRUE);

      HttpServletRequest httpRequest = (HttpServletRequest) request;
      boolean fragmentRequest = isFragment(httpRequest);

      String key = conf.getCacheKeyProvider().createKey(httpRequest);

      CacheEntry cacheEntry = conf.getCacheProvider().getEntry(key);
      if (cacheEntry != null) {

        if (!fragmentRequest) {
          
          /*
           * -1 of no in header
           */
          long clientLastModified = httpRequest.getDateHeader("If-Modified-Since"); 
          
          /*
           * Reply with SC_NOT_MODIFIED for client that has newest page 
           */
          if ((clientLastModified != -1) && (clientLastModified >= cacheEntry.getLastModified())) {
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
          }
        }

        writeCacheToResponse(cacheEntry, response, fragmentRequest);

      } else {
        cacheEntry = conf.getCacheProvider().instantiateEntry();
        CacheHttpServletResponse cacheResponse = new CacheHttpServletResponse((HttpServletResponse) response, cacheEntry, conf, fragmentRequest);
        chain.doFilter(request, cacheResponse);

        if (cacheResponse.getStatus() == HttpServletResponse.SC_OK) {
          cacheResponse.commit();
          if (conf.getUserDataProvider() != null) {
            cacheEntry.setUserData(conf.getUserDataProvider().createUserData(httpRequest));
          }
          conf.getCacheProvider().putEntry(key, cacheEntry);
        }
      }
    }

  }

  public void writeCacheToResponse(CacheEntry cacheEntry, ServletResponse response, boolean fragment) throws IOException {

    if (cacheEntry.getContentType() != null) {
      response.setContentType(cacheEntry.getContentType());
    }

    if (!fragment) {

      if (response instanceof HttpServletResponse) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (cacheEntry.getLastModified() != -1) {
          httpResponse.setDateHeader("Last-Modified", cacheEntry.getLastModified());
        }

        if (cacheEntry.getExpires() != Long.MAX_VALUE) {
          httpResponse.setDateHeader("Expires", cacheEntry.getExpires());
        }

        if (cacheEntry.getMaxAge() != -1) {
          if (cacheEntry.getMaxAge() < 0) { // set max-age based on life time
            long currentMaxAge = - cacheEntry.getMaxAge() / 1000 - System.currentTimeMillis() / 1000;
            if (currentMaxAge < 0) {
              currentMaxAge = 0;
            }
            httpResponse.addHeader("Cache-Control", "max-age=" + currentMaxAge);
          } else {
            httpResponse.addHeader("Cache-Control", "max-age=" + cacheEntry.getMaxAge());
          }
        }

      }
    }

    if (cacheEntry.getLocale() != null) {
      response.setLocale(cacheEntry.getLocale());
    }

    OutputStream out = new BufferedOutputStream(response.getOutputStream());
    response.setContentLength(cacheEntry.getContent().length);
    out.write(cacheEntry.getContent());
    out.flush();
  }
  
  public void init(FilterConfig config) {
    conf = new CacheConfiguration(config);
    
    log.info("CacheFilter: Filter starting. " + conf.toString());
    conf.getCacheProvider().init(conf.getCacheName());
    
    if (conf.getUserDataProvider() != null) {
      conf.getUserDataProvider().createIndexes(conf.getCacheProvider().getCache());
    }
  }

}
