/* <Cycle-2 locks=" com.sun.org.omg.CORBA.ParameterDescriptionHelper.class org.omg.CORBA.TypeCode.class">
 * <Thread-1>
 * com.sun.org.omg.CORBA.ParameterDescriptionHelper.insert:(Lorg/omg/CORBA/Any;Lcom/sun/org/omg/CORBA/ParameterDescription;)V
 * <com.sun.org.omg.CORBA.ParameterDescriptionHelper.class>com.sun.org.omg.CORBA.ParameterDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * org.omg.CORBA.TypeCode.class
 * <org.omg.CORBA.TypeCode.class>com.sun.org.omg.CORBA.ParameterDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;#Lorg/omg/CORBA/TypeCode.class;#0
 *    =VarientOf=com.sun.org.omg.CORBA.ParameterDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * </Thread-1>
 * <Thread-2>
 * <com.sun.org.omg.CORBA.OperationDescriptionHelper.class>com.sun.org.omg.CORBA.OperationDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * <org.omg.CORBA.TypeCode.class>com.sun.org.omg.CORBA.OperationDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;#Lorg/omg/CORBA/TypeCode.class;#0
 *    =VarientOf=com.sun.org.omg.CORBA.OperationDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * com.sun.org.omg.CORBA.ParameterDescriptionHelper.class
 * <com.sun.org.omg.CORBA.ParameterDescriptionHelper.class>com.sun.org.omg.CORBA.ParameterDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * </Thread-2>
 * </Cycle-2>
 */

public class CorbaDeadlock   {
 public static void main(String[] args)  {
  new Thread() {
   public void run() {  com.sun.org.omg.CORBA.ParameterDescriptionHelper.type(); };
  }.start();
  new Thread() {
   public void run() {  com.sun.org.omg.CORBA.OperationDescriptionHelper.type(); };
  }.start();
 }
}
