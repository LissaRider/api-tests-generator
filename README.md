# generator-maven-plugin

generator-maven-plugin - это генератор API тестов на основе OAS 2 (Swagger)
с использованием Citrus framework.

Системные требования
--------------------
JDK - 1.8  
Maven - 3.3 и выше    

Как добавить в проект
--------------------
Предварительно нужно скачать плагин и установить его в локальный репозиторий с помощью команды:
>mvn clean install

Далее добавить в проект следующую конфигурацию плагина в POM-файл Maven:
```xml
            <plugin>
                <groupId>com.consol.citrus</groupId>
                <artifactId>generator-maven-plugin</artifactId>
                <version>2.0.0</version>
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
                                <file>file://${project.basedir}/src/test/resources/swagger.json</file>
                            </swagger>
                        </test>
                    </tests>
                </configuration>
            </plugin>
```
где указывается путь к файлу спецификации Swagger OpenAPI:
```xml
<file>file://${project.basedir}/src/test/resources/swagger.json</file>
```

Так же можно добавить дополнительный плагин 'build-helper-maven-plugin'  
Он добавляет сгенерированные тесты в компиляцию проекта Maven.  
Это автоматически активирует сгенерированные тесты для выполнения с жизненным циклом сборки Maven на этапе тестирования.
```xml
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
                                <source>${project.build.directory}/generated/citrus/java</source>
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
                                    <directory>${project.build.directory}/generated/citrus/resources</directory>
                                </resource>
                            </resources>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
```
Для работы тестов необходимо добавить ряд зависимостей:
```xml
    <dependencies>
        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>jcl-over-slf4j</artifactId>
            <version>${slf4j.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>${slf4j.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j-impl</artifactId>
            <version>2.13.3</version>
        </dependency>

        <!-- Test scoped dependencies -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Citrus -->
        <dependency>
            <groupId>com.consol.citrus</groupId>
            <artifactId>citrus-core</artifactId>
            <version>${citrus.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.consol.citrus</groupId>
            <artifactId>citrus-java-dsl</artifactId>
            <version>${citrus.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.consol.citrus</groupId>
            <artifactId>citrus-http</artifactId>
            <version>${citrus.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
```

Генерация тестов
--------------------
Генерация тестового кода происходит в жизненном цикле сборки Maven и использует плагин generator-maven-plugin.  
Выполняется с помощью команды:
>mvn clean install

По умолчанию тесты генерируются в каталог: 
>target/generated/citrus