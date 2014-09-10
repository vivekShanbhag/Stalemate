/*
 * <Cycle-3 locks=" java.util.TimeZone.class sun.util.calendar.ZoneInfo.class java.io.PrintStream">
 * <Thread-1>
 * java.sql.Date.getTimeImpl:()J=VarientOf=java.util.Date.getTimeImpl:()J
 * java.sql.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;=THROandVarientOf=java.util.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;
 * sun.util.calendar.BaseCalendar.getCalendarDate:(JLsun/util/calendar/BaseCalendar$Date;)Lsun/util/calendar/CalendarDate;=THROandVarientOf=sun.util.calendar.BaseCalendar.getCalendarDate:(JLsun/util/calendar/CalendarDate;)Lsun/util/calendar/CalendarDate;
 * sun.util.calendar.ZoneInfo.getOffsets:(J[I)I
 * sun.util.calendar.ZoneInfo.getOffsets:(J[II)I
 * java.util.SimpleTimeZone.inDaylightTime:(Ljava/util/Date;)Z
 * java.util.Date.getTime:()J
 * java.util.Date.getTimeImpl:()J
 * java.util.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;
 * java.util.Date.normalize:(Lsun/util/calendar/BaseCalendar$Date;)Lsun/util/calendar/BaseCalendar$Date;
 * <java.util.TimeZone.class>java.util.TimeZone.getTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
 * java.util.TimeZone.getTimeZone:(Ljava/lang/String;Z)Ljava/util/TimeZone;
 * sun.util.calendar.ZoneInfo.getTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
 * sun.util.calendar.ZoneInfo.class
 * <sun.util.calendar.ZoneInfo.class>sun.util.calendar.ZoneInfo.getAliasTable:()Ljava/util/Map;
 * </Thread-1>
 * <Thread-1>
 * com.sun.java.util.jar.pack.UnpackerImpl.unpack:(Ljava/io/FileInputStream;Ljava/util/jar/JarOutputStream;)V=VarientOf=com.sun.java.util.jar.pack.UnpackerImpl.unpack:(Ljava/io/InputStream;Ljava/util/jar/JarOutputStream;)V
 * com.sun.java.util.jar.pack.NativeUnpack.run:(Ljava/io/BufferedInputStream;Ljava/util/jar/JarOutputStream;)V=THROandVarientOf=com.sun.java.util.jar.pack.NativeUnpack.run:(Ljava/io/InputStream;Ljava/util/jar/JarOutputStream;)V
 * com.sun.java.util.jar.pack.NativeUnpack.run:(Ljava/io/BufferedInputStream;Ljava/util/jar/JarOutputStream;Ljava/nio/ByteBuffer;)V=THROandVarientOf=com.sun.java.util.jar.pack.NativeUnpack.run:(Ljava/io/InputStream;Ljava/util/jar/JarOutputStream;Ljava/nio/ByteBuffer;)V
 * com.sun.java.util.jar.pack.NativeUnpack.writeEntry:(Ljava/util/jar/JarOutputStream;Ljava/lang/String;JJZLjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)V
 * java.util.zip.ZipEntry.setTime:(J)V
 * java.util.zip.ZipEntry.javaToDosTime:(J)J
 * java.util.Date.getMonth:()I
 * java.util.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;
 * java.util.Date.normalize:(Lsun/util/calendar/BaseCalendar$Date;)Lsun/util/calendar/BaseCalendar$Date;
 * <java.util.TimeZone.class>java.util.TimeZone.getTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
 * java.util.TimeZone.getTimeZone:(Ljava/lang/String;Z)Ljava/util/TimeZone;
 * sun.util.calendar.ZoneInfo.getTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
 * sun.util.calendar.ZoneInfo.class
 * <sun.util.calendar.ZoneInfo.class>sun.util.calendar.ZoneInfo.getAliasTable:()Ljava/util/Map;
 * </Thread-1>
 * <Thread-2>
 * <sun.util.calendar.ZoneInfo.class>sun.util.calendar.ZoneInfo.getAliasTable:()Ljava/util/Map;
 * sun.util.calendar.ZoneInfoFile.getZoneAliases:()Ljava/util/Map;
 * java.io.PrintStream.println:(Ljava/lang/String;)V
 * java.io.PrintStream
 * <java.io.PrintStream>java.io.PrintStream.println:(Ljava/lang/String;)V#Ljava/io/PrintStream;#0=VarientOf=java.io.PrintStream.println:(Ljava/lang/String;)V
 * </Thread-2>
 * <Thread-3>
 * sun.rmi.server.UnicastServerRef.oldDispatch:(Ljava/rmi/Remote;Ljava/rmi/server/RemoteCall;I)V
 * sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * <java.io.PrintStream>sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V#Ljava/io/PrintStream;#0=VarientOf=sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * java.lang.StringBuilder.append:(Ljava/util/Date;)Ljava/lang/StringBuilder;=THROandVarientOf=java.lang.StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 * java.lang.String.valueOf:(Ljava/util/Date;)Ljava/lang/String;=THROandVarientOf=java.lang.String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
 * java.util.Date.toString:()Ljava/lang/String;
 * java.util.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;
 * java.util.Date.normalize:(Lsun/util/calendar/BaseCalendar$Date;)Lsun/util/calendar/BaseCalendar$Date;
 * java.util.TimeZone.class
 * <java.util.TimeZone.class>java.util.TimeZone.getTimeZone:(Ljava/lang/String;)Ljava/util/TimeZone;
 * </Thread-3>
 * <Thread-3>
 * sun.rmi.server.UnicastServerRef.oldDispatch:(Ljava/rmi/Remote;Ljava/rmi/server/RemoteCall;I)V
 * sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * <java.io.PrintStream>sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V#Ljava/io/PrintStream;#0=VarientOf=sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * java.lang.StringBuilder.append:(Ljava/util/Date;)Ljava/lang/StringBuilder;=THROandVarientOf=java.lang.StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 * java.lang.String.valueOf:(Ljava/util/Date;)Ljava/lang/String;=THROandVarientOf=java.lang.String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
 * java.util.Date.toString:()Ljava/lang/String;
 * java.util.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;
 * java.util.Date.normalize:(Lsun/util/calendar/BaseCalendar$Date;)Lsun/util/calendar/BaseCalendar$Date;
 * java.util.Date.getCalendarSystem:(J)Lsun/util/calendar/BaseCalendar;
 * java.util.TimeZone.getDefaultRef:()Ljava/util/TimeZone;
 * java.util.TimeZone.class
 * <java.util.TimeZone.class>java.util.TimeZone.getDefaultInAppContext:()Ljava/util/TimeZone;
 * </Thread-3>
 * <Thread-3>
 * sun.rmi.server.UnicastServerRef.oldDispatch:(Ljava/rmi/Remote;Ljava/rmi/server/RemoteCall;I)V
 * sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * <java.io.PrintStream>sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V#Ljava/io/PrintStream;#0=VarientOf=sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * java.lang.StringBuilder.append:(Ljava/util/Date;)Ljava/lang/StringBuilder;=THROandVarientOf=java.lang.StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 * java.lang.String.valueOf:(Ljava/util/Date;)Ljava/lang/String;=THROandVarientOf=java.lang.String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
 * java.util.Date.toString:()Ljava/lang/String;
 * java.util.Date.normalize:()Lsun/util/calendar/BaseCalendar$Date;
 * java.util.Date.normalize:(Lsun/util/calendar/BaseCalendar$Date;)Lsun/util/calendar/BaseCalendar$Date;
 * java.util.Date.getCalendarSystem:(J)Lsun/util/calendar/BaseCalendar;
 * java.util.TimeZone.getDefaultRef:()Ljava/util/TimeZone;
 * java.util.TimeZone.class
 * <java.util.TimeZone.class>java.util.TimeZone.setDefaultZone:()Ljava/util/TimeZone;
 * </Thread-3>
 */
public class TzZiPsDeadlock {
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
  new Thread() {
   public void run() {
      sun.util.calendar.ZoneInfo.getAliasTable();
   };
  }.start();
 }
}
