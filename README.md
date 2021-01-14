# generator-maven-plugin
version 3.1.1

generator-maven-plugin - это генератор API тестов на основе OpenApi 3.0
с использованием Citrus framework.

Системные требования
--------------------
JDK - 1.8  
Maven - 3.3 и выше    

Как добавить в проект
--------------------
Предварительно нужно скачать плагин и установить его в локальный репозиторий с помощью команды:
>mvn clean install

Далее создать новый Maven проект и добавить в него плагины:
```xml
                    <plugins>
                        <plugin>
                            <groupId>ru.lanit.generator</groupId>
                            <artifactId>generator-maven-plugin</artifactId>
                            <version>3.1.1</version>
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
                                <buildDirectory>${project.basedir}/src/test</buildDirectory>
                                <tests>
                                    <test>
                                        <packageName>org.example</packageName>
                                        <endpoint>httpClient</endpoint>
                                        <author>Unknown</author>
                                        <swagger>
                                            <file>file:${project.basedir}\src\test\resources\petstore.json</file>
                                        </swagger>
                                    </test>
                                </tests>
                            </configuration>
                        </plugin>
                        <plugin>
                            <groupId>org.apache.maven.plugins</groupId>
                            <artifactId>maven-compiler-plugin</artifactId>
                            <configuration>
                                <source>8</source>
                                <target>8</target>
                            </configuration>
                            <executions>
                                <execution>
                                    <id>default-testCompile</id>
                                    <phase>test-compile</phase>
                                    <goals>
                                        <goal>testCompile</goal>
                                    </goals>
                                    <configuration>
                                        <skip>true</skip>
                                    </configuration>
                                </execution>
                            </executions>
                        </plugin>
                    </plugins>
```
Конфигурация плагина
-------------------- 
Configuration:  
`framework` - использование тестового фреймворка: testng, junit4, junit5. По умолчанию: testng;  
`isCoverage` - генерация декодера для citrus-swagger-covegare. По умолчанию: false;  
`buildDirectory` - директория куда будут сгенерированы тесты. По умолчанию: /target/generated/citrus/; 
`tests > test > endpoint` - имя переменной с урл клиента;  
`tests > test > author` - указывает в javaDoc имя автора тестов. По умолчанию: "Unknown";  
`tests > test > packageName` - задаёт структуру папок для сгенерированных тестов. По умолчанию: com.consol.citrus;  
`tests > test > description` - создаёт описание тестов в javaDoc. По умолчанию: "TODO: Description"; 
`tests > test > swagger > file` - путь к файлу спецификации Swagger OpenAPI;  

Для работы тестов необходимо добавить ряд зависимостей:  
*Прим. генератор работает только с Citrus Framework версии 3.0.0-M1*  
```xml
    <dependencies>
            <dependency>
                <groupId>org.threeten</groupId>
                <artifactId>threetenbp</artifactId>
                <version>1.5.0</version>
            </dependency>
            <dependency>
                <groupId>io.swagger.core.v3</groupId>
                <artifactId>swagger-core</artifactId>
                <version>2.1.6</version>
            </dependency>
            <dependency>
                <groupId>com.google.code.gson</groupId>
                <artifactId>gson</artifactId>
                <version>2.8.6</version>
            </dependency>
            <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-api</artifactId>
                <version>1.7.30</version>
            </dependency>
            <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>jcl-over-slf4j</artifactId>
                <version>1.7.30</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>org.slf4j</groupId>
                <artifactId>slf4j-log4j12</artifactId>
                <version>1.7.30</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-slf4j-impl</artifactId>
                <version>2.13.3</version>
            </dependency>
            <dependency>
                <groupId>org.testng</groupId>
                <artifactId>testng</artifactId>
                <version>7.1.0</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>com.consol.citrus</groupId>
                <artifactId>citrus-core</artifactId>
                <version>3.0.0-M1</version>
            </dependency>
            <dependency>
                <groupId>com.consol.citrus</groupId>
                <artifactId>citrus-java-dsl</artifactId>
                <version>3.0.0-M1</version>
            </dependency>
            <dependency>
                <groupId>com.consol.citrus</groupId>
                <artifactId>citrus-http</artifactId>
                <version>3.0.0-M1</version>
            </dependency>
            <dependency>
                <groupId>com.consol.citrus</groupId>
                <artifactId>citrus-testng</artifactId>
                <version>3.0.0-M1</version>
            </dependency>
        </dependencies>
```

Генерация тестов
--------------------
Генерация тестового кода происходит в жизненном цикле сборки Maven и использует плагин generator-maven-plugin.  
Выполняется с помощью команды:
>mvn clean package
