package com.dell.cpsd.paqx.dne.service.workflow.preprocess;

import com.dell.cpsd.paqx.dne.service.model.Step;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class PreProcessTaskConfigTest
{
    @InjectMocks
    private PreProcessTaskConfig preProcessTaskConfig;

    @Test
    public void testPreProcessWorkflowSteps()
    {
        Map<String, Step> preProcesswfSteps = preProcessTaskConfig.preProcessWorkflowSteps();
        assertNotNull(preProcesswfSteps);
    }
}
