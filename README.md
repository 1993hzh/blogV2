# IBM bluemix+docker+custom image
Remember that ng stands for America Region and eu-gb stands for England Region, make sure this prefix is always the same.

Deploy scala env in bluemix using docker.

## Docs
bluemix docs: https://www.ng.bluemix.net/docs/containers/container_cli_cfic.html

docker docs: https://docs.docker.com/installation/

## Setup bluemix env in local
Make sure Docker CLI,CloudFoundry CLI, ibm-containers cf CLI have all been installed
Then login in shell: 
```sh
$ cf login -a https://api.ng.bluemix.net
$ cf ic login
```
> Currently the ibm-containers have some bugs with cloudfoundry in linux64, see the issue for details: #639
> If you meet this bug, suggest you to install `ice`, docs: https://www.eu-gb.bluemix.net/docs/containers/container_cli_ice_ov.html#container_cli_ice_install


```sh
$ docker build https://github.com/1993hzh/blogV2.git
```

## TODO
