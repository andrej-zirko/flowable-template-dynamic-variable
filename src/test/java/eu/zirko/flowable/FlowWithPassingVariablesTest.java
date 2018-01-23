package eu.zirko.flowable;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FlowWithPassingVariablesTest {
	private static final String STEP_TEMPLATE_PROCESS_KEY = "stepTemplateProcess";
	private static final String POSITIVE_STEP_PROCESS_KEY = "positiveStepProcess";
	private static final String TEST_VARIABLE1 = "testVariable1";
	private static final String TEST_VARIABLE_CONTENT1 = "testVariableContent1";
	private static final String TEST_VARIABLE2 = "testVariable2";
	private static final String TEST_VARIABLE_CONTENT2 = "testVariableContent2";
	private static final String RESULT_VARIABLE1 = "resultVariable1";
	private static final String RESULT_VARIABLE2 = "resultVariable2";
	private static final String FLOW_UNDER_TEST_PROCESS_KEY = "flowWithPassingVariablesProcess";
	
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private FlowUtils flowUtils;

	@Test
	public void should_passVariablesToAndFromStepImplementation() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(FLOW_UNDER_TEST_PROCESS_KEY);
		
		String flowProcessInstanceId = processInstance.getId();
		String stepTemplateProcessInstanceId = flowUtils.getSubProcessInstanceId(processInstance, STEP_TEMPLATE_PROCESS_KEY);
		String stepImplProcessInstanceId = flowUtils.getSubProcessInstanceId(processInstance, POSITIVE_STEP_PROCESS_KEY);

		// input variables are present in step template
		flowUtils.assertVariableValue(stepTemplateProcessInstanceId, TEST_VARIABLE1, TEST_VARIABLE_CONTENT1);
		flowUtils.assertVariableValue(stepTemplateProcessInstanceId, TEST_VARIABLE2, TEST_VARIABLE_CONTENT2);

		// input variables are passed to stepImpl
		flowUtils.assertVariableValue(stepImplProcessInstanceId, TEST_VARIABLE1, TEST_VARIABLE_CONTENT1);
		flowUtils.assertVariableValue(stepImplProcessInstanceId, TEST_VARIABLE2, TEST_VARIABLE_CONTENT2);

		// result variables are passed back to step template
		flowUtils.assertVariableValue(stepTemplateProcessInstanceId, RESULT_VARIABLE1, TEST_VARIABLE_CONTENT1);
		flowUtils.assertVariableValue(stepTemplateProcessInstanceId, RESULT_VARIABLE2, TEST_VARIABLE_CONTENT2);

		// result variables are passed back to flow
		flowUtils.assertVariableValue(flowProcessInstanceId, RESULT_VARIABLE1, TEST_VARIABLE_CONTENT1);
		flowUtils.assertVariableValue(flowProcessInstanceId, RESULT_VARIABLE2, TEST_VARIABLE_CONTENT2);
	}
}
