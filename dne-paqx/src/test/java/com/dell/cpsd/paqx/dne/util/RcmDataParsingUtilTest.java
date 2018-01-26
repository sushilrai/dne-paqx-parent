package com.dell.cpsd.paqx.dne.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RcmDataParsingUtilTest
{

    @Test
    public void test_parseRcmDefinitionResponse() throws IOException
    {
        String jsonString = readFile("src/test/resources/rcm_definition.json");
        String rcmUuid = RcmDataParsingUtil.parseRcmDefinitionResponse(jsonString, "3.2.1");
        Assert.assertEquals("57a662f7-9d61-4d25-b59b-eccae1637488", rcmUuid);
    }

    @Test
    public void test_parseRcmEvaluationResponse() throws IOException
    {
        String jsonString = readFile("src/test/resources/rcm_evaluation.json");
        String result = RcmDataParsingUtil.parseRcmEvaluationResponse(jsonString, "bios");
        Assert.assertEquals("mismatch", result);
    }

    @Test
    public void test_parseRcmEvaluationResponse1() throws IOException
    {
        String jsonString = readFile("src/test/resources/rcm_evaluation.json");
        String result = RcmDataParsingUtil.parseRcmEvaluationResponse(jsonString, "idrac");
        Assert.assertEquals("match", result);
    }

    private String readFile(String filePath) throws IOException
    {
        String jsonString = null;
        //read json string from file
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            jsonString = stringBuilder.toString();
        } finally {
            reader.close();
        }
        return jsonString;
    }
}
