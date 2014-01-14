import java.lang.annotation.Annotation;
import sun.reflect.annotation.*;
public class Dl  {
    String className;
    public Dl (String s) {
        className = s;
        new Thread1().start();
        new Thread2().start();
    }
    public static void main(String[] args)  {
        Dl dl = new Dl(args[0]);
    }

    class Thread1 extends Thread  {
        public void run()  {
            try {
                Annotation[] annArray = Class.forName(className).getAnnotations();
                for (Annotation a: annArray)  System.out.println (a);
            } catch (Exception e) {
                System.out.printf ("Thread1 failes: %s %n", e.getCause());
            }
        }
    }

    class Thread2 extends Thread  {
        public void run() {
            try {
                    AnnotationType test = AnnotationType.getInstance (Class.forName(className));
                    System.out.println (test);
            } catch (Exception e) {
                    System.out.printf ("Thread2 failes: %s %n", e.getCause());
            }
        }
    }
}
