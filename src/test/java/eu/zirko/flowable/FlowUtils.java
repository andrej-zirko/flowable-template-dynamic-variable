package eu.zirko.flowable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class FlowUtils {
	private final HistoryService historyService;

	public FlowUtils(HistoryService historyService) {
		super();
		this.historyService = historyService;
	}

	public List<String> getSubProcessInstanceIds(ProcessInstance processInstance, String subProcessKey) {
		List<String> stepProcessInstanceIds = listSubProcessInstances(processInstance.getId(), subProcessKey).stream()
				.map(h -> h.getId()).collect(Collectors.toList());

		return stepProcessInstanceIds;
	}
	
	public String getSubProcessInstanceId(ProcessInstance processInstance, String subProcessKey) {
		List<String> stepProcessInstanceIds = getSubProcessInstanceIds(processInstance, subProcessKey);
		
		assertThat(stepProcessInstanceIds.size()).isEqualTo(1);
		
		return stepProcessInstanceIds.get(0);
	}

	public List<HistoricProcessInstance> listSubProcessInstances(String rootProcessInstanceId,
			String subProcessDefinitionKey) {
		List<HistoricProcessInstance> subProcessesInstances = listAllSubProcessesInstances(rootProcessInstanceId);

		List<HistoricProcessInstance> subProcessInstances = subProcessesInstances.stream()
				.filter(instance -> instance.getProcessDefinitionKey().equals(subProcessDefinitionKey))
				.collect(Collectors.toList());

		return subProcessInstances;
	}

	public List<HistoricProcessInstance> listAllSubProcessesInstances(String superProcessInstanceId) {
		List<HistoricProcessInstance> result = new ArrayList<>();

		List<HistoricProcessInstance> subprocesses = historyService.createHistoricProcessInstanceQuery()
				.superProcessInstanceId(superProcessInstanceId).list();

		for (HistoricProcessInstance historicProcessInstance : subprocesses) {
			result.add(historicProcessInstance);
			List<HistoricProcessInstance> subResult = listAllSubProcessesInstances(historicProcessInstance.getId());
			result.addAll(subResult);
		}

		return result;
	}

	public void assertVariableValue(String processInstanceId, String variableName, Object expectedVariableValue) {
		HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstanceId).variableName(variableName).singleResult();
		Assert.notNull(historicVariableInstance, "Variable [" + variableName + "] is not set !");
		Object variableValue = historicVariableInstance.getValue();
		Assert.notNull(variableValue, "Variable [" + variableName + "] is not set !");

		assertThat(variableValue).isEqualTo(expectedVariableValue);
	}
}
