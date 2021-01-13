package com.consol.citrus.generate;

import com.consol.citrus.Generator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class ResourcesGenerator extends Generator {
    private final String contextFile = "citrus-context.xml";
    private final String log4jFile = "log4j2.xml";

    @Override
    public void create() {
        try {
            createCitrusContext();
            createLog4j();
        } catch (ParserConfigurationException | TransformerException ex) {
            ex.printStackTrace();
        }
    }

    private void createCitrusContext() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element beansElement = doc.createElement("beans");
        beansElement.setAttribute("xmlns", "http://www.springframework.org/schema/beans");
        beansElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        beansElement.setAttribute("xmlns:citrus", "http://www.citrusframework.org/schema/config");
        beansElement.setAttribute("xmlns:citrus-http", "http://www.citrusframework.org/schema/http/config");
        beansElement.setAttribute("xsi:schemaLocation", "http://www.springframework.org/schema/beans" +
                "   http://www.springframework.org/schema/beans/spring-beans.xsd" +
                "   http://www.citrusframework.org/schema/config" +
                "   http://www.citrusframework.org/schema/config/citrus-config.xsd" +
                "   http://www.citrusframework.org/schema/http/config" +
                "   http://www.citrusframework.org/schema/http/config/citrus-http-config.xsd");

        Element repository = doc.createElement("citrus:schema-repository");
        repository.setAttribute("id", "schemaRepository");
        repository.setAttribute("type", "json");
        beansElement.appendChild(repository);

        Element schemas = doc.createElement("citrus:schemas");
        repository.appendChild(schemas);

        Element client = doc.createElement("citrus-http:client");
        client.setAttribute("id", "httpClient");
        client.setAttribute("request-url", "https://petstore3.swagger.io");
        beansElement.appendChild(client);

        Element objectMapper = doc.createElement("bean");
        objectMapper.setAttribute("class", "com.fasterxml.jackson.databind.ObjectMapper");
        objectMapper.setAttribute("name", "objectMapper");
        beansElement.appendChild(objectMapper);

        Element messageListener = doc.createElement("bean");
        messageListener.setAttribute("class", "org.example.MessageListener");
        beansElement.appendChild(messageListener);

        doc.appendChild(beansElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(directory + contextFile));

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(source, result);

        log.info("Successfully created file: " + contextFile);
    }

    private void createLog4j() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element configuration = doc.createElement("Configuration");
        configuration.setAttribute("status", "WARN");
        configuration.setAttribute("packages", "scenarioReporter");

        Element appender = doc.createElement("Appenders");

        Element console = doc.createElement("Console");
        console.setAttribute("name", "Console");
        console.setAttribute("target", "SYSTEM_OUT");

        Element pattern = doc.createElement("PatternLayout");
        pattern.setAttribute("pattern", "%d{HH:mm:ss.SSS} [%highlight{%5p}] [%t] %c{1}(%M) - %msg%n");
        console.appendChild(pattern);
        appender.appendChild(console);
        configuration.appendChild(appender);

        Element loggers = doc.createElement("Loggers");

        Element root = doc.createElement("Root");
        root.setAttribute("level", "info");

        Element appenderRef = doc.createElement("AppenderRef");
        appenderRef.setAttribute("ref", "Console");
        root.appendChild(appenderRef);
        loggers.appendChild(root);

        Element logger1 = doc.createElement("Logger");
        logger1.setAttribute("name", "org.springframework");
        logger1.setAttribute("level", "info");
        logger1.setAttribute("additivity", "true");
        Element appenderRef1 = doc.createElement("AppenderRef");
        appenderRef1.setAttribute("ref", "Console");
        logger1.appendChild(appenderRef1);
        loggers.appendChild(logger1);

        Element logger2 = doc.createElement("Logger");
        logger2.setAttribute("name", "com.consol.citrus");
        logger2.setAttribute("level", "info");
        logger2.setAttribute("additivity", "true");
        Element appenderRef2 = doc.createElement("AppenderRef");
        appenderRef2.setAttribute("ref", "Console");
        logger2.appendChild(appenderRef2);
        loggers.appendChild(logger2);

        configuration.appendChild(loggers);

        doc.appendChild(configuration);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(directory + log4jFile));

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(source, result);

        log.info("Successfully created file: " + log4jFile);
    }
}
