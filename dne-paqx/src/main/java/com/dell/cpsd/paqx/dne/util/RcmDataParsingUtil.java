/**
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 */

package com.dell.cpsd.paqx.dne.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


/**
 * Utility to parse RCM response.
 * <p>
 * Copyright &copy; 2017 Dell Inc. or its subsidiaries. All Rights Reserved. Dell EMC Confidential/Proprietary Information
 * </p>
 *
 * @version 1.0
 * @since 1.0
 */
public class RcmDataParsingUtil
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RcmDataParsingUtil.class);

    public static String parseRcmDefinitionResponse(String jsonString, String rcmVersion)
    {
        if (StringUtils.isEmpty(jsonString) || StringUtils.isEmpty(rcmVersion))
        {
            return null;
        }

        String rcmUuid = null;
        DocumentContext context = JsonPath.parse(jsonString.toLowerCase());
        int length = context.read("$['rcminventoryitems'].length()");

        for (int iCount = 0; iCount < length; iCount++)
        {
            String currentRcmVersion = context.read("$['rcminventoryitems'][" + iCount + "]['rcmversion']", String.class);
            if (rcmVersion.equalsIgnoreCase(currentRcmVersion))
            {
                rcmUuid = context.read("$['rcminventoryitems'][" + iCount + "]['uuid']", String.class);
                break;
            }
        }
        return rcmUuid;
    }


    public static String parseRcmEvaluationResponse(String jsonString, String evaluationType) {

        String result = null;
        DocumentContext context = JsonPath.parse(jsonString.toLowerCase());
        if (StringUtils.isEmpty(jsonString) || StringUtils.isEmpty(evaluationType))
        {
            return null;
        }
        int length = context.read("$['rcmevaluationresults'].length()");
        for (int iCount = 0; iCount < length; iCount++)
        {
            String evalType = context.read("$['rcmevaluationresults'][" + iCount + "]['evaluatedversiondatum']['identity']['elementtype']", String.class);
            if (evaluationType.equalsIgnoreCase(evalType)) {
                result = context.read("$['rcmevaluationresults'][" + iCount + "]['evaluationresult']", String.class);
                String actualVersion = context.read("$['rcmevaluationresults'][" + iCount + "]['actualvalue']", String.class);

                int expectedValueLen = context.read("$['rcmevaluationresults'][" + iCount + "]['expectedvalues'].length()");

                for (int eCount = 0; eCount < expectedValueLen; eCount++)
                {
                    String expectedVersion = context.read("$['rcmevaluationresults'][" + iCount + "]['expectedvalues'][" + eCount + "]", String.class);
                    LOGGER.info("Evaluation Type: " + evaluationType + ", actual: " + actualVersion + ", expected: " + expectedVersion);
                }
                break;
            }
        }
        return result;
    }
}
