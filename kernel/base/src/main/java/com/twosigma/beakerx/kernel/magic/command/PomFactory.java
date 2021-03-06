/*
 *  Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.twosigma.beakerx.kernel.magic.command;

import static com.twosigma.beakerx.kernel.magic.command.MavenJarResolver.POM_XML;

import com.twosigma.beakerx.kernel.magic.command.MavenJarResolver.Dependency;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

public class PomFactory {

  public String createPom(String pathToMavenRepo, Dependency dependency, Map<String, String> repos) throws IOException {
    InputStream pom = getClass().getClassLoader().getResourceAsStream(POM_XML);
    String pomAsString = IOUtils.toString(pom, StandardCharsets.UTF_8);
    pomAsString = configureOutputDir(pathToMavenRepo, pomAsString);
    pomAsString = configureDependencies(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), pomAsString);
    pomAsString = configureRepos(repos, pomAsString);
    return pomAsString;
  }

  private String configureDependencies(String groupId, String artifactId, String version, String pomAsString) {
    return pomAsString.replace(
            "<dependencies></dependencies>",
            "<dependencies>\n" +
                    "  <dependency>\n" +
                    "    <groupId>" + groupId + "</groupId>\n" +
                    "    <artifactId>" + artifactId + "</artifactId>\n" +
                    "    <version>" + version + "</version>\n" +
                    "  </dependency>\n" +
                    "</dependencies>");
  }

  private String configureOutputDir(String pathToMavenRepo, String pomAsString) {
    String absolutePath = new File(pathToMavenRepo).getAbsolutePath();
    return pomAsString.replace(
            "<outputDirectory>pathToNotebookJars</outputDirectory>",
            "<outputDirectory>" + absolutePath + "</outputDirectory>");
  }


  private String configureRepos(Map<String, String> repos, String pomAsString) {
    for (Entry<String, String> entry : repos.entrySet()) {
      pomAsString = configureRepos(pomAsString, entry.getKey(), entry.getValue());
    }
    return pomAsString;
  }


  private String configureRepos(String pomAsString, String name, String url) {
    String repoPattern = "" +
            "<repository>\n" +
            "    <id>%s</id>\n" +
            "    <url>%s</url>\n" +
            "</repository>\n";

    return pomAsString.replace("</repositories>", String.format(repoPattern, name, url) + "</repositories>");
  }
}
