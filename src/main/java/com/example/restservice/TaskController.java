package com.example.restservice;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.model.GetActivityTaskRequest;
import com.amazonaws.services.stepfunctions.model.GetActivityTaskResult;
import com.amazonaws.services.stepfunctions.model.SendTaskFailureRequest;
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/task")
public class TaskController {

    @PostMapping("/create")
    public String createTask(@RequestBody Task task) {
        return "Task of type "+ task.getType()+" Created";
    }

    @PutMapping("/approve")
    public String submitApproval(@RequestBody Approval approval) throws InterruptedException {

        log.info("Entered put method");
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSocketTimeout((int) TimeUnit.SECONDS.toMillis(70));

        log.info("configured client");
        AWSStepFunctions client = AWSStepFunctionsClientBuilder.standard()
                .withRegion(Regions.US_EAST_2)
                .withCredentials(new InstanceProfileCredentialsProvider(false))
                .withClientConfiguration(clientConfiguration)
                .build();

        String output = "random_output";

        log.info("entering step function");
        while (true) {
            log.info("in while loop");
            GetActivityTaskResult getActivityTaskResult =
                    client.getActivityTask(
                            new GetActivityTaskRequest().withActivityArn("arn:aws:states:us-east-2:311958188063:activity:parse-greeting"));

            log.info("got activity task result");
            if (getActivityTaskResult.getTaskToken() != null) {
                try {
                    log.info("entered if loop");
//					JsonNode json = Jackson.jsonNodeOf(getActivityTaskResult.getInput());
//					String greetingResult = greeterActivities.getGreeting(json.get("who").textValue());
                    client.sendTaskSuccess(new SendTaskSuccessRequest().withOutput(output)
                            .withTaskToken(getActivityTaskResult.getTaskToken()));
                    return output;
                } catch (Exception e) {
                    log.info("Exception msg 1 - "+ e.getMessage());
                    client.sendTaskFailure(new SendTaskFailureRequest().withTaskToken(
                            getActivityTaskResult.getTaskToken()));
                }
            } else {
                Thread.sleep(1000);
            }
        }
    }
}
