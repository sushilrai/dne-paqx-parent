import pytest
import af_support_tools

try:
    env_file = 'env.ini'
    ipaddress = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='hostname')
    ssh_user = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='username')
    ssh_pass = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='password')
except:
    print('Failed to read env variables from ' + env_file)


#####################################################################
# These are the main tests.
#####################################################################

@pytest.mark.dne_paqx_parent
@pytest.mark.dne_paqx_parent_mvp
@pytest.mark.parametrize("container_name,expected", [
	("symphony-credential-service",'Up'),
	("symphony-identity-service",'Up'),
	("symphony-hal-orchestrator-service",'Up'),
	("symphony-system-definition-service",'Up'),
	("symphony-node-discovery-paqx",'Up'),
	("symphony-dne-paqx",'Up'),
	("symphony-capability-registry-service",'Up'),
	("symphony-endpoint-registration-service", 'Up'),
	("symphony-rackhd-adapter-service",'Up'),
	("symphony-coprhd-adapter-service",'Up'),
	("symphony-vcenter-adapter-service",'Up'),
])

def test_generic_container_up(container_name, expected):
    """
    Description	:	This method asserts if the output returned from container_verify matches the expected string.
    Parameters	:	1. container_name	-	Name of the container(STRING)
    				2. expected			-	expected output to be obtained from container_verify function(STRING).
    Returns		: 	None
    """
    print (container_name + ' status after deployment')
    assert container_verify(container_name, 'Status', '1') == expected


#####################################################################
# These are supporting functions called throughout the test.
#####################################################################
def container_verify(container, attrib, pos):
    """
    Description	:	This method verifies if the container name is up or not.
    Parameters	:	1. container	-	Name of the container (STRING)
    				2. expected		-	expected output to be obtained from container_verify function(STRING).
    				3. pos			-	position of the string to be extracted from the formatted 'docker ps' command(STRING)
    Returns		: 	output			-	"Up\n" or "Down\n" string coming from 'docker ps' command(STRING).
    """
    print('Getting Values Specified Container')

    ipaddress = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='hostname')
    username = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='username')
    password = af_support_tools.get_config_file_property(config_file=env_file, heading='Base_OS', property='password')

    cmd = 'docker ps -f name=' + str(container) + ' --format "{{.' + str(attrib) + '}}" | awk \'{print $' + str(pos) + '}\''
    output = af_support_tools.send_ssh_command(host=ipaddress, username=username, password=password, command=cmd, return_output=True).strip()

    return output