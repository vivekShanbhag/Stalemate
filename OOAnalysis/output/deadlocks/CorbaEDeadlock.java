/* <Cycle-2 locks=" org.omg.CORBA.TypeCode.class com.sun.org.omg.CORBA.ExceptionDescriptionHelper.class">
 * <Thread-1>
 * <com.sun.org.omg.CORBA.OperationDescriptionHelper.class>com.sun.org.omg.CORBA.OperationDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * <org.omg.CORBA.TypeCode.class>com.sun.org.omg.CORBA.OperationDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;#Lorg/omg/CORBA/TypeCode.class;#0=VarientOf=com.sun.org.omg.CORBA.OperationDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * com.sun.org.omg.CORBA.ExceptionDescriptionHelper.class
 * <com.sun.org.omg.CORBA.ExceptionDescriptionHelper.class>com.sun.org.omg.CORBA.ExceptionDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * </Thread-1>
 * <Thread-2>
 * com.sun.org.omg.CORBA.ExceptionDescriptionHelper.insert:(Lorg/omg/CORBA/Any;Lcom/sun/org/omg/CORBA/ExceptionDescription;)V
 * <com.sun.org.omg.CORBA.ExceptionDescriptionHelper.class>com.sun.org.omg.CORBA.ExceptionDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * org.omg.CORBA.TypeCode.class
 * <org.omg.CORBA.TypeCode.class>com.sun.org.omg.CORBA.ExceptionDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;#Lorg/omg/CORBA/TypeCode.class;#0=VarientOf=com.sun.org.omg.CORBA.ExceptionDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * </Thread-2>
 * </Cycle-2>
 */
//import com.sun.org.omg.CORBA.*;
public class CorbaEDeadlock   {
 public static void main(String[] args)  {
  new Thread() {
   public void run() {  com.sun.org.omg.CORBA.ExceptionDescriptionHelper.type(); };
  }.start();
  new Thread() {
   public void run() {  com.sun.org.omg.CORBA.OperationDescriptionHelper.type(); };
  }.start();
 }
}
