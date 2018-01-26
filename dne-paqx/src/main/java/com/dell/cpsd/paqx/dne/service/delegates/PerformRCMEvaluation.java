/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.service.delegates;

import com.dell.cpsd.paqx.dne.exception.TaskResponseFailureException;
import com.dell.cpsd.paqx.dne.service.NodeService;
import com.dell.cpsd.paqx.dne.service.delegates.model.NodeDetail;
import com.dell.cpsd.paqx.dne.service.delegates.model.RCMEvaluation;
import com.dell.cpsd.paqx.dne.util.RcmDataParsingUtil;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.NODE_DETAIL;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.EVALUATION_RESULTS;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.RACADM_FIRMWARE_LIST_CATALOG_FAILED;
import static com.dell.cpsd.paqx.dne.service.delegates.utils.DelegateConstants.RCM_EVALUATION_FAILED;

/**
 * Perform RCM Evaluation for node components
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
@Qualifier("performRCMEvaluation")
public class PerformRCMEvaluation extends BaseWorkflowDelegate
{

    /*
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformRCMEvaluation.class);

    /*
     * Node Service instance
     */
    private NodeService nodeService;

    /*
     * Rest template to make RCM REST calls
     */
    private RestTemplate restTemplate;

    private static final int TIMEOUT = 10000;

    /**
     * @param nodeService - The <code>NodeService</code> instance
     */
    @Autowired
    public PerformRCMEvaluation(final NodeService nodeService, final RestTemplate restTemplate)
    {
        super(LOGGER, "Perform RCM Evaluation");
        this.nodeService = nodeService;
        this.restTemplate = restTemplate;
    }

    @Override
    public void delegateExecute(final DelegateExecution delegateExecution)
    {
        NodeDetail nodeDetail = (NodeDetail) delegateExecution.getVariable(NODE_DETAIL);
        updateDelegateStatus("Attempting " + this.taskName + " on Node " + nodeDetail.getServiceTag() + ".");

        // Post racadm service to RackHD, this is precursor for collections job
        // Response send the system def component id (device id in RCM lingo), it is configured this way because of a bug in system
        // definition where component id in sys def does not match node uuid in node discovery
        String systemDefUuid = null;
        try
        {
            systemDefUuid = nodeService.postRacadmFirmwareListCatalogService(nodeDetail.getId(), nodeDetail.getMacAddress());
        }
        catch (TaskResponseFailureException e)
        {
            updateDelegateStatus("Error posting racadm-firmware-list-catalog to rackhd: " + e.getMessage(), e);
            throw new BpmnError(RACADM_FIRMWARE_LIST_CATALOG_FAILED, e.getMessage());
        }

        // Trigger RCM collections job
        nodeService.triggerRCMCollectionsJob();

        // Retrieve RCM uuid from rcm version
        String rcmUuid = fetchRCMUuidBasedOnVersion(nodeDetail.getRcmVersion());

        // Perform RCM evaluation
        Map<String, String> evalResults = performRcmEvaluation(rcmUuid, systemDefUuid, Arrays.asList("idrac", "bios"));
        delegateExecution.setVariable(EVALUATION_RESULTS, evalResults);
    }

    private String fetchRCMUuidBasedOnVersion(String rcmVersion)
    {
        String rcmUuid = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        URI uri = UriComponentsBuilder.fromHttpUrl(buildBaseUrl()).pathSegment("rcm-fitness-api").pathSegment("api")
                .pathSegment("rcm").pathSegment("inventory").pathSegment("vxrack").build().toUri();

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().equals(HttpStatus.OK))
        {
            String jsonBody = response.getBody();
            rcmUuid = RcmDataParsingUtil.parseRcmDefinitionResponse(jsonBody, rcmVersion);

            if (StringUtils.isEmpty(rcmUuid)) {
                final String message = "Could not find rcmUuid based on rcmVersion: " + rcmVersion;
                updateDelegateStatus(message);
                throw new BpmnError(RCM_EVALUATION_FAILED, message);
            }
        }
        else
        {
            final String message = "RCM definition failed with status: " + response.getStatusCode().toString();
            updateDelegateStatus(message);
            throw new BpmnError(RCM_EVALUATION_FAILED, message);
        }

        return rcmUuid;
    }


    private Map<String, String> performRcmEvaluation(String rcmUuid, String componentUuid, List<String> evaluationTypes) {
        Map<String, String> evalResults = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        RCMEvaluation request = new RCMEvaluation();
        request.setRcmUuid(rcmUuid);
        request.setDeviceUuids(Arrays.asList(componentUuid));
        request.setSubComponentTypes(evaluationTypes);
        request.setTimeout(TIMEOUT);

        HttpEntity<RCMEvaluation> entity = new HttpEntity<>(request, headers);

        URI uri = UriComponentsBuilder.fromHttpUrl(buildBaseUrl()).pathSegment("rcm-fitness-api").pathSegment("api")
                .pathSegment("rcm").pathSegment("evaluation").pathSegment("subComponent").build().toUri();

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().equals(HttpStatus.OK))
        {
            String jsonBody = response.getBody();
            evaluationTypes.stream().filter(Objects::nonNull).forEach( evalType -> {
                String result = RcmDataParsingUtil.parseRcmEvaluationResponse(jsonBody, evalType);
                evalResults.put(evalType, result);
            });
        } else {
            final String message = "RCM evaluation failed with status: " + response.getStatusCode().toString();
            updateDelegateStatus(message);
            throw new BpmnError(RCM_EVALUATION_FAILED, message);
        }
        return evalResults;
    }



    /**
     * This has to change. URLs will be retrieved from the API gateway, eventually.
     *
     * @return
     */
    private String buildBaseUrl()
    {
        return "https://rcm-fitness-paqx.cpsd.dell:19080";
    }
}
