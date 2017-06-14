#!/usr/bin/python
# Author: cullia
# Revision: 1.1
# Code Reviewed by:
# Description: Testing the DNE Container.
#
# Copyright (c) 2017 Dell Inc. or its subsidiaries.  All Rights Reserved.
# Dell EMC Confidential/Proprietary Information
#
import af_support_tools
import pytest
import json
import time
import requests
import os


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
    rmq_username = af_support_tools.get_config_file_property(config_file=env_file, heading='RabbitMQ',
                                                             property='username')
    global rmq_password
    rmq_password = af_support_tools.get_config_file_property(config_file=env_file, heading='RabbitMQ',
                                                             property='password')
    global port
    port = af_support_tools.get_config_file_property(config_file=env_file, heading='RabbitMQ',
                                                     property='ssl_port')


#####################################################################
# These are the main tests.
#####################################################################

@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_servicerunning():
    """
    Description     :       This method tests docker service for a container
                            It will fail if :
                                Docker service is not running for the container
    Parameters      :       none
    Returns         :       None
    """

    print('\n* * * Testing the DNE PAQX on system:', ipaddress, '* * *\n')

    service_name = 'symphony-dne-paqx'

    sendCommand = "docker ps --filter name=" + service_name + "  --format '{{.Status}}' | awk '{print $1}'"
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password,
                                                         command=sendCommand, return_output=True)
    my_return_status = my_return_status.strip()
    print('\nDocker Container is:', my_return_status, '\n')
    assert my_return_status == 'Up', (service_name + " not running")


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_registered_in_Consul():
    """
    Description     :       This method tests that dne-paqx is registered in the Consul API http://{SymphonyIP}:8500/v1/agent/services
                            It will fail if :
                                The line 'Service: "dne-paqx"' is not present
    Parameters      :       none
    Returns         :       None
    """

    service = 'dne-paqx'

    url_body = ':8500/v1/agent/services'
    my_url = 'http://' + ipaddress + url_body

    print('GET:', my_url)

    try:
        url_response = requests.get(my_url)
        url_response.raise_for_status()

    # Error check the response
    except requests.exceptions.HTTPError as err:
        # Return code error (e.g. 404, 501, ...)
        print(err)
        print('\n')
        assert False

    # Error check the response
    except requests.exceptions.Timeout as err:
        # Not an HTTP-specific error (e.g. connection refused)
        print(err)
        print('\n')
        assert False

    else:
        # A 200 has been received
        print(url_response)

        the_response = url_response.text

        # Create the sting as it should appear in the API
        serviceToCheck = '"Service": "' + service + '"'

        assert serviceToCheck in the_response, ('ERROR:', service, 'is not in Consul\n')

        print(service, 'Registered in Consul')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_status_in_Consul():
    """
    Description     :       This method tests that dne-paqx has a passing status in the Consul API http://{SymphonyIP}:8500/v1/health/checks/dne-paqx
                            It will fail if :
                                The line '"Status": "passing"' is not present
    Parameters      :       none
    Returns         :       None
    """
    service = 'dne-paqx'

    url_body = ':8500/v1/health/checks/' + service
    my_url = 'http://' + ipaddress + url_body

    print('GET:', my_url)

    try:
        url_response = requests.get(my_url)
        url_response.raise_for_status()

    # Error check the response
    except requests.exceptions.HTTPError as err:
        # Return code error (e.g. 404, 501, ...)
        print(err)
        print('\n')
        assert False

    # Error check the response
    except requests.exceptions.Timeout as err:
        # Not an HTTP-specific error (e.g. connection refused)
        print(err)
        print('\n')
        assert False

    else:
        # A 200 has been received
        print(url_response)
        the_response = url_response.text

        serviceStatus = '"Status": "passing"'
        assert serviceStatus in the_response, ('ERROR:', service, 'is not Passing in Consul\n')
        print(service, 'Status = Passing in consul\n\n')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_rmq_bindings_response(suppliedExchange='exchange.dell.cpsd.paqx.node.discovery.response',
                                   suppliedQueue='queue.dell.cpsd.dne-paqx.response.dne-paqx'):
    """
    Description     :       This method tests that a binding exists between a RMQ Exchange & a RMQ Queue.
                            It uses the RMQ API to check.
                            It will fail if :
                                The RMQ binding does not exist
    Parameters      :       1. RMQ Exchange. 2. RQM Queue
    Returns         :       None
    """

    queues = rest_queue_list(user=rmq_username, password=rmq_password, host=ipaddress, port=15672, virtual_host='%2f',
                             exchange=suppliedExchange)
    queues = json.dumps(queues)

    assert suppliedQueue in queues, 'The queue "' + suppliedQueue + '" is not bound to the exchange "' + suppliedExchange + '"'
    print(suppliedExchange, '\nis bound to\n', suppliedQueue, '\n')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_rmq_bindings_request(suppliedExchange='exchange.dell.cpsd.hdp.capability.registry.response',
                                  suppliedQueue='queue.dell.cpsd.hdp.capability.registry.response.dne-paqx'):
    """
    Description     :       This method tests that a binding exists between a RMQ Exchange & a RMQ Queue.
                            It uses the RMQ API to check.
                            It will fail if :
                                The RMQ binding does not exist
    Parameters      :       1. RMQ Exchange. 2. RQM Queue
    Returns         :       None
    """

    queues = rest_queue_list(user=rmq_username, password=rmq_password, host=ipaddress, port=15672, virtual_host='%2f',
                             exchange=suppliedExchange)
    queues = json.dumps(queues)

    assert suppliedQueue in queues, 'The queue "' + suppliedQueue + '" is not bound to the exchange "' + suppliedExchange + '"'
    print(suppliedExchange, '\nis bound to\n', suppliedQueue, '\n')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_rmq_bindings_cap_reg_event(suppliedExchange='exchange.dell.cpsd.hdp.capability.registry.event',
                                        suppliedQueue='queue.dell.cpsd.hdp.capability.registry.event.dne-paqx'):
    """
    Description     :       This method tests that a binding exists between a RMQ Exchange & a RMQ Queue.
                            It uses the RMQ API to check.
                            It will fail if :
                                The RMQ binding does not exist
    Parameters      :       1. RMQ Exchange. 2. RQM Queue
    Returns         :       None
    """
    queues = rest_queue_list(user=rmq_username, password=rmq_password, host=ipaddress, port=15672, virtual_host='%2f',
                             exchange=suppliedExchange)
    queues = json.dumps(queues)

    assert suppliedQueue in queues, 'The queue "' + suppliedQueue + '" is not bound to the exchange "' + suppliedExchange + '"'
    print(suppliedExchange, '\nis bound to\n', suppliedQueue, '\n')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_log_files():
    """
    Description     :       This method tests that the ESS log files exist and contain no Exceptions.
                            It will fail:
                                If the the error and/or info log files do not exists
                                If the error log file contains AuthenticationFailureException, RuntimeException or NullPointerException.
    Parameters      :       None
    Returns         :       None
    """

    filePath = '/opt/dell/cpsd/dne-paqx/logs/'
    errorLogFile = 'dne-paqx-error.log'
    infoLogFile = 'dne-paqx-info.log'

    # Verify the log files exist
    sendCommand = 'ls ' + filePath
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password,
                                                         command=sendCommand, return_output=True)
    assert errorLogFile in my_return_status, 'Error: ' + errorLogFile + ' does not exist'
    assert infoLogFile in my_return_status, 'Error: ' + infoLogFile + ' does not exist'
    print('Valid log files exist')

    # Verify there are no Authentication errors
    sendCommand = 'cat ' + filePath + errorLogFile + ' | grep \'com.rabbitmq.client.AuthenticationFailureException\''
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password,
                                                         command=sendCommand, return_output=True)
    assert 'AuthenticationFailureException' not in my_return_status, 'AuthenticationFailureException in log files, Review the ' + errorLogFile + ' file'

    # Verify there are no RuntimeException errors
    sendCommand = 'cat ' + filePath + errorLogFile + ' | grep \'RuntimeException\''
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password,
                                                         command=sendCommand, return_output=True)
    assert 'RuntimeException' not in my_return_status, 'RuntimeException in log files, Review the ' + errorLogFile + ' file'

    # Verify there are no NullPointerException errors
    sendCommand = 'cat ' + filePath + errorLogFile + ' | grep \'NullPointerException\''
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password,
                                                         command=sendCommand, return_output=True)
    assert 'NullPointerException' not in my_return_status, 'NullPointerException in log files, Review the ' + errorLogFile + ' file'

    print('No Authentication, RuntimeException or NullPointerException in log files\n')


##############################################################################################


def rest_queue_list(user=None, password=None, host=None, port=None, virtual_host=None, exchange=None):
    """
    Description     :       This method returns all the RMQ Queues that are bound to a names RMQ Exchange.
    Parameters      :       RMQ User, RMQ password, host, port & exchange. Always virtual_host = '%2f'
    Returns         :       A list of the Queues bound to the named Exchange/
    """

    url = 'http://%s:%s/api/exchanges/%s/%s/bindings/source' % (host, port, virtual_host, exchange)
    response = requests.get(url, auth=(user, password))
    queues = [q['destination'] for q in response.json()]
    return queues
