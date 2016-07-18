## setup
### Install selenium and scrapy
- Install python-dev, xml, ffi and so on for scrapy
  + on ubuntu: `sudo apt-get install -y python-pip libxml2-dev libxslt1-dev python-dev build-essential libssl-dev libffi-dev`
  + on centos: `sudo yum install -y libffi-devel libxml2-devel openssl-devel`
- Install selenium and scrapy (better inside some virtualenv)
`pip install selenium==2.53.5 Scrapy==1.1.0`

### Install firefox and virutal display
__On ubuntu__:
```
# install xvfb to simuate hardware display
apt-get install -y xvfb xserver-xephyr
# removes a native Debian browser Iceweasel
apt-get remove iceweasel
# install firefox like this?
echo -e "\ndeb http://downloads.sourceforge.net/project/ubuntuzilla/mozilla/apt all main" | sudo tee -a /etc/apt/sources.list 
sudo apt-key adv --recv-keys --keyserver keyserver.ubuntu.com C1289A29
sudo apt-get update
sudo apt-get install -y firefox-mozilla-build
sudo apt-get install -y libdbus-glib-1-2 libgtk2.0-0 libasound2
pip install pyvirtualdisplay
```
__On centos__:
```
# install xvfb to simuate hardware display
yum install -y python-xvfbwrapper Xephyr
# install firefox
yum install -y firefox
sudo apt-get install -y libdbus-glib-1-2 libgtk2.0-0 libasound2
pip install pyvirtualdisplay
```
__Hello test__:
```
from selenium import webdriver
from selenium.webdriver.firefox.webdriver import FirefoxBinary
import pyvirtualdisplay

with pyvirtualdisplay.Display(visible=False):
    binary = FirefoxBinary()
    driver = webdriver.Firefox(None, binary)
    driver.get("http://www.bing.com")
    driver.quit()
```

## Notice
- Docker does not work here since firefox will try to start a daemon service,and will throw the following error inside container:
```
(firefox:9465): GConf-WARNING **: Client failed to connect to the D-BUS daemon:
/usr/bin/dbus-launch terminated abnormally without any error message

(firefox:9465): GConf-WARNING **: Client failed to connect to the D-BUS daemon:
/usr/bin/dbus-launch terminated abnormally without any error message
```
