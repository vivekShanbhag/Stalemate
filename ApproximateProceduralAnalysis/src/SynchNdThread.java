import java.io.*;
import java.util.*;
public class SynchNdThread implements Runnable {
    static BufferedReader textIn  = null;
    static String         classesHome = ".";
    static Collection     classesList = null;
    static HashMap        inheritanceMap = new HashMap();
    static final String   tP = "java.lang.Thread";
    static int stPconLvlEntities = 0, classes = 0;
    static int cumMethods = 0, cMethods = 0;
    static int cumUses = 0, cUses = 0, mUses = 0;
    static int mEnters = 0, mExits = 0, synchNeEuMs = 0;
    static int synchMs = 0, synchSMs = 0, synchSeSeMs = 0, synchSeMeMs = 0,
               synchMeMs = 0, synchOeMs = 0;
    
    static public Collection
    getClassNamesFromJar (String jarFile)  {
        boolean onlyOne = jarFile != null;
        try {
            if (! onlyOne)  {
                String version = System.getProperty ("java.version"),
                       home    = System.getProperty ("java.home");
                if (version.startsWith ("1.6"))
                    jarFile = home + "/lib/rt.jar "  + home + "/lib/jce.jar " +
                              home + "/lib/jsse.jar " + home + "/lib/charsets.jar";
                              // home + "/lib/ext/sunpkcs11.jar " + home + "/../lib/tools.jar " +
                              // home + "/lib/ext/dnsns.jar;"; // + home + "/lib/ext/localedata.jar ";
                if (version.startsWith ("1.5"))
                    jarFile = home + "/lib/rt.jar "  + home + "/lib/jce.jar " +
                              home + "/lib/jsse.jar " + home + "/lib/charsets.jar " +
                              home + "/lib/ext/sunjce_provider.jar " + home+"/lib/ext/sunpkcs11.jar " +
                              home + "/lib/ext/dnsns.jar " + home + "/lib/ext/localedata.jar ";
                else if (version.startsWith ("1.4"))
                    jarFile = home + "/lib/rt.jar "  + home + "/lib/jce.jar " +
                              home + "/lib/jsse.jar " + home + "/lib/charsets.jar " +
                              home + "/lib/sunrsasign.jar " +
                              home + "/lib/ext/sunjce_provider.jar " + home + "/lib/ext/ldapsec.jar " +
                              home + "/lib/ext/dnsns.jar " + home + "/lib/ext/localedata.jar";
            }
    
            Process child = Runtime.getRuntime().exec (
                (onlyOne  ?  "./grepNDcut "  :  "./grepNDcutInLoop ") + jarFile);
            InputStream grepOut = child.getInputStream();
            InputStreamReader r = new InputStreamReader (grepOut);
            BufferedReader in   = new BufferedReader (r);
    
            TreeSet retval = new TreeSet();
            String line    = null;
    
            while ((line = in.readLine()) != null) {
                line = line.trim().replace ('/','.');
                line = line.substring (0, line.lastIndexOf(".class"));
                if (!retval.add(line))
                    System.err.println ("Dropped here: " + line);
            }
    
            if (child.waitFor() != 0)
                System.out.println ( "grepNDcut Failed: " + child.exitValue());
    
            return retval;
        } catch (Exception e) {
            System.out.println (e.toString());
        }
        return null;
    };
    static final int  NoOfClassesPerJavapInvocation = 20;
    public void run()  {
        String  subList;          InputStream textOut = null;
        Process child    = null;  InputStreamReader r = null;  int done = 0, j;
        try {Iterator i = classesList.iterator();
            do {subList = "";
                for (j = NoOfClassesPerJavapInvocation;  --j>=0 && i.hasNext();  )  {
                    String t = (String) i.next();
                    subList = subList + " " + t;
                    if (breakpoint (t))  break;
                }
                done += j;
                child = Runtime.getRuntime().exec (System.getProperty ("java.home") +
                                                   "/../bin/javap -private -c -J-Xmx98m " +
                            (classesHome!=null ? ("-classpath " + classesHome) : "") + " " + subList);
                textOut = child.getInputStream();
                r = new InputStreamReader (textOut);
                if (textIn==null)              textIn  = new BufferedReader (r);
                else  synchronized (textIn) {  textIn  = new BufferedReader (r);  }
    
                if (child.waitFor() != 0)
                    System.err.println ( "javap (" + subList + ") Failed: " + child.exitValue());
            } while (i.hasNext());
            synchronized (textIn) {  textIn = null;  }
        } catch (Exception e) {
            System.out.println (e.toString());
        }
    }
    
