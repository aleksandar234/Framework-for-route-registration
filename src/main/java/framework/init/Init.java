package framework.init;

import controller.Controller;
import getting.GettingClassAndPackage;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Init {

    public static Set<String> annotatedClasses = new HashSet<>();
    public static Set<String> allPackages = new HashSet<>();
    public static Set<Class<?>> annotatedControllerClasses = new HashSet<>();
    public static Map<Class, Object> controllerInstances = new HashMap<>();

    public static void getAllPackagesFromClassPath() {
        Set<String> files = new HashSet<>();
        GettingClassAndPackage.listOfPackage("src/",files);
        for(String f: files) {
            String newPath = "";
            String[] path = f.split("\\.");
            for(int i = 2; i < path.length; i++) {
                newPath += path[i];
                if(i != path.length-1)
                    newPath += ".";
            }
            allPackages.add(newPath);
        }
    }

    public static void getAllAnnotatedClasses() throws IOException, ClassNotFoundException {
        for(String packageName: allPackages) {
            Class[] allClasses = GettingClassAndPackage.getClasses(packageName);

            for(Class c: allClasses) {
                Class annotatedClass = Class.forName(c.getName());
                if(annotatedClass.isAnnotationPresent(Controller.class)) {
                    annotatedClasses.add(c.getName());
                }
            }
        }
    }

    public static void getAllControllerAnnotatedClasses() {
        for(String pack: allPackages) {
            Reflections reflections = new Reflections(pack);
            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Controller.class);
            annotatedControllerClasses.addAll(annotated);
        }

        for(Class<?> cl: annotatedControllerClasses) {
            System.out.println("Anotirane klase sa anotacijom @Controller su: " + cl.getName());
        }
    }

    public static void initializeControllerClasses() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for(Class<?> cls: annotatedControllerClasses) {
            if(!controllerInstances.containsKey(cls)) {
                Constructor constructor = cls.getDeclaredConstructor();
                Object obj = constructor.newInstance();
                controllerInstances.put(cls, obj);
            }
        }
    }

    public static void autoWireControllers() throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        for(Class cls: controllerInstances.keySet()) {
            DIEngine.autowire(controllerInstances.get(cls));
        }
    }

    public static void init() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        getAllPackagesFromClassPath();
        getAllAnnotatedClasses();
        getAllControllerAnnotatedClasses();
        initializeControllerClasses();
        autoWireControllers();
        DIEngine.markBeans();
        DIEngine.autowireBeans();
        DIEngine.autowireServices();
        DIEngine.autowireComponents();
    }

}
