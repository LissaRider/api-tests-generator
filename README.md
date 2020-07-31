# citrus-test-generator

1. Compile:

mvn clean install

2. Add plugin in your project:

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
