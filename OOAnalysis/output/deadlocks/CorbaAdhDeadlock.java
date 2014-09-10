/* <Cycle-2 locks=" com.sun.org.omg.CORBA.AttributeDescriptionHelper.class(1.3) org.omg.CORBA.TypeCode.class(1.2)(Update)">
 * <Thread-1>
 * com.sun.org.omg.CORBA.AttributeDescriptionHelper.insert:(Lorg/omg/CORBA/Any;Lcom/sun/org/omg/CORBA/AttributeDescription;)V
 * <com.sun.org.omg.CORBA.AttributeDescriptionHelper.class>com.sun.org.omg.CORBA.AttributeDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * org.omg.CORBA.TypeCode.class
 * <org.omg.CORBA.TypeCode.class>com.sun.org.omg.CORBA.AttributeDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;#Lorg/omg/CORBA/TypeCode.class;#0=VarientOf=com.sun.org.omg.CORBA.AttributeDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * </Thread-1>
 * <Thread-2>
 * <com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.class>com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * <org.omg.CORBA.TypeCode.class>com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;#Lorg/omg/CORBA/TypeCode.class;#0=VarientOf=com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * com.sun.org.omg.CORBA.AttributeDescriptionHelper.class
 * <com.sun.org.omg.CORBA.AttributeDescriptionHelper.class>com.sun.org.omg.CORBA.AttributeDescriptionHelper.type:()Lorg/omg/CORBA/TypeCode;
 * </Thread-2>
 * </Cycle-2>
 */ 

public class CorbaAdhDeadlock   {
 public static void main(String[] args)  {
  new Thread() {
   public void run() {  com.sun.org.omg.CORBA.AttributeDescriptionHelper.insert(
                    new com.sun.corba.se.impl.corba.AnyImpl(
                    new com.sun.corba.se.impl.orb.ORBSingleton()), null); };
  }.start();
  new Thread() {
   public void run() {  com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper.type(); };
  }.start();
 }
}
