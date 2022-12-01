package framework.response;

import framework.request.Header;

public abstract class Response {
    // Mora da sadzi hedere
    protected Header header;

    public Response() {
        this.header = new Header();
    }

    // i mora da ima render metodu koja ce zapravo da
    // izrenderuje ono sto ce doci nakon hedera tj body
    public abstract String render();
}
