package server;

import controller.*;
import framework.response.JsonResponse;
import framework.response.Response;
import framework.request.enums.Methods;
import framework.request.Header;
import framework.request.Helper;
import framework.request.Request;
import framework.request.exceptions.RequestNotValidException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URL;
import java.util.*;

// ServerThread je procesiranje razlicitog korisnika
public class ServerThread implements Runnable{

    // Da seljemo nesto na socket i da primamo nesto sa socketa
    // koristimo input i output stream
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // Sad mi trebaju samo imena svih paketa i kad ih imam prolazim kroz svaki paket
    // i onda uzimam klasu po klasu i pitam da li je anotirana sa @Controller



    public ServerThread(Socket socket) throws IOException, ClassNotFoundException {
        this.socket = socket;


        try {
            // Na ovaj input stream dolazi neki string koji mi moramo da isparsiramo
            // Koju metodu, koju rutu je hteo, koje argumente, koje headere je prosledio i koje parametre je dobio u body
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));

            // Ovde sta vracamo
            // Koje headere koji status koji body
            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try {

//            Class controllerAnnotation = Class.forName("controller.impl.MyController");
//            Class controllerAnnotation = Class.forName("controller.impl.MyController", false, getClass().getClassLoader());
//            Class controllerAnnotation = MC.class;
//            Class c = new ClassLoader() {
//                Class c = findLoadedClass(controllerAnnotation.toString());
//            }.c;
//            if(controllerAnnotation.isAnnotationPresent(Controller.class)) {
//                Controller controller = (Controller) controllerAnnotation.getAnnotation(Controller.class);
//                System.out.println("Con: " + controller.value());
//            }
//
//            Methods requestMethod = null;
//
//            Method[] methodeController = controllerAnnotation.getDeclaredMethods();
//            for(Method m: methodeController) {
//                System.out.println("Annotation methode is: " + Arrays.toString(m.getDeclaredAnnotations()));
//                if(m.isAnnotationPresent(Path.class) && m.isAnnotationPresent(POST.class)) {
//                    GET get = m.getAnnotation(GET.class);
//                    requestMethod = Methods.POST;
//              }
//              else if(m.isAnnotationPresent(Path.class) && m.isAnnotationPresent(POST.class)) {
////                    POST post = m.getAnnotation(POST.class);
//                    requestMethod = Methods.POST;
//                } else {
//                    System.out.println("Your methode is not annotated");
//                }
//            }


            // Imam ovu klasu Request koja enkapsulira ceo taj string => Request
            // Kako ja to kreiram -> Citam ono sto dolazi na input stream => generateRequest()
            // Nakon svega ovoga, uzeo sam ovo sa input srema
            Request request = this.generateRequest();
            if(request == null) {
                in.close();
                out.close();
                socket.close();
                return;
            }


            // i sad mogu da izbildam odgovor, i kreiram neki response, koji je neka moja klasa
            // koji mora da sazdrzi hedere => Response
            // Response example
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("route_location", request.getLocation());
            responseMap.put("route_method", request.getMethod().toString());
            responseMap.put("parameters", request.getParameters());
            Response response = new JsonResponse(responseMap);

            // render vraca stgring i onda sam to samo reko da mi se ispise na output i to je ono sto mi
            // zapravo vidimo kad pokrenemo aplikaciju i koji odg nam prikaze
            out.println(response.render());

            in.close();
            out.close();
            socket.close();

        } catch (IOException | RequestNotValidException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    // Citam liniju po liniju
    // Kazem da znam da kad splitujem string po spaceu da ce mi prvi argumrnt biti metoda
    // Drugi ce da mi bude lokacija

    private Request generateRequest() throws IOException, RequestNotValidException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        String command = in.readLine();
        if(command == null) {
            return null;
        }
        // Splitovao po spaceu
        String[] actionRow = command.split(" ");
        // Uzeo metodu, stavio ga unutar koje neke Method varijable
        Methods methods = Methods.valueOf(actionRow[0]);
        System.out.println(methods);
        // Uzeo rutu tj lokaciju koja predtavlja lokaciju putanje koju zapravo gadjam
        String route = actionRow[1];
        Header header = new Header();
        // I za parametre sam opet napravio hashMapu koja je po istom principu kljuc value
        // gde imam tu helper classu koja kaze => getParametersFromRoute
        HashMap<String, String> parameters = Helper.getParametersFromRoute(route, methods);

        // I onda sam isao liniju po liniju da evidentiram ove hedere sta mi kljuc a sta mi je vrednost
        do {
            command = in.readLine();
            String[] headerRow = command.split(": ");
            if(headerRow.length == 2) {
                header.add(headerRow[0], headerRow[1]);
            }
        } while(!command.trim().equals(""));

        // Ukoliko je metoda bila post
        // Rekli smo da post moze da sadrzi body
        if(methods.equals(Methods.POST)) {
            // Kako procitam taj body: Tako sto znam duzinu bodya znamo na osnovu content-length hedera
            // I klijent sam preracuna kolka je duzna tog bodya i on sam preracuna kolka je duzina
            // i onda mi na osnovu toga znmo kolki treba da procitamo
            int contentLength = Integer.parseInt(header.get("Content-Length"));
            char[] buff = new char[contentLength];
            // procito sam sve to te duzine sto dolazi na kraju
            in.read(buff, 0, contentLength);
            String parametersString = new String(buff);
            System.out.println(parametersString);
            // Posto znam da je to po istom principu razdvojeno ampersentima pozivam opet onu metodu
            // Dodajem to u ovu mapu paramatara jedan po jedan
            HashMap<String, String> postParameters = Helper.getParametersFromString(parametersString);
            for (String parameterName : postParameters.keySet()) {
                parameters.put(parameterName, postParameters.get(parameterName));
            }
        }
        // I na kraju sam sve to strpao u jedan request koji sve to enkapsulira
        Request request = new Request(methods, route, header, parameters);

        return request;
    }
}
