# Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from notebook.utils import url_path_join
from notebook.base.handlers import APIHandler, IPythonHandler

import json
from tornado import web
from .environment import *
import beakerx
import tornado


class SettingsHandler(APIHandler):
    def data_received(self, chunk):
        pass

    @staticmethod
    def _read_property():
        return EnvironmentSettings.read_setting_from_file()

    @web.authenticated
    def get(self):
        self.finish(SettingsHandler._read_property())

    @web.authenticated
    def post(self):
        data = tornado.escape.json_decode(self.request.body)

        EnvironmentSettings.save_setting_to_file(json.dumps(data))

        self.finish(json.dumps(SettingsHandler._read_property()))


class VersionHandler(APIHandler):
    def data_received(self, chunk):
        pass

    @web.authenticated
    def get(self):
        data = {'version': beakerx.__version__}
        self.finish(json.dumps(data))


class JavaDoc(IPythonHandler):

    @web.authenticated
    def get(self, path):
        print ("---------------------path: ", path)
        try:
            file = open('/home/robot/proj/beakerx/kernel/groovy/build/docs/javadoc/' + path, 'r')
            content = file.read()
        except IOError:
            content = "nie działa"

        else:
            file.close()
        self.set_header('Content-Type', 'text/html')
        self.finish(content)


def load_jupyter_server_extension(nbapp):
    web_app = nbapp.web_app
    host_pattern = '.*$'
    settings_route_pattern = url_path_join(web_app.settings['base_url'], '/beakerx', '/settings')
    version_route_pattern = url_path_join(web_app.settings['base_url'], '/beakerx', '/version')
    javadoc_route_pattern = url_path_join(web_app.settings['base_url'], '/beakerx', '/javadoc/(.*)')
    web_app.add_handlers(host_pattern, [(settings_route_pattern, SettingsHandler)])
    web_app.add_handlers(host_pattern, [(version_route_pattern, VersionHandler)])
    web_app.add_handlers(host_pattern, [(javadoc_route_pattern, JavaDoc)])
    #web_app.add_handlers(javadoc_route_pattern, tornado.web.StaticFileHandler, {"path": '/home/robot/proj/beakerx/kernel/groovy/build/docs/javadoc/', "default_filename": "index.html"})
    nbapp.log.info("[beakerx] enabled")
