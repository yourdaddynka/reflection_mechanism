package edu.school21.app;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;


public class Menu {
    private final static String path = "src/main/java/edu/school21/model";
    private final static String subPatch = path.substring("src/main/java/".length()).replace('/', '.');
    private final Scanner in;
    private List<Class<?>> classes;
    private List<List<String>> fields;
    private List<List<String>> methods;
    private Object resultObject;

    public Menu() {
        in = new Scanner(System.in);
        initClasses();
        initFields();
        initMethods();
    }

    private void initClasses() {
        Reflections reflections = new Reflections(subPatch, new SubTypesScanner(false));
        classes = new ArrayList<>(reflections.getSubTypesOf(Object.class));
    }

    private void initFields() {
        fields = new ArrayList<>();
        for (Class<?> aClass : classes) {
            List<String> tempList = new ArrayList<>();
            Field[] classFields = aClass.getDeclaredFields();
            for (Field classField : aClass.getDeclaredFields()) {
                tempList.add(classField.getType().getSimpleName() + " " + classField.getName());
            }
            fields.add(tempList);
        }
    }

    private void initMethods() {
        methods = new ArrayList<>();
        for (Class<?> aClass : classes) {
            List<String> tempList = new ArrayList<>();
            Method[] classMethods = aClass.getDeclaredMethods();
            for (Method classMethod : classMethods) {
                tempList.add(classMethod.getReturnType().getSimpleName() + " " + classMethod.getName());
            }
            methods.add(tempList);
        }
    }

    public void run() {
        try {
            Class<?> resClass = printFirst();
            printFieldsMethods(resClass);
            printCreateAnObject(resClass);
            printChanging(resClass);
            methodForCall();
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> printFirst() {
        Class<?> result = null;
        System.out.println("Classes:");
        for (Class<?> value : classes) {
            System.out.println("  - " + value.getSimpleName());
        }
        while (in.hasNext()) {
            String temp = in.nextLine();
            for (Class<?> aClass : classes) {
                if (aClass.getSimpleName().equals(temp)) {
                    result = aClass;
                    return result;
                }
            }
            System.out.println("Incorrect input");
            System.out.println("Classes:");
            for (Class<?> aClass : classes) {
                System.out.println("  - " + aClass.getSimpleName());
            }
        }
        return result;
    }

    private void printFieldsMethods(Class<?> thisField) {
        System.out.println("---------------------");
        System.out.println("fields:");
        fieldsAndMethodsSplit(thisField, fields);
        System.out.println("methods:");
        fieldsAndMethodsSplit(thisField, methods);
        System.out.println("---------------------\n" + "Let’s create an object.");
    }

    private void fieldsAndMethodsSplit(Class<?> thisField, List<List<String>> fields) {
        for (int i = 0; i < fields.size(); i++) {
            if (classes.get(i).equals(thisField)) {
                for (int j = 0; j < fields.get(i).size(); j++) {
                    System.out.println("\t" + "\t" + fields.get(i).get(j));
                }
            }
        }
    }

    private void printCreateAnObject(Class<?> thisField) throws InstantiationException, IllegalAccessException {
        resultObject = thisField.newInstance();
        Field[] fields = resultObject.getClass().getDeclaredFields();
        for (Field value : fields) {
            System.out.println(value.getType().getSimpleName() + " " + value.getName() + ":");
            setParam(value, returnObject(value.getType()));
        }
        System.out.println("Object created: " + resultObject + "\n---------------------");
    }

    private void printChanging(Class<?> changeClass) throws InstantiationException, IllegalAccessException {
        System.out.println("Enter name of the field for changing:");
        Field[] fields = changeClass.newInstance().getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; ) {
            if (fields[i].getName().equals(in.nextLine())) {
                System.out.println("Enter " + fields[i].getType().getSimpleName() + " value:");
                Object changeObjec = returnObject(fields[i].getType());
                System.out.println("changeObjec = " + changeObjec);
                setParam(fields[i], changeObjec);
                System.out.println("parameter changed, enter next");
                i++;
            }
        }
        System.out.println("Object updated: " + resultObject + "\n---------------------");
    }

    private void setParam(Field field, Object changeObjec) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(resultObject, changeObjec);
    }

    private String signatureCreate(Method method) {
        StringJoiner signatureString = new StringJoiner(", ", "(", ")");
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length > 0) {
            for (Parameter parameter : parameters) {
                signatureString.add(parameter.getType().getSimpleName());
            }
        }
        return signatureString.toString();
    }

    private Object returnObject(Object thisObject) throws IllegalAccessException {
        if (thisObject.equals(int.class)) {
            return Integer.parseInt(in.nextLine());
        } else if (thisObject.equals(double.class)) {
            return Double.parseDouble(in.nextLine());
        } else if (thisObject.equals(long.class)) {
            return Long.parseLong(in.nextLine());
        } else if (thisObject.equals(boolean.class)) {
            return Boolean.parseBoolean(in.nextLine());
        } else if (thisObject.equals(String.class)) {
            return in.nextLine();
        }
        throw new IllegalAccessException();
    }

    private void methodForCall() throws IllegalAccessException, InvocationTargetException {
        System.out.println("Enter name of the method for call:");
        String methodName = in.nextLine();
        for (Method method : resultObject.getClass().getDeclaredMethods()) {
            if (methodName.equals(method.getName() + signatureCreate(method))) {
                ArrayList<Object> arguments = new ArrayList<>();
                for (Class<?> parameter : method.getParameterTypes()) {
                    System.out.println("Enter " + parameter.getSimpleName() + " value: ");
                    arguments.add(returnObject(parameter));
                }
                Object returnValue = method.invoke(resultObject, arguments.toArray());
                if (!method.getReturnType().equals(void.class)) {
                    System.out.println("Method returned:\n" + returnValue);
                }
                return;
            }
        }
        throw new IllegalAccessException("Method not found");
    }
}



