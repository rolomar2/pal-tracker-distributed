package io.pivotal.pal.tracker.timesheets;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProjectClient {

    private final RestOperations restOperations;
    private final String endpoint;
    private final Map<Long, ProjectInfo> projCache = new ConcurrentHashMap<>();
    private final Logger myLog = LoggerFactory.getLogger(getClass());

    public ProjectClient(RestOperations restOperations, String registrationServerEndpoint) {
        this.restOperations = restOperations;
        this.endpoint = registrationServerEndpoint;
    }

    @CircuitBreaker(name = "project", fallbackMethod = "getProjectFromCache")
    public ProjectInfo getProject(long projectId) {
        ProjectInfo newProj = restOperations.getForObject(endpoint + "/projects/" + projectId, ProjectInfo.class);

        projCache.put(projectId, newProj);

        myLog.info("Adding project with id {} to cache", projectId);

        return newProj;
    }

    public ProjectInfo getProjectFromCache(long projectId, Throwable cause) {
        myLog.info("Getting project with id {} from cache. Cause {}", projectId, cause.getMessage());
        return projCache.get(projectId);
    }
}
