/* <Cycle-2 locks=" com.sun.org.omg.CORBA.InitializerHelper.class(1.3) org.omg.CORBA.TypeCode.class(1.2)(Update)">
 * <Thread-1>
 * com.sun.org.omg.CORBA.InitializerHelper.insert:(Lorg/omg/CORBA/Any;Lcom/sun/org/omg/CORBA/Initializer;)V
 * <com.sun.org.omg.CORBA.InitializerHelper.class>com.sun.org.omg.CORBA.InitializerHelper.type:()Lorg/omg/CORBA/TypeCode;
 * org.omg.CORBA.TypeCode.class
 * <org.omg.CORBA.TypeCode.class>com.sun.org.omg.CORBA.InitializerHelper.type:()Lorg/omg/CORBA/TypeCode;#Lorg/omg/CORBA/TypeCode.class;#0=VarientOf=com.sun.org.omg.CORBA.InitializerHelper.type:()Lorg/omg/CORBA/TypeCode;
 * </Thread-1>
 * <Thread-2>
 * <com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.class>com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * <org.omg.CORBA.TypeCode.class>com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;#Lorg/omg/CORBA/TypeCode.class;#0=VarientOf=com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * com.sun.org.omg.CORBA.InitializerHelper.class
 * <com.sun.org.omg.CORBA.InitializerHelper.class>com.sun.org.omg.CORBA.InitializerHelper.type:()Lorg/omg/CORBA/TypeCode;
 * </Thread-2>
 * </Cycle-2>
 */ 

public class CorbaIhDeadlock   {
 public static void main(String[] args)  {
  new Thread() {
   public void run() {  com.sun.org.omg.CORBA.InitializerHelper.insert(
                    new com.sun.corba.se.impl.corba.AnyImpl(
                    new com.sun.corba.se.impl.orb.ORBSingleton()), null); };
  }.start();
  new Thread() {
   public void run() {  com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.type(); };
  }.start();
 }
}
