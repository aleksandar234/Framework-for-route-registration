package framework.init;

import controller.*;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DIEngine {

    private static Set<Class<?>> enteredClasses = new HashSet<>();
    public static HashMap<Class, Object> beanInstances = new HashMap<>();
    public static HashMap<Class, Object> serviceInstances = new HashMap<>();
    public static HashMap<Class, Object> componentInstances = new HashMap<>();

    public static void markBeans() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for(String pack: Init.allPackages) {
            Reflections reflections = new Reflections(pack);
            Set<Class<?>> beans = reflections.getTypesAnnotatedWith(Bean.class);
            Set<Class<?>> services = reflections.getTypesAnnotatedWith(Service.class);
            Set<Class<?>> components = reflections.getTypesAnnotatedWith(Component.class);

            for(Class cl: beans) {
                Bean bean = (Bean) cl.getDeclaredAnnotation(Bean.class);
                Scope scopeValue = bean.scope();
                if(scopeValue.equals(Scope.SINGLETON) || scopeValue.equals(Scope.PROTOTYPE)) {
                    Constructor constructor = cl.getDeclaredConstructor();
                    Object obj = constructor.newInstance();
                    beanInstances.put(cl, obj);
                }
            }

            for(Class cl: services) {
                Constructor constructor = cl.getDeclaredConstructor();
                Object obj = constructor.newInstance();
                serviceInstances.put(cl, obj);
            }

            for(Class cl: components) {
                Constructor constructor = cl.getDeclaredConstructor();
                Object obj = constructor.newInstance();
                componentInstances.put(cl, obj);
            }

        }
    }

    public static void autowireBeans() throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        for (Class c : beanInstances.keySet()) {
            autowire(beanInstances.get(c));
        }
    }

    public static void autowireServices() throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        for (Class c : serviceInstances.keySet()) {
            autowire(serviceInstances.get(c));
        }
    }

    public static void autowireComponents() throws IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException {
        for (Class c : componentInstances.keySet()) {
            autowire(componentInstances.get(c));
        }
    }


    public static void autowire(Object obj) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {

        // Ovde setujem objekat da mi bude clasa u kojoj se nalazim
        // Potom uzimam sva polja te klase i prolazim kroz njih
        Field[] fields = obj.getClass().getDeclaredFields();

//        System.out.println("Nalazim se u klasiama:" + obj.getClass().getName());

        for(Field field: fields) {
            field.setAccessible(true);
            if(field.isAnnotationPresent(Autowired.class)) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                if(!enteredClasses.contains(field.getType())){
                    if(Init.controllerInstances.containsKey(field.getType())) {
                        // Ovim ovde settujem da mi taj objekat vise ne bude objekat ove klase koje sam gore settovao
                        // nego da mi bude novi objekat klase koja je anotrana sa autowired
                        // kako bi posle mogao u rekurziji da pristupim toj klasi
                        field.set(obj, Init.controllerInstances.get(field.getType()));
                        if(autowired.verbose()) {
                            injectAutowire(field, obj);
                        }
                        enteredClasses.add(field.getType());
                        autowire(field.get(obj));
                    }
                    else if(beanInstances.containsKey(field.getType())) {
                        System.out.println(field.getName());
                        Bean bean = field.getType().getDeclaredAnnotation(Bean.class);
                        Object singletonOrPrototype;
                        if(bean.scope().equals(Scope.SINGLETON)) {
                            System.out.println("SINGLETON");
                            singletonOrPrototype = beanInstances.get(field.getType());
                        } else {
                            System.out.println("PROTOTYPE");
                            Constructor constructor = field.getType().getDeclaredConstructor();
                            singletonOrPrototype = constructor.newInstance();
                        }
                        field.set(obj, singletonOrPrototype);
                        if(autowired.verbose()) {
                            injectAutowire(field, obj);
                        }
                        enteredClasses.add(field.getType());
                        autowire(field.get(obj));
                    } else if(serviceInstances.containsKey(field.getType())){
                        Object singleton = serviceInstances.get(field.getType());
                        field.set(obj, singleton);
                        if(autowired.verbose()) {
                            injectAutowire(field, obj);
                        }
                        enteredClasses.add(field.getType());
                        autowire(field.get(obj));
                    } else if(componentInstances.containsKey(field.getType())) {
                        Constructor constructor = field.getType().getDeclaredConstructor();
                        Object prototype = constructor.newInstance();
                        field.set(obj, prototype);
                        if(autowired.verbose()) {
                            injectAutowire(field, obj);
                        }
                        enteredClasses.add(field.getType());
                        autowire(field.get(obj));
                    }
                }
            }
        }

    }

    public static void injectAutowire(Field field, Object parent) throws IllegalAccessException {
        LocalDateTime now = LocalDateTime.now();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Initialized ");
        stringBuilder.append("<" + field.getType() + ">" + " " + "<" + field.getName() + ">" + " in ");
        stringBuilder.append("<" + parent.getClass().getName() + ">"+ " on " + now);
        stringBuilder.append(" with " + field.get(parent).hashCode());
        System.out.println(stringBuilder);
        System.out.println("------------------------");
    }


}
