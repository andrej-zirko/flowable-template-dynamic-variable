package eu.zirko.flowable;

import java.util.List;
import java.util.stream.Collectors;

import org.flowable.bpmn.model.CallActivity;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.IOParameter;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class StepImplCallParametersListener implements ExecutionListener {
	private static final long serialVersionUID = 1L;
	private static final int ENDLESS_LOOP_PROTECT = 10;

	@Override
	public void notify(DelegateExecution execution) {
		CallActivity callStepTemplateActivity = getParentCallActivity(execution);
		CallActivity callStepImplActivity = (CallActivity) execution.getCurrentFlowElement();

		List<IOParameter> callStepImplInParameters = mapInParametersFromParentCallActivity(callStepTemplateActivity);
//		callStepImplInParameters.add(createParameterWithSameSourceAndTarget(FlowContext.VAR_NAME_TRANSACTION_UNIT_ID));
		
		List<IOParameter> callStepImplOutParameters = mapOutParametersFromParentCallActivity(callStepTemplateActivity);

		// previously here was statement to add parameters
		// later it was changed to set as it was observed 
		// that flowable shares same call activity impl instance for all steps
		callStepImplActivity.setInParameters(callStepImplInParameters); 
		callStepImplActivity.setOutParameters(callStepImplOutParameters);
	}

	private List<IOParameter> mapInParametersFromParentCallActivity(CallActivity parentCallActivity) {
		return parentCallActivity.getInParameters().stream()
				.map(parentInParameter -> createParameterWithSameSourceAndTarget(parentInParameter.getTarget()))
				.collect(Collectors.toList());

	}

	private List<IOParameter> mapOutParametersFromParentCallActivity(CallActivity parentCallActivity) {
		return parentCallActivity.getOutParameters().stream()
				.map(parentOutParameter -> createParameterWithSameSourceAndTarget(parentOutParameter.getSource()))
				.collect(Collectors.toList());

	}

	private IOParameter createParameterWithSameSourceAndTarget(String sourceAndTarget) {
		IOParameter parameter = new IOParameter();
		parameter.setSource(sourceAndTarget);
		parameter.setTarget(sourceAndTarget);
		return parameter;
	}

	private CallActivity getParentCallActivity(DelegateExecution execution) {
		DelegateExecution parentExecution = execution;
		int loopCounter = 0;
		// superExecution could be get only on top parent execution
		// for stepTemplate as of time of writing, two parent levels must be reached
		// 1. parent - stepTemplateSubprocess, 2. parent - ProcessInstance
		while ((loopCounter < ENDLESS_LOOP_PROTECT) && (parentExecution.getParent() != null)) {
			parentExecution = parentExecution.getParent();
			loopCounter++;
		}

		Assert.isTrue(loopCounter < ENDLESS_LOOP_PROTECT,
				"Parent execution for execution [" + execution.getId() + "] not found");
		Assert.isInstanceOf(ExecutionEntity.class, parentExecution,
				"Parent execution for execution [" + execution.getId() + "] must be instance of ExecutionEntity");
		ExecutionEntity executionEntity = (ExecutionEntity) parentExecution;
		ExecutionEntity superExecution = executionEntity.getSuperExecution();
		Assert.notNull(superExecution, "Super execution for execution [" + execution.getId() + "] not found");

		FlowElement superFlowElement = superExecution.getCurrentFlowElement();
		Assert.isInstanceOf(CallActivity.class, superFlowElement, "Super execution current element for execution ["
				+ execution.getId() + "] must be instance of CallActivity");
		return (CallActivity) superFlowElement;
	}

}
