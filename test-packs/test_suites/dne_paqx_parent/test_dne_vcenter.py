#!/usr/bin/python
# Author: cullia
# Revision: 2.0
# Code Reviewed by:
# Description:Testing the Node Expamsion Teams, VCenter Cluster Discovery.

from pyVmomi import vim
from pyVim.connect import SmartConnect, Disconnect
import atexit
import argparse
import getpass
import ssl
import requests
import af_support_tools
import json
import time

port = 5672

ipaddress = '10.3.60.83'

rmq_username = 'test'
rmq_password = 'test'
cli_username = 'root'
cli_password = 'V1rtu@1c3!'

# Currently these details need to be hardcoded as dynamic discovery is not in place yet,
vCenterFQDN_IP = 'mcr1-cvcsa2.mdmz.lab.vce.com'
vCenterUser = 'administrator@vsphere.local'
vCenterPassword = '1vBlock9!'

# vCenterFQDN_IP = '10.3.9.69'
# vCenterUser = 'administrator@vsphere.local'
# vCenterPassword = 'V1rtu@1c3!'

vCenterPort = 443

#######################################################################################################################

def test_vcenter():
    print('\n*******************************************************')
    print('\nRunning test on Symphony system:', ipaddress, 'and Test vCenter:', vCenterFQDN_IP)

    cleanup()
    bindQueus()
    vCenterRegistrationMsg()  # Until there is a way to automatically register a vcenter we need to register it manually by sending this message

    # Get a list of the actual clusters direct from the test vCenter Server
    actualvCenterClusterList = getRealVcenterInfo()
    numOfClustersInList = len(actualvCenterClusterList)  # Get the number of clusters in the list

    print('\nStep 1: Data direct from Test vCenter')
    print('Total Number of Clusters:', numOfClustersInList)
    print(actualvCenterClusterList, '\n')

    # Purge the queue to ensure the test response query is empty
    af_support_tools.rmq_purge_queue(host=ipaddress, port=port, rmq_username=rmq_username, rmq_password=rmq_password,
                                     queue='test.vcenter.response')

    # Publish the vcenter cluster discover request message
    the_payload = '{"messageProperties":{"timestamp":"2010-01-01T12:00:00Z","correlationId":"discover-clusters-cor-id-01234","replyTo":"test"},"discoveryRequestInfo":{"address":"' + vCenterFQDN_IP + ':443","username":"' + vCenterUser + '","password":"' + vCenterPassword + '"}}'
    af_support_tools.rmq_publish_message(host=ipaddress, rmq_username=rmq_username, rmq_password=rmq_password,
                                         exchange='exchange.cpsd.controlplane.vcenter.request',
                                         routing_key='controlplane.hypervisor.vcenter.cluster.discover',
                                         headers={
                                             '__TypeId__': 'com.dell.cpsd.vcenter.discoverClusterRequestInfo'},
                                         payload=the_payload)

    # Consume the vcenter.cluster.discover.response message
    waitForMsg('test.vcenter.response')
    return_message = af_support_tools.rmq_consume_message(host=ipaddress, port=port, rmq_username=rmq_username,
                                                          rmq_password=rmq_password,
                                                          queue='test.vcenter.response', remove_message=False)

    return_json = json.loads(return_message, encoding='utf-8')
    clusterInfo = return_json['discoverClusterResponseInfo']['clusters']

    print('\nStep 2: Data from Symphony:\n')
    countNumOfClusters = 0

    for cluster in clusterInfo:
        assert cluster in actualvCenterClusterList, 'Error: unexpected cluster'  # Check that each cluster is in the source list
        print(cluster, 'Verified valid at source')
        countNumOfClusters += 1

    assert countNumOfClusters == numOfClustersInList, 'Not all clusters returned'  # Check that Symphony has the same number of clusters that source has

    print('\nNumber of Clusters at source: ', numOfClustersInList,
          '\nNumber of Clusters returned by Symphony: ', countNumOfClusters)

    print('\nTest Pass')
    print('\n*******************************************************')

    cleanup()


#######################################################################################################################

# These functions get the info direct from the specified vcenter

def get_obj(content, vimtype, name=None):
    return [item for item in content.viewManager.CreateContainerView(
            content.rootFolder, [vimtype], recursive=True).view]


