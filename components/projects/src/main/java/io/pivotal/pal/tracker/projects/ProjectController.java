package io.pivotal.pal.tracker.projects;

import io.pivotal.pal.tracker.projects.data.ProjectDataGateway;
import io.pivotal.pal.tracker.projects.data.ProjectFields;
import io.pivotal.pal.tracker.projects.data.ProjectRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.pivotal.pal.tracker.projects.ProjectInfo.projectInfoBuilder;
import static io.pivotal.pal.tracker.projects.data.ProjectFields.projectFieldsBuilder;
import static io.pivotal.pal.tracker.restsupport.InjectDelay.injectDelay;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final Logger log = LoggerFactory.getLogger(ProjectController.class);
    private final Tracer tracer;

    private final ProjectDataGateway gateway;

    public ProjectController(ProjectDataGateway gateway, Tracer tracer) {
        this.tracer = tracer;
        this.gateway = gateway;
    }

    @PostMapping
    public ResponseEntity<ProjectInfo> create(@RequestBody ProjectForm form) {
        ProjectRecord record = gateway.create(formToFields(form));
        return new ResponseEntity<>(present(record), HttpStatus.CREATED);
    }

    @GetMapping
    public List<ProjectInfo> list(@RequestParam long accountId) {
        return gateway.findAllByAccountId(accountId)
            .stream()
            .map(this::present)
            .collect(toList());
    }

    @GetMapping("/{projectId}")
    public ProjectInfo get(@PathVariable long projectId) {
        injectDelay(1L);

        Span findProjectSpan = this.tracer.createSpan("ProjectController.findProject");

        log.info("Finding project with ID {}", projectId);

        ProjectRecord record = gateway.find(projectId);

        this.tracer.close(findProjectSpan);

        if (record != null) {
            return present(record);
        }

        return null;
    }


    private ProjectFields formToFields(ProjectForm form) {
        return projectFieldsBuilder()
            .accountId(form.accountId)
            .name(form.name)
            .active(form.active)
            .build();
    }

    private ProjectInfo present(ProjectRecord record) {
        return projectInfoBuilder()
            .id(record.id)
            .accountId(record.accountId)
            .name(record.name)
            .active(record.active)
            .info("project info")
            .build();
    }
}
