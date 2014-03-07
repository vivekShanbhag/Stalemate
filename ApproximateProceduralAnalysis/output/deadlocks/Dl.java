import java.lang.annotation.Annotation;
import sun.reflect.annotation.*;
public class Dl  {
 public static void main(String[] args)  {
  final String cN = args[0];
  new Thread() {
   public void run() { try {
     for (Annotation a: Class.forName(cN).getAnnotations())
      System.out.println (a);
    } catch (Exception e) {
     System.out.printf ("T1: %s", e.getCause()); }
   };}.start();
  new Thread() {
   public void run() { try {
     System.out.println (
      AnnotationType.getInstance (Class.forName(cN)));
    } catch (Exception e) {
       System.out.printf ("T2: %s", e.getCause()); }
   };}.start();
 }
}
