[![License](https://img.shields.io/badge/License-EPL%201.0-red.svg)](https://opensource.org/licenses/EPL-1.0)
[![Build Status](https://travis-ci.org/dellemc-symphony/dne-paqx-parent.svg?branch=master)](https://travis-ci.org/dellemc-symphony/dne-paqx-parent)
# dne-paqx-parent
## Description
This repository provides the service to add a new server to an existing system in the scope of the Dell EMC VxRack Flex node expansion functionality of Project Symphony.  
## Documentation
You can find additional documentation for Project Symphony at [dellemc-symphony.readthedocs.io][documentation].

## API overview
The REST API documentation can be accessed using the following link: http://[symphony-ova]:10000/dne
## Before you begin
Verify that the following tools are installed:
 
* Apache Maven 3.0.5+
* Docker 1.12+
* Docker Compose 1.8.0+
* Java Development Kit (version 8)
* RabbitMQ  3.6.6
## Building
Run the following command to build this project:
```bash
mvn clean install
```
## Deploying
The output of running the build step is a tagged Docker image.
 
Run this locally:
```bash
docker run -it --net="host" <docker_image_hash>
```
This deploys a container that communicates with the host's RabbitMQ installation. The container is based on the image created in the build step.

## Contributing
Project Symphony is a collection of services and libraries housed at [GitHub][github].
Contribute code and make submissions at the relevant GitHub repository level. See [our documentation][contributing] for details on how to contribute.
## Community
Reach out to us on the Slack [#symphony][slack] channel by requesting an invite at [{code}Community][codecommunity].
You can also join [Google Groups][googlegroups] and start a discussion.
 
[slack]: https://codecommunity.slack.com/messages/symphony
[googlegroups]: https://groups.google.com/forum/#!forum/dellemc-symphony
[codecommunity]: http://community.codedellemc.com/
[contributing]: http://dellemc-symphony.readthedocs.io/en/latest/contributingtosymphony.html
[github]: https://github.com/dellemc-symphony
[documentation]: https://dellemc-symphony.readthedocs.io/en/latest/







