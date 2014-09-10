/*
 * <Cycle-2 locks=" sun.util.calendar.ZoneInfo.class java.io.PrintStream">
 * <Thread-1>
 * <sun.util.calendar.ZoneInfo.class>sun.util.calendar.ZoneInfo.getAliasTable:()Ljava/util/Map;
 * sun.util.calendar.ZoneInfoFile.getZoneAliases:()Ljava/util/Map;
 * java.io.PrintStream.println:(Ljava/lang/String;)V
 * java.io.PrintStream
 * <java.io.PrintStream>java.io.PrintStream.println:(Ljava/lang/String;)V#Ljava/io/PrintStream;#0=VarientOf=java.io.PrintStream.println:(Ljava/lang/String;)V
 * </Thread-1>
 * <Thread-2>
 * sun.rmi.server.UnicastServerRef.oldDispatch:(Ljava/rmi/Remote;Ljava/rmi/server/RemoteCall;I)V
 * sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * <java.io.PrintStream>sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V#Ljava/io/PrintStream;#0=VarientOf=sun.rmi.server.UnicastServerRef.logCallException:(Ljava/lang/Throwable;)V
 * java.lang.StringBuilder.append:(Ljava/util/Date;)Ljava/lang/StringBuilder;=THROandVarientOf=java.lang.StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
 * java.lang.String.valueOf:(Ljava/util/Date;)Ljava/lang/String;=THROandVarientOf=java.lang.String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
 * java.util.Date.toString:()Ljava/lang/String;
 * java.util.TimeZone.getDisplayName:(ZILjava/util/Locale;)Ljava/lang/String;
 * java.util.TimeZone.getDisplayNames:(Ljava/lang/String;Ljava/util/Locale;)[Ljava/lang/String;
 * sun.util.TimeZoneNameUtility.retrieveDisplayNames:(Ljava/lang/String;Ljava/util/Locale;)[Ljava/lang/String;
 * sun.util.TimeZoneNameUtility.retrieveDisplayNames:(Lsun/util/resources/OpenListResourceBundle;Ljava/lang/String;Ljava/util/Locale;)[Ljava/lang/String;
 * sun.util.LocaleServiceProviderPool.getLocalizedObject:(Lsun/util/TimeZoneNameUtility$TimeZoneNameGetter;Ljava/util/Locale;Lsun/util/resources/OpenListResourceBundle;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;=THROandVarientOf=sun.util.LocaleServiceProviderPool.getLocalizedObject:(Lsun/util/LocaleServiceProviderPool$LocalizedObjectGetter;Ljava/util/Locale;Lsun/util/resources/OpenListResourceBundle;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;
 * sun.util.LocaleServiceProviderPool.getLocalizedObjectImpl:(Lsun/util/TimeZoneNameUtility$TimeZoneNameGetter;Ljava/util/Locale;ZLsun/util/resources/OpenListResourceBundle;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;=THROandVarientOf=sun.util.LocaleServiceProviderPool.getLocalizedObjectImpl:(Lsun/util/LocaleServiceProviderPool$LocalizedObjectGetter;Ljava/util/Locale;ZLsun/util/resources/OpenListResourceBundle;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;
 * sun.util.TimeZoneNameUtility$TimeZoneNameGetter.getObject:(Ljava/util/spi/LocaleServiceProvider;Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;=THRO=sun.util.LocaleServiceProviderPool$LocalizedObjectGetter.getObject:(Ljava/lang/Object;Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;=VarientOf=sun.util.TimeZoneNameUtility$TimeZoneNameGetter.getObject:(Ljava/lang/Object;Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;
 * sun.util.TimeZoneNameUtility$TimeZoneNameGetter.getObject:(Ljava/util/spi/TimeZoneNameProvider;Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)[Ljava/lang/String;
 * sun.util.calendar.ZoneInfo.class
 * <sun.util.calendar.ZoneInfo.class>sun.util.calendar.ZoneInfo.getAliasTable:()Ljava/util/Map;
 * </Thread-2>
 * </Cycle-2>
 */

public class ZoneInfoPrintStreamDeadlock {
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
      sun.util.calendar.ZoneInfo.getAliasTable();
   };
  }.start();
 }
}
