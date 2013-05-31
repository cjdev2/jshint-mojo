Simple JSHint mojo
=============

It's real simple, and it runs [JSHint](http://www.jshint.com/) on your *.js files.

Usage:

~~~~~ xml
          <plugin>
                <groupId>com.cj.jshintmojo</groupId>
                <artifactId>jshint-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>lint</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <options>maxparams:3,indent,camelcase,eqeqeq,forin,immed,latedef,noarg,noempty,nonew,undef,trailing</options>
                    <globals>require,$,yourFunkyJavascriptModule</globals>
                    <!-- THESE ARE THE PLACES IN WHICH THE PLUGIN WILL SEARCH FOR *.js FILES
                          THIS LIST DEFAULTS TO "src" (i.e. it searches for *.js everywhere under 'src/*')
                    <directories>
                        <directory>src/main/javascript</directory>
                    </directories>
                    -->
                    <excludes>
                        <!-- EXCLUDES ARE RESOLVED RELATIVE TO THE BASEDIR OF THE MODULE 
  					            <exclude>src/main/webapp/hackyScript.js</exclude>
  					            <exclude>src/main/webapp/myDirectoryForThirdyPartyStuff</exclude>
                        -->
                    </excludes>
                    <!-- CONTROLS WHETHER THE PLUGIN FAILS THE BUILD WHEN JSHINT IS UNHAPPY.
                         This defaults to  "true".  
                         Setting this to "false" is discouraged, as it removes most of the benefit of using this plugin.
                         Instead, if you have problem files that you can't fix:
                            * disable/override jshint on a per-file basis (http://www.jshint.com/docs/#config), or
                            * tell the plugin to specifically exclude them in the 'excludes' section
                    <failOnError>true</failOnError>
                     -->
                </configuration>
           </plugin>
~~~~~
