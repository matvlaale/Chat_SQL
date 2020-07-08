package client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class TestingClass {
    public static void main(String[] args) {
        Class tests = Testing.class;
        start(tests);
    }

    public static void start(String tests) throws ClassNotFoundException {
        start(Class.forName(tests));
    }

    public static void start(Class tests) {
        Method[] mas = tests.getMethods();

        Method[] befaft = getBefAft(mas);
        ArrayList<Method> methods = prepare(mas);

        try {
            if (befaft[0] != null) befaft[0].invoke(null);

            for (Method meth : methods)
                meth.invoke(null);

            if (befaft[1] != null) befaft[1].invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Method[] getBefAft(Method[] methods) {
        Method[] mas = {null, null};
        for (Method method : methods) {
            if (method.isAnnotationPresent(BeforeSuit.class) && mas[0] == null) {
                mas[0] = method;
            } else if (method.isAnnotationPresent(BeforeSuit.class)) throw new RuntimeException("Not one BeforeSuit!");

            if (method.isAnnotationPresent(AfterSuit.class) && mas[1] == null) {
                mas[1] = method;
            } else if (method.isAnnotationPresent(AfterSuit.class)) throw new RuntimeException("Not one AfterSuit!");
        }
        return mas;
    }

    public static ArrayList<Method> prepare(Method[] methods) {
        ArrayList<Method> mas = new ArrayList<>();
        for (Method method : methods)
            if (method.isAnnotationPresent(Test.class))
                mas.add(method);

        for (int j = 1; j < mas.size(); j++) {
            for (int i = 1; i < mas.size() - j + 1; i++) {
                if (mas.get(i - 1).getDeclaredAnnotation(Test.class).priority() < mas.get(i).getDeclaredAnnotation(Test.class).priority()) {
                    Method zap = mas.get(i - 1);
                    mas.set(i - 1, mas.get(i));
                    mas.set(i, zap);
                }
            }
        }
        return mas;
    }
}

class Testing {
    //No annotation
    public static void before() {
        System.out.println("before");
    }

    @AfterSuit
    public static void after() {
        System.out.println("after");
    }

    @Test(priority = 1)
    public static void first() {
        System.out.println("First test");
    }

    @Test(priority = 5)
    public static void second() {
        System.out.println("Second test");
    }

    public static void notTest() {
        System.out.println("There are no tests here ^.^");
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {
    int priority();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface BeforeSuit {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface AfterSuit {
}