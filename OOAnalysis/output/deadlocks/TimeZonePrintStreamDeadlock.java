/*
 * <Cycle-2 locks=" java.util.TimeZone.class java.io.PrintStream">
 * <Thread-1>
 * com.sun.java.util.jar.pack.PackerImpl.pack:(Ljava/util/jar/JarFile;Ljava/io/OutputStream;)V
 * <java.util.TimeZone.class>java.util.TimeZone.getTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
 * java.util.TimeZone.getTimeZone:(Ljava/lang/String;Z)Ljava/util/TimeZone;
 * java.util.TimeZone.parseCustomTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
 * sun.util.calendar.ZoneInfoFile.getZoneInfo:(Ljava/lang/String;)Lsun/util/calendar/ZoneInfo;
 * sun.util.calendar.ZoneInfoFile.createZoneInfo:(Ljava/lang/String;)Lsun/util/calendar/ZoneInfo;
 * sun.util.calendar.ZoneInfoFile.readZoneInfoFile:(Ljava/lang/String;)[B
 * java.io.PrintStream.println:(Ljava/lang/String;)V
 * java.io.PrintStream
 * <java.io.PrintStream>java.io.PrintStream.println:(Ljava/lang/String;)V#Ljava/io/PrintStream;#0
 *    =VarientOf=java.io.PrintStream.println:(Ljava/lang/String;)V
 * </Thread-1>
 * <Thread-2>
 * sun.rmi.server.UnicastServerRef.oldDispatch:(Ljava/rmi/Remote;Ljava/rmi/server/RemoteCall;I)V
 * sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * <java.io.PrintStream>sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V#Ljava/io/PrintStream;#0
 *    =VarientOf=sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * java.lang.StringBuilder.append:(Ljava/util/Date;)Ljava/lang/StringBuilder;
 *    =THROandVarientOf=java.lang.StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 * java.lang.String.valueOf:(Ljava/util/Date;)Ljava/lang/String;=THROandVarientOf=java.lang.String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
 * java.util.Date.toString:()Ljava/lang/String;
 * java.util.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;
 * java.util.Date.normalize:(Lsun/util/calendar/BaseCalendar$Date;)Lsun/util/calendar/BaseCalendar$Date;
 * java.util.TimeZone.class
 * <java.util.TimeZone.class>java.util.TimeZone.getTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
 * </Thread-2>
 */

public class TimeZonePrintStreamDeadlock    {
 public static void main(String[] args)  {
  new Thread() {
   public void run() {
      try {  (new sun.rmi.server.UnicastServerRef (77)).dispatch (null, null); }
      catch (java.io.IOException e)
          { System.out.println ("Exception thrown: " + e); };
   };
  }.start();
  new Thread() {
   public void run() {
      try { (new com.sun.java.util.jar.pack.PackerImpl()).pack ((java.util.jar.JarFile) null, null); }
      catch (java.io.IOException e)
          { System.out.println ("Exception thrown: " + e); };
   };
  }.start();
 }
}
