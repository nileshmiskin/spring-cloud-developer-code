package io.pivotal.pal.tracker.projects;

import io.pivotal.pal.tracker.projects.data.ProjectDataGateway;
import io.pivotal.pal.tracker.projects.data.ProjectFields;
import io.pivotal.pal.tracker.projects.data.ProjectRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.pivotal.pal.tracker.projects.ProjectInfo.projectInfoBuilder;
import static io.pivotal.pal.tracker.projects.data.ProjectFields.projectFieldsBuilder;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectDataGateway gateway;
    private final long delayMillis;

    public ProjectController(ProjectDataGateway gateway,
                             @Value("${delay.millis:0}") long delayMillis) {
        this.gateway = gateway;
        this.delayMillis = delayMillis;
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
        ProjectRecord record = gateway.find(projectId);

        if (record != null) {
            log.info("returning project info for project id {}", projectId);

            if (delayMillis > 0) {
                log.info("artificial delay of {} ms", delayMillis);
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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