import java.util.*;
import java.util.regex.*;
import java.io.*;
import deadlockPrediction.*;
public class JavaBlob extends deadlockPrediction.CodeBlob {
    private JavaBlob (String mSignature) {
        super (mSignature);
    };

    public JavaBlob (String fsClassNm, String mNm, String prtEnc,
        boolean isStat, boolean isSynch, boolean isPub)  {
        super (fsClassNm, mNm, prtEnc, isStat, isSynch, isPub);
    }

    public void noteCallTo (String caller, String called)  {
        deadlockPrediction.CodeBlob rv = null;
        if (!  nativeMethods.contains (called)  &&
            !abstractMethods.contains (called)  &&
            (rv = (deadlockPrediction.CodeBlob)unexploredCode.get (called)) == null   &&
            (rv = (deadlockPrediction.CodeBlob)  exploredCode.get (called)) == null)
            rv  = new JavaBlob (called.intern());
        super.noteCallTo (caller, called, rv);
    }

    public JavaBlob (String p, String c, String lT)  {
        super (p, c, lT);
    }
    
    static BufferedReader textIn = null;
    static volatile java.util.concurrent.SynchronousQueue<BufferedReader> javaPout =
           new java.util.concurrent.SynchronousQueue<BufferedReader>();
    static String         classesHome = null;
    static Set            classesList = null;
    static Collection     interfaces  = new HashSet();
    