    public static boolean breakpoint (String fqcn)  {
        String version = System.getProperty ("java.version");
        if (fqcn == null) return true;
        if (version.startsWith ("1.6"))
            return  fqcn.startsWith ("com.sun.crypto.provider")  ||
                    fqcn.startsWith ("com.sun.corba.se.spi.transport") ||
                    fqcn.startsWith("sun.net.spi") || fqcn.startsWith("sun.net.www") ||
                    fqcn.startsWith("sun.text.resources");
        else if (version.startsWith ("1.5"))
            return  fqcn.equals("com.sun.crypto.provider.ai") ||
                    fqcn.equals("sun.security.pkcs11.wrapper.PKCS11RuntimeException") ||
                    fqcn.startsWith("sun.net.spi") || fqcn.startsWith("sun.net.www") ||
                    fqcn.startsWith("sun.text.resources");
        else if (version.startsWith ("1.4"))
            return  fqcn.equals("com.sun.crypto.provider.ai") ||
                    fqcn.startsWith("com.sun.jndi.ldap") || fqcn.startsWith("sun.text.resources") ||
                    fqcn.startsWith("sun.net.spi") || fqcn.startsWith("sun.net.www") ||
                    fqcn.equals("com.sun.security.sasl.util.SaslImpl");
         return true;
    }
    
