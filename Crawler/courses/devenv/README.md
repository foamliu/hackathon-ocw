## Set up host VM (CentOS)
- Install and upgrade docker engine to >1.10 (to support docker-compose v2 syntax)
```
sudo yum install epel-release -y
sudo yum upgrade
sudo yum install -y docker python-pip
sudo yum upgrade docker
```
- Install docker-compose
```
pip install docker-compose
```
