import java.util.*;

import java.util.regex.*;
import java.io.*;
import deadlockPrediction.*;
public class JavaBlob extends deadlockPrediction.CodeBlob {
    public int      locals = -1;
    public int[]    offset = null;
    public String[] code   = null;
    public List     monitorExits = null;
    public int      aReturnCount = 0;
    
    public JavaBlob (String mSignature) {
        super (mSignature);
    };

    public JavaBlob (MethodSignature ms, boolean isStat,
                     boolean isSynch, boolean isPub)  {
        super (ms.signature(), isStat, isSynch, isPub);
    }

    public JavaBlob (JavaBlob p, String c, String lT)  {
        super ((deadlockPrediction.CodeBlob) p, c, lT);
    }
    public JavaBlob (JavaBlob ct, String rt)  {
        super ((deadlockPrediction.CodeBlob) ct, rt);
    }
    
    static BufferedReader textIn = null;
    static volatile java.util.concurrent.SynchronousQueue<BufferedReader> javaPout =
           new java.util.concurrent.SynchronousQueue<BufferedReader>();
    static String         classesHome = null;
    static Set            classesList = null;
    
    static public void classListGivenUsage(String[] args)  {
        System.out.println ("Argument.length = " + args.length);
        if (args.length==0)
            classesList = classNamesFromJar (null);
        else if (args.length==2) {
            assert (args[args.length-2].indexOf(".txt")>=0);
            try {
            BufferedReader in = new BufferedReader (
                                new FileReader (args[args.length-2]));
            classesList = new TreeSet();
            for (String line = in.readLine();  line != null;  line = in.readLine())  {
                //line = line.trim().replace ('/', '.');
                //line = line.substring (0, line.lastIndexOf(".class"));
                if (!classesList.add(line))
                    System.err.println ("Dropped here: " + line);
            }   System.out.println ("ClassList cardinality: " + classesList.size());
            } catch (java.lang.Exception e) {
                System.out.println ("Exception: " + e);
            }
            classesHome = args[args.length-1];
        } else if (args[args.length-1].indexOf(".class")<0  &&
                   args[args.length-1].indexOf(".jar")>=0) {
            classesList = classNamesFromJar (args[args.length-1]);
            classesHome = args[args.length-1];
        } else {
            classesList = new TreeSet();
            for (int k = 0; k<args.length;  k++)
                if (args[k].indexOf(".class")>=0)
                    if (!classesList.add(args[k].substring(0, args[k].indexOf(".class"))))
                        System.err.println ("Dropped: " + args[k]); 
            classesHome = ".";
        }
    }
    static public Set classNamesFromJar (String jar)  {
        boolean onlyOne = jar != null;
        try {
            if (! onlyOne)  {
                String version = System.getProperty ("java.version"),
                       home    = System.getProperty ("java.home");
                jar = home + "/lib/rt.jar "   + home + "/lib/jce.jar " +
                      home + "/lib/jsse.jar " + home + "/lib/charsets.jar";
                if (version.startsWith ("1.5") || version.startsWith ("1.6"))
                    jar += ' ' + home + "/lib/ext/sunjce_provider.jar " +
                                 home + "/lib/ext/dnsns.jar " + 
                                 home + "/lib/ext/sunpkcs11.jar " +
                                 home + "/lib/ext/localedata.jar";
                else if (version.startsWith ("1.4"))
                    jar += ' ' + home + "/lib/sunrsasign.jar " +
                                 home + "/lib/ext/localedata.jar " +
                                 home + "/lib/ext/sunjce_provider.jar " +
                                 home + "/lib/ext/dnsns.jar " +
                                 home + "/lib/ext/ldapsec.jar";
            }

            Process child = Runtime.getRuntime().exec (
                (onlyOne  ?  "./grepNDcut "  :  "./grepNDcutInLoop ") + jar);
            BufferedReader in   = new BufferedReader (
                                  new InputStreamReader (child.getInputStream()));
            TreeSet retval = new TreeSet();
            for (String line = in.readLine();  line != null;  line = in.readLine())  {
                line = line.trim().replace ('/', '.');
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
    static final int  NoOfClassesPerJavapInvocation = 1;
    public static void runJavapInvoker()  {
        Runnable invoker = new Runnable() {
            public void run() {
                String  subList;       InputStream textOut = null;
                Process child = null;  InputStreamReader r = null;
                try {Iterator i = classesList.iterator();
                    do {subList = "";
                        for (int j = NoOfClassesPerJavapInvocation;
                             --j>=0 && i.hasNext();  )  {
                            String t = (String) i.next();
                            subList = subList + " " + t;
                            if (breakpoint (t))  break;
                        }
                        String delete2be = System.getProperty ("java.home") +
                               "/../bin/javap -private -c -verbose -J-Xmx424m " +
                               (classesHome!=null ? ("-classpath "+classesHome) : "")
                               + " " + subList;
                        System.err.println (subList.replace (' ', '\n'));
                        child = Runtime.getRuntime().exec (System.getProperty ("java.home") +
                                    "/../bin/javap -private -c -verbose -J-Xmx424m " +
                            (classesHome!=null ? ("-classpath " + classesHome) : "")
                               + " " + subList);
                        r = new InputStreamReader (child.getInputStream());
                        javaPout.put (new BufferedReader (r));
                        if (child.waitFor() != 0)
                            System.err.println("javap (" + subList + ") Failed: " +
                                               child.exitValue());
                    } while (i.hasNext());
                } catch (Exception e) {
                    System.out.println (e.toString());
                }
            }
        };
        new Thread(invoker).start();
    }
    public static boolean breakpoint (String fqcn)  {
        String version = System.getProperty ("java.version");
        if (fqcn == null) return true;
        if (fqcn.startsWith ("sun.net.spi") ||
            fqcn.equals ("com.sun.crypto.provider") ||
            fqcn.startsWith ("sun.text.resources"))
            return true;
        if (version.startsWith ("1.7"))
            return  fqcn.startsWith ("com.sun.org.apache") ||
                    fqcn.startsWith ("com.sun.xml.internal.ws")  ||
                    fqcn.startsWith ("java.io")  ||  fqcn.startsWith ("javax.swing")  ||
                    fqcn.startsWith ("javax.xml")  ||  fqcn.startsWith ("java.util.regex")  ||
                    fqcn.startsWith ("sun.awt")  ||  fqcn.startsWith ("sun.java2d.cmm.lcms");
        else if (version.startsWith ("1.6"))
            return  fqcn.startsWith ("com.sun.corba.se.spi.transport") ||
                    fqcn.startsWith ("sun.util.resources") ||
                    fqcn.startsWith ("com.sun.crypto.provider.TlsRsaPremasterSecretGenerator") ||
                    fqcn.startsWith ("com.sun.crypto.provider.ai") ||
                    fqcn.startsWith ("sun.security.pkcs11.wrapper.PKCS11RuntimeException");
        else if (version.startsWith ("1.5"))
            return  fqcn.startsWith ("sun.security.pkcs11.wrapper.PKCS11RuntimeException") ||
                    fqcn.startsWith ("sun.net.www");
        else if (version.startsWith ("1.4"))
            return  fqcn.startsWith ("com.sun.jndi.ldap") ||
                    fqcn.startsWith ("sun.net.www") ||
                    fqcn.startsWith ("com.sun.security.sasl.util.SaslImpl");
        return true;
    }
    
    static void readInterface (String line)
        throws java.lang.Exception {
        String iii = "", iNm = line.substring (line.indexOf(" interface ") +
                                               " interface ".length());
        System.out.println ("Starting to read INTERFACE: " + iNm);
        if (iNm.indexOf(' ') > 0)
            iNm  = iNm.substring (0, iNm.indexOf(' '));
        if (iNm.indexOf('<') > 0)
            iNm  = iNm.substring (0, iNm.indexOf('<'));
        int indExtends = line.indexOf(" extends ");
        assert (iNm.indexOf('<') < 0);
        if (indExtends >= 0) {
            String ii = line.substring(indExtends + " extends ".length());
            for (int i = 0, j = 0;  i < ii.length();  i++)  {
                if (ii.charAt(i) == '<')           j++;
                if (j == 0)                        iii += ii.charAt(i);
                if (j>0  &&  ii.charAt(i) == '>')  j--;
            }
        }
        interfaces.put (iNm, (indExtends < 0) ? null : iii.split(","));
        while (! (line=textIn.readLine().trim()).equals  ("{"));
        processInterfaceMethodList(iNm);
        System.err.println (iNm + " -- DONE");
    };
    static void processInterfaceMethodList (String iNm)
        throws java.lang.Exception {
        String line = null;
        while ((line=textIn.readLine()) != null)  {
            line = line.trim();
            if (line.endsWith("}"))     break;
            assert (!line.startsWith("Compiled from "));
            if (line.length() == 0)     continue;
            if (line.startsWith("Exceptions:")) {
                line = textIn.readLine();
                continue;
            }
            int openP  = line.indexOf("("),
                closeP = line.indexOf(")");
            if (line.indexOf('=')<0 && line.indexOf('#')<0 &&
                line.indexOf(':')<0 && openP>=0 && closeP>=0 && openP<closeP) {
                assert (line.substring(closeP+1).trim().startsWith(";")  ||
                        line.substring(closeP+1).trim().startsWith("throws "));
                String mNm = readMethodHeader(iNm, line);
                assert (mNm == null);
            }        
        }
    };    
    
    public static void readClassNameProcessBody (String line)
        throws java.lang.Exception {
        String cNm = readClassName(line);
        processFieldNmNdType (cNm);
        System.out.println ("Starting to read CLASS: " + cNm);
        while ((line=textIn.readLine()) != null)  {
            line = line.trim();
            assert (!line.startsWith ("Code:")  &&
                    !line.toLowerCase().startsWith ("stack:"));
            int openP, closeP;
            if (line.length() == 0)       continue;
            assert (!line.startsWith("Compiled from "));
            if (line.startsWith("}"))     break; // reached end-of-class.
            openP  = line.indexOf("(");
            closeP = line.indexOf(")");
            if (line.indexOf('=')<0 && line.indexOf('#')<0 &&
                line.indexOf(':')<0 && openP>=0 && closeP>=0 && openP<closeP) {
                assert (line.substring(closeP+1).trim().startsWith(";")  ||
                        line.substring(closeP+1).trim().startsWith("throws "));
                String mNm = readMethodHeader(cNm, line);
                if (mNm != null) {
                    ((JavaBlob)definedCode.get(mNm)).processMethodBody();
                    if (line.indexOf("   throws ") > 0) {
                        textIn.mark(256);
                        line = textIn.readLine();
                        if (! line.trim().startsWith("throws "))
                            textIn.reset();
                    }
                }
            }  else if (line.trim().startsWith("static {};"))
                while ((line = textIn.readLine()) != null  &&
                       line.trim().length() > 0  &&
                       !line.trim().startsWith("LineNumberTable:"));
            
        }
        System.err.println (cNm + " -- DONE");
        // System.out.println ("Completed reading CLASS: " + cNm);
    };

    public static String readClassName (String line) {
        assert (line.indexOf("class ")>=0);
        boolean isAbstract = line.indexOf("abstract ") >= 0;
        int     indExtends = line.indexOf(" extends ");
        String  cNm = null, pNm = null;
        if (indExtends >= 0)  {
            cNm  = line.substring (line.indexOf(" class ")+7, indExtends);
            line = line.substring (indExtends + " extends ".length());
            pNm  = line.indexOf(' ')>0 ? line.substring(0, line.indexOf(' ')) : line;
            pNm  = pNm.replace('/', '.').intern();
        } else {
            line = line.substring (line.indexOf(" class ")+7);
            cNm  = line.indexOf(' ')>0 ? line.substring(0, line.indexOf(' ')) : line;
        }
        cNm = cNm.replace('/', '.').intern();
        if (cNm.indexOf('<') >= 0)
            cNm = cNm.substring (0, cNm.indexOf('<'));
        assert (cNm.indexOf('<') < 0);
        ClassInfo tv  = (ClassInfo) classes.get (cNm);
        if (tv == null) {
            classes.put (cNm, (tv = new ClassInfo (cNm, pNm)));
            System.out.println ("added into classes: " + cNm + ":" + pNm);
        } else            tv.parentType = pNm;
        tv.isAbstract = isAbstract;
        int indImplements = line.indexOf(" implements ");
        if (indImplements >= 0) {
            String iii = "", ii = line.substring(indImplements + " implements ".length());
            for (int i = 0, j = 0;  i < ii.length();  i++)  {
                if (ii.charAt(i) == '<')           j++;
                if (j == 0)                        iii += ii.charAt(i);
                if (j>0  &&  ii.charAt(i) == '>')  j--;
            }
            tv.noteInterfaces (iii.split(","));
        }
        return cNm;
    }
    
    static String readMethodHeader (String cName, String line)  
        throws Exception {
        int openP = line.indexOf("("),  closeP = line.indexOf(")");
        String mNm, decoration = "",    rType  = null, prt = null;

        int lastBlankB4OpenP = line.substring(0, openP).lastIndexOf(" ");
        mNm = line.substring(lastBlankB4OpenP+1, openP);
        if (lastBlankB4OpenP >= 0)
            decoration = line.substring(0, lastBlankB4OpenP).trim();
        if (decoration.endsWith(">")) {
            assert (decoration.indexOf ("<") >= 0);
            decoration = decoration.substring (0, decoration.indexOf("<"));
        }

        // String[] pTypes   = line.substring(openP+1, closeP).split(",");
        if (cName.replace('/', '.').equals(mNm))
            mNm = "\"<init>\"";  // Encountered Constructor. ==> rType is null.
        else  {
            int lastBlankB4MethodName = decoration.lastIndexOf (" ");
            if (lastBlankB4MethodName < 0)  {
                rType = decoration;
                decoration = "";
            }  else  {
                rType = decoration.substring (lastBlankB4MethodName + 1);
                decoration = decoration.substring (0, lastBlankB4MethodName);
            }
        }
        // System.out.println ("Decoration: " + decoration);
        MethodSignature test = new MethodSignature (cName, mNm, // prt = getPRTcode (pTypes, rType);
                               line.substring(openP, closeP+1), new TypeSignature (rType, false));
        JavaBlob tv = (JavaBlob) definedCode.get (test.signature());
        boolean isNative   = decoration.indexOf("native")    >= 0;
        boolean isAbstract = decoration.indexOf("abstract")  >= 0;
        if (isNative || isAbstract) {
            nativeOrAbstract (test.signature(), isNative);
            return null;
        } else {
            boolean isPublic   = decoration.indexOf("public")    >= 0;
            boolean isStatic   = decoration.indexOf("static")    >= 0;
            boolean isSynch = decoration.indexOf("synchronized") >= 0;
            if (tv == null)
                new JavaBlob (test, isStatic, isSynch, isPublic);
            else 
                tv.upgradeCB (isStatic, isSynch, isPublic);
            return test.signature();
        }
    }
    
    static void processFieldNmNdType (String cNm)
        throws java.lang.Exception {
        String line = null;
        while ((line=textIn.readLine()) != null)  {
            if ((line = line.trim()).equals("{"))
                break;
            if ((line.startsWith("const #") || line.trim().startsWith("#")) &&
                line.indexOf("= Field")>0) {
                if (! line.endsWith(";"))
                    continue;
                TypeSignature fType = new TypeSignature (line.substring (line.indexOf(':')+1), true);
                line = line.substring (line.indexOf("//  ") + "//  ".length(),
                                       line.indexOf(':')).replace('/', '.');
                // System.out.println ("Read <" + fType.signature() + " / " + fType.name() + " : "+ line+ ">");
                
                /* ClassInfo tv = (ClassInfo) classes.get (fType.name());
                if (tv == null)
                    classes.put (fType.name(), tv = new ClassInfo (fType.name(), null));
                tv.setAsInstanceAttribute (line);
                tv = (ClassInfo) classes.get (cNm);
                if (! TypeSignature.basicEncoded.contains (fType.signature()))  {
                    tv.setInstanceAttribute (line.substring(line.lastIndexOf('.')+1),
                                             fType.signature());
                    // System.out.println ("Added <" + fType.signature() + ", " + line + ">");
                } */
            }
        }
    }
    
    public void processMethodBody() throws java.lang.Exception {
        String version = System.getProperty ("java.version");
        String line = textIn.readLine(), tl = null;
        boolean monitorUsed = false;
        assert (getContainerType().name() != null);
        System.out.println ("Started reading method: " + getNodeName());
        if (line.trim().startsWith("flags:"))
            line = textIn.readLine();
        if (line.trim().startsWith("Synthetic:"))
            line = textIn.readLine();
        assert (line.trim().startsWith("Code:"));
        assert ((line = textIn.readLine()) != null  &&  line.indexOf("ocals=") > 0);
        locals = Integer.parseInt (tl = line.substring (
            line.indexOf("ocals=")+"ocals=".length(), line.toLowerCase().indexOf(", args_")));
        totalLocals += locals;
        int bcOffset, colon = -1;
        List toffset = new ArrayList(),  tcode = new ArrayList();

        while ((line=textIn.readLine()) != null  &&  line.trim().length() > 0)  {
            if (line.trim().startsWith("Exceptions:")  ||// point 2 above.
                line.trim().startsWith("}"))             // Surprise!: reached end-of-class.
                break;
            else if (line.trim().startsWith("Exception table:"))  {  // point 1 above.
                readExceptionNdLineNoTables (toffset, tcode);
                break;
            } else if (line.trim().startsWith("LineNumberTable:")  ||
                       line.trim().startsWith("StackMapTable:")) {
                while ((line = textIn.readLine()) != null  &&
                       line.trim().length() > 0  &&  !line.trim().equals("}"));
                break;
            }
            if ((colon = line.indexOf(':')) < 0) {
                System.out.println ("How-come this is read here: " + line); 
                assert (false);
            }
            bcOffset = Integer.parseInt (line.substring(0, colon).trim());
            if (line.indexOf("tableswitch{")>=0  || line.indexOf("tableswitch   {")>=0  ||
                line.indexOf("lookupswitch{")>=0 || line.indexOf("lookupswitch  {")>=0)
                do {
                    line = line + (tl = textIn.readLine());   // Multi-line instructions
                } while ((version.startsWith("1.6") &&  tl.indexOf("default:")<0)  ||
                         (version.startsWith("1.7") && !tl.trim().equals("}")));
            line = addDefiningContextIfNecessary (line.substring(colon+1).trim());
            toffset.add (bcOffset);
            tcode.add   (line);
            monitorUsed = monitorUsed || line.startsWith("monitore");  // point 6 above
            if (line.startsWith("areturn"))                 aReturnCount++;
                 if (newOp  .matcher(line).lookingAt())         newCount++;
            else if (stores .matcher(line).lookingAt())        ssaLocals++;
            else if (invokes.matcher(line).lookingAt()) invocationsCount++;
            if ((getLockType()==null && !monitorUsed)  &&
                (line.startsWith("invokevirtual\t")   ||    // point 5 above
                 line.startsWith("invokespecial\t")   ||    // -- " --
                 line.startsWith("invokeinterface\t") ||    // -- " --
                 line.startsWith("invokestatic\t"))) {      // -- " --
                String ms = line.substring (line.indexOf("; //") + "; //".length());
                       ms = line.substring (line.indexOf("Method ") + "Method ".length());
                //interpretInvocation (ms, line.startsWith("invokestatic\t"),
                //                         line.startsWith("invokespecial\t"),
                //                         line.startsWith("invokeinterface\t"),
                //                         line.startsWith("invokevirtual\t"), null);
            } else if (line.indexOf("tstatic")==2 &&  //line.indexOf("//Field ")>0) {
                       line.substring(line.indexOf("//")+2).trim().startsWith("Field ")) {
                line = line.substring(line.indexOf("//")+2).trim();
                if (! line.endsWith(";"))
                    continue;
                TypeSignature fType = new TypeSignature (line.substring (line.indexOf(':')+1), true);
                line = line.substring (line.indexOf("Field ") + "Field ".length(),
                                       line.indexOf(':')).replace('/', '.');
                // System.out.println ("Read <" + fType.signature() + " / " + fType.name() + " : "+ line+ ">");
                
                if (line.indexOf('.') <= 0)
                    line = getContainerType().name() + '.' + line;
                /* ClassInfo tv = (ClassInfo) classes.get (fType.name());
                assert (tv != null);
                tv.setAsClassAttribute (line);
                assert ((tv = (ClassInfo) classes.get (getContainerType().name())) != null);
                if (! TypeSignature.basicEncoded.contains (fType.signature())) {
                    tv.setClassAttribute (line.substring(line.lastIndexOf('.')+1),
                                          fType.signature());
                    // System.out.println ("Re-Setting " +
                    //       line.substring(line.lastIndexOf('.')+1) + ":" + fType.signature());
                }*/
            }
        }
        offset = new int [toffset.size()];
        code   = new String [tcode.size()];
        for (int j = toffset.size();  --j >= 0;  ) {
            offset[j] = ((Integer) toffset.get(j)).intValue();
            code[j]   = (String) tcode.get(j);
        }
        toffset.clear();  tcode.clear();
        accuracy = AnalysisLevel.CompileTimeType;
        System.out.println ("Completed reading method: " + getNodeName());
        assert (!monitorUsed || monitorExits.size()>0);
    }

    public String addDefiningContextIfNecessary (String l) {
        String rv = l;
        if (l.startsWith("invokevirtual\t")   ||
            l.startsWith("invokespecial\t")   ||
            l.startsWith("invokeinterface\t") ||
            l.startsWith("invokestatic\t")) {
            String ms = l.substring (l.indexOf("; //") + "; //".length());
                   ms = l.substring (l.indexOf("Method ") + "Method ".length());
            if (! MethodSignature.isFullySpecified (ms))
                rv = l.substring (0, l.indexOf(ms)) + getContainerType().name() + "." + ms;
            //if (! l.equals(rv))
            //    System.out.println ("addDefiningContextIfNecessary: " + l + "/" + rv);
        }
        return rv;
    };
    public void readExceptionNdLineNoTables(List offset, List code)
        throws java.lang.Exception {
        String version = System.getProperty ("java.version");
        assert (textIn.readLine().trim().startsWith("from"));
        int    i[] = new int[50], j[] = new int[50], k[] = new int[50],
               t=-1, n=0, ni=0, nj=0, nk=0; 
        String l = textIn.readLine();
        for (;  l != null  &&  l.trim().length()>0  &&
                !l.trim().startsWith("LineNumberTable:")  &&
                !l.trim().startsWith("StackMapTable:")  &&
                !l.trim().startsWith("Exceptions:");  l = textIn.readLine())  {
            ni = Integer.parseInt ((l = l.trim()).substring (0, t = l.indexOf(' ')));
            l  = l.substring(t).trim(); // read and drop the 'from' int value
            nj = Integer.parseInt (l.substring (0, t = l.indexOf(' ')));
            l  = l.substring(t).trim(); // read and drop the 'to' int value
            nk = Integer.parseInt (l.substring (0, t = l.indexOf(' ')));
            l  = l.substring(t).trim(); // read and drop the 'target' int value
            if (l.startsWith("any")  &&  ni != nk  &&
                ((String)code.get(offset.indexOf(nj)-1)).startsWith("monitore"))  {
                   i[n] = ni;  j[n] = nj;  k[n] = nk;  n++;
                   // System.out.println ("Processed: " +ni+','+nj+','+nk+','+n);
            } else if (l.startsWith("Class") && !version.startsWith("1.7")) {
                //System.out.println ("l reads: " + l);
                l = textIn.readLine();
                //System.out.println ("l reads: " + l);
                assert (l==null  ||  l.trim().length()==0);
            }
        }
        monitorExits = new ArrayList();
        for (int m = 0;  m < n;  m++)
            if (m != (n-1)  &&  k[m] == k[m+1]) {
                if ((offset.indexOf(j[m])+1) != offset.indexOf(i[m+1]))
                    System.out.println ("Doing inaccurate analysis of " + getNodeName());
                i[m+1] = i[m];
            } else if (i[m] != 0  && ((String)code.get(offset.indexOf(i[m])-1)).
                                              startsWith("monitorenter"))
                monitorExits.add (j[m]);
        // System.out.println ("Matching Exits at n(" + n + "): " + monitorExits);
        if (l!=null && l.trim().length()>0 && l.trim().startsWith("StackMapTable:")) {
            while ((l=textIn.readLine()) != null  &&  l.trim().length() > 0);
            System.out.println ("Read after StackMapTable: " + (l = textIn.readLine()));
        }
        if (l!=null && l.trim().length()>0 && l.trim().startsWith("LineNumberTable:"))
            while ((l=textIn.readLine()) != null  &&
                   l.trim().length() > 0  &&  !l.trim().equals("}"));
    }
    public static Set bfcSet = new HashSet();
    public void upgradeToRTTAnalysis (
        deadlockPrediction.CodeBlob spec)  throws java.lang.Exception {
        if (accuracy == AnalysisLevel.RunTimeType)
            return;
        if (spec == null || bfcSet.contains (spec.getNodeName()))  {
            System.out.println ("XXX No Interpretation for null behavioured: " +getNodeName());
            return;
        };
        if (spec != this) {
            isPublic  = spec.isPublic;
            isStatic  = spec.isStatic;
            String lt = spec.getLockType();
            if (lt != null  &&  accuracy == AnalysisLevel.Null)
                setLockType (getContainerType().name() +
                             (lt.endsWith(".class") ? ".class" : ""));
            locals = ((JavaBlob) spec).locals;
            offset = ((JavaBlob) spec).offset;
            code   = ((JavaBlob) spec).code;
            monitorExits = ((JavaBlob) spec).monitorExits;
            assert (! getNodeName().equals (spec.getNodeName()));
            behaviourSpec = spec;
        }
        if (offset == null) {
            System.out.println ("Offset null for: " + getNodeName() +
                              "\n      From Spec: " + spec.getNodeName());
            return;
        }
        System.out.println ("Interpreting: " +getNodeName()+
              (spec==this ? "." : ("\n        From: " + spec.getNodeName())));
        rttAnalysisCount++;
        List execStartOffsets = new LinkedList(); // pc(s) where executions (re)start
        List stackAtExecStart = new LinkedList(); // Stack-state at those PC(s)
        List arrayAtExecStart = new LinkedList(); // Local-state at those PC(s)
        java.lang.Object[] typeAtTOSlist = new java.lang.Object[offset.length];
            // Execution begins at PC-Zero, with an empty stack.
        updateBBlist (0, new Stack(), getTypeArrayFromParams (locals),
                      execStartOffsets, stackAtExecStart, arrayAtExecStart);
        boolean doneOffsets[] = new boolean [offset.length];
        Stack objStack = new Stack();   objStack.push (this);
        StringBuffer eCounter = new StringBuffer("0"); 
        for (int i = 0;  i < execStartOffsets.size();  i++)  {
            int bcOffset       = ((Integer)execStartOffsets.get(i)).intValue();
            Object[] typeArray =  (Object[]) arrayAtExecStart.get(i);
            Stack typeStack =  (Stack) stackAtExecStart.get(i);
            assert (indexOfOffset(bcOffset) != -1);
            for (int j = indexOfOffset(bcOffset);
                 j<offset.length && !doneOffsets[j];  j++)  {
                typeAtTOSlist[j] = typeStack.empty() ? null : typeStack.peek();
                doneOffsets[j] = true;
                // printArrayNdStack (typeArray, typeStack);
                if (! interpret(objStack, j, typeStack, typeArray,
                      execStartOffsets, stackAtExecStart, arrayAtExecStart, eCounter))
                    break;
            }
        }
        accuracy = AnalysisLevel.RunTimeType;
    }

    public void upgradeToCTTAnalysis (
        deadlockPrediction.CodeBlob spec)  throws java.lang.Exception {
        if (     accuracy != AnalysisLevel.Null  ||
            spec.accuracy == AnalysisLevel.Null)
            return;
        System.out.println ("CT-Interpreting: " +getNodeName()+
              (spec==this ? "." : ("\n        From: " + spec.getNodeName())));
        if (spec != this) {
            isPublic  = spec.isPublic;
            isStatic  = spec.isStatic;
            String lt = spec.getLockType();
            if (lt != null)
                setLockType (getContainerType().name() +
                             (lt.endsWith(".class") ? ".class" : ""));
            locals = ((JavaBlob) spec).locals;
            offset = ((JavaBlob) spec).offset;
            assert ((code = ((JavaBlob) spec).code) != null);
            monitorExits = ((JavaBlob) spec).monitorExits;
            assert (! getNodeName().equals (spec.getNodeName()));
            behaviourSpec = spec;
        }
        cttAnalysisCount++;
        for (int i = 0;  i < code.length;  i++)  {
            String line =  code[i];
            if (line.startsWith("invokevirtual\t")   ||    // point 5 above
                line.startsWith("invokespecial\t")   ||    // -- " --
                line.startsWith("invokeinterface\t") ||    // -- " --
                line.startsWith("invokestatic\t")) {       // -- " --
                String ms = line.substring (line.indexOf("; //") + "; //".length());
                       ms = line.substring (line.indexOf("Method ") + "Method ".length());
                interpretInvocation (ms, line.startsWith("invokestatic\t"),
                                         line.startsWith("invokespecial\t"),
                                         line.startsWith("invokeinterface\t"),
                                         line.startsWith("invokevirtual\t"), null);
            } else if (line.indexOf("tstatic")==2 &&  //line.indexOf("//Field ")>0) {
                       line.substring(line.indexOf("//")+2).trim().startsWith("Field ")) {
                line = line.substring(line.indexOf("//")+2).trim();
                if (! line.endsWith(";"))
                    continue;
                TypeSignature fType = new TypeSignature (line.substring (line.indexOf(':')+1), true);
                line = line.substring (line.indexOf("Field ") + "Field ".length(),
                                       line.indexOf(':')).replace('/', '.');
                // System.out.println ("Read <" + fType.signature() + " / " + fType.name() + " : "+ line+ ">");
                
                if (line.indexOf('.') <= 0)
                    line = getContainerType().name() + '.' + line;
                /* ClassInfo tv = (ClassInfo) classes.get (fType.name());
                assert (tv != null);
                tv.setAsClassAttribute (line);
                assert ((tv = (ClassInfo) classes.get (getContainerType().name())) != null);
                if (! TypeSignature.basicEncoded.contains (fType.signature())) {
                    tv.setClassAttribute (line.substring(line.lastIndexOf('.')+1),
                                          fType.signature());
                    // System.out.println ("Re-Setting " +
                    //       line.substring(line.lastIndexOf('.')+1) + ":" + fType.signature());
                }*/
            }
        }
        accuracy = AnalysisLevel.CompileTimeType;
    }
    static Pattern
    u_arith = Pattern.compile ("[ilfd](?:neg)"),           // unary negate
    dtc     = Pattern.compile ("[dfil]2[bcsdfil]"),        // Data Type conversion
    wideinc = Pattern.compile ("(?:wide)|(?:iinc)"),       // wide / iinc
    loads   = Pattern.compile ("[adfil]load(?:_[0123])?"), // Loads
    stores  = Pattern.compile ("[adfil]store(?:_[0123])?"),// Stores
    consts  = Pattern.compile ("[dfil]const_m?[012345]"),  // Push Constants
    push    = Pattern.compile ("[bs]ipush"),               // byte/short Push
    b_arith = Pattern.compile ("[ilfd]((?:add)|(?:sub)|(?:mul)|(?:div)|(?:rem))"),
    bitwise = Pattern.compile ("[il]((?:x?or)|(?:and))"),  // bitwise: and, or, xor
    shifts  = Pattern.compile ("[il]u?sh[lr]"),            // Shift rght/lft unsighed?
    lfdCmp  = Pattern.compile ("[lfd]cmp[lg]?"),           // double-word compares
    cmpares = Pattern.compile ("if(?:_[ai]cmp)?((?:eq)|(?:g[et])|(?:l[et])|(?:ne))"),
    nullCmp = Pattern.compile ("if(?:non)?null"),          // Null compares
    returns = Pattern.compile ("[adfil]?ret(?:urn)?"),     // retrn frm method/subrtn
    aloads  = Pattern.compile ("[abcdsilf]aload"),         // Array Load
    astores = Pattern.compile ("[abcdsilf]astore"),        // Array Store
    gotoJsr = Pattern.compile ("(?:goto)|((?:jsr)(?:_w)?)"),  // goto/jump 2 subrtn
    mTarget = Pattern.compile ("((lookup)|(table))(switch)"), // multi-target switch
    ldConst = Pattern.compile ("ldc2?(?:_w)?"),            // Load constant ...
    stackOp = Pattern.compile ("(?:dup2?(?:_x[12])?)|(?:pop2?)|(?:swap)"),
    invokes = Pattern.compile ("(invoke)" +
                               "((?:special)|(?:static)|(?:virtual)|(?:interface))"),
    fieldOp = Pattern.compile ("((?:get)|(?:put))((?:field)|(?:static))"),
    castOp  = Pattern.compile ("(?:checkcast)|(?:instanceof)"),
    newOp   = Pattern.compile ("(?:multi)?a?new(?:array)?"),  // various 'new's.
    lockOp  = Pattern.compile ("monitor((?:enter)|(?:exit))"),// Monitor operations
    restOp  = Pattern.compile ("a((?:throw)|(?:rraylength)|(?:const_null))"),
    c1Match = Pattern.compile ("[BCFISZ]"),                // c1 matcher
    c2Match = Pattern.compile ("[DJ]");                    // c2 matcher
    static String  c1 = "c1".intern(),  c2 = "c2".intern();

    boolean interpret(Stack oStack, int j, Stack tS, Object[] tA,
        List execStartOffsets, List stackAtExecStart, List arrayAtExecStart,
        StringBuffer eCounter) throws Exception {
        Matcher m;
        String bc = (String) code[j];   // System.out.println (j + ":" + bc);
        if (bc.startsWith("nop") || u_arith.matcher(bc).lookingAt())
            ;
        else if ((m = dtc.matcher(bc)).lookingAt())  {
            char from = bc.charAt(m.start()), to = bc.charAt(m.end()-1);
            if (from == 'd'  ||  from == 'l') {
                assert (tS.pop() == c2);        assert (tS.pop() == c2);
            } else                              assert (tS.pop() == c1);
            if (to == 'd'  ||  to == 'l')     { tS.push(c2);  tS.push(c2); }
            else                                tS.push(c1);
        } else if ((m = loads.matcher(bc)).lookingAt()  ||
                   (m = stores.matcher(bc)).lookingAt() ||  
                   (m = wideinc.matcher(bc)).lookingAt())  {
            if (bc.charAt(m.start()) == 'w')    bc = bc.substring(m.end()).trim();
            if (bc.startsWith ("iinc"))         return true;
            if ((m = loads.matcher(bc)).lookingAt()  ||
                (m = stores.matcher(bc)).lookingAt()) {
                char tb = bc.charAt(m.start()), tc = bc.charAt(m.end()-1);
                int i = (tc!='d' && tc!='e') ?
                        (tc-'0') : Integer.parseInt(bc.substring(m.end()).trim());
                if (bc.charAt(m.start()+1) == 'l')
                    if (tb == 'd'  ||  tb == 'l') {
                        assert (tA[i] == c2);      assert (tA[i+1] == c2);
                        tS.push(c2);               tS.push(c2);
                    } else
                        tS.push (tA[i]);
                else
                    if (tb == 'd'  ||  tb == 'l') {
                        assert(tS.pop() == c2);   assert(tS.pop() == c2);
                        tA[i] = c2;               tA[i+1] =  c2;
                    } else
                        tA[i] = tS.pop();
                if (tb == 'a')
                    assert (tA[i] != c1  && tA[i] != c2);
                //System.out.println ("pushed / popped: tA[" + i + "] = " + tA[i]);
            }
        } else if ((m = consts.matcher(bc)).lookingAt()  ||
                   (m = push.matcher(bc)).lookingAt()) {
            char tb = bc.charAt(m.start());
            if (tb == 'd'  ||  tb == 'l') { tS.push(c2);  tS.push(c2); }
            else                            tS.push(c1);
        } else if ((m = b_arith.matcher(bc)).lookingAt() ||
                   (m = bitwise.matcher(bc)).lookingAt()) {
            char tb = bc.charAt(m.start());
            if (tb == 'd'  ||  tb == 'l') {
                assert(tS.pop() == c2);   assert(tS.pop() == c2);
                assert(tS.pop() == c2);   assert(tS.pop() == c2);
                tS.push(c2);              tS.push(c2);
            } else {
                assert(tS.pop() == c1);   assert(tS.pop() == c1);    tS.push(c1);
            }
        } else if ((m = shifts.matcher(bc)).lookingAt()) {
            assert (tS.pop() == c1);
            if (bc.charAt(m.start()) == 'l') {
                assert(tS.pop() == c2);   assert(tS.peek() == c2);   tS.push(c2);
            } else
                assert(tS.peek() == c1);
        } else if ((m = lfdCmp.matcher(bc)).lookingAt()) {
            char tb = bc.charAt(m.start());
            if (tb == 'd'  ||  tb == 'l') {
                assert(tS.pop() == c2);   assert(tS.pop() == c2);
                assert(tS.pop() == c2);   assert(tS.pop() == c2);
            } else {
                assert(tS.pop() == c1);   assert(tS.pop() == c1);
            }
            tS.push(c1);
        } else if ((m = cmpares.matcher(bc)).lookingAt() ||
                   (m = nullCmp.matcher(bc)).lookingAt()) {
            tS.pop();
            if (bc.charAt(m.start()+2) == '_')
                tS.pop();           //below: ctt: control transfer target
            int ctt = Integer.parseInt (bc.substring (m.end()).trim());
            updateBBlist (ctt, tS, tA, execStartOffsets,
                          stackAtExecStart, arrayAtExecStart);
        } else if ((m = returns.matcher(bc)).lookingAt())  {
            char tb = bc.charAt(m.start());
            if (tb=='d' || tb=='l')  {
                assert(tS.pop() == c2);          assert(tS.pop() == c2);
            } else if (tb=='a')  {
                String rtRet = (String) tS.pop(); // RTTAnalysis records this
                if (rtReturnTypes != null  ||
                    !rtRet.equals (getCTReturnType().signature()))  {
                    if (rtRet.equals("Ljava/lang/Object;")  ||
                        cExtends (getCTReturnType().name(),
                                   new TypeSignature (rtRet, true).name()))
                        ;
                    else {
                        if (rtReturnTypes == null)
                            rtReturnTypes = new TreeSet();
                        else
                            System.out.println ("XYZ: Adding runtime: " + rtRet + " for: " +
                                        getReportableName(null));
                        rtReturnTypes.add (rtRet);
                        System.err.println ("XYZ: Returning " + rtRet +
                                        " instead of " + getCTReturnType().signature()); 
                        System.err.println ("XYZ: Method " + getReportableName(null) +
                                        " returns " + rtRet);
                    }
                }
            } else if (m.group().length() == 7)    assert(tS.pop() == c1);
            else if (m.group().length() == 3) {
                int ctt = Integer.parseInt(bc.substring(m.end()).trim());
                    ctt = ((Integer) tA[ctt]).intValue();
                updateBBlist (ctt, tS, tA, execStartOffsets,
                              stackAtExecStart, arrayAtExecStart);
            }
            return false;
        } else if ((m = aloads.matcher(bc)).lookingAt()) {
            char tb = bc.charAt(m.start());
            assert (tS.pop() == c1);                   // consume index
            String tmp = (String) tS.pop();            // RTTAnalysis processes this
            assert (tmp.startsWith("[") ||             // confirm array-base
                        tmp.equals("Ljava/lang/Object;"));
            if (tb != 'a') {
                if (tb=='d' || tb=='l') {  tS.push(c2);    tS.push(c2);  }
                else                       tS.push(c1);
            } else
                tS.push(tmp.startsWith("[") ? tmp.substring(1) : tmp);
                // consume array-base & re-instate element-type
            //System.out.println ("Popped: " + tmp + "; Pushed: " + tS.peek());
        } else if ((m = astores.matcher(bc)).lookingAt())  {
            char tb = bc.charAt(m.start());
            if (tb == 'a')               tS.pop(); // RTTAnalysis processies this
            else if (tb=='d' || tb=='l') {
                assert(tS.pop() == c2);  assert(tS.pop() == c2);
            } else                       assert(tS.pop() == c1);
            assert(tS.pop() == c1);
            String tmp = (String) tS.pop();
            assert (tmp.startsWith("[") || tmp.equals("Ljava/lang/Object;"));
        } else if ((m = gotoJsr.matcher(bc)).lookingAt())  {
            if (bc.charAt(m.start()) == 'j') {
                assert (j < offset.length-1);
                tS.push(offset[j+1]);
            }
            int ctt = Integer.parseInt (bc.substring (m.end()).trim());
            updateBBlist (ctt, tS, tA, execStartOffsets,
                          stackAtExecStart, arrayAtExecStart);
            return false;
        } else if ((m = mTarget.matcher(bc)).lookingAt()) {
            while (true) {
                int cln = bc.indexOf(':'),  sCln = bc.indexOf(';');
                if (sCln<0) sCln = bc.indexOf(" }");
                if (cln>0 && sCln>0 && cln<sCln) {
                    int ctt = Integer.parseInt (bc.substring (cln+1, sCln).trim());
                    updateBBlist (ctt, tS, tA, execStartOffsets,
                                  stackAtExecStart, arrayAtExecStart);
                    bc = bc.substring(sCln+1);
                } else
                    break;
            }
        }  else if ((m = ldConst.matcher(bc)).lookingAt())
            if (m.group().length() > 3  &&  bc.charAt(3) == '2') {
                tS.push (c2);  tS.push (c2);
            } else {
                //String tmp = bc.substring(m.end());
                String tmp = bc.substring(bc.indexOf("//")+2).trim();
                int cInd = tmp.indexOf ("class "), sInd = tmp.indexOf ("String");
                tS.push(sInd>=0 ? "Ljava/lang/String;" :
                        (cInd<0 ? c1 : "Ljava/lang/Class;"  //));
                         +":L"+tmp.substring(cInd + "class ".length()).trim()+".class;"));
            }
        else if ((m = stackOp.matcher(bc)).lookingAt())  {
            char tb = bc.charAt(m.start());
            Object ts = null, tsn = null, tsnn = null, tsnnn = null;
            if (tb == 'd') {
                if (m.group().length() == 3) {  //dup
                    assert ((ts = tS.peek()) != c2);  tS.push(ts);
                } else if (m.group().length() == 4) {    //dup2
                    if ((ts=tS.pop()) == c2) {
                        assert (tS.peek() == c2);  tS.push(c2);
                        tS.push(c2);               tS.push(c2);
                    } else {
                        assert((tsn=tS.peek()) !=  c2);  tS.push(ts);
                        tS.push(tsn);                    tS.push(ts);
                    }
                } else if (m.group().length()==6 && bc.charAt(m.end()-1)=='1') {
                    assert ((ts=tS.pop()) != c2);        //dup_x1
                    assert ((tsn=tS.pop()) != c2);
                    tS.push(ts);         tS.push(tsn);   tS.push(ts);
                } else if (m.group().length()==6 && bc.charAt(m.end()-1)=='2') {
                    assert ((ts=tS.pop()) != c2);        //dup_x2
                    if ((tsn=tS.pop()) == c2) {
                        assert (tS.pop() == c2);         tS.push(ts);
                        tS.push(c2);     tS.push(c2);    tS.push(ts);
                    } else {
                        assert ((tsnn=tS.pop()) != c2);  tS.push(ts); 
                        tS.push(tsnn);   tS.push(tsn);   tS.push(ts);
                    }
                } else if (m.group().length()==7 && bc.charAt(m.end()-1)=='1') {
                    ts = tS.pop();       tsn = tS.pop(); //dup2_x1
                    assert((tsnn=tS.pop()) != c2); 
                    tS.push(tsn);        tS.push(ts);    tS.push(tsnn);
                    tS.push(tsn);        tS.push(ts);
                } else if (m.group().length()==7 && bc.charAt(m.end()-1)=='2') {
                    ts = tS.pop();       tsn = tS.pop(); //dup2_x2
                    tsnn = tS.pop();     tsnnn = tS.pop();
                    assert ((ts!=c2 && tsn!=c2)  ||  (ts==c2 && tsn==c2));
                    assert ((tsnn!=c2 && tsnnn!=c2)  ||  (tsnn==c2 && tsnnn==c2));
                    tS.push(tsn);        tS.push(ts);    tS.push(tsnnn);
                    tS.push(tsnn);       tS.push(tsn);   tS.push(ts);
                }
            } else if (tb == 'p') {            // pop or pop2
                if (m.group().length() == 3)      assert(tS.pop() != c2);
                else if ((ts=tS.pop()) != c2)     assert(tS.pop() != c2);
                else                              assert(tS.pop() == c2);
            } else {                           // The instruction would be "swap".
                assert ((ts = tS.pop()) != c2);   assert ((tsn = tS.pop()) != c2);
                tS.push(tsn);                     tS.push(ts);
            }
        } else if ((m = invokes.matcher(bc)).lookingAt()) {
            boolean isInterface = m.group().endsWith ("interface");
            String sstr = isInterface ? "InterfaceMethod " : "Method ";
            String ms = bc.substring(bc.indexOf("//") + 2).trim();
                   ms = ms.substring(ms.indexOf(sstr) + sstr.length());
                   ms = ms.substring (0, ms.indexOf(':')).replace ('/', '.') +
                        ms.substring (ms.indexOf(':'));
            JavaBlob tos = (JavaBlob)oStack.peek();
            deadlockPrediction.CodeBlob tB  = // Target Behaviour invoked below !!
            tos.interpretInvocation (ms, m.group().endsWith ("static"),
                                         m.group().endsWith ("special"), isInterface, 
                                         m.group().endsWith ("virtual"), tS);
            if (tB != null  &&  tB.rtReturnTypes != null  &&
               !tB.getCTReturnType().signature().startsWith("V"))  {
                String ctrt = (String) tS.pop();
                for (Iterator ii = tB.rtReturnTypes.iterator();  ii.hasNext();  ) {
                    tS.push (ii.next());
                    updateBBlist (offset[j+1], tS, tA, execStartOffsets,
                              stackAtExecStart, arrayAtExecStart);
                    tS.pop();
                }
                tS.push (ctrt);
                return false;
            }
        } else if ((m = fieldOp.matcher(bc)).lookingAt())  {
            Object ts = null, tss = null;
            ClassInfo objT = null;
            String attT = null, attN = bc.substring(bc.lastIndexOf(' ')+1, bc.indexOf(':'));
            String  ctT = bc.substring(bc.indexOf(':')+1).trim(), cn;
            if (attN.indexOf('.') >= 0)
                attN = attN.substring (attN.lastIndexOf('.')+1);
            if (bc.charAt(m.start()) == 'g') {
                if (bc.charAt(m.start()+3) == 'f') {
                    // System.out.println ("getfield container fetched: " + ((String) tS.peek()));
                    assert ((ts = tS.pop()) != c1  &&  ts != c2);
                    assert (((String)ts).startsWith("L"));
                    cn = ((String)ts).substring (1, ((String)ts).length()-1).replace('/', '.');
                } else
                    cn = getContainerType().name();
                if (ensureField (cn, attN, bc.charAt(m.start()+3) == 's', ctT))  {
                    assert ((objT = (ClassInfo) classes.get (cn)) != null);
                    attT = (String) ((bc.charAt(m.start()+3) == 'f')  ?
                          objT.instanceAttributes.get(attN) : objT.classAttributes.get(attN));
                }
                tS.push (abstractTenc (moreConfiningOf (attT, ctT)));
                // System.out.println ("getField/static Fetched: " + tS.peek());
                if (tS.peek().equals(c2))   tS.push(c2);
            } else {
                if ((ts=tS.pop()) == c1)
                    assert (c1Match.matcher(ctT).lookingAt());
                if (ts==c2) {            assert (tS.pop()==c2);
                    assert (c2Match.matcher(ctT).lookingAt());
                }
                if (bc.charAt(m.start()+3) == 'f')
                    assert ((tss = tS.pop())!=c1   &&   tss!=c2);
                // if (!((String)ts).endsWith(c1)  &&  !((String)ts).endsWith(c2)) {
                if (ts != c1  &&  ts != c2) {
                    cn = (bc.charAt(m.start()+3) == 's') ? getContainerType().name()
                       : ((String)tss).substring (1, ((String)tss).length()-1).replace ('/', '.');
                    if (ensureField (cn, attN, bc.charAt(m.start()+3) == 's', ctT))  {
                        assert ((objT = (ClassInfo) classes.get (cn)) != null);
                        if (bc.charAt(m.start()+3) == 'f')
                            objT.instanceAttributes.put (attN, ts);
                        else
                            objT.classAttributes.put (attN, ts);
                    }
                }
            }
        } else if ((m = castOp.matcher(bc)).lookingAt())  {
            Object ts = null;
            assert ((ts=tS.pop()) != c1  &&  ts != c2);
            if (bc.charAt(m.start()) == 'i')
                tS.push (c1);
            else {
                String t = bc.substring(bc.indexOf("//") + 2).trim();
                       t =  t.substring( t.indexOf("class ") + "class ".length()).trim();
                tS.push (t.charAt(0) == '"' ? t.substring(1, t.length()-1) : ("L" + t + ";"));
            }
        } else if ((m = newOp.matcher(bc)).lookingAt())  {
            String typePreFix = "", tmp = null;
            if (!m.group().equals("new")  &&  !m.group().startsWith("multi"))  {
                assert (tS.pop() == c1);
                typePreFix = "[";
            } else if (m.group().equals("multianewarray")) {
                int i = bc.indexOf('#');
                while (bc.charAt(i++) != ' ');
                for (i = Integer.parseInt (bc.substring(i, bc.indexOf(';')).trim()); 
                     --i >= 0  &&  tS.peek()==c1;  tS.pop())
                    typePreFix += "[";
            }
            if (m.group().equals("newarray"))
                tmp = (new TypeSignature(bc.substring(m.end()).trim(), false)).signature();
            else {
                tmp = bc.substring(bc.indexOf("//") + 2).trim();
                tmp = tmp.substring(tmp.indexOf("class ") + "class ".length()).trim();
            }
            //String tmp = (m.group().equals("newarray"))
            //    ? (new TypeSignature(bc.substring(m.end()).trim(), false)).signature()
            //    : bc.substring(bc.indexOf("//class ") + "//class ".length()).trim();
            tS.push (typePreFix + (m.group().equals("newarray") ? abstractTenc (tmp)
                : (tmp.charAt(0)=='"' ? tmp.substring(1, tmp.length()-1) : ('L' + tmp +';'))));
        } else if ((m = lockOp.matcher(bc)).lookingAt())  {
            JavaBlob po = (JavaBlob) oStack.peek();
            if (m.group().endsWith("enter")) {
                String lock = (String)tS.pop();
                if (lock.indexOf(":") > 0)
                    lock = lock.substring (lock.indexOf(":")+1);
                assert (lock.startsWith("L") || lock.startsWith("["));
                String key = (po.getNodeName() +'#'+ lock +'#'+ eCounter.charAt(0)).intern();
                oStack.push (new JavaBlob(po, key, (lock.endsWith(c1) || lock.endsWith(c2)) ?
                                                   lock : new TypeSignature (lock, true).name()));
                eCounter.setCharAt(0, (char)(eCounter.charAt(0) + 1));
            } else {
                //System.out.println ("monitorExits: " + monitorExits +
                //    ";\nCompare: " + (String)tS.peek() + ":" + po.getLockType() +
                //    ";\nnextOffset: " + offset[j+1]);
                if (monitorExits.contains(offset[j+1])) {
                    String lock = (String) tS.peek();
                    if (lock.indexOf(":") > 0)
                        lock = lock.substring (lock.indexOf(":")+1);
                    assert (lock.startsWith("L") || lock.startsWith("["));
                    assert (po.getLockType().equals (new TypeSignature (lock, true).name()) ||
                            lock.equals (po.getLockType()) ||
                            lock.equals (new TypeSignature (po.getLockType(), false).signature()));
                    oStack.pop();
                }
                tS.pop();
            }
        } else if ((m = restOp.matcher(bc)).lookingAt()) {
            if (! m.group().equals("aconst_null")) {
                Object ts = null;
                assert ((ts=tS.pop()) != c1  &&  ts != c2);
                if (m.group().equals("arraylength"))
                    tS.push (c1);
                else  {
                    for (int i = tS.size();  --i >=0 ;  tS.pop());
                    tS.push (ts);
                    return false;
                } 
            } else
                tS.push ("Ljava/lang/Object;");
        } else
            System.out.println ("Unprocessed instruction!!: " + bc);
        return true;
    }
    public Object[] getTypeArrayFromParams (int locals)  {
        // System.out.println ("Locals here: " + locals + "isStatic: " + isStatic);
        java.lang.Object[] retval = new java.lang.Object[locals];
        TypeSignature[] pList = getParams();
        if (((pList==null ? 0 : pList.length) + (isStatic ? 0 : 1)) > locals)
            System.out.println ("Insufficient locals specified: " + getNodeName());
        if (! isStatic)
            retval[0] = getContainerType().signature();
        for (int k = 0, j = isStatic ? 0 : 1;  k < getParamCount();  k++, j++) {
            retval[j] = abstractTenc (pList[k].signature());
            if (retval[j] == c2) retval[++j] = c2;
        }
        // System.out.println ("initialisation fetches:");
        // for (int kk = 0;  kk<locals && retval[kk]!=null; kk++)
        //     System.out.println (retval[kk].toString());
        return retval;
    }

    public static void printArrayNdStack (Object[] a, Stack s) {
        for (int i = 0;  i < a.length;  i++)
            System.out.println ("a["+i+"] = " + (a[i]!=null ? a[i].toString() : "null"));
        Stack ss = new Stack();
        while (!s.empty()) {
            System.out.println ("s = " + (s.peek()!= null ? s.peek() : null));
            ss.push (s.pop());
        }
        while (!ss.empty())
            s.push (ss.pop());
    }

    public static String abstractTenc (String type)  {
        if (type == null  ||  type.equals(c1)  ||  type.equals(c2)  ||
            (type.startsWith("L") && type.endsWith(";")))
            return type;
        else if (c1Match.matcher(type.substring(0, 1)).lookingAt())
            return c1;
        else if (c2Match.matcher(type.substring(0, 1)).lookingAt())
            return c2;
        else if (type.charAt(0) == '[')
            return "[" + abstractTenc (type.substring(1));
        else {
            System.out.println ("Bad input: " + type + " ?");
            assert (false);
        }
        return null;
    }
    public static String moreConfiningOf (String rt, String ct) {
        String retval;
        if (rt == null  ||  (rt.length()==2  &&  ct.length()==1))
            retval = ct;
        else if (rt.startsWith("[")  ||  ct.startsWith("[")) {
            int rti = rt.lastIndexOf("["), cti = ct.lastIndexOf("[");
            retval = (rti > cti  ?  rt 
                                 : (rti < cti  ?  ct  
                                               :  (rt.substring (0, rti+1) +
                moreConfiningOf (rt.substring(rti+1), ct.substring(cti+1)))));
        } else {
            //int rti = rt.length(), cti = ct.length();
            //retval = (rti >= cti) ?  rt  :  ct));
            retval = betterDefined (new TypeSignature(rt, true),
                                    new TypeSignature(ct, true)).signature();
        }
        // System.out.println ("rt: " + rt + "; ct: " + ct + "; retval: " + retval);
        return retval;
    };

    public static boolean cExtends (String c1, String c2) {
        for (ClassInfo ci = null;  c1 != null;
             c1 = ((ci = (ClassInfo)classes.get(c1)) == null) ? null : ci.parentType)
             if (c1.equals(c2))
                 return true;
             // else
             //     System.out.println (c1 + " != " + c2);
        return false;
    };

    public static boolean iExtends (String i1, String i2) {
        Stack il = new Stack(); // list of interfaces represented/extended by i1
        String[] ie = null;     // temp: set of interfaces extended by an interface
        for (il.push (i1);  !il.empty();  )  {
            String iN = (String) il.pop(); // interface Name
            if ((ie = (String[]) interfaces.get(iN)) != null)
                for (int j = 0;  j < ie.length;  j++)
                    il.push (ie[j]);
            //System.out.println (iN + " ?= " + i2);
            if (iN.equals(i2))
                return true;
        }
        return false;
    };

    public static String rmSqBracs (String cn) {
        int ocsb = -1;
        System.out.println ("XXX Removing Square brace-sets: " + cn);
        while ((ocsb = cn.indexOf("[]")) >= 0)
            cn = cn.substring(0, ocsb) + cn.substring(ocsb+2);
        return cn;
    };
    public static boolean cImplementsI (String c, String i) {
        Stack iS = new Stack ();
        for (ClassInfo ci = (ClassInfo) classes.get(c);  c != null;
             c = ((ci = (ClassInfo)classes.get(c)) == null) ? null : ci.parentType)
             if (ci.interfacesImplemented != null)
                 for (int j = 0;  j < ci.interfacesImplemented.length;  j++)
                     iS.push (ci.interfacesImplemented[j]);
        for (String[] ie = null;  !iS.isEmpty();  )  {
            String ii = (String) iS.pop();
            if (ii.equals(i))    return true;
            if ((ie = (String[]) interfaces.get(ii)) != null)
                for (int j = 0;  j < ie.length;  j++)
                    iS.push (ie[j]);
        }
        while (!iS.isEmpty())
            if (i.equals((String)iS.pop()))
                return true;
            // else System.out.println (i + " != " + i2);
        return false;
    };

    public static TypeSignature betterDefined (TypeSignature t1, TypeSignature t2)  {
        assert (t1 != null  &&  t2 != null);
        String cn1 = t1.name(), cn2 = t2.name();
        if (cn1.indexOf("[]") >= 0)  cn1 = rmSqBracs (cn1);
        if (cn2.indexOf("[]") >= 0)  cn2 = rmSqBracs (cn2);
        Collection cL = classes.keySet(), iL = interfaces.keySet();
        //System.out.println ("Comparing Types: " + cn1 + "::" + cn2);
        if (cn1.startsWith ("java.lang.Class;")  ||
            cn2.startsWith ("java.lang.Class;"))
            return new TypeSignature ("Ljava/lang/Class;", true);
        else if (cL.contains (cn1)  &&  cL.contains (cn2)) {
            //System.out.println ("betterDefined:case 1!!");
                 if (cExtends (cn1, cn2))  return t1;
            else if (cExtends (cn2, cn1))  return t2;
            else return t2; // works well with moreConfiningOf!!
        } else if (iL.contains(cn1)  &&  iL.contains(cn2)) {
            // System.out.println ("bD2: "+ cn1 + "---" + cn2);
                 if (iExtends (cn1, cn2))  return t1;
            else if (iExtends (cn2, cn1))  return t2;
            else                           return t2;
        } else if ((cL.contains (cn1) && iL.contains (cn2))  ||
                   (iL.contains (cn1) && cL.contains (cn2)))  {
            //System.out.println ("betterDefined:case 3!!");
            String cN = cL.contains(cn1) ? cn1 : cn2;
            String iN = iL.contains(cn1) ? cn1 : cn2;
            if (cImplementsI (cN, iN))
                 return cN.equals(cn1) ? t1 : t2;
            else if (cN.equals ("java.lang.Object"))
                 return iN.equals(cn1) ? t1 : t2;
            else return t2; // assert (false);
        } else if (cn1.equals ("c1")  ||  cn1.equals ("c2"))
            return t2;
        return t2;  // t1;
    }
    public static boolean isArelation (TypeSignature tOfo, String t)  {
        assert (tOfo != null  &&  t != null);
        String cn = tOfo.name();
        Collection cL = classes.keySet(), iL = interfaces.keySet();
             if (cn.equals (t))
            return true;
        else if (cL.contains(cn) && cL.contains(t) && cExtends (cn, t))
            return true;
        else if (iL.contains(cn) && iL.contains(t) && iExtends (cn, t))
            return true;
        else if (cL.contains(cn) && iL.contains(t) && cImplementsI (cn, t))
            return true;
        else if (iL.contains(cn) && cL.contains(t) && cImplementsI (t, cn))
            return true;
        if (t.equals("java.lang.Object"))  return true;
        //System.out.println (cn + " is not of type " + t);
        return false;
    }

    public deadlockPrediction.CodeBlob
           interpretInvocation (String tMethod, boolean isStatic, boolean isSpecial,
                                boolean isInterface, boolean isVirtual, Stack typeStack)  {
        if (tMethod.startsWith("\"")  &&  tMethod.indexOf("\".clone:")>0) {
            if (typeStack != null) {
                String tos = (String) typeStack.peek();
                assert (tos != c1  &&  tos != c2);
                typeStack.push ("Ljava/lang/Object;");
            }
            return null;
        }
        TypeSignature definingClass = (behaviourSpec == null) ?
                 getContainerType() : behaviourSpec.getContainerType();
        assert (MethodSignature.isFullySpecified (tMethod));
        MethodSignature tM = new MethodSignature (tMethod);
        String rt  = tM.retType().signature();
        String cms = tM.signature();
        JavaBlob rb = null;
        if (typeStack == null) { // conducting CompileTimeType-Level interpretation
            if (! notDefinedCode.contains (cms)) {
                if (isInterface || tobeDefinedCode.get(cms)!=null)
                    noteCallTo (cms);
                else {
                    JavaBlob cb = (JavaBlob) definedCode.get (cms);
                    if (cb == null)
                        cb = new JavaBlob (cms);
                    if (cb != null)
                        noteCallTo (cb);
                }
            }
        }  else  { // conducting RunTimeType-Level interpretation
            String rms = "", ccsNm = null, rcms = null;
            for (int i = tM.paramCount();  --i >= 0;  )  { 
                String pt = tM.paramList()[i].signature();
                if (c1Match.matcher(pt).lookingAt()  ||  pt.startsWith ("[")) {
                    typeStack.pop();                            rms = pt + rms;
                } else if (c2Match.matcher(pt).lookingAt()) {
                    typeStack.pop();      typeStack.pop();      rms = pt + rms;
                } else if (pt.equals("Ljava/lang/Class;")) {
                    TypeSignature actual = new TypeSignature ((String) typeStack.pop(), true);
                    if (! actual.name().startsWith("java.lang.Class")  &&
                        ! actual.name().equals("java.lang.Object"))
                        System.out.println ("InterpretInvocation: " + actual.name() + "#-#" + tM.paramList()[i].name() +
                                            " When " + getNodeName() + " Invoking " + tM.signature());
                    rms  = "Ljava/lang/Class;" + rms;
                } else if (pt.startsWith("L")  && pt.endsWith(";")) {
                    TypeSignature actual = new TypeSignature ((String) typeStack.pop(), true);
                    if (!isArelation (actual, tM.paramList()[i].name())  &&
                        !isArelation (tM.paramList()[i], actual.name()))
                        System.out.println ("InterpretInvocation: " + actual.name() + "/" + tM.paramList()[i].name() +
                                            " When " + getNodeName() + " Invoking " + tM.signature());
                    rms  = (pt.endsWith(";") ?
                           betterDefined (actual, tM.paramList()[i]).signature() : pt) + rms;
                } else
                    assert (false);
            }
            rms = "." + tM.methodName() + ":(" + rms + ")" + rt;
            if (!isStatic)  {
                TypeSignature tOfo = new TypeSignature ((String)typeStack.peek(), true);
                if (tOfo.signature().startsWith("Ljava/lang/Class;")) { 
                    if (!tM.className().name().equals("java.lang.Class")  &&
                        !tM.className().name().equals("java.lang.Object"))
                        System.out.println ("InterpretInvocation1: " + tOfo.name() +"#-#"+
                                             tM.className().name() + " When " + getNodeName() +
                                            " Invoking " + tM.signature());
                } else if (!isArelation (tOfo, tM.className().name())  &&
                    !isArelation (tM.className(), tOfo.name()))
                    System.out.println ("InterpretInvocation2: " + tOfo.name() +"#-#"+
                                         tM.className().name() + " When " + getNodeName() +
                                        " Invoking " + tM.signature());
            }
            rms = (isStatic ? tM.className().name()
                   : (((ccsNm = (String) typeStack.pop()).endsWith(c1) || ccsNm.endsWith(c2))
                      ? ccsNm : betterDefined (new TypeSignature (ccsNm, true),
                                               tM.className()).name())) + rms;
            if (isVirtual || isInterface) {
                rcms  = rms.substring  (0, rms.indexOf(":"));
                rcms  = rcms.substring (0, rcms.lastIndexOf(".")) + ".";
                rcms += (tM.methodName() + ":" + tM.prtEncoding());
            }
            if (!(notDefinedCode.contains(cms) && (isSpecial || isStatic)))  {
                if ( rms.indexOf("[]") >= 0)     rms = rmSqBracs ( rms);
                if (isSpecial || isStatic) {
                    JavaBlob cb = (JavaBlob) definedCode.get (cms);
                    if (cb != null) {
                        rb = (JavaBlob) getCodeOrVarientBlob(cb.getNodeName(), rms);
                        if (rb == null)
                            rb = new JavaBlob (cb, rms);
                        noteCallTo (rb);
                    } else
                        System.out.println ("XXX Cant find non-native static/special cms: " + cms);
                } else {
                    if (rcms.indexOf("[]") >= 0)    rcms = rmSqBracs (rcms);
                    JavaBlob cb = (JavaBlob) definedCode.get (rcms);
                    if (isInterface  &&  tobeDefinedCode.get(cms) == null)
                        System.out.println ("XXX How come " + cms + " is not in tobeDefinedCode");
                    if (cb == null)
                        cb = (JavaBlob) getBspecForIVcall (new MethodSignature (rcms));
                    if (cb != null) {
                        rb = (JavaBlob) getCodeOrVarientBlob  (cb.getNodeName(), rms);
                        if (rb == null)
                            rb = new JavaBlob (cb, rms);
                        if (isInterface)  noteCallTo (cms, rb);
                        else              noteCallTo (cb,  rb);
                    }
                }
            }
            if (rt.charAt(0) != 'V') {
                typeStack.push (abstractTenc (rt));
                if (((String)typeStack.peek()).equals(c2))    typeStack.push(c2);
            }
        }
        return rb;
    }

    public void  updateBBlist (int ctt, Stack tS, Object[] tA,
           List execStartOffsets, List stackAtExecStart, List arrayAtExecStart)  {
        if (indexOfOffset(ctt) != -1) {
            int i = 0;
            while (i < execStartOffsets.size()) {
                 int temp = ((Integer)execStartOffsets.get(i)).intValue();
                      if (ctt >  temp)    i++;
                 else if (ctt == temp)    return;
                 else                     break;
            }
            execStartOffsets.add(i, ctt);
            stackAtExecStart.add(i, tS.clone());
            arrayAtExecStart.add(i, tA.clone());
        }
    }

    private int indexOfOffset (int ctt) {
        int start = 0, end = offset.length - 1, midPt;
        while (start <= end) {
            midPt = (start + end) / 2;
            if (offset[midPt] == ctt) {
                return midPt;
            } else if (offset[midPt] < ctt) {
                start = midPt + 1;
            } else {
                end = midPt - 1;
            }
        }
        return -1;
    };
    
    public static void
    readInputNdCreateCallGraph (String[] args) throws java.lang.Exception {



        if ((textIn = new BufferedReader (new FileReader ("inputFromFile/1.6.classContents.txt"))) != null) {
          String line;
          while ((line=textIn.readLine()) != null)
            if (line.indexOf('#')>=0 || line.indexOf('=')>=0)
                continue;
            else if (line.trim().startsWith ("Classfile jar:")) { //1.7.XXX Specific Requirement!!
              assert (textIn.readLine().trim().startsWith("Last modified"));
              assert (textIn.readLine().trim().startsWith("MD5 checksum"));
            } else if (line.indexOf("interface ")>=0)
              readInterface (line);
            else if (line.indexOf("class ")>=0)
              readClassNameProcessBody  (line);
            else if (line.trim().length() > 0  &&  !line.trim().startsWith("Compiled from ")) {
              System.err.println ("WHAT IS THIS: " + line);
              assert (false);
            }
        }
    }
    
    
    public static void main (String[] args) {
        try {
            readInputNdCreateCallGraph (args);            // Step 1
            exhaustedReadingInputJars = true;
            inheritFromDefinedCode ();                    // Step 2
            reportSubclassingNdImplements ();             // for the popl paper !!
            //if (true) return;          // No analysis necessary !!
            System.out.println ("sCode cardinality after exhausting input: " + sCode.size());
            for (Iterator i = definedCode.values().iterator();  i.hasNext();  )  {
                 JavaBlob cb = (JavaBlob) i.next();
                 if (cb != null  &&  cb.accuracy != AnalysisLevel.RunTimeType  &&
                     cb.aReturnCount > 0)
                     cb.upgradeToRTTAnalysis();
            }
            for (int fpCount = 1; --fpCount > 0;  )
               for (Iterator i = definedCode.values().iterator();  i.hasNext();  )  {
                   JavaBlob cb = (JavaBlob) i.next();
                   if (cb != null  &&  cb.accuracy != AnalysisLevel.RunTimeType  &&
                       cb.monitorExits != null)
                       cb.upgradeToRTTAnalysis();
               }
            System.out.println ("sCode cardinality after interpreting non-null monitorexits: " + sCode.size());
            // interpretSynchNdVarients ();                   // Step 4
            // interpretInterfaceVarients ();                 // Step 4
              countStartPoints ();                            // Step 3
            // collectStartPoints ();                         // Step 3
            System.err.println (
                    "\n        Method invocation count:\t" +        invocationsCount +
                    "\n                      New count:\t" +                newCount +
                    "\n             Total locals count:\t" +             totalLocals +
                    "\n               SSA locals count:\t" +               ssaLocals +
                    "\nDefined or invoked method count:\t" +      definedCode.size() +
                    "\n    Invocation graph Edge count:\t" +        invocationsCount +
                    "\n Defined          Varient count:\t" +         varients.size() +
                    "\n            Native method count:\t" +   notDefinedCode.size() +
                    "\n  Abstract or I/f  method count:\t" +  tobeDefinedCode.size() +
                    "\n  Abstract or I/f Varient count:\t" + abstrctIntrfceVarient() +
                    "\n                    Class count:\t" + classes.keySet().size() +
                    "\n               Interfaces count:\t" +       interfaces.size() +
                    "\n Synch code-blob & method count:\t" +            sCode.size() +
                    "\n Synch static      method count:\t" +     staticSynchronizeds +
                    "\n Synch          Code-Blob count:\t" +         synchronizedCBs +
                    "\n Synch Code       Varient count:\t" +     synchVarientCount() +
                    "\n Synch Code         Total count:\t" +            sCode.size() +
                    "\n               size of the tlog:\t" +   OTlock.locks.keySet().size() +
                    "\n Run-Time   Type-Analysed count:\t" +       rttAnalysisCount);
            
            System.out.println ("Starting to execute Step 4.");
            for (int fpCount = 3; --fpCount > 0;  )
               for (Iterator i = definedCode.values().iterator();  i.hasNext();  )  {
                   JavaBlob cb = (JavaBlob) i.next();
                   if (cb != null  &&  cb.accuracy != AnalysisLevel.RunTimeType)
                       cb.upgradeToRTTAnalysis();
               }
            computeTransitiveClosureReachableFromDefinedPublicConcreteEntries ();
            System.out.println ("sCode cardinality after computing Transitive Closure: " + sCode.size());
            deadlockPrediction.OTlock.reportLockUsage();
            sCodeReachedFromSCode ();                         // Step 5
            System.err.println (
                    "\n        Method invocation count:\t" +        invocationsCount +
                    "\n                      New count:\t" +                newCount +
                    "\n             Total locals count:\t" +             totalLocals +
                    "\n               SSA locals count:\t" +               ssaLocals +
                    "\nDefined or invoked method count:\t" +      definedCode.size() +
                    "\n    Invocation graph Edge count:\t" +        invocationsCount +
                    "\n Defined          Varient count:\t" +         varients.size() +
                    "\n            Native method count:\t" +   notDefinedCode.size() +
                    "\n  Abstract or I/f  method count:\t" +  tobeDefinedCode.size() +
                    "\n  Abstract or I/f Varient count:\t" + abstrctIntrfceVarient() +
                    "\n                    Class count:\t" + classes.keySet().size() +
                    "\n               Interfaces count:\t" +       interfaces.size() +
                    "\n Synch code-blob & method count:\t" +            sCode.size() +
                    "\n Synch static      method count:\t" +     staticSynchronizeds +
                    "\n Synch          Code-Blob count:\t" +         synchronizedCBs +
                    "\n Synch Code       Varient count:\t" +     synchVarientCount() +
                    "\n Synch Code         Total count:\t" +            sCode.size() +
                    "\n               size of the tlog:\t" +   OTlock.locks.keySet().size() +
                    "\n Run-Time   Type-Analysed count:\t" +       rttAnalysisCount);
            
            deadlockPrediction.OTlock.computeTLOGedges ();    // Step 6
            deadlockPrediction.OTlock.reportTLOGcycles (4);   // Step 7
            System.err.println (
                    "\n        Method invocation count:\t" +        invocationsCount +
                    "\n                      New count:\t" +                newCount +
                    "\n             Total locals count:\t" +             totalLocals +
                    "\n               SSA locals count:\t" +               ssaLocals +
                    "\nDefined or invoked method count:\t" +      definedCode.size() +
                    "\n    Invocation graph Edge count:\t" +        invocationsCount +
                    "\n Defined          Varient count:\t" +         varients.size() +
                    "\n            Native method count:\t" +   notDefinedCode.size() +
                    "\n  Abstract or I/f  method count:\t" +  tobeDefinedCode.size() +
                    "\n  Abstract or I/f Varient count:\t" + abstrctIntrfceVarient() +
                    "\n                    Class count:\t" + classes.keySet().size() +
                    "\n               Interfaces count:\t" +       interfaces.size() +
                    "\n Synch code-blob & method count:\t" +            sCode.size() +
                    "\n Synch static      method count:\t" +     staticSynchronizeds +
                    "\n Synch          Code-Blob count:\t" +         synchronizedCBs +
                    "\n Synch Code       Varient count:\t" +     synchVarientCount() +
                    "\n Synch Code         Total count:\t" +            sCode.size() +
                    "\n               size of the tlog:\t" +   OTlock.locks.keySet().size() +
                    "\n Run-Time   Type-Analysed count:\t" +       rttAnalysisCount);
            
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }
    /*  System.out.println ("Input Class  count: \t" + classesList.size() + ".");
     *  Set extra = classes.keySet();
     *  classesList.removeAll (interfaces);
     *  extra.removeAll (classesList);
     *  Object[] overDefinedClasses = extra.toArray();
     *  System.out.println ("Over defined Class count:" + overDefinedClasses.length);
     *  for (int k = overDefinedClasses.length;  --k >= 0;  )
     *      System.out.println ("Overdefined Class: " + ((String)overDefinedClasses[k]));
     */
     
}
