Simple JSHint mojo
=============

It's real simple, and it runs [JSHint](http://www.jshint.com/) on your *.js files.

Usage:

~~~~~ xml
          <plugin>
                <groupId>com.cj.jshintmojo</groupId>
                <artifactId>jshint-maven-plugin</artifactId>
                <version>0.0.6-SNAPSHOT</version>
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
                </configuration>
           </plugin>
~~~~~
