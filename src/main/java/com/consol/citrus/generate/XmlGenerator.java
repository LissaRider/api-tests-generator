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

public class XmlGenerator extends Generator {
    private final String contextFile = "citrus-context.xml";
    private final String pomFile = "pom.xml";

    @Override
    public void create() {
        try {
            createCitrusContext();
        } catch (ParserConfigurationException | TransformerException ex) {
            ex.printStackTrace();
        }
    }

    private void createCitrusContext() throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element beansElement = doc.createElement("beans");
        doc.appendChild(beansElement);
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

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(directory + contextFile));

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(source, result);

        log.info("Successfully created file: " + contextFile);
    }

    //TODO: implement method
    private void createPom() throws ParserConfigurationException, TransformerException {

    }
}
