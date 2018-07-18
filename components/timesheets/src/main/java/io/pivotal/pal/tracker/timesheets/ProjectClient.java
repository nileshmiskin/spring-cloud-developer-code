package io.pivotal.pal.tracker.timesheets;

import io.pivotal.pal.tracker.restsupport.ServiceLocator;
import org.springframework.web.client.RestOperations;

public class ProjectClient {

    private final ServiceLocator serviceLocator;
    private final RestOperations restOperations;

    public ProjectClient(ServiceLocator serviceLocator,
                         RestOperations restOperations) {
        this.serviceLocator = serviceLocator;
        this.restOperations = restOperations;
    }

    public ProjectInfo getProject(long projectId) {
        String endpoint = serviceLocator.lookUpServiceUrl("registration-server");
        return restOperations.getForObject(endpoint + "/projects/"
                + projectId, ProjectInfo.class);
    }


}