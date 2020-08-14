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
                <version>2.1.3</version>
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
                    <isCoverage>true</isCoverage>
                    <buildDirectory>${project.basedir}/src/test</buildDirectory>
                    <tests>
                        <test>
                            <endpoint>httpClient</endpoint>
                            <swagger>
                                <file>file://${project.basedir}/src/test/resources/petstore.json</file>
                            </swagger>
                        </test>
                    </tests>
                </configuration>
            </plugin>
```
где указывается путь к файлу спецификации Swagger OpenAPI:
```xml
<file>file://${project.basedir}/src/test/resources/petstore.json</file>
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