    static public void classListGivenUsage(String[] args)  {
        if (args.length==0)
            classesList = classNamesFromJar (null);
        else if (args[args.length-1].indexOf(".class")<0  &&
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
    static final int  NoOfClassesPerJavapInvocation = 25;
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
                               "/../bin/javap -private -c -verbose -J-Xmx128m " +
                               (classesHome!=null ? ("-classpath "+classesHome) : "")
                               + " " + subList;
                        System.out.println (delete2be.replace (' ', '\n'));
                        child = Runtime.getRuntime().exec (System.getProperty ("java.home") +
                                    "/../bin/javap -private -c -verbose -J-Xmx128m " +
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
        if (version.startsWith ("1.6"))
            return  fqcn.startsWith ("com.sun.crypto.provider")  ||
                    fqcn.startsWith ("com.sun.corba.se.spi.transport") ||
                    fqcn.startsWith ("sun.net.spi") ||
                    fqcn.startsWith ("sun.text.resources") ||
                    fqcn.startsWith ("sun.util.resources") ||
                    fqcn.startsWith ("sun.security.pkcs11.wrapper.PKCS11RuntimeException");
        else if (version.startsWith ("1.5"))
            return  fqcn.equals ("com.sun.crypto.provider.ai") ||
                    fqcn.equals ("sun.security.pkcs11.wrapper.PKCS11RuntimeException") ||
                    fqcn.startsWith ("sun.net.spi") ||
                    fqcn.startsWith ("sun.net.www") ||
                    fqcn.startsWith ("sun.text.resources");
        else if (version.startsWith ("1.4"))
            return  fqcn.equals ("com.sun.crypto.provider.ai") ||
                    fqcn.startsWith ("com.sun.jndi.ldap") ||
                    fqcn.startsWith("sun.text.resources") ||
                    fqcn.startsWith ("sun.net.spi") ||
                    fqcn.startsWith("sun.net.www") ||
                    fqcn.equals ("com.sun.security.sasl.util.SaslImpl");
        return true;
    }
    
    static String readClassName (String line)  {
        assert (line.indexOf("class ")>=0); // Originally  &&  line.indexOf("{")>=0);
        boolean isAbstract = line.indexOf("abstract ") >= 0;
        int     indExtends = line.indexOf(" extends ");
        String  cNm = null, pNm = null;
        if (indExtends > 0)  {
            cNm  = line.substring (line.indexOf(" class ")+7, indExtends);
            line = line.substring (indExtends + " extends ".length());
            pNm  = line.indexOf(' ')>0 ? line.substring(0, line.indexOf(' ')) : line;
            pNm  = pNm.replace('/', '.').intern();
        } else {
            line = line.substring (line.indexOf(" class ")+7);
            cNm  = line.indexOf(' ')>0 ? line.substring(0, line.indexOf(' ')) : line;
        }
        cNm = cNm.replace('/', '.').intern();
        ClassInfo tv = (ClassInfo) classes.get (cNm);
        if (tv == null)
            classes.put (cNm, new ClassInfo (pNm));
        else
            tv.parentName = pNm;
        if (isAbstract)
            abstractClasses.add (cNm);
        return cNm;
    }

    static void readInterfaceNameSkipBody (String line) throws java.lang.Exception {
        assert (line.indexOf("interface ")>=0);
        String  iNm  = line.substring (line.indexOf(" interface ") +
                                       " interface ".length());
        if (iNm.indexOf(' ') > 0)
                iNm  = iNm.substring (0, iNm.indexOf(' '));
        interfaces.add (iNm);
        while (! (line=textIn.readLine()).equals("{"))
            ;
        while (! (line=textIn.readLine()).equals("}"))
            ;
    }
    
    static String readMethodHeader (String cName, String line)  
        throws Exception {
        int openP = line.indexOf("("),  closeP = line.indexOf(");");
        if (closeP < 0)                 closeP = line.indexOf(")   throws ");
        String mNm, decoration = "",    rType  = null;

        int lastBlankB4OpenP = line.substring(0, openP).lastIndexOf(" ");
        mNm = line.substring(lastBlankB4OpenP+1, openP);
        if (lastBlankB4OpenP >= 0)
            decoration = line.substring(0, lastBlankB4OpenP);

        String[] pTypes   = line.substring(openP+1, closeP).split(",");
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
        String prt = getPRTcode (pTypes, rType);
        String key = (cName + "." + mNm + ":" + prt).intern();
        deadlockPrediction.CodeBlob tv =
           (deadlockPrediction.CodeBlob) unexploredCode.remove (key);
        if (decoration.indexOf("native") >= 0  ||
            decoration.indexOf("abstract") >= 0)  {
            if (decoration.indexOf("native") >= 0)   nativeMethods.add (key);
            else                                   abstractMethods.add (key);
            if (tv != null)                tv.removeFromUnexploredCode (key);
        } else {
            if (tv == null)   new JavaBlob (cName, mNm, prt,
                                            decoration.indexOf("static")>=0,
                                      decoration.indexOf("synchronized")>=0,
                                            decoration.indexOf("public")>=0);
            else              tv.upgradeCB (key,
                                            decoration.indexOf("static")>=0,
                                      decoration.indexOf("synchronized")>=0,
                                            decoration.indexOf("public")>=0);
            return key;
        }
        return null;
    }
    
    static void processFieldNmNdType (String cNm)
        throws java.lang.Exception {
        String line = null;
        while ((line=textIn.readLine()) != null  &&  !line.equals("{"))  {
            line = line.trim();
            if (line.startsWith("const #")  &&  line.indexOf("= Field")>0) {
                int i, comment = line.indexOf("//  ");
                String fType = line.substring (line.indexOf(':')+1);
                for (i = 0;  fType.charAt(i)=='[';  i++);
                if (fType.charAt(i) != 'L')
                    continue;
                fType = fType.substring (i+1, fType.indexOf(';')).replace ('/', '.');
                line  = line.substring (comment + "//  ".length());
                line  = line.substring (0, line.indexOf(':')).replace('/', '.');
                
                ClassInfo tv = (ClassInfo) classes.get (fType);
                if (tv == null)
                    classes.put (fType, tv = new ClassInfo (null));
                tv.setInstanceAttribute (line);
                // System.out.println ("Added <" + fType + ", " + line + ")");
            }
        }
    }
    
    public void processMethodBody(String cbn) throws java.lang.Exception {
        List offset = new ArrayList(),
             code   = new ArrayList();
        boolean monitorUsed = false;
        String line = textIn.readLine(), tl = null;
        // System.out.println ("Started reading method: " + cbn);
        if (line.startsWith("  Synthetic:"))
            line = textIn.readLine();
        assert (line.startsWith("  Code:"));
        assert ((line = textIn.readLine()) != null  &&  line.trim().length() > 0);
        int locals = Integer.parseInt (line.substring (
            line.indexOf("Locals=")+"Locals=".length(), line.indexOf(", Args_")));
        List executionStartOffset  = new ArrayList(); // pc(s) where executions (re)start
        List stackAtExecutionStart = new ArrayList(); // Stack-state at those PC(s)
        List arrayAtExecutionStart = new ArrayList(); // Local-state at those PC(s)
        Integer m1 = new Integer (-1);
        int bcOffset, colon = -1;
        List matchingMonitorExits = new ArrayList();

        while ((line=textIn.readLine()) != null  &&  line.trim().length() > 0)  {
            if (line.indexOf("  Exceptions:")==0)  {       // point 2 above.
                assert ((line = textIn.readLine())  !=  null  &&
                        line.indexOf("   throws ") == 0);
                break;
            } else if (line.indexOf("Exception table:")>=0)  {  // point 1 above.
                if (monitorUsed)  {
                    int sz = readExceptionNdLineNoTable (executionStartOffset,
                        stackAtExecutionStart, offset, code, matchingMonitorExits, cbn);
                    for (  ;  --sz >= 0;  )
                        arrayAtExecutionStart.add (getTypeArrayFromArguments (cbn, locals));
                } else {
                    boolean lastLineBlank = false;
                    while ((line = textIn.readLine()) != null  &&
                           (!lastLineBlank  ||  line.trim().length() != 0)  &&
                           line.indexOf("Exceptions:") < 0  &&
                           line.indexOf("LineNumberTable:") < 0)  {
                        lastLineBlank = line.trim().length()==0;
                        textIn.mark(256);
                    }
                    if (line.trim().length() != 0)              textIn.reset();
                    if (line.indexOf("LineNumberTable:") > 0)   continue;
                }
                break;
            } else if (line.indexOf("LineNumberTable:") >= 0) {
                while ((line = textIn.readLine()) != null  &&  line.trim().length() > 0)
                    ;
                break;
            }
            if ((colon = line.indexOf(':')) <= 0)
                System.out.println ("HowCome (" + cbn + "):" + line + ".");
            // assert ((colon = line.indexOf(':')) > 0); 
            bcOffset = Integer.parseInt (line.substring(0, colon).trim());
            if (line.indexOf("tableswitch{")>=0 || line.indexOf("lookupswitch{")>=0)
                do {
                    line = line + (tl = textIn.readLine());
                } while(tl.indexOf("default:")<0); // Multi-line instruction: point 12 
            line = line.substring(colon+1).trim(); // instruction, operands, & comments
            offset.add (bcOffset);                          // points 3,
            code.add   (line);                              //   and 4 above.
            if (line.startsWith("monitore"))
                monitorUsed = true;                         // point 6 above
            String cn = cbn.substring (0, cbn.lastIndexOf("."));
            if (! monitorUsed &&                            // point 7 above 
                (line.startsWith("invokevirtual\t") ||      // point 5 above
                 line.startsWith("invokespecial\t") ||      // -- " --
                 line.startsWith("invokestatic\t"))) {      // -- " --
                // System.out.println ("broken Line (?) : " + line);
                String ms = line.substring (line.indexOf("; //Method ") + "; //Method ".length());
                String mn = ms.substring (0, ms.indexOf(':')).replace ('/', '.');
                ms = mn + ms.substring (ms.indexOf(":"));
                if (mn.charAt(0)=='\"'  &&  mn.endsWith("\".clone"))   // point 13, above
                    noteCallTo (cbn, "java.lang.Object.clone:()Ljava/lang/Object;");
                else
                    noteCallTo (cbn, (mn.indexOf('.')<0 ? (cn + ".") : "") + ms);
            } else if (line.indexOf("tstatic")==2 && line.indexOf("//Field ")>0) {
                int i, comment = line.indexOf("//Field ");
                String fType = line.substring (line.indexOf(':')+1);
                for (i = 0;  fType.charAt(i)=='[';  i++);
                if (fType.charAt(i) != 'L')
                    continue;
                fType = fType.substring (i+1, fType.indexOf(';')).replace ('/', '.');
                line  = line.substring (comment + "//Field ".length());
                line  = line.substring (0, line.indexOf(':')).replace('/', '.');
                
                if (line.indexOf('.') <= 0)
                    line = cn + '.' + line;
                ClassInfo tv = (ClassInfo) classes.get (fType);
                assert (tv != null);
                tv.setClassAttribute (line);
            }
        }
        // System.out.println ("Completed reading method: " + cbn);
        if (! monitorUsed)  {              // Second pass is unnecessary
            offset.clear();  code.clear();    executionStartOffset.clear();
            stackAtExecutionStart.clear();    arrayAtExecutionStart.clear();
            return;                        // per point 6, above. Clean-up, go-home.
        }
        java.lang.Object[] typeAtTOSlist = new java.lang.Object[offset.size()];

        // Execution begins at PC-Zero, with an empty stack, per point 8.
        updateBBlist (0, new Stack(),getTypeArrayFromArguments (cbn, locals), offset,
                      executionStartOffset, stackAtExecutionStart, arrayAtExecutionStart);

        Stack objStack = new Stack(), nameStack = new Stack();
              objStack.push (this);   nameStack.push (cbn);
        StringBuffer enterCounter = new StringBuffer("0"); 
        for (int i = 0;  i < executionStartOffset.size();  i++)  {
            bcOffset           = ((Integer)executionStartOffset.get(i)).intValue();
            Object[] typeArray =  (Object[]) arrayAtExecutionStart.get(i);
            Stack typeStack =  (Stack) stackAtExecutionStart.get(i);
            if (offset.contains(bcOffset)) {
                // System.out.println ("Execution-start-offsets: " + executionStartOffset);
                // System.out.println ("Starting interpretation from offset: " + bcOffset);
                for (int j = offset.indexOf(bcOffset);  j<offset.size();  j++)  {
                    typeAtTOSlist[j] = typeStack.empty() ? null : typeStack.peek();
                    // System.out.println ("Before: "+offset.get(j)+ ' ' +typeStack + " e-counter:" + enterCounter);
                    offset.set (j, m1);
                    if (! interpret(objStack, nameStack, (String) code.get(j), typeStack, typeArray,
                                executionStartOffset, stackAtExecutionStart, arrayAtExecutionStart,
                                j == offset.size()-1 ? -1 : ((Integer)offset.get(j+1)).intValue(),
                                enterCounter, offset, matchingMonitorExits))
                        break;
                }
            }
        }
    }
    public int readExceptionNdLineNoTable (List execStart, List stacks, List offset, List code,
                                           List matchingExits, String cbn) throws java.lang.Exception {
        assert (textIn.readLine().trim().startsWith("from"));
        int    i[] = new int[50], j[] = new int[50], k[] = new int[50], t=-1, n=0, ni=0, nj=0, nk=0; 
        String l = textIn.readLine();
        for (;  l != null  &&  l.trim().length() > 0  &&
                l.trim().indexOf("LineNumberTable:") < 0  &&
                l.trim().indexOf("Exceptions:") < 0;  l = textIn.readLine())  {
            ni = Integer.parseInt ((l = l.trim()).substring (0, t = l.indexOf(' ')));
            l  = l.substring(t).trim(); // read and dropped the 'from' integer value
            nj = Integer.parseInt (l.substring (0, t = l.indexOf(' ')));
            l  = l.substring(t).trim(); // read and dropped the 'to' integer value
            nk = Integer.parseInt (l.substring (0, t = l.indexOf(' ')));
            l  = l.substring(t).trim(); // read and dropped the 'target' integer value
            if (l.startsWith("any"))  {
                if (ni != nk  &&  ((String)code.get(offset.indexOf(nj)-1)).startsWith("monitore"))  {
                   i[n] = ni;  j[n] = nj;  k[n] = nk;  n++;
                   // System.out.println ("Processed: " + ni +','+ nj +','+ nk +','+ n);
                }
            } else {
                // execStart.add (nk);
                // Stack s = new Stack();
                // s.push ("L" + l.substring(l.indexOf(' ')+1) + ";");
                // stacks.add (s);
            }
        }
        for (int m = 0;  m < n;  m++)
            if (m != (n-1)  &&  k[m] == k[m+1]) {
                if ((offset.indexOf(new Integer(j[m]))+1) != offset.indexOf(new Integer (i[m+1])))
                    System.out.println ("Proceeding with Inaccurate analysis of " + cbn);
                i[m+1] = i[m];
            } else if (i[m] != 0  && 
                       ((String)code.get(offset.indexOf(i[m])-1)).startsWith("monitorenter"))
                matchingExits.add (j[m]);
        // System.out.println ("Matching Exits at n(" + n + "): " + matchingExits);
        if (l.trim().indexOf("  Exceptions:") >= 0)
            assert (textIn.readLine().indexOf("   throws ") == 0);
        else
            while ((l=textIn.readLine()) != null  &&  l.trim().length() > 0)
                ;
        return execStart.size();
    }
    // import java.util.regex;
    static Pattern u_arith = Pattern.compile ("[ilfd](?:neg)");           // unary negate
    static Pattern dtc     = Pattern.compile ("[dfil]2[bcsdfil]");        // Data Type conversion
    static Pattern wideinc = Pattern.compile ("(?:wide)|(?:iinc)");       // wide / iinc
    static Pattern loads   = Pattern.compile ("[adfil]load(?:_[0123])?"); // Loads
    static Pattern stores  = Pattern.compile ("[adfil]store(?:_[0123])?");// Stores
    static Pattern consts  = Pattern.compile ("[dfil]const_m?[012345]");  // Push Constants
    static Pattern push    = Pattern.compile ("[bs]ipush");               // byte/short Push
    static Pattern b_arith = Pattern.compile ("[ilfd]((?:add)|(?:sub)|(?:mul)|(?:div)|(?:rem))");
    static Pattern bitwise = Pattern.compile ("[il]((?:x?or)|(?:and))");  // bitwise: and, or, xor
    static Pattern shifts  = Pattern.compile ("[il]u?sh[lr]");            // Shift right/left unsighed?
    static Pattern lfdCmp  = Pattern.compile ("[lfd]cmp[lg]?");           // double-word compares
    static Pattern cmpares = Pattern.compile ("if(?:_[ai]cmp)?((?:eq)|(?:g[et])|(?:l[et])|(?:ne))");
    static Pattern nullCmp = Pattern.compile ("if(?:non)?null");          // Null compares
    static Pattern returns = Pattern.compile ("[adfil]?ret(?:urn)?");     // return from method/subrtn
    static Pattern aloads  = Pattern.compile ("[abcdsilf]aload");         // Array Load
    static Pattern astores = Pattern.compile ("[abcdsilf]astore");        // Array Store
    static Pattern gotoJsr = Pattern.compile ("(?:goto)|((?:jsr)(?:_w)?)"); // goto/jump to subroutines
    static Pattern mTarget = Pattern.compile ("((lookup)|(table))(switch)"); // multi-target switch
    static Pattern ldConst = Pattern.compile ("ldc2?(?:_w)?");            // Load constant ...
    static Pattern stackOp = Pattern.compile ("(?:dup2?(?:_x[12])?)|(?:pop2?)|(?:swap)");
    static Pattern invokes = Pattern.compile ("(invoke)((?:special)|(?:static)|(?:virtual)|(?:interface))");
    static Pattern fieldOp = Pattern.compile ("((?:get)|(?:put))((?:field)|(?:static))");
    static Pattern castOp  = Pattern.compile ("(?:checkcast)|(?:instanceof)");
    static Pattern newOp   = Pattern.compile ("(?:multi)?a?new(?:array)?");  // various 'new's.
    static Pattern lockOp  = Pattern.compile ("monitor((?:enter)|(?:exit))");  // Monitor operations
    static Pattern restOp  = Pattern.compile ("a((?:throw)|(?:rraylength)|(?:const_null))");
    static Pattern c1Match = Pattern.compile ("[BCFISZ]");                // c1 matcher
    static Pattern c2Match = Pattern.compile ("[DJ]");                    // c2 matcher
    static String  c1      = "c1".intern(),
                   c2      = "c2".intern();

    static boolean interpret(Stack oStack, Stack nStack, String bc, Stack tS, Object[] tA,
        List execStartOffset, List stackAtExecStart, List arrayAtExecStart,
        int nextOffset, StringBuffer eCounter, List bcOffsets, List monitorExits) throws Exception {
        Matcher m; // System.out.println (bc);
        if (bc.startsWith("nop") || u_arith.matcher(bc).lookingAt())
            ;
        else if ((m = dtc.matcher(bc)).lookingAt())  {
            char from = bc.charAt(m.start()), to = bc.charAt(m.end()-1);
            if (from == 'd'  ||  from == 'l') { assert (tS.pop() == c2);  assert (tS.pop() == c2); }
            else                                assert (tS.pop() == c1);
            if (to == 'd'  ||  to == 'l')     { tS.push(c2);  tS.push(c2); }
            else                                tS.push(c1);
        } else if ((m = loads.matcher(bc)).lookingAt()  ||  (m = stores.matcher(bc)).lookingAt()  ||  
                   (m = wideinc.matcher(bc)).lookingAt())  {
            if (bc.charAt(m.start()) == 'w')    bc = bc.substring(m.end()).trim();
            if (bc.startsWith ("iinc"))         return true;
            if ((m = loads.matcher(bc)).lookingAt()  ||  (m = stores.matcher(bc)).lookingAt()) {
                char tb = bc.charAt(m.start()), tc = bc.charAt(m.end()-1);
                int i = (tc!='d' && tc!='e') ? (tc-'0') : Integer.parseInt(bc.substring(m.end()).trim());
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
                    } else // if (tA[i]==null || !((String)tS.peek()).equals("Ljava/lang/Object;"))
                        tA[i] = tS.pop();
                    // else
                        // tS.pop();
            }
        } else if ((m = consts.matcher(bc)).lookingAt()  ||  (m = push.matcher(bc)).lookingAt()) {
            char tb = bc.charAt(m.start());
            if (tb == 'd'  ||  tb == 'l') { tS.push(c2);  tS.push(c2); }
            else                            tS.push(c1);
        } else if ((m = b_arith.matcher(bc)).lookingAt() || (m = bitwise.matcher(bc)).lookingAt()) {
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
        } else if ((m = cmpares.matcher(bc)).lookingAt() || (m = nullCmp.matcher(bc)).lookingAt()) {
            tS.pop();
            if (bc.charAt(m.start()+2) == '_')
                tS.pop();
            int ctt = Integer.parseInt (bc.substring (m.end()).trim()); //control transfer target
            updateBBlist (ctt, tS, tA, bcOffsets, execStartOffset, stackAtExecStart, arrayAtExecStart);
        } else if ((m = returns.matcher(bc)).lookingAt())  {
            char tb = bc.charAt(m.start());
            if (tb=='d' || tb=='l')           {  assert(tS.pop() == c2);   assert(tS.pop() == c2);  }
            else if (tb=='a')                    tS.pop();
            else if (m.group().length() == 7)    assert(tS.pop() == c1);
            else if (m.group().length() == 3) {
                int ctt = ((Integer) tA[Integer.parseInt(bc.substring(m.end()).trim())]).intValue();
                updateBBlist (ctt, tS, tA, bcOffsets, execStartOffset, stackAtExecStart, arrayAtExecStart);
            }
            return false;
        } else if ((m = aloads.matcher(bc)).lookingAt()) {
            char tb = bc.charAt(m.start());
            assert (tS.pop() == c1);                                             // consume the index
            if (tb != 'a') {       assert (((String)tS.pop()).startsWith("["));  // consume the array-base
                if (tb=='d' || tb=='l') {  tS.push(c2);         tS.push(c2);  }
                else                       tS.push(c1);
            } else {
                String tmp = (String) tS.pop();
                assert (tmp.startsWith("[") || tmp.equals("Ljava/lang/Object;")); // confirm that there is the array-base
                tS.push(tmp.startsWith("[") ? tmp.substring(1) : tmp); // consume the array-base, but re-instate element-type
            }
        } else if ((m = astores.matcher(bc)).lookingAt())  {
            char tb = bc.charAt(m.start());
            if (tb == 'a')                  tS.pop();
            else if (tb=='d' || tb=='l') {  assert(tS.pop() == c2);   assert(tS.pop() == c2);  }
            else                            assert(tS.pop() == c1);
            assert(tS.pop() == c1);         assert(((String)tS.pop()).startsWith("["));
        } else if ((m = gotoJsr.matcher(bc)).lookingAt())  {
            if (bc.charAt(m.start()) == 'j')
                tS.push(nextOffset);
            int ctt = Integer.parseInt (bc.substring (m.end()).trim());
            updateBBlist (ctt, tS, tA, bcOffsets, execStartOffset, stackAtExecStart, arrayAtExecStart);
            return false;
        } else if ((m = mTarget.matcher(bc)).lookingAt()) {
            // System.out.println ("entered here !");
            // for (int cln = bc.indexOf(':'), sCln = bc.indexOf(';'); 
            //      cln>0 && sCln>0 && cln<sCln;  ) {
            //     int ctt = Integer.parseInt (bc.substring (cln+1, sCln).trim());
            //     updateBBlist (ctt, tS, tA, bcOffsets, execStartOffset,
            //                                           stackAtExecStart, arrayAtExecStart);
            //     bc = bc.substring(sCln+1);  cln  = bc.indexOf(':');  sCln = bc.indexOf(';');
            // }
            while (true) {
                int cln = bc.indexOf(':'),  sCln = bc.indexOf(';');
                if (sCln<0) sCln = bc.indexOf(" }");
                if (cln>0 && sCln>0 && cln<sCln) {
                    int ctt = Integer.parseInt (bc.substring (cln+1, sCln).trim());
                    updateBBlist (ctt, tS, tA, bcOffsets, execStartOffset,
                                                          stackAtExecStart, arrayAtExecStart);
                    bc = bc.substring(sCln+1);
                } else
                    break;
            }
        }  else if ((m = ldConst.matcher(bc)).lookingAt())
            if (m.group().length() > 3  &&  bc.charAt(3) == '2') {
                tS.push (c2);  tS.push (c2);
            } else {
                String tmp = bc.substring(m.end());
                tS.push(tmp.indexOf("//String")>=0 || tmp.indexOf("//class")>=0  ?  "Ljava.lang.String;"  :  c1);
            }
        else if ((m = stackOp.matcher(bc)).lookingAt())  {
            char tb = bc.charAt(m.start());
            Object ts = null, tsn = null, tsnn = null, tsnnn = null;
            if (tb == 'd') {
                if (m.group().length() == 3) {  assert ((ts = tS.peek()) != c2);  tS.push(ts); }  //dup
                else if (m.group().length() == 4) {                             //dup2
                    if ((ts=tS.pop()) == c2) {  assert (tS.peek() == c2);  tS.push(c2);   tS.push(c2);  tS.push(c2);  }
                    else {  assert((tsn=tS.peek()) !=  c2);  tS.push(ts);  tS.push(tsn);  tS.push(ts); }
                } else if (m.group().length()==6 && bc.charAt(m.end()-1)=='1') {  //dup_x1
                    assert ((ts=tS.pop()) != c2);        assert ((tsn=tS.pop()) != c2);
                    tS.push(ts);         tS.push(tsn);   tS.push(ts);
                } else if (m.group().length()==6 && bc.charAt(m.end()-1)=='2') {  //dup_x2
                    assert ((ts=tS.pop()) != c2);
                    if ((tsn=tS.pop()) == c2) {
                        assert (tS.pop() == c2);         tS.push(ts);
                        tS.push(c2);     tS.push(c2);    tS.push(ts);
                    } else {
                        assert ((tsnn=tS.pop()) != c2);  tS.push(ts); 
                        tS.push(tsnn);   tS.push(tsn);   tS.push(ts);
                    }
                } else if (m.group().length()==7 && bc.charAt(m.end()-1)=='1') {  //dup2_x1
                    ts = tS.pop();       tsn = tS.pop(); assert((tsnn=tS.pop()) != c2); 
                    tS.push(tsn);        tS.push(ts);    tS.push(tsnn);   tS.push(tsn);  tS.push(ts);
                } else if (m.group().length()==7 && bc.charAt(m.end()-1)=='2') {  //dup2_x2
                    ts = tS.pop();       tsn = tS.pop(); tsnn = tS.pop(); tsnnn = tS.pop();
                    assert ((ts!=c2 && tsn!=c2)  ||  (ts==c2 && tsn==c2));
                    assert ((tsnn!=c2 && tsnnn!=c2)  ||  (tsnn==c2 && tsnnn==c2));
                    tS.push(tsn);        tS.push(ts);
                    tS.push(tsnnn);      tS.push(tsnn);  tS.push(tsn);    tS.push(ts);
                }
            } else if (tb == 'p') {                      // pop or pop2
                if (m.group().length() == 3)             assert(tS.pop() != c2);
                else if ((ts=tS.pop()) != c2)            assert(tS.pop() != c2);
                else                                     assert(tS.pop() == c2);
            } else {                                     // The instruction would be "swap".
                assert ((ts = tS.pop()) != c2);          assert ((tsn = tS.pop()) != c2);
                tS.push(tsn);                            tS.push(ts);
            }
        } else if ((m = invokes.matcher(bc)).lookingAt()) {
            boolean isInterface = m.group().endsWith ("interface");
            interpretInvocation (m.group().endsWith ("static"), isInterface ?
                bc.substring(bc.indexOf("//InterfaceMethod ")+"//InterfaceMethod ".length()) :
                bc.substring(bc.indexOf("//Method ")+"//Method ".length()),
                tS, (String)nStack.peek(), (JavaBlob)oStack.peek(), isInterface);
        } else if ((m = fieldOp.matcher(bc)).lookingAt())  {
            Object ts = null;
            if (bc.charAt(m.start()) == 'g') {
                if (bc.charAt(m.start()+3) == 'f')
                    assert ((ts = tS.pop()) != c1  &&  ts != c2);
                tS.push (abstractTenc (bc.substring(bc.indexOf(':')+1).trim()));
                if (((String)tS.peek()).equals(c2))    tS.push(c2);
            } else {
                if ((ts=tS.pop()) == c1)
                    assert (c1Match.matcher(bc.substring(bc.indexOf(':')+1)).lookingAt());
                if (ts==c2) {            assert (tS.pop()==c2);
                    assert (c2Match.matcher(bc.substring(bc.indexOf(':')+1)).lookingAt());
                }
                if (bc.charAt(m.start()+3) == 'f')
                    assert ((ts = tS.pop())!=c1   &&   ts!=c2);
            }
        } else if ((m = castOp.matcher(bc)).lookingAt())  {
            Object ts = null;
            assert ((ts=tS.pop()) != c1  &&  ts != c2);
            if (bc.charAt(m.start()) == 'i')
                tS.push (c1);
            else {
                String t = bc.substring(bc.indexOf("//class ") + "//class ".length()).trim();
                tS.push (t.charAt(0) == '"'  ?  t.substring(1, t.length()-1)  :  ("L" + t + ";"));
            }
        } else if ((m = newOp.matcher(bc)).lookingAt())  {
            String typePreFix = "";
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
            tS.push (typePreFix + (m.group().equals("newarray")
                                   ?  abstractTenc (encodeType(bc.substring(m.end()).trim()))
                                   :  ("L"+bc.substring(bc.indexOf("//class ") + "//class ".length()).trim()+';')));
        } else if ((m = lockOp.matcher(bc)).lookingAt())  {
            deadlockPrediction.CodeBlob po = (JavaBlob) oStack.peek();
            if (m.group().endsWith("enter")) {
                String pnm = (String) nStack.peek();
                String lock= ((String)tS.pop()).replace('/', '.');
                if (lock.startsWith("L"))
                    lock   = lock.substring (1, lock.length()-1);// Get rid of the leading "L",and the trailing ';'.
                String key = (pnm +'#'+ lock +'#'+ eCounter.charAt(0)).intern();
                oStack.push (new JavaBlob(pnm, key, lock));
                nStack.push (key);
                eCounter.setCharAt(0, (char)(eCounter.charAt(0) + 1));
            } else { //2 lines below need 2 b deleted
                // System.out.println ("Compare: " + (String)tS.peek() + ":" + po.getLockType());
                // System.out.println ("monitorExits: " + monitorExits + "; nextOffset: " + nextOffset);
                if (monitorExits.contains(nextOffset)) {
                    String lock = ((String) tS.peek()).replace('/', '.');
                    if (lock.startsWith("L"))
                        assert (lock.equals ("L"+po.getLockType()+";"));
                    else
                        assert (lock.equals (po.getLockType()));
                    oStack.pop();  nStack.pop();
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
    public Object[] getTypeArrayFromArguments (String cbn, int locals)  {
        // System.out.println ("Local-count Received here: " + locals+ "isStatic: " + isStatic);
        java.lang.Object[] retval  = new java.lang.Object[locals];
        String pList = cbn.substring (cbn.indexOf(":(")+2, cbn.indexOf(")"));
        String prefix = "";
        if (! isStatic)
            retval[0] = "L" + cbn.substring(0, cbn.lastIndexOf('.')) + ";";
        for (int j = isStatic ? 0 : 1;  pList.length() > 0;  pList = pList.substring(1), j++) {
            if (pList.charAt(0) == '[') {
                prefix = "[";
                pList  = pList.substring(1);
            } else
                prefix = "";
            if (c1Match.matcher(pList.substring(0, 1)).lookingAt())
                retval[j] = prefix.length()>0 ? (prefix + c1) : c1;
            else if (c2Match.matcher(pList.substring(0, 1)).lookingAt()) {
                retval[j]   = prefix.length()>0 ? (prefix + c2) : c2;
                retval[++j] = prefix.length()>0 ? (prefix + c2) : c2;
            } else {
                assert (pList.startsWith("L"));
                retval[j] = prefix + pList.substring (0, pList.indexOf(";")+1);
                pList = pList.substring (pList.indexOf(";"));
            }
        }
        // System.out.println ("initialisation fetches:");
        // for (int kk = 0;  kk<locals && retval[kk]!=null; kk++)
        //     System.out.println (retval[kk].toString());
        return retval;
    }

    public static String abstractTenc (String type)  {
        if (type == null  ||  type.charAt(0) == 'L')
            return type;
        else if (c1Match.matcher(type.substring(0, 1)).lookingAt())
            return c1;
        else if (c2Match.matcher(type.substring(0, 1)).lookingAt())
            return c2;
        else if (type.charAt(0) == '[')
            return "[" + abstractTenc (type.substring(1));
        else
            assert (false);
        return null;
    }

    public static void interpretInvocation (boolean isStatic, String tMethod, Stack typeStack,
                       String callerNm,  JavaBlob callerNode, boolean isInterface)  {
        if (! isStatic)
            typeStack.pop();
        String prtEnc = tMethod.substring(tMethod.indexOf(':')+1);
        assert (prtEnc.charAt(0) == '(');
        if (prtEnc.charAt(1) != 'V')
            for (int i = 1;  prtEnc.charAt(i) != ')';  i++)  { 
                if (prtEnc.charAt(i) == 'L')  {
                    typeStack.pop();          for (  ; prtEnc.charAt(i) != ';';  i++);
                } else if (c1Match.matcher(prtEnc.substring(i, i+1)).lookingAt())
                    typeStack.pop();
                else if (c2Match.matcher(prtEnc.substring(i, i+1)).lookingAt())  {
                    typeStack.pop();          typeStack.pop();
                }  else if (prtEnc.charAt(i) == '[') {
                    if (c2Match.matcher(prtEnc.substring(i+1, i+2)).lookingAt())  {
                        typeStack.pop();          i++;
                    }
                }  else
                    assert (false);
            }
        if (!isInterface) {
            String tmn = tMethod.substring (0, tMethod.indexOf(':')).replace ('/', '.');
            tMethod = tmn + ':' + prtEnc;
            if (tmn.charAt(0)=='\"'  &&  tmn.endsWith("\".clone"))    // point 13, above
                callerNode.noteCallTo (callerNm, "java.lang.Object.clone:()Ljava/lang/Object;");
            else {
                String cn = callerNm.substring (0, callerNm.indexOf(':'));
                       cn =       cn.substring (0,   cn.lastIndexOf("."));
                callerNode.noteCallTo (callerNm, (tmn.indexOf('.')<0 ? (cn+".") : "") + tMethod);
            }
        }
        String rt = tMethod.substring(tMethod.indexOf(')')+1).trim();
        if (rt.charAt(0) != 'V') {
            typeStack.push (abstractTenc (rt));
            if (((String)typeStack.peek()).equals(c2))    typeStack.push(c2);
        }
    }

    public static void  updateBBlist (int ctt, Stack tS, Object[] tA, List bcOffsets,
                  List execStartOffset, List stackAtExecStart, List arrayAtExecStart)  {
        if (bcOffsets.contains (new Integer(ctt))) {
            int i = 0;
            while (i < execStartOffset.size()) {
                 int temp = ((Integer)execStartOffset.get(i)).intValue();
                      if (ctt >  temp)    i++;
                 else if (ctt == temp)    return;
                 else                     break;
            }
            execStartOffset.add (i, ctt);
            stackAtExecStart.add(i, tS.clone());
            arrayAtExecStart.add(i, tA.clone());
        }
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
        else if (type!=null  &&  type.trim().length()>0)
                 rv += "L" + type.replace ('.', '/') + ";";
        return rv;
    }
    
    public static void
    readInputNdCreateCallGraph (String[] args) throws java.lang.Exception {
      classListGivenUsage(args);
      runJavapInvoker();
      while ((textIn = javaPout.poll(5L, java.util.concurrent.TimeUnit.SECONDS)) != null)  {
          String line;
          while ((line=textIn.readLine()) != null)  {
            if (line.indexOf('#')<0 && line.indexOf('=')<0 &&
                line.indexOf("interface ")>=0)
              readInterfaceNameSkipBody (line);
            else if (line.indexOf('#')<0 && line.indexOf('=')<0 &&
                line.indexOf("class ")>=0) {
              String cNm = readClassName (line);
              processFieldNmNdType (cNm);
              System.err.println (cNm);
              while ((line=textIn.readLine()) != null)  {
                line = line.trim();
                int openP, closeP;
                if (line.length() == 0)       continue;
                if (line.startsWith("}"))     break; // reached end-of-class.
                openP  = line.indexOf("(");
                if ((closeP = line.indexOf(");")) < 0)
                    closeP = line.indexOf(")   throws ");
                if (line.indexOf('=')<0 && line.indexOf('#')<0 &&
                    line.indexOf(':')<0 &&
                    openP>=0 && closeP>=0 && openP<closeP) {
                  String mNm = readMethodHeader(cNm, line);
                  if (mNm != null) {
                    ((JavaBlob)exploredCode.get(mNm)).processMethodBody(mNm);
                    if (line.indexOf("   throws ") >= 0) {
                      textIn.mark(256);
                      if (textIn.readLine().indexOf("  Exceptions:") != 0)
                          textIn.reset();
                      else
                          assert (textIn.readLine().indexOf("   throws ") == 0);
                    }
                  }
                }  else if (line.indexOf("static ();") >= 0)
                  while ((line = textIn.readLine()) != null  &&
                         line.trim().length() > 0)
                      ;
              }
            }
          }
      }
    }
    
    
    public static void main (String[] args) {
        try {
            readInputNdCreateCallGraph (args);
            inheritNecessaryParentMethods();  // Step 2.1
            deadlockPrediction.OTlock.reportLockUsage();
            sCodeReachedFromSCode ();         // Step 3.1
            dropUnsynchronizedCodeBlobs ();   // Step 3.1.3
            deadlockPrediction.OTlock.computeLAGedges ();    // Cycle finding Step.
            deadlockPrediction.OTlock.reportLAGcycles (3);   // 7, Cycle reporting

            System.out.println ("Input Class  count: \t" + classesList.size() + ".");
            System.out.println ("Interfaces   count: \t" +  interfaces.size() + ".");
            Set extra = classes.keySet();
            classesList.removeAll (interfaces);
            extra.removeAll (classesList);
            Object[] overDefinedClasses = extra.toArray();
            System.out.println ("Over defined Class count:" + overDefinedClasses.length);
            for (int k = overDefinedClasses.length;  --k >= 0;  )
                System.out.println ("Overdefined Class: " + ((String)overDefinedClasses[k]));
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
    }
     
}
