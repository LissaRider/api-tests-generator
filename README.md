# generator-maven-plugin
version 2.1.7 only positive test cases

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
                <groupId>ru.lanit.generator</groupId>
                <artifactId>generator-maven-plugin</artifactId>
                <version>2.1.5</version>
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
                    <framework>testng</framework>
                    <isCoverage>false</isCoverage>
                    <buildDirectory>${project.basedir}/src/test</buildDirectory>
                    <tests>
                        <test>
                            <endpoint>httpClient</endpoint>
                            <author>Unknown</author>
                            <swagger>
                                <file>file://${project.basedir}/src/test/resources/petstore.json</file>
                            </swagger>
                        </test>
                    </tests>
                </configuration>
            </plugin>
```
Конфигурация плагина
-------------------- 
Основные настройки:  
`<framework>` - использование тестового фреймворка: testng, junit4, junit5. По умолчанию: testng;  
`<isCoverage>` - генерация декодера для citrus-swagger-covegare. По умолчанию: false;  
`<buildDirectory>` - директория куда будут сгенерированы тесты. По умолчанию: /target/generated/citrus/;  
`<file>` - путь к файлу спецификации Swagger OpenAPI;

Настройки тестов:  
`<endpoint>` - указывает адрес тестируемого сервиса;  
`<author>` - указывает в javaDoc имя автора тестов. По умолчанию: "Unknown";  
`<packageName>` - задаёт структуру папок для сгенерированных тестов. По умолчанию: com.consol.citrus;  
`<description>` - создаёт описание тестов в javaDoc. По умолчанию: "TODO: Description";  

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
