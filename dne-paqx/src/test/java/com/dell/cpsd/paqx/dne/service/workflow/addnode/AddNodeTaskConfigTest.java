package com.dell.cpsd.paqx.dne.service.workflow.addnode;

import com.dell.cpsd.paqx.dne.service.model.Step;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AddNodeTaskConfigTest
{

    @InjectMocks
    private AddNodeTaskConfig addNodeTaskConfig;

    @Test
    public void testAddNodeWorkflowSteps()
    {
        Map<String, Step> addNodewfSteps = addNodeTaskConfig.addNodeWorkflowSteps();
        assertNotNull(addNodewfSteps);
    }
}
