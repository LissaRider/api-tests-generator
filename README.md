# citrus-test-generator

1. Compile:

mvn clean install

2. Add plugins in your project:

```xml
<plugin>
                <groupId>com.consol.citrus</groupId>
                <artifactId>generator-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <id>generate-tests</id>
                        <phase>generate-test-sources</phase>
                        <goals>
                            <goal>generate-tests</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <type>java</type>
                    <framework>testng</framework>
                    <tests>
                        <test>
                            <endpoint>todoClient</endpoint>
                            <disabled>true</disabled>
                            <swagger>
                                <file>file://${project.basedir}/src/test/resources/user-login-api.json</file>
                            </swagger>
                        </test>
                    </tests>
                </configuration>
            </plugin>

            <plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.0.0</version>
    <executions>
      <execution>
        <id>add-test-sources</id>
        <phase>generate-test-sources</phase>
        <goals>
          <goal>add-test-source</goal>
        </goals>
        <configuration>
          <sources>
            <source>/home/sven/IdeaProjects/citrus-website/target/generated/citrus/java</source>
          </sources>
        </configuration>
      </execution>
      <execution>
        <id>add-test-resources</id>
        <phase>generate-test-resources</phase>
        <goals>
          <goal>add-test-resource</goal>
        </goals>
        <configuration>
          <resources>
            <resource>
              <directory>/home/sven/IdeaProjects/citrus-website/target/generated/citrus/resources</directory>
            </resource>
          </resources>
        </configuration>
      </execution>
    </executions>
</plugin>
  ```
  
  