def getRealVcenterInfo():
    # Disabling urllib3 ssl warnings
    requests.packages.urllib3.disable_warnings()

    # Disabling SSL certificate verification
    context = ssl.SSLContext(ssl.PROTOCOL_TLSv1)
    context.verify_mode = ssl.CERT_NONE

    # connect this thing
    si = SmartConnect(
            host=vCenterFQDN_IP,
            user=vCenterUser,
            pwd=vCenterPassword,
            port=vCenterPort,
            sslContext=context)

    # disconnect this thing
    atexit.register(Disconnect, si)

    content = si.RetrieveContent()

    clusterList = []

    for cluster_obj in get_obj(content, vim.ComputeResource):
        cluster = {'name': (cluster_obj.name), 'numberOfHosts': (len(cluster_obj.host))}
        clusterList.append(cluster)

    return clusterList


#######################################################################################################################
# These are common functions that are used throughout the main test.


def cleanup():
    # Delete the test queues
    print('Cleaning up...')

    af_support_tools.rmq_delete_queue(host=ipaddress, port=port, rmq_username=rmq_username, rmq_password=rmq_password,
                                      queue='test.vcenter.response')


def bindQueus():
    print('\nCreating test Queues')
    af_support_tools.rmq_bind_queue(host=ipaddress,
                                    port=port, rmq_username=rmq_username, rmq_password=rmq_password,
                                    queue='test.vcenter.response',
                                    exchange='exchange.cpsd.controlplane.vcenter.response',
                                    routing_key='#')


def vCenterRegistrationMsg():
    # Until there is a way to automatically register a vcenter we need to register it manually by sending this message.

    the_payload = '{"messageProperties":{"timestamp":"2010-01-01T12:00:00Z","correlationId":"vcenter-registtration-corr-id","replyTo":"localhost"},"registrationInfo":{"address":"https://' + vCenterFQDN_IP + ':443","username":"' + vCenterUser + '","password":"' + vCenterPassword + '"}}'

    af_support_tools.rmq_publish_message(host=ipaddress, rmq_username=rmq_username, rmq_password=rmq_password,
                                         exchange='exchange.cpsd.controlplane.vcenter.request',
                                         routing_key='controlplane.hypervisor.vcenter.endpoint.register',
                                         headers={
                                             '__TypeId__': 'com.dell.cpsd.vcenter.registration.info.request'},
                                         payload=the_payload)


def waitForMsg(queue):
    q_len = 0
    timeout = 0

    while q_len < 1:
        time.sleep(1)
        timeout += 1

        q_len = af_support_tools.rmq_message_count(host=ipaddress,
                                                   port=port,
                                                   rmq_username=rmq_username,
                                                   rmq_password=rmq_password,
                                                   queue=queue)

        if timeout > 50:
            print('ERROR: Message took to long to return. Something is wrong')
            checkForErrorMsg()


def checkForErrorMsg():
    print('Checking for error messages...')
    msg_count = af_support_tools.rmq_message_count(host=ipaddress,
                                                   port=port,
                                                   rmq_username=rmq_username,
                                                   rmq_password=rmq_password,
                                                   queue='test.system.list.found')

    if msg_count == 1:
        return_message = af_support_tools.rmq_consume_message(host=ipaddress, port=port, rmq_username=rmq_username,
                                                              rmq_password=rmq_password,
                                                              queue='test.system.list.found')

        return_json = json.loads(return_message, encoding='utf-8')

        error_msg = return_json['errors'][0]['message']
        print('\nRMQ Error Message:: ', error_msg)
        assert False

    msg_count = af_support_tools.rmq_message_count(host=ipaddress,
                                                   port=port,
                                                   rmq_username=rmq_username,
                                                   rmq_password=rmq_password,
                                                   queue='test.system.definition.response')

    if msg_count == 1:
        return_message = af_support_tools.rmq_consume_message(host=ipaddress, port=port, rmq_username=rmq_username,
                                                              rmq_password=rmq_password,
                                                              queue='test.system.definition.response')

        return_json = json.loads(return_message, encoding='utf-8')

        error_msg = return_json['errors'][0]['message']
        print('\nRMQ Error Message:: ', error_msg)
        assert False


    else:
        print('No specific error looked for.')
        assert False

#######################################################################################################################
# test_vcenter()
