Simple JSHint mojo
=============

It's real simple, and it runs [JSHint](http://www.jshint.com/) on your *.js files.

Goals:
--------------

* jshint:lint -> runs jshint on your files (per your configuration settings)

Example configuration:
--------------

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
         <version>2.4.3</version>
         <options>maxparams:3,indent,camelcase,eqeqeq,forin,immed,latedef,noarg,noempty,nonew</options>
         <globals>require,$,yourFunkyJavascriptModule</globals>
         <configFile>src/main/resources/jshint.conf.js</configFile>
         <directories>
             <directory>src/main/javascript</directory>
         </directories>
         <excludes>
              <exclude>src/main/webapp/hackyScript.js</exclude>
              <exclude>src/main/webapp/myDirectoryForThirdyPartyStuff</exclude>
         </excludes>
         <reporter>jslint</reporter>
         <reportFile>target/jshint.xml</reportFile>
         <failOnError>false</failOnError>
     </configuration>
</plugin>
~~~~~

Example of `configFile` contents, equivalent to the XML configuration above:

```javascript
{
  // Options
  "maxparams": 3,
  "indent": true,
  "camelcase": true,
  "eqeqeq": true,
  "forin": true,
  "immed": true,
  "latedef": true,
  "noarg": true,
  "noempty": true,
  "nonew": true,
  /*
   * Globals
   */
  "globals": { 
     "require": false,
     "$": false,
     "yourFunkyJavascriptModule": false
  }
}
```

Configuration options:
-----------------------

| Option          |  Default value                | Explanation  |
| ---------------: | :---------------------------: | -------------|
| version         |  2.4.1                        |   Selects which embedded version of jshint will be used |
| options         |                               |   List of comma-separated [JSHint options](http://www.jshint.com/docs/#options)            |
| globals         |                               |   List of comma-separated [JSHint globals](http://www.jshint.com/docs/#usage)             |
| configFile      |                               |   Path to a JSHint JSON config file. Its contents will override values set in `options` and `globals`, if present. Please note that block and line comments will be stripped prior to processing so it's OK to include them. |
| directories     |  `<directory>src</directory>` |   Locations in which the plugin will search for *.js files |
| excludes        |                               |   Excludes are resolved relative to the basedir of the module |
| reporter        |                               |   If present, JSHint will generate a reporting file which can be used for some CI tools. Currently, only `jslint` and `checkstyle` format are supported. |
| reportFile      |  target/jshint.xml            |   Path to an output reporting file |
| failOnError     |                  true         |   Controls whether the plugin fails the build when JSHint is unhappy. Setting this to `false` is discouraged, as it removes most of the benefit of using this plugin. Instead, if you have problem files that you can't fix [disable/override JSHint on a per-file basis](http://www.jshint.com/docs/#config), or tell the plugin to specifically exclude them in the `excludes` section |


