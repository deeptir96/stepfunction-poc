package com.example.restservice;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.model.GetActivityTaskRequest;
import com.amazonaws.services.stepfunctions.model.GetActivityTaskResult;
import com.amazonaws.services.stepfunctions.model.SendTaskFailureRequest;
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class GreetingController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();


	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) throws InterruptedException {
		//
		log.info("entered greeting controller");
		Boolean success = false;
		if(name.equals("hello")) {
			success = true;
		}
		else {
			success = false;
		}
		//
		Greeting output = new Greeting(counter.incrementAndGet(), String.format(template, name));
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		clientConfiguration.setSocketTimeout((int) TimeUnit.SECONDS.toMillis(70));

		AWSStepFunctions client = AWSStepFunctionsClientBuilder.standard()
				.withRegion(Regions.US_EAST_2)
				.withCredentials(new InstanceProfileCredentialsProvider(false))
				.withClientConfiguration(clientConfiguration)
				.build();

		while (true) {
			GetActivityTaskResult getActivityTaskResult =
					client.getActivityTask(
							new GetActivityTaskRequest().withActivityArn("arn:aws:states:us-east-2:311958188063:activity:parse-greeting"));

			if (getActivityTaskResult.getTaskToken() != null) {
				try {
//					JsonNode json = Jackson.jsonNodeOf(getActivityTaskResult.getInput());
//					String greetingResult = greeterActivities.getGreeting(json.get("who").textValue());
					client.sendTaskSuccess(new SendTaskSuccessRequest().withOutput(output.getContent())
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
