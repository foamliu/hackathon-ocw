import sys
import time

from selenium import webdriver
from selenium.common.exceptions import TimeoutException, WebDriverException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.action_chains import ActionChains
from pyvirtualdisplay import Display

import scrapy

NEXT_WAIT_TIMEOUT = 120
MAX_RETRY = 3


class BrowserRobot(object):
    """
    provide interface for spiders to scrape pages that needs to "manual" click
    """

    def __init__(self, profile_preference=None):
        self.display = Display(visible=0, size=(1024, 768))
        profile = webdriver.FirefoxProfile()
        if profile_preference:
            for key in profile_preference:
                profile.set_preference(key, profile_preference[key])
        self.display.start()
        self.browser = webdriver.Firefox(profile)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.stop()

    def stop(self):
        self.browser.quit()
        self.display.stop()

    def load(self, url):
        self.browser.get(url)
        return scrapy.Selector(text=self.browser.page_source)

    def load_until(self, url, until):
        """
        wait until some specified element is loaded by xpath
        :param url:
        :param until:
        :return:
        """
        self.browser.get(url)
        WebDriverWait(self.browser, NEXT_WAIT_TIMEOUT) \
            .until(EC.element_to_be_clickable((By.XPATH, until)))
        return scrapy.Selector(text=self.browser.page_source)

    def explore(self, start_url, scrape_func, next_xpath, max_count=-1):
        try:
            self.browser.get(start_url)
            old_page = self.browser.page_source
            counter = 0
            while True:
                # refer to the following blog for wait trick:
                # http://www.obeythetestinggoat.com/how-to-get-selenium-to-wait-for-page-load-after-a-click.html
                WebDriverWait(self.browser, NEXT_WAIT_TIMEOUT) \
                    .until(EC.element_to_be_clickable((By.XPATH, next_xpath)))
                # always sleep for a while to be polite
                time.sleep(0.3)
                if old_page == self.browser.page_source or \
                    (max_count != -1 and counter >= max_count):
                    break
                else:
                    old_page = self.browser.page_source
                counter += 1
                response = scrapy.Selector(text=self.browser.page_source)
                yield scrape_func(response)
                next_elem = self.browser.find_element_by_xpath(next_xpath)
                cnt = 0
                while cnt < MAX_RETRY:
                    try:
                        ActionChains(self.browser).move_to_element(next_elem).click().perform()
                        break
                    except WebDriverException as we:
                        time.sleep(1)
                        cnt += 1
        except TimeoutException as te:
            sys.stderr.write("Fail to wait for page to be loaded. Error:{}\n".format(te))
        except Exception as oe:
            sys.stderr.write("unexpected exception:{}".format(oe))
            import traceback
            traceback.print_exc()
            raise


def uniq(elements):
    """
    return unique elements with original order
    :param elements:
    :return:
    """
    us = set()
    ret = []
    for e in elements:
        if e not in us:
            ret.append(e)
            us.add(e)
    return ret
