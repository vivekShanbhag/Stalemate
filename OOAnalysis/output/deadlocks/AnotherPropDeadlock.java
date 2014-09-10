/* 
 * <Cycle-2 locks=" java.io.PrintStream java.util.Properties">
 * <Thread-1>
 * sun.rmi.server.UnicastServerRef.oldDispatch:(Ljava/rmi/Remote;Ljava/rmi/server/RemoteCall;I)V
 * sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * <java.io.PrintStream>sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V#Ljava/io/PrintStream;#0
 * java.lang.StringBuilder.append:(Ljava/util/Date;)Ljava/lang/StringBuilder;
 *  =THROandVarientOf=java.lang.StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 * java.lang.String.valueOf:(Ljava/util/Date;)Ljava/lang/String;=THROandVarientOf=java.lang.String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
 * java.util.Date.toString:()Ljava/lang/String;
 * java.util.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;
 * sun.util.calendar.BaseCalendar.getCalendarDate:(JLsun/util/calendar/BaseCalendar$Date;)Lsun/util/calendar/CalendarDate;
 *  =THROandVarientOf=sun.util.calendar.BaseCalendar.getCalendarDate:(JLsun/util/calendar/CalendarDate;)Lsun/util/calendar/CalendarDate;
 * sun.util.calendar.ZoneInfo.getOffsets:(J[I)I
 * sun.util.calendar.ZoneInfo.getOffsets:(J[II)I
 * java.util.SimpleTimeZone.inDaylightTime:(Ljava/util/Date;)Z
 * java.util.SimpleTimeZone.getOffset:(J)I
 * java.util.SimpleTimeZone.getOffsets:(J[I)I
 * sun.util.calendar.CalendarSystem.forName:(Ljava/lang/String;)Lsun/util/calendar/CalendarSystem;
 * sun.util.calendar.LocalGregorianCalendar.getLocalGregorianCalendar:(Ljava/lang/String;)Lsun/util/calendar/LocalGregorianCalendar;
 * java.util.Properties.getProperty:(Ljava/lang/String;)Ljava/lang/String;
 * java.util.Properties
 * <java.util.Properties>java.util.Properties.get:(Ljava/lang/String;)Ljava/lang/Object;
 * </Thread-1>
 * <Thread-2>
 * java.util.Properties.storeToXML:(Ljava/io/OutputStream;Ljava/lang/String;)V
 * java.util.Properties.storeToXML:(Ljava/io/OutputStream;Ljava/lang/String;Ljava/lang/String;)V
 * java.util.XMLUtils.save:(Ljava/util/Properties;Ljava/io/OutputStream;Ljava/lang/String;Ljava/lang/String;)V
 * javax.xml.parsers.DocumentBuilderFactory.newInstance:()Ljavax/xml/parsers/DocumentBuilderFactory;
 * javax.xml.parsers.FactoryFinder.find:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;
 * javax.xml.parsers.FactoryFinder.findJarServiceProvider:(Ljava/lang/String;)Ljava/lang/Object;
 * javax.xml.parsers.FactoryFinder.newInstance:(Ljava/lang/String;Ljava/lang/ClassLoader;Z)Ljava/lang/Object;
 * java.lang.Class.newInstance:()Ljava/lang/Object;
 * java.lang.Class.checkMemberAccess:(ILjava/lang/ClassLoader;)V
 * java.lang.SecurityManager.checkMemberAccess:(Ljava/lang/Class;I)V
 * java.lang.SecurityManager.checkPermission:(Ljava/lang/RuntimePermission;)V=THRO=java.lang.SecurityManager.checkPermission:(Ljava/security/Permission;)V
 * java.security.AccessController.checkPermission:(Ljava/lang/RuntimePermission;)V=THRO=java.security.AccessController.checkPermission:(Ljava/security/Permission;)V
 * java.lang.Thread.dumpStack:()V
 * java.lang.Exception.printStackTrace:()V
 * java.lang.Exception.printStackTrace:(Ljava/io/PrintStream;)V
 * java.io.PrintStream
 * java.lang.Exception.printStackTrace:(Ljava/io/PrintStream;)V#Ljava/io/PrintStream;#0
 * </Thread-2>
 */ 

public class AnotherPropDeadlock    {
 public static void main(String[] args)  {
  new Thread() {
   public void run() { try {
      sun.rmi.server.UnicastServerRef kk = new sun.rmi.server.UnicastServerRef (77);
      System.out.println ("sun.rmi.server.UnicastServerRef.callLog is" + 
             (sun.rmi.server.UnicastServerRef.callLog == null ? " " : " not ") + "null.");
      kk.dispatch (null, null);
    } catch (java.io.IOException e) {
      System.out.println ("Exception thrown: " + e);
    };
   };
  }.start();
  new Thread() {
   public void run() { try {
      System.getProperties().storeToXML(System.err, "kkzb");
    } catch (java.io.IOException e) {
      System.out.println ("Exception thrown: " + e);
    };
   };
  }.start();
 }
}
