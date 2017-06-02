#!/usr/bin/python
# Copyright Â© 01 April 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
#
# Author: russed5
# Revision: 1.1
# Code Reviewed by:
# Description:

import pytest
import requests
import json
import time
import af_support_tools


##############################################################################################

@pytest.fixture(scope="module", autouse=True)
def load_test_data():

     import cpsd
     global cpsd

     # Set config ini file name
     global env_file
     env_file = 'env.ini'

     # Test VM Details
     global ipaddress
     ipaddress = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='hostname')
     global cli_username
     cli_username = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS',
                                                              property='username')
     global cli_password
     cli_password = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS',
                                                              property='password')

     # RMQ Details
     global rmq_username
     rmq_username = 'guest'
     global rmq_password
     rmq_password = 'guest'
     global port
     port = 5672

##############################################################################################


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dnePAQX_container_using_AMQP_over_TLS():
    # this test uses the  processId for the dne paqx container
    # to verify that the network connection to rabbitmq for that container
    # is using the rabbitmq ssl port

    ssl_listen_port = getRabbitmqSSLport(ipaddress)

    connectionPorts = getContainerRabbitPorts('cpsd-dne-paqx')

    connectionsText = connectionPorts.splitlines()     # shouldn't be more then 1 line returned, but just in case
    assert connectionsText, 'no rabbitmq connection detected for DNE PAQX'
    for line in connectionsText:
        assert ssl_listen_port in line, "The DNE PAQX is not using the SSL Port"


#####################################################################################################

@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_DNEpaqx_AMQP_data_is_encrypted():
    #
    # 1. get the processId associated with the DNE paqx
    # 2. use the returned processId to filter the output of the 'lsof' utility
    # 3. check that the listed AMQP connection is secure by checking for the phrase 'amqps' in the listing
    #
    commandGetContainerProcessID = "ps -ef | grep [j]ava | grep dne-paqx | awk '{print $2}' "

    processId = af_support_tools.send_ssh_command(
        host=ipaddress,
        username=cli_username,
        password=cli_password,
        command=commandGetContainerProcessID,
        return_output=True)

    # using lsof, we expect to see output similar to below and are particularly interested in the 'amqps' text
    #    java    4969 root   21u  IPv6    57839    0t0    TCP localhost:48786->localhost:amqps (ESTABLISHED)
    commandCheckEncryption = "lsof -p " + processId.rstrip() + " | grep [E]STABLISHED | grep amqps"

    return_text = af_support_tools.send_ssh_command(
        host=ipaddress,
        username=cli_username,
        password=cli_password,
        command=commandCheckEncryption,
        return_output=True)

    assert return_text, "Error : no amqps connection was discovered for the dne paqx"

#####################################################################################################

def getQueueDetails(queue):
#
# query specific rabbitmq queue to return the connection name
# this name is of the form 'host:port -> host:port'
#
    # query rabbitmq for details of 'queue'
    apipath = "/api/queues/%2f/" + queue
    my_url = 'http://' + ipaddress + ':15672' + apipath

    try:
        url_response = requests.get(my_url, auth=(rmq_username,rmq_password))
        url_response.raise_for_status()

    except requests.exceptions.HTTPError as err:
        # Return code error (e.g. 404, 501, ...)
        print(err)
        print('\n')
        assert False

    except requests.exceptions.Timeout as err:
        # Not an HTTP-specific error (e.g. connection refused)
        print(err)
        print('\n')
        assert False

    else:
        # 200
        data = json.loads(url_response.text)

    return data


#####################################################################################################

def getRabbitmqSSLport(ip):
#
# retrieve the rabbitmq ssl port via the rabbitmq http api
#

    apipath = "/api/overview"
    my_url = 'http://' + ipaddress + ':15672' + apipath

    try:
        url_response = requests.get(my_url, auth=(rmq_username,rmq_password))
        url_response.raise_for_status()

    except requests.exceptions.HTTPError as err:
        # Return code error (e.g. 404, 501, ...)
        print(err)
        print('\n')
        assert False

    except requests.exceptions.Timeout as err:
        # Not an HTTP-specific error (e.g. connection refused)
        print(err)
        print('\n')
        assert False

    else:
        # 200
        data = json.loads(url_response.text)
        ssl_port = '9999'       #non-valid number in case no ssl port found

        for i in data['listeners']:
            if i['protocol'] == 'amqp/ssl' :
                ssl_port = i['port']

        portToReturn = str(ssl_port)
        assert ssl_port != '9999', "Error : Rabbitmq is not configured for TSL "

    return portToReturn

#########################################################################################################

def getPeerPortFromNetstatOutput(netstatText):
    #
    # expected format of text is :
    #    ::1:56814 ::1:5671

    portstext = netstatText.split(' ')
    peer_port_raw = portstext[0].split(':')
    peer_port = peer_port_raw[-1]

    return peer_port

######################################################################################################

def getContainerRabbitPorts(containerName):

    #ssl_listen_port = getRabbitmqSSLport(ipaddress)

    commandGetContainerId = "docker ps | grep " + containerName + " |cut -f1 -d ' '"
    containerId = af_support_tools.send_ssh_command(
                host=ipaddress,
                username=cli_username,
                password=cli_password,
                command=commandGetContainerId,
                return_output=True)
    print(containerId)

    commandGetContainerProcessID = "ps -ef | grep '[c]ontainer.id=" + containerId.rstrip() + "' | grep java | awk '{print $2}' "
    print(commandGetContainerProcessID)
    processId = af_support_tools.send_ssh_command(
                host=ipaddress,
                username=cli_username,
                password=cli_password,
                command=commandGetContainerProcessID,
                return_output=True)
    print(processId)

    commandGetContainerConnectionPort = "netstat -tupn | grep " + processId.rstrip() + " | grep -v 8500"
    print(commandGetContainerConnectionPort)
    connectionPorts = af_support_tools.send_ssh_command(
                host=ipaddress,
                username=cli_username,
                password=cli_password,
                command=commandGetContainerConnectionPort,
                return_output=True)

    print(connectionPorts)

    return connectionPorts