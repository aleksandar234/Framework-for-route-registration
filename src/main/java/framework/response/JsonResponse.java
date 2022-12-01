package framework.response;

import com.google.gson.Gson;

// JsonResponse ovde pomocu render zapravo kreira odgovor
public class JsonResponse extends Response{

    private Gson gson;
    private Object jsonObject;

    public JsonResponse(Object jsonObject)
    {
        this.gson = new Gson();
        this.jsonObject = jsonObject;
    }

    @Override
    public String render() {
        StringBuilder responseContent = new StringBuilder();
        // Appenduje mi ovo
        responseContent.append("HTTP/1.1 200 OK\n");
        // Proso kroz sve hedere koje sam nakacio
        for (String key : this.header.getKeys()) {
            responseContent.append(key).append(":").append(this.header.get(key)).append("\n");
        }
        responseContent.append("\n");

        // na kraj sam koriscene biblioteke gson , serijalizovao java object na json i dodao na kraj kao
        // body mog odgovora
        responseContent.append(this.gson.toJson(this.jsonObject));

        return responseContent.toString();
    }
}
