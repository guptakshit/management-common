/*
 * All content copyright (c) 2003-2012 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 * Copyright IBM Corp. 2024, 2025
 */

package net.sf.ehcache.management.resource.services.validator;

import org.terracotta.management.resource.exceptions.ResourceRuntimeException;
import org.terracotta.management.resource.services.Utils;
import org.terracotta.management.resource.services.validator.RequestValidator;

import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract implementation of ehcache resource service {@link RequestValidator}.
 * <br>
 * {@inheritDoc}
 *
 * @author brandony
 */
public abstract class AbstractEhcacheRequestValidator implements RequestValidator {

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract void validateSafe(UriInfo info);

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(UriInfo info) {
    validateCacheRequestSegment(info.getPathSegments());
  }

  protected void validateCacheRequestSegment(List<PathSegment> pathSegments) {
    if (pathSegments.size() >= 3) {
      String cacheNames = pathSegments.get(2).getMatrixParameters().getFirst("names");
      Set<String> cNames = Utils.trimToNull(cacheNames) == null ? null : new HashSet<String>(
          Arrays.asList(cacheNames.split(",")));
      if (cNames == null) {
        throw new ResourceRuntimeException("No cache specified. Unsafe requests must specify a single cache name.",
            Response.Status.BAD_REQUEST.getStatusCode());
      }

      if (cNames.size() != 1) {
        throw new ResourceRuntimeException("Multiple caches specified. Unsafe requests must specify a single cache name.",
            Response.Status.BAD_REQUEST.getStatusCode());
      }
    }

    validateCacheManagerRequestSegment(pathSegments);
  }

  protected void validateCacheManagerRequestSegment(List<PathSegment> pathSegments) {
    if (pathSegments.size() >= 2) {
      String cacheManagerNames = getCacheManagerPathSegmentAccordingToVersion(pathSegments).getMatrixParameters().getFirst("names");
      Set<String> cmNames = Utils.trimToNull(cacheManagerNames) == null ? null : new HashSet<String>(
          Arrays.asList(cacheManagerNames.split(",")));
      if (cmNames == null) {
        throw new ResourceRuntimeException("No cache manager specified. Unsafe requests must specify a single cache manager name.",
            Response.Status.BAD_REQUEST.getStatusCode());
      }

      if (cmNames.size() != 1) {
        throw new ResourceRuntimeException("Multiple cache managers specified. Unsafe requests must specify a single cache manager name.",
            Response.Status.BAD_REQUEST.getStatusCode());
      }
    }

    validateAgentSegment(pathSegments);
  }

  protected PathSegment getCacheManagerPathSegmentAccordingToVersion(List<PathSegment> pathSegments) {
    PathSegment cacheManagerPathSegment = null;
    if ("v2".equals(pathSegments.get(0).getPath())) {
      cacheManagerPathSegment = pathSegments.get(2);
    } else {
      // v1
      cacheManagerPathSegment = pathSegments.get(1);
    }
    return cacheManagerPathSegment;
  }

  protected abstract void validateAgentSegment(List<PathSegment> pathSegments);
}
