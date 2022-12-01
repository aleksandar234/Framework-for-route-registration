package framework.request;

import controller.Controller;
import controller.GET;
import controller.POST;
import controller.Path;
import framework.init.Init;
import framework.request.enums.Methods;
import getting.GettingClassAndPackage;
import server.Server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class Helper {

    private static Object singleInstance = null;

    public static Object getInstance() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if(singleInstance == null) {
            singleInstance = Class.forName("controller.impl.MyController").getDeclaredConstructor().newInstance();
        }
        return singleInstance;
    }



    // Daj mi parametre iz rute
    // i posto znam da parametri u ruti dolaze nakon upitnika
    public static HashMap<String, String> getParametersFromRoute(String route, Methods methodGetOrPost) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, ClassNotFoundException, IOException {
        // Daj mi sve posle upitnika ako postoji
        String[] splittedRoute = route.split("\\?");

        if(splittedRoute.length == 1) {
            return new HashMap<String, String>();
        }

//        Set<String> files=new HashSet<>();
//        GettingClassAndPackage.listOfPackage("src/",files);
//
//
//        List<String> allPackages = new ArrayList<>();
//        for(String f: files) {
//            String newPath = "";
//            String[] path = f.split("\\.");
//            for(int i = 2; i < path.length; i++) {
//                newPath += path[i];
//                if(i != path.length-1)
//                    newPath += ".";
//            }
//            allPackages.add(newPath);
//        }


//        Set<String> annotatedClasses = new HashSet<>();
//
//        for(String packageName: allPackages) {
//            Class[] allClasses = GettingClassAndPackage.getClasses(packageName);
//
//            for(Class c: allClasses) {
//                Class annotatedClass = Class.forName(c.getName());
//                if(annotatedClass.isAnnotationPresent(Controller.class)) {
//                    annotatedClasses.add(c.getName());
//                }
//            }
//        }
//
//        System.out.println("Ovo su klase:");
//        for(String annotatedCl: annotatedClasses) {
//            System.out.println(annotatedCl);
//        }


//        int len = splittedRoute[0].length();
//        String nameOfRoute = splittedRoute[0].substring(1, len);
        String nameOfRoute = splittedRoute[0];
        HashMap<String, String> hm = new HashMap<>();

        for(String annotated: Init.annotatedClasses) {
            System.out.println("Entering u class: " + annotated);
            Class controllerClass = Class.forName(annotated); // Nasao sam klasu ovim
            Method[] methods = controllerClass.getDeclaredMethods();
            boolean flag = false;
            for(Method mt: methods) {
                System.out.println("Entering in method: " + mt.getName());
                if(mt.isAnnotationPresent(Path.class)) {
                    Path path = mt.getAnnotation(Path.class);
                    if(path.location().equals(nameOfRoute) && mt.isAnnotationPresent(GET.class) && methodGetOrPost.toString().equals(mt.getAnnotation(GET.class).value())) {
                        Method m = controllerClass.getDeclaredMethod(mt.getName(), HashMap.class);
                        Object obj = getInstance();
                        System.out.println("HashCode: " + obj.hashCode());
                        m.setAccessible(true);
                        hm = (HashMap<String, String>) m.invoke(getInstance(), getParametersFromString(splittedRoute[1]));
                        flag = true;
                        break;
                    } else if(path.location().equals(nameOfRoute) && mt.isAnnotationPresent(POST.class) && methodGetOrPost.toString().equals(mt.getAnnotation(POST.class).value())){
                        Method m = controllerClass.getDeclaredMethod(mt.getName());
                        Object obj = getInstance();
                        Object obj1;
                        System.out.println("HashCode: " + obj.hashCode());
                        m.setAccessible(true);
                        obj1 = m.invoke(getInstance());
                        flag = true;
                        break;
                    } else {
                        System.out.println("Finished checking method:" + mt.getName());
                    }
                }
            }
            if(flag) break;
        }

        if(hm.isEmpty()) {
            return Helper.getParametersFromString(splittedRoute[1]);
        }
        return hm;
    }

    public static HashMap<String, String> getParametersFromString(String parametersString) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        // Splituj na osnovu ampersenta
        String[] pairs = parametersString.split("&");
        for (String pair:pairs) {
            // Splituj na osnovu jednako
            String[] keyPair = pair.split("=");
            parameters.put(keyPair[0], keyPair[1]);
        }

        return parameters;
    }
}
