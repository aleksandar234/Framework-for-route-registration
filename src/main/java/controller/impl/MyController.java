package controller.impl;

import controller.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MyController{

    public MyController() {}

//    @Autowired(verbose = true)
//    private TestController testController;

    @GET
    @Path(location = "/upperCase")
    private HashMap<String, String> allUpper(HashMap<String, String> parameters) {
        HashMap<String, String> upperParams = new HashMap<>();
        for (Map.Entry<String, String> set : parameters.entrySet()) {
            String newSet = set.getValue().toUpperCase();
            upperParams.put(set.getKey(), newSet);
        }
        return upperParams;
    }

    @GET
    @Path(location = "/capitalized")
    private HashMap<String, String> allFirstLettersUpper(HashMap<String, String> parameters) {
        HashMap<String, String> capitalizedParams = new HashMap<>();
        for(Map.Entry<String, String> set: parameters.entrySet()) {
            String newSet = set.getValue().substring(0, 1).toUpperCase() + set.getValue().substring(1);
            capitalizedParams.put(set.getKey(), newSet);
        }
        return capitalizedParams;
    }

    @POST
    @Path(location = "/someAction")
    private void somePostAction() {
        System.out.println("Redirektovano na neku akciju");
    }


}
