#!/usr/bin/python
# Author: cullia
# Revision: 1.0
# Code Reviewed by:
# Description: Testing the DNE Container.

import af_support_tools
import pytest
import json
import time
import requests
import os


##############################################################################################

@pytest.fixture(scope="module", autouse=True)
def load_test_data():

    # Set config ini file name
    global env_file
    env_file = 'env.ini'


    # Test VM Details
    global ipaddress
    ipaddress = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='hostname')
    global cli_username
    cli_username = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='username')
    global cli_password
    cli_password = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='password')


    # RMQ Details
    global rmq_username
    rmq_username = 'guest'
    global rmq_password
    rmq_password = 'guest'


##############################################################################################

@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_servicerunning():

    print('\n* * * Testing the DNE PAQX on system:', ipaddress, '* * *\n')

    service_name = 'symphony-dne-paqx'


    sendCommand = "docker ps --filter name=" + service_name + "  --format '{{.Status}}' | awk '{print $1}'"
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password, command=sendCommand, return_output=True)
    my_return_status=my_return_status.strip()
    print('\nDocker Container is:', my_return_status,'\n')
    assert my_return_status == 'Up', (service_name + " not running")


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_reg_in_Consul():
    verifyServiceInConsulAPI('dne-paqx')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_rmq_bindings_response():
    validate_queues_on_exchange('exchange.dell.cpsd.paqx.node.discovery.response',
                          'queue.dell.cpsd.dne-paqx.response.dne-paqx')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_rmq_bindings_cr_event():
    validate_queues_on_exchange('exchange.dell.cpsd.hdp.capability.registry.event',
                          'queue.dell.cpsd.hdp.capability.registry.event.dne-paqx')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_rmq_bindings_request():
    validate_queues_on_exchange('exchange.dell.cpsd.hdp.capability.registry.response',
                          'queue.dell.cpsd.hdp.capability.registry.response.dne-paqx')


@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_log_files():

    # Verify the log files exist
    sendCommand = 'ls /opt/dell/cpsd/dne-paqx/logs'
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password, command=sendCommand, return_output=True)
    assert 'dne-paqx-error.log' in my_return_status, 'dne-paqx-error.log does not exist'
    assert 'dne-paqx-info.log' in my_return_status, 'dne-paqx-info.log does not exist'
    print('Valid log files exist')

    # Verify there are no Authentication errors
    sendCommand = 'cat /opt/dell/cpsd/dne-paqx/logs/dne-paqx-error.log | grep \'com.rabbitmq.client.AuthenticationFailureException\''
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password, command=sendCommand, return_output=True)
    assert 'AuthenticationFailureException' not in my_return_status, 'AuthenticationFailureException in log files, Review the dne-paqx-error.log file'

    # Verify there are no RuntimeException errors
    sendCommand = 'cat /opt/dell/cpsd/dne-paqx/logs/dne-paqx-error.log | grep \'RuntimeException\''
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password, command=sendCommand, return_output=True)
    assert 'RuntimeException' not in my_return_status, 'RuntimeException in log files, Review the dne-paqx-error.log file'

    # Verify there are no NullPointerException errors
    sendCommand = 'cat /opt/dell/cpsd/dne-paqx/logs/dne-paqx-error.log | grep \'NullPointerException\''
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password, command=sendCommand, return_output=True)
    assert 'NullPointerException' not in my_return_status, 'NullPointerException in log files, Review the dne-paqx-error.log file'

    print('No Authentication, RuntimeException or NullPointerException in log files\n')

##############################################################################################


def rest_queue_list(user=None, password=None, host=None, port=None, virtual_host=None, exchange=None):
    url = 'http://%s:%s/api/exchanges/%s/%s/bindings/source' % (host, port, virtual_host, exchange)
    response = requests.get(url, auth=(user, password))
    queues = [q['destination'] for q in response.json()]
    return queues


def validate_queues_on_exchange(suppliedExchange, suppliedQueue):
    queues = rest_queue_list(user=rmq_username, password=rmq_password, host=ipaddress, port=15672, virtual_host='%2f',
                             exchange=suppliedExchange)
    queues = json.dumps(queues)

    assert suppliedQueue in queues, 'The queue "' + suppliedQueue + '" is not bound to the exchange "' + suppliedExchange + '"'
    print(suppliedExchange, '\nis bound to\n', suppliedQueue, '\n')


def verifyServiceInConsulAPI(service):

    url_body = ':8500/v1/agent/services'
    my_url = 'http://' + ipaddress + url_body

    print('GET:', my_url)

    try:
        url_response = requests.get(my_url)
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
        print(url_response)

        the_response = url_response.text

        serviceToCheck = '"Service": "'+service+'"'
        assert serviceToCheck in the_response, ('ERROR:', service, 'is not in Consul\n')
        print(service, 'Registered in consul')

        if serviceToCheck in the_response:
            verifyServiceStatusInConsulAPI(service)


def verifyServiceStatusInConsulAPI(service):

    url_body = ':8500/v1/health/checks/'+service
    my_url = 'http://' + ipaddress + url_body

    print('GET:', my_url)

    try:
        url_response = requests.get(my_url)
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
        print(url_response)

        the_response = url_response.text

        serviceStatus = '"Status": "passing"'
        assert serviceStatus in the_response, ('ERROR:', service, 'is not Passing in Consul\n')
        print(service, 'Status = Passing in consul\n\n')


#######################################################################################################################