    public static int countThreadInheritors()  {
        int retval = 0;
        for (Iterator i = inheritanceMap.keySet().iterator();  i.hasNext();  )
            for (String cl = (String) i.next();  cl!=null;  cl = (String) inheritanceMap.get(cl))
                if (tP.equals(cl))  {
                    retval++;  break;
                }
        return retval;
    };
    public static void main (String[] args) {
        String fileName="", className="", methodName="", parentName="", line=null;
        char   conLevelAt = '\0';
        try {
            if (args.length > 0  &&  args[0].charAt(0) == '-')  {
                conLevelAt = args[0].charAt(1);
                if (conLevelAt != 'm'  &&  conLevelAt != 'c')  {
                    System.out.println ("Usage: java SynchNdThread -[mc] [class-file-list | jar-file-name]");
                    return;
                }
            }
            if (args.length==0 ||
                (args[args.length-1].indexOf(".class")<0 && args[args.length-1].indexOf(".jar")<0)) {
                // System.out.println ("./grepNDcutInLoop " + System.getProperty ("java.home"));
                classesList = getClassNamesFromJar (null);
                classesHome = null;
            } else if (args[args.length-1].indexOf(".class")<0  &&  args[args.length-1].indexOf(".jar")>=0) {
                classesList = getClassNamesFromJar (args[args.length-1]);
                classesHome = args[args.length-1];
            } else {
                classesList = new TreeSet();
                for (int k = 0; k<args.length;  k++)
                    if (args[k].indexOf(".class")>=0)
                        if (!classesList.add(args[k].substring(0, args[k].indexOf(".class"))))
                            System.err.println ("Dropped: " + args[k]); 
            }
            new Thread(new SynchNdThread()).start();
            Thread.currentThread().sleep (2000); // milliseconds
            while (textIn != null)  {
              synchronized (textIn)  {
                while ((line=textIn.readLine()) != null)  {
                  if (line.indexOf("Compiled from ") >= 0)
                      if (line.indexOf('\"')<0)  //System.out.println ("Problem-case:" + line);
                          fileName = line.substring(line.lastIndexOf(' ')+1);
                      else
                          fileName = line.substring(line.indexOf('\"')+1, line.lastIndexOf('\"'));
                  
                  if (line.indexOf("class ")>=0  &&  line.indexOf("{")>=0)  {
                      if (line.indexOf(" extends ") > 0)  {
                          int tint  = line.indexOf(" extends ");
                          className = line.substring(line.indexOf(" class ")+7, tint);
                          line = line.substring (tint + " extends ".length());
                          int endParent = line.indexOf(" ")>0 ? line.indexOf(" ") : line.indexOf("{");
                          parentName    = line.substring (0, endParent);
                      } else {
                          className = line.substring(line.indexOf(" class ")+7, line.indexOf("{"));
                          parentName= null;
                      }
                      
                      inheritanceMap.put (className,  parentName);
                      
                      while (((line=textIn.readLine()) != null)  &&  line.indexOf("}") != 0)  {
                          if (line.indexOf("(") >= 0)  {
                              int lastBlankB4OpenP = line.substring(0, line.indexOf("(")).lastIndexOf(" ");
                              methodName = line.substring(lastBlankB4OpenP<0 ? 0 : lastBlankB4OpenP, line.indexOf("("));
                              String decoration = line.substring(0, lastBlankB4OpenP<0 ? 0 : lastBlankB4OpenP);
                              if (decoration.indexOf("synchronized") >= 0)
                                  if (decoration.indexOf("static") >=0)  synchSMs++;
                                  else                                   synchMs++;
                          }  else if (line.indexOf("Code:") >= 0)  {
                              while ((line=textIn.readLine()) != null  &&  line.trim().length() != 0)
                                  if ((line.indexOf("invokevirtual")>0 || line.indexOf("invokestatic")>0)  &&
                                       line.indexOf("//Method java/lang/Thread")>0)     mUses++;
                                  else if (line.indexOf("monitorenter")>0)              mEnters++;
                                  else if (line.indexOf("monitorexit")>0)               mExits++;
                              if (mEnters>0 || mExits>0) {
                                       if (mEnters==1 && mExits==1)  synchSeSeMs++;
                                  else if (mEnters==1 && mExits>1)   synchSeMeMs++;
                                  else if (mEnters>1  && mExits>1)   synchMeMs++;
                                  else if (mEnters==0 && mExits>0)   synchNeEuMs++;
                                  else if (mExits%2==1)              synchOeMs++;
                                  mEnters = 0;  mExits = 0;
                              }
                              if (conLevelAt == 'm'  &&  mUses>0)  {
                                  stPconLvlEntities++;
                                  System.out.println (methodName + "." + className + "(" + fileName + "):");
                                  System.out.println ("    " +mUses+ " `Thread.*' calls.");
                              }  else if (conLevelAt == 'c')  {
                                  cUses += mUses;  cMethods++;
                              }
                              cumUses += mUses;  cumMethods++;  mUses = 0;
                          }
                      }
                      
                      if (conLevelAt != 'm'  &&  cUses>0)  {
                          stPconLvlEntities++;
                          if (conLevelAt == 'c') {
                              System.out.println (className + "(" + fileName + "):");
                              System.out.println ("    " +cUses+ " `Thread.*' calls in " +cMethods+ " Methods.");
                          }
                      }
                      cUses = 0;  cMethods = 0;  classes++;
                  }
                }
              }
              Thread.currentThread().yield();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        System.out.println ("Total:");
                                  System.out.print   ("    " + cumUses+ " `Thread.*' calls in ");
             if (conLevelAt=='m') System.out.print   (stPconLvlEntities + "/" +cumMethods+ " Methods in ");
        else if (conLevelAt=='c') System.out.print   (cumMethods+ " Methods, in " +stPconLvlEntities+ "/");
        else                      System.out.print   (cumMethods+ " Methods, in ");
                                  System.out.println (classes + " Classes.");
        System.out.println ("    " + synchMs     + "                 synchronised   Methods,");
        System.out.println ("    " + synchSMs    + "          static synchronised   Methods,");
        System.out.println ("    " + synchSeSeMs + "          simple synchronised() Usages,");
        System.out.println ("    " + synchSeMeMs + "     semi-simple synchronised() Usages,");
        System.out.println ("    " + synchMeMs   + " nested/multiple synchronised() Usages.");
        System.out.println ("    " + synchOeMs   + " odd-exit (hard) synchronised() Usages.");
        System.out.println ("    " + synchNeEuMs + " ill-formed      synchronised() Usages.");
        
        System.out.println ("Count of sub-Classes of " +tP+ ": " +countThreadInheritors());
    }
}