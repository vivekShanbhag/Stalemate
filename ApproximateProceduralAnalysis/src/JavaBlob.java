/* CodeBlob.java -- A Node in the Concurrency-Conflict Graph
 *
 * author: Vivek K. Shanbhag, Philips Research, India, Bangalore.
 */

import java.util.*;
import java.io.*;

public class JavaBlob extends CodeBlob implements Runnable {
    static BufferedReader textIn  = null;
    static String         classesHome = ".";
    static Collection     classesList = null;
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
    
    static CodeBlob
    readMethodHeader (String cName, String line) throws Exception {
        int openP=-1, closeP=-1;
        while (line != null)  {
            if (line.indexOf("}") == 0) return null; // Reached end of Class-definition
            // System.out.println ("head ::== " + line + "::" + line.length());
            openP  = line.indexOf("(");  closeP = line.indexOf(")");
            if ((openP>=0 && closeP>=0 && openP<closeP)  ||  line.indexOf("static {};")>=0)
                break;
            line = textIn.readLine();
        }
        if (line == null)  return null;  // Reached end of Input !! (before end of class ?)
    
        CodeBlob rv = null;
        String methodName, decoration = "", returnType = null;
        if (line.indexOf("static {};") < 0)  {
            int lastBlankB4OpenP = line.substring(0, openP).lastIndexOf(" ");
            // System.out.println ("Cur Line ::== " + line + "::" + line.length());
            methodName = line.substring(lastBlankB4OpenP+1, openP);
            if (lastBlankB4OpenP >= 0)
                decoration = line.substring(0, lastBlankB4OpenP);
    
            // Unfortunately, the 1.3.1 API doesnot include the java.lang.String.split() function.
            String parameters = line.substring(openP+1, closeP).trim();
            int    count      = parameters.length() > 0  ?  1  :  0;
            for (int kk = 0;  parameters.length() > kk;  kk++)
                 if (parameters.charAt(kk) == ',')   count++;
            String[] params = new String[count];
            if (count == 1)
                params[0] = parameters; 
            else if (count > 1) {
                for (int c = 0;  c < count-1;  c++)  {
                    int jj = parameters.indexOf(",");
                    params[c]  = parameters.substring (0, jj);
                    parameters = parameters.substring(jj+1);
                    jj = parameters.indexOf(",");
                }
                params[count-1] = parameters;
            }
            // String[] params   = line.substring(openP+1, closeP).split(",");
            // System.out.println ("Params: "+line+" broken into " + params.length + " components.");
            // for (int j = 0;  j < params.length;  j++)
            //     System.out.println (params[j]);
    
    
            if (cName.replace('/', '.').equals(methodName))
                methodName = "\"<init>\"";  // Encountered Constructor. ==> returnType is null.
            else  {
                // System.out.println ("cName is " + cName + "; mName is " + methodName);
                int lastBlankB4MethodName = decoration.lastIndexOf (" ");
                if (lastBlankB4MethodName < 0)  {
                    returnType = decoration;
                    decoration = "";
                }  else  {
                    returnType = decoration.substring (lastBlankB4MethodName + 1);
                    decoration = decoration.substring (0, lastBlankB4MethodName);
                }
            }
            rv = newCodeBlob (cName, methodName, getPRTcode(params, returnType),
                     decoration.indexOf("public")>=0, decoration.indexOf("static")>=0, 
                     decoration.indexOf("synchronized")>=0, decoration.indexOf("native")>=0);
        }  else  {
            decoration = line.substring (0, line.indexOf("{}"));
            rv = newCodeBlob (cName, getUniqueName(), "()", false, true,
                     decoration.indexOf("synchronized")>=0, false);
        }
        return rv;
    }
    
    static String getPRTcode (String[] params, String rt)  {
        String rv = "(";
        for (int i = 0;  i < params.length;  i++)
            rv += encodeType (params[i].trim());
        return rv + ")" + (rt==null ? "V" : encodeType(rt));
    }
    
    static String encodeType (String type)  {
        String rv = "";
        while (type != null  &&  type.length() != 0  &&  type.endsWith("[]"))  {
            rv += "[";
            type = type.substring (0, type.length()-2);
        }
             if ("byte"   .equals(type))   rv += "B";
        else if ("char"   .equals(type))   rv += "C";
        else if ("double" .equals(type))   rv += "D";
        else if ("float"  .equals(type))   rv += "F";
        else if ("int"    .equals(type))   rv += "I";
        else if ("long"   .equals(type))   rv += "J";
        else if ("short"  .equals(type))   rv += "S";
        else if ("void"   .equals(type))   rv += "V";
        else if ("boolean".equals(type))   rv += "Z";
        else if (type!=null  &&  type.trim().length()>0)  rv += "L" + type.replace ('.', '/') + ";";
    
        return rv;
    }
    static void processMethodBody (CodeBlob n, String cn) throws Exception {
        boolean firstExitSeen = false;  String line;
        while ((line=textIn.readLine()) != null)  {
            //   System.out.println ("body ::== " + line + "::" + line.length());
                 if (line.length()==0)                                     break;
            else if ((line.indexOf("\tinvokevirtual\t")>0) || (line.indexOf("\tinvokespecial\t")>0) ||
                     (line.indexOf("\tinvokestatic\t")>0)) {
                String ms = line.substring (line.indexOf("; //Method ") + "; //Method ".length());
                String mn = ms.substring (0, ms.indexOf(":"));
                if (mn.charAt(0)=='\"'  &&  mn.endsWith("\".clone"))
                    ms = "java/lang/Object.clone:()Ljava/lang/Object;";
                
                n.noteCallTo ((mn.indexOf(".")<0 ? (cn.replace('.', '/') + ".") : "") + ms);
            }  // else if (line.indexOf("\tinvokeinterface\t")>0)
               //  n.noteCallTo (line.substring (line.indexOf("; //InterfaceMethod ") +
               //                                             "; //InterfaceMethod ".length()));
            else if (line.indexOf("\tmonitorenter")>0)
                processMethodBody (n.noteCallTo(null), cn);
            else if (line.indexOf("\tmonitorexit")>0  &&  firstExitSeen)   break;
            else if (line.indexOf("\tmonitorexit")>0  &&  !firstExitSeen)  firstExitSeen = true;
            else if (line.indexOf("Exception table:")>=0)  {
                boolean ClassSeen = false, nearEnd = false;
                while((line=textIn.readLine()) != null)  {
                    if (nearEnd   &&  line.length()==0)    return;
                    nearEnd    = (ClassSeen  &&  line.length()==0)  ||  !ClassSeen;
                    ClassSeen  = line.indexOf("Class")>0;
                }
            }
        }
    }
    public JavaBlob () { super (""); }
    public static void main (String[] args) {
        String className="", methodName="", parentName="", line=null;
        try {
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
            new Thread(new JavaBlob()).start();
            Thread.currentThread().sleep (2000); // milliseconds
            while (textIn != null)  {
              synchronized (textIn)  {
                while ((line=textIn.readLine()) != null)  {
                  if (line.indexOf("class ")>=0  &&  line.indexOf("{")>=0)  {
                    boolean isAbstract = line.indexOf("abstract ") >= 0;
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
                    
                    inheritanceMap.put (className.replace('.', '/'),
                                        parentName == null ? null : parentName.replace('.', '/'));
                    if (isAbstract)
                        abstractClasses.add (className.replace('.', '/'));
                    while ((line=textIn.readLine()) != null)  {
                        if (line.length() == 0)       continue;
                        if (line.indexOf("}") == 0)   break;
                        CodeBlob m = readMethodHeader(className.replace('.', '/'), line);
                        if (m != null)
                            processMethodBody (m, className);
                        else
                            break;
                    }
                  }
                }
              }
              Thread.currentThread().yield();
            }

            defineReferencedInheritedFunctions ();
                  // Moves nodes from Undefined list into the defined list of nodes.

            SynchCodeReachedFromSynchCode (); // Step 2.1
            dropUnsynchronizedCodeBlobs ();   // Step 2.1.3
            // findCycles (null, null, 5);       // Cycle finding & reporting Step.
            report2LockDeadlockingThreads();
            System.out.println ("LHC: " + lhcs.size()  + ":" + lhcs.toString());
            System.out.println ("PHC: " + t12hc.size() + ":" + t12hc.toString());
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }
}