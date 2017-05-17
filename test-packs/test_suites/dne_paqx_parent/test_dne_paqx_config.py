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
    global port
    port = 5672

##############################################################################################

@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.dne_paqx_parent_mvp_extended
def test_dne_paqx_config():

    print('\n* * * Testing the DNE PAQX on system:', ipaddress, '* * *\n')

    service_name = 'symphony-dne-paqx'


    # 1. Test the service is running
    sendCommand = "docker ps --filter name=" + service_name + "  --format '{{.Status}}' | awk '{print $1}'"
    my_return_status = af_support_tools.send_ssh_command(host=ipaddress, username=cli_username, password=cli_password, command=sendCommand, return_output=True)
    my_return_status=my_return_status.strip()
    print('\nDocker Container is:', my_return_status,'\n')
    assert my_return_status == 'Up', (service_name + " not running")


    # 2. Verify the DNE related Exchanges are bound to the correct queues
    validate_queues_on_exchange('exchange.dell.cpsd.paqx.node.discovery.response',
                          'queue.dell.cpsd.dne-paqx.response.dne-paqx')

    validate_queues_on_exchange('exchange.dell.cpsd.hdp.capability.registry.event',
                          'queue.dell.cpsd.hdp.capability.registry.event.dne-paqx')

    validate_queues_on_exchange('exchange.dell.cpsd.hdp.capability.registry.response',
                          'queue.dell.cpsd.hdp.capability.registry.response.dne-paqx')


    # 3. Verify the log files

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


#######################################################################################################################
