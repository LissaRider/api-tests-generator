package com.consol.citrus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Generator {
    protected Logger log = LoggerFactory.getLogger(getClass());
    protected String swaggerResource;
    protected String packageName;
    protected String directory;
    protected String baseDir;
    protected String endpoint;

    public abstract void create();

    public String getSwaggerResource() {
        return swaggerResource;
    }

    public void setSwaggerResource(String swaggerResource) {
        this.swaggerResource = swaggerResource;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
