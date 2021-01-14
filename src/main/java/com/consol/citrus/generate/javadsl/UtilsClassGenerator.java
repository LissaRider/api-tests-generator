package com.consol.citrus.generate.javadsl;

import com.consol.citrus.Generator;
import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.report.MessageListener;
import com.consol.citrus.report.MessageTracingTestListener;
import com.squareup.javapoet.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UtilsClassGenerator extends Generator {

    @Override
    public void create() {
        TypeSpec customListener = getCustomListener();

        CodeBlock initLog = CodeBlock.builder()
                .add("$T.getLogger($T.class.getSimpleName())",
                        LogManager.class, MessageListener.class).build();

        FieldSpec logField = FieldSpec.builder(ClassName.get(Logger.class), "log")
                .addModifiers(Modifier.STATIC, Modifier.PROTECTED)
                .initializer(initLog)
                .build();

        MethodSpec bean = MethodSpec.methodBuilder("messageTracingTestListener")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(Bean.class)
                .returns(ClassName.get(MessageTracingTestListener.class))
                .addCode(CodeBlock.builder()
                        .add("return new CustomMessageListener();\n")
                        .build())
                .build();

        TypeSpec classBuilder = TypeSpec.classBuilder("MessageListener")
                .addMethod(bean)
                .addType(customListener)
                .addModifiers(Modifier.PUBLIC)
                .addField(logField)
                .addAnnotation(Component.class)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, classBuilder).indent("    ")
                .build();

        try {
            javaFile.writeTo(new File(baseDir));
            log.info("Successfully created class: MessageListener");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeSpec getCustomListener() {
        CodeBlock initStringBuilder = CodeBlock.builder()
                .add("new $T()", StringBuilder.class)
                .build();

        FieldSpec stringBuilderField = FieldSpec.builder(StringBuilder.class, "stringBuilder")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(initStringBuilder).build();

        return TypeSpec.classBuilder("CustomMessageListener")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addField(stringBuilderField)
                .superclass(MessageTracingTestListener.class)
                .addMethods(getMethods())
                .build();

    }

    private Iterable<MethodSpec> getMethods() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        MethodSpec onInboundMessage = MethodSpec.methodBuilder("onInboundMessage")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(getParameters())
                .addCode(getMessageMethod("INBOUND_MESSAGE"))
                .build();

        MethodSpec onOutboundMessage = MethodSpec.methodBuilder("onOutboundMessage")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameters(getParameters())
                .addCode(getMessageMethod("OUTBOUND_MESSAGE"))
                .build();

        MethodSpec onTestFinish = MethodSpec.methodBuilder("onTestFinish")
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(TestCase.class, "test").build())
                .addModifiers(Modifier.PUBLIC)
                .addCode(CodeBlock.builder()
                        .add("super.onTestFinish(test);\n")
                        .add("stringBuilder.setLength(0);\n")
                        .build()).build();

        MethodSpec separator = MethodSpec.methodBuilder("separator")
                .addModifiers(Modifier.PRIVATE)
                .returns(ClassName.get(String.class))
                .addCode(CodeBlock.builder()
                        .add("return \"======================================================================\";\n")
                        .build())
                .build();

        MethodSpec afterPropertiesSet = MethodSpec.methodBuilder("afterPropertiesSet")
                .addModifiers(Modifier.PUBLIC)
                .addException(ClassName.get(Exception.class))
                .addCode(CodeBlock.builder()
                        .add("try {\n\tsuper.afterPropertiesSet();\n} catch ($T ignore) {}\n", CitrusRuntimeException.class)
                        .build())
                .build();


        methodSpecs.add(onInboundMessage);
        methodSpecs.add(onOutboundMessage);
        methodSpecs.add(onTestFinish);
        methodSpecs.add(separator);
        methodSpecs.add(afterPropertiesSet);

        return methodSpecs;
    }

    private Iterable<ParameterSpec> getParameters() {
        List<ParameterSpec> list = new ArrayList<>();

        list.add(ParameterSpec.builder(Message.class, "message").build());
        list.add(ParameterSpec.builder(TestContext.class, "context").build());

        return list;
    }

    private CodeBlock getMessageMethod(String text) {
        return CodeBlock.builder()
                .add("stringBuilder.append(\"" + text +":\").append(\"n\").append(message).append(\"n\").append(separator()).append(\"n\");\n\n")
                .add("log.info(\"\\n\" + separator() + \"\\n" + text + ":\\n\" + message.toString() + \"\\n\" + separator());\n\n")
                .add("super.onInboundMessage(message, context);\n")
                .build();

    }
}
