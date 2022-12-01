package framework.request;

import framework.request.enums.Methods;

import java.util.HashMap;

public class Request {

    // Ona sadrzi metdu koja je poslata u requestu
    private Methods methods;
    // Sadrzi string lokaciju koja se salje u requestu
    private String location;
    // Headere koja je opet zapravo neka zasebna klasa
    // Koja je zapravo hashMapa ovih hedera -> (key, value)
    private Header header;
    private HashMap<String, String> parameters;

    public Request() {
        this(Methods.GET, "/");
    }

    public Request(Methods methods, String location) {
        this(methods, location, new Header(), new HashMap<String, String>());
    }

    public Request(Methods methods, String location, Header header, HashMap<String, String> parameters) {
        this.methods = methods;
        this.location = location;
        this.header = header;
        this.parameters = parameters;
    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    public HashMap<String, String> getParameters() {
        return new HashMap<String, String>(this.parameters);
    }

    public boolean isMethod(Methods methods) {
        return this.getMethod().equals(methods);
    }

    public Methods getMethod() {
        return methods;
    }

    public String getLocation() {
        return location;
    }

    public Header getHeader() {
        return header;
    }
}
