/* CodeBlob.java -- Lock Acquisition Graph Construction, Cycle detection / reporting
 *
 * author: Vivek K. Shanbhag, IIIT-B, Bangalore, India.
 */

package deadlockPrediction;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public abstract class CodeBlob {
    
    protected static ConcurrentHashMap definedCode = new ConcurrentHashMap(); // defined methods
    public    static ConcurrentHashMap sCode = new ConcurrentHashMap(); // Synchronized code
    
    public static Set  notDefinedCode = new HashSet();
    public static Map tobeDefinedCode = new HashMap();
    public static Map      interfaces = new HashMap();
    public static Map        varients = new HashMap();
    
    public static int     synchronizedCBs = 0;  // count        synchronized statements
    public static int staticSynchronizeds = 0;  // count static-synchronized methods
    public static int      totalLocals = 0;  // Accumulate the locals count
    public static int        ssaLocals = 0;  // Count local-array "stores".
    public static int invocationsCount = 0;  // Count invocations.
    public static int         newCount = 0;  // Count the various types of newOp.
    public static int invGraphEdgeCount= 0;  // Count the edges in the Inv. Graph.
    static final int fanout = 5;
    static protected HashMap classes = new HashMap();
    /* This class-attribute stores a ClassInfo object corresponding
     *  to every class encountered while parsing the input, mapping
     *  parent-child relationships. Every entry into this map is a
     *  <key, value> pair, where the key is the name of the sub-class,
     *  and the value is the parent-class-name. The Java \& C-Sharp
     *  applications that inherit from \verb|CodeBlob| must populate
     *  this data, using calls like:
     *  classes.put (subClass, ClassInfo.getObject (subClass, parentType));
     */

    public boolean isConcrete () {
        ClassInfo cinfo = ((ClassInfo) classes.get(getContainerType().name()));
        if (cinfo != null  &&  cinfo.isAbstract)
            return false;
        if (interfaces.keySet().contains (getContainerType().name()))
            return false;
        for (int i = 0;  i < getParamCount();  i++)  {
            String nm = getParams()[i].name();
            ClassInfo c = (ClassInfo) classes.get (nm);
            if (interfaces.keySet().contains(nm)  ||  c == null  ||  c.isAbstract)
                return false;
        }
        return true;
    };
    static public boolean
        exhaustedReadingInputJars = false;
    
    private String  lockType = null;
        // Stores the type of object that is synchronised upon:
        //  Null implies an 'un-Synchronised' code-blob
        //  ".class" postfix for static synch methods
        //  setLockType() ensures consistancy with the locks hashMap.
    public boolean isPublic = false;    // Is this a public method ?
    public boolean isStatic = false;    // Is this a static method ?
    //private String  nodeName      = null; // Name of the node
    private MethodSignature nodeName= null;
        //  String "package-name/class-name.method-name:prtEncoding"
    //private String  containerType = null; // Type of the container Class
    //private String  returnType    = null;
    //private int     noOfParams    = -1;
    //private String[] paramList    = null;
    
    public Collection    rtReturnTypes      = null;
    public TypeSignature getContainerType() { return nodeName.className(); };
    public TypeSignature getCTReturnType()  { return nodeName.retType();   };
    public String        getNodeName()      { return nodeName.signature(); };
    public TypeSignature[] getParams()      { return nodeName.paramList(); };
    public int           getParamCount()    { return nodeName.paramCount();};
    
    public enum AnalysisLevel {Null, CompileTimeType, RunTimeType };
    public      AnalysisLevel accuracy = AnalysisLevel.Null;
    public CodeBlob behaviourSpec = null;
    
    protected HashMap   calledCode = new HashMap();
    ConcurrentHashMap  callingCode = new ConcurrentHashMap();
    Set  concPublicInvokers = null;
    HashSet sEdges = null;
    
    public CodeBlob (String mSign) {
        assert ((definedCode.get(mSign) == null)  &&
                !notDefinedCode.contains(mSign)  &&  (tobeDefinedCode.get(mSign) == null));
        
        assert (mSign.indexOf ('#') < 0);
        setNodeName (mSign);
        System.err.println ("ZZZ: Created defined Node: " + mSign);
        if (! exhaustedReadingInputJars)
            definedCode.put (mSign, this);
    };
    /*public CodeBlob (String mSign, String cbName) {
        setNodeName (cbName);
        this.behaviourSpec = mSign;
        ConcurrentHashMap implementors = (ConcurrentHashMap) tobeDefinedCode.get(mSign);
        if (implementors == null)
            tobeDefinedCode.put (mSign, implementors = new ConcurrentHashMap ());
        implementors.put (getNodeName().intern(), this);
    }; */
    public CodeBlob (CodeBlob cttb, String cbName) {
        String newName = cbName + "-:varientOf:-" + cttb.getNodeName();
        assert (cttb != null  &&  !cttb.getNodeName().equals (cbName));
        assert (varients.get(newName) == null);
        setNodeName (cbName);
        this.behaviourSpec = cttb;  //.nodeName.signature();
        varients.put (newName, this);
        System.out.println ("NewlyCreated: " + newName);
        assert (!cttb.getNodeName().equals (getNodeName()));
    };
    public CodeBlob (String ms, boolean isStat,
                               boolean isSynch, boolean isPub)  {
        this (ms);
        isPublic = isPub;
        isStatic = isStat;
        if (isSynch)  {
            setLockType (nodeName.className().name() + (isStatic ? ".class" : ""));
            if (isStatic)  staticSynchronizeds++;
        }
        
    }
    public CodeBlob (CodeBlob p, String c, String lT)  {
        System.out.println ("Creating: " + c); //Debug comment
        setNodeName (c);
        setLockType (lT);
        p.noteCallTo (this);
        behaviourSpec = p.behaviourSpec == null ? p : p.behaviourSpec;
        accuracy = AnalysisLevel.RunTimeType;
        synchronizedCBs++;
    }
    
    private void setNodeName (String nm) {
        assert (nodeName == null);
        nodeName = new MethodSignature (nm.intern());
        if (nm.indexOf('#') > 0)
            return;
    }
    public void upgradeCB (boolean isStat,
        boolean isSynch, boolean isPub)  {
        assert (definedCode.get (nodeName.signature()) != null);
        isPublic = isPub;    isStatic = isStat;
        if (isSynch)  {
            setLockType (nodeName.className().name() + (isStatic ? ".class" : ""));
            if (isStatic)  staticSynchronizeds++;
        }
        
    }
    public void noteCallTo (CodeBlob called)  {
        if (calledCode.keySet().contains(called.getNodeName()))
            return;
        called.callingCode.put (getNodeName(), this);
        calledCode.put (called.getNodeName(), called); invGraphEdgeCount++;
    }
    public void noteCallTo (String called)  {
        if (calledCode.keySet().contains(called) || notDefinedCode.contains(called))
            return;
        if (tobeDefinedCode.get (called) == null)
            tobeDefinedCode.put (called, new ConcurrentHashMap());
        calledCode.put (called, null);
    }
    public void noteCallTo (String crMsign, CodeBlob rtcb)  {
        ConcurrentHashMap hmp = null;
        if (notDefinedCode.contains(crMsign))
            return;
        if ((hmp = (ConcurrentHashMap) tobeDefinedCode.get (crMsign)) == null)
            tobeDefinedCode.put (crMsign, hmp = new ConcurrentHashMap ());
        calledCode.put (crMsign, rtcb);
        rtcb.callingCode.put (getNodeName(), this); invGraphEdgeCount++;
        hmp.put (rtcb.getNodeName(), rtcb);
    }
    public void noteCallTo (CodeBlob crcb, CodeBlob rtcb)  {
        calledCode.put (crcb.getNodeName(), rtcb);
        rtcb.callingCode.put (getNodeName(), this); invGraphEdgeCount++;
    }
    public void setLockType (String lock)  {
        assert (lockType == null  &&  lock != null);
        assert (!lock.equals ("com")  &&  !lock.equals("java")  &&
                !lock.equals ("sun")  &&  !lock.equals("javax"));
        lockType = lock;
        OTlock rv = (OTlock)OTlock.locks.get(lock);
        if (rv == null)
            OTlock.locks.put(lock, rv = new OTlock(lock));
        rv.lContenders.add (this);
        sCode.put (getNodeName(), this);
    }

    public String getLockType()     {  return lockType;  };

    public abstract void processMethodBody() throws java.lang.Exception;
    
    /****
    public boolean inheritFromDefinedCode ()
        throws java.lang.Exception {
        if (accuracy == AnalysisLevel.Null)  {
            String clsNm = nodeName.className().name();
            String rest  = nodeName.dropClassNmGetRest();
            CodeBlob pc = null, ac = null;
            for (ClassInfo ci = null;  clsNm != null;
                 clsNm = ((ci = (ClassInfo)classes.get(clsNm)) == null) ?
                         null : ci.parentType) {
                 String bspecNm = clsNm +"."+ rest;
                 if (notDefinedCode.contains (bspecNm)) {
                     assert (    definedCode.get(bspecNm) == null  &&
                             tobeDefinedCode.get(bspecNm) == null);
                     notDefinedCode.add (c.getNodeName());
                     definedCode.remove (c.getNodeName());
                     for (Iterator j = callingCode.values().iterator();  j.hasNext();  )  {
                       CodeBlob caller = (CodeBlob) j.next();
                       if (caller != null  &&  definedCode.get(caller.getNodeName()) != null)
                         caller.calledCode.remove(getNodeName());
                     }
                     callingCode.clear();
                     break;
                 } else if (tobeDefinedCode.get(bspecNm) != null) {
                     assert (   definedCode.get(bspecNm) == null  &&
                            !notDefinedCode.contains(bspecNm));
                     tobeDefinedCode.put (getNodeName(), new ConcurrentHashMap());
                     definedCode.remove (c.getNodeName());
                     for (Iterator j = callingCode.values().iterator();  j.hasNext();  )  {
                       CodeBlob caller = (CodeBlob) j.next();
                       if (caller != null  &&
                         definedCode.get(caller.getNodeName()) != null) {
                         caller.calledCode.remove(getNodeName());
                         caller.calledCode.put (getNodeName(), null);
                       }
                     }
                     callingCode.clear();
                     break;
                 } else if ((pc = (CodeBlob) definedCode.get(bspecNm)) != null)
                     if (pc.accuracy != AnalysisLevel.Null) {
                         c.upgradeToCTTAnalysis (pc);
                     break;
                 }
            }
        }
        if (definedCode.get (c.getNodeName()) == c  &&
          c.accuracy == AnalysisLevel.Null)  {
          String clsNm = c.nodeName.className().name();
          String rest  = c.nodeName.dropClassNmGetRest();
          ClassInfo ci = (ClassInfo) classes.get (clsNm);
          List ii = ci != null ? ci.getInterfacesImplemented()
                               : getInterfacesExtendedBy (clsNm);
          // System.out.println ("Looking for undefined: " + c.getNodeName());
          if (ii != null  &&  ii.size() > 0) {
            for (int k = 0;  k < ii.size();  k++) {
              String ipNm = null;
              ipNm = (String) ii.get(k) + "." + rest;
              // System.out.println ("Checking: " + ipNm + " against interface: " + ii.get(k));
              if (tobeDefinedCode.get (ipNm) != null) {
                assert (     definedCode.get(ipNm) == null  &&
                       ! notDefinedCode.contains(ipNm));
                tobeDefinedCode.put (c.getNodeName(), new ConcurrentHashMap());
                assert (c == (CodeBlob) definedCode.remove (c.getNodeName()));
                for (Iterator j = c.callingCode.values().iterator();  j.hasNext();  )  {
                  CodeBlob caller = (CodeBlob) j.next();
                  if (caller != null  &&
                    definedCode.get(caller.getNodeName()) != null) {
                    caller.calledCode.remove(c.getNodeName());
                    caller.calledCode.put (c.getNodeName(), null);
                  }
                }
                c.callingCode.clear();
                // System.out.println ("Able to find undefined: " + ipNm);
                break;
              }
            }
          }
        }
        if (definedCode.get (c.getNodeName()) == c  &&
          c.accuracy == AnalysisLevel.Null)
          System.out.println ("XXX Undefined: " + c.getNodeName());
    };
    ****/
    public static void inheritFromDefinedCode ()
        throws java.lang.Exception {
        for (Iterator i = definedCode.values().iterator();  i.hasNext();  )  {
            CodeBlob c = (CodeBlob) i.next();
            if (c != null  &&  c.accuracy == AnalysisLevel.Null)  {
                String clsNm = c.nodeName.className().name();
                String rest  = c.nodeName.dropClassNmGetRest();
                CodeBlob pc = null, ac = null;
                for (ClassInfo ci = null;  clsNm != null;
                     clsNm = ((ci = (ClassInfo)classes.get(clsNm)) == null) ?
                             null : ci.parentType) {
                     String bspecNm = clsNm +"."+ rest;
                     if (notDefinedCode.contains (bspecNm)) {
                         assert (    definedCode.get(bspecNm) == null  &&
                                 tobeDefinedCode.get(bspecNm) == null);
                         notDefinedCode.add (c.getNodeName());
                         assert (c == (CodeBlob) definedCode.remove (c.getNodeName()));
                         for (Iterator j = c.callingCode.values().iterator();  j.hasNext();  )  {
                           CodeBlob caller = (CodeBlob) j.next();
                           if (caller != null  &&  definedCode.get(caller.getNodeName()) != null)
                             caller.calledCode.remove(c.getNodeName());
                         }
                         c.callingCode.clear();
                         break;
                     } else if (tobeDefinedCode.get (bspecNm) != null) {
                         assert (     definedCode.get(bspecNm) == null  &&
                                 ! notDefinedCode.contains(bspecNm));
                         tobeDefinedCode.put (c.getNodeName(), new ConcurrentHashMap());
                         assert (c == (CodeBlob) definedCode.remove (c.getNodeName()));
                         for (Iterator j = c.callingCode.values().iterator();  j.hasNext();  )  {
                           CodeBlob caller = (CodeBlob) j.next();
                           if (caller != null  &&
                             definedCode.get(caller.getNodeName()) != null) {
                             caller.calledCode.remove(c.getNodeName());
                             caller.calledCode.put (c.getNodeName(), null);
                           }
                         }
                         c.callingCode.clear();
                         break;
                     } else if ((pc = (CodeBlob) definedCode.get(bspecNm)) != null)
                         if (pc.accuracy != AnalysisLevel.Null) {
                             c.upgradeToCTTAnalysis (pc);
                         break;
                     }
                }
            }
            if (definedCode.get (c.getNodeName()) == c  &&
              c.accuracy == AnalysisLevel.Null)  {
              String clsNm = c.nodeName.className().name();
              String rest  = c.nodeName.dropClassNmGetRest();
              ClassInfo ci = (ClassInfo) classes.get (clsNm);
              List ii = ci != null ? ci.getInterfacesImplemented()
                                   : getInterfacesExtendedBy (clsNm);
              // System.out.println ("Looking for undefined: " + c.getNodeName());
              if (ii != null  &&  ii.size() > 0) {
                for (int k = 0;  k < ii.size();  k++) {
                  String ipNm = null;
                  ipNm = (String) ii.get(k) + "." + rest;
                  // System.out.println ("Checking: " + ipNm + " against interface: " + ii.get(k));
                  if (tobeDefinedCode.get (ipNm) != null) {
                    assert (     definedCode.get(ipNm) == null  &&
                            ! notDefinedCode.contains(ipNm));
                    tobeDefinedCode.put (c.getNodeName(), new ConcurrentHashMap());
                    assert (c == (CodeBlob) definedCode.remove (c.getNodeName()));
                    for (Iterator j = c.callingCode.values().iterator();  j.hasNext();  )  {
                      CodeBlob caller = (CodeBlob) j.next();
                      if (caller != null  &&
                        definedCode.get(caller.getNodeName()) != null) {
                        caller.calledCode.remove(c.getNodeName());
                        caller.calledCode.put (c.getNodeName(), null);
                      }
                    }
                    c.callingCode.clear();
                    // System.out.println ("Able to find undefined: " + ipNm);
                    break;
                  }
                }
              }
            }
            if (definedCode.get (c.getNodeName()) == c  &&
              c.accuracy == AnalysisLevel.Null)
              System.out.println ("XXX Undefined: " + c.getNodeName());
            else  if (definedCode.get (c.getNodeName()) == null)
              System.out.println ("XXX Removed from Defined Code: " + c.getNodeName());
        }
    };

    public static CodeBlob getCodeOrVarientBlob (String rcms, String rms) {
        CodeBlob rv = null;
        if (rcms.indexOf(".\"<init>\":") > 0  ||  rcms.equals(rms))
            rv = (CodeBlob) definedCode.get (rcms);
        else
            rv = (CodeBlob) varients.get (rms +"-:varientOf:-"+ rcms);
        if (rv == null) System.err.println ("ZZZ: Searched for: " + rcms + ":" +
                                             rms + "\n\tBut returned Null");
        return rv;
    };
    //public static void cleanseDefinedCode ()
    //    throws java.lang.Exception {
    //    for (Iterator i = definedCode.values().iterator();  i.hasNext();  )  {
    //        CodeBlob c = (CodeBlob) i.next();
    //        if (c != null  &&  c.accuracy == AnalysisLevel.Null)  {
    //            i.remove ();
    //            if (noteVarientAndGetBehaviourSpec (c) == null)
    //                System.out.println ("XXX Deleted undefined-Node: " +
    //                                    c.getNodeName());
    //        }
    //    }
    //};
    //            if (arg instanceof CodeBlob)
    //                if ((ac = (CodeBlob) pc.varients.get (cnns)) != null) {
    //                    ((CodeBlob)arg).setRightCallerNdCalledNodes (cnns, ac);
    //                    ac.calledCode .putAll (((CodeBlob)arg).calledCode);
    //                    ac.callingCode.putAll (((CodeBlob)arg).callingCode);
    //                    assert (ac.behaviourSpec == pc); //.nodeName.signature());
    //                } else {
    //                    ((CodeBlob)arg).behaviourSpec = pc; // .nodeName.signature();
    //                    pc.varients.put (cnns, arg);
    //                }

    public static CodeBlob getBspecForIVcall (MethodSignature ms)  { // Interface or Virtual onle !!
        // null-return implies that the behaviour provider is a native method ...
        String cN = ms.className().name();
        if (interfaces.keySet().contains(cN)  ||
            tobeDefinedCode.get(ms.signature()) != null)
            return null; // ... or an interface method -- not to some implementing class !!
        String rest = ms.dropClassNmGetRest();
        CodeBlob pc = null, ac = null;
        for (ClassInfo ci = null;  cN != null;
             cN = ((ci = (ClassInfo)classes.get(cN)) == null) ? null : ci.parentType) {
            String lookinFor = cN +"."+ rest; 
            if ((pc = (CodeBlob) definedCode.get (lookinFor)) != null)
                return pc;
            else if (notDefinedCode.contains (lookinFor))
                return null;  // Behaviour provider is a native method !!
            else if (tobeDefinedCode.get (lookinFor) != null) {
                System.out.println ("Binding against Abstract Method: " + lookinFor);
                return null;
            }
        }
        System.out.println ("Behaviour Spec missing: " + ms.signature());
        return null;     // this makes the compiler not-to-complain.
    }
    public Set collectPublicInvokers (int countDown)
        throws java.lang.Exception {
        Set rv = new HashSet();
        rv.add (this);
        if (countDown == 0  ||  callingCode == null)
            return rv;
        for (Iterator i = callingCode.values().iterator();  i.hasNext();  )  {
            CodeBlob c = (CodeBlob) i.next();
            if (c != null)  {
                if (c.isPublic  &&  c.isConcrete())
                    rv.add (c);
                else if (c.getLockType() == null)  {
                    Set temp = c.collectPublicInvokers(countDown - 1);
                    if (temp != null  &&  temp.size() > 0)
                        rv.addAll (temp);
                }
            }
        }
        return rv;
    }
    public CodeBlob upgradeToRTTAnalysis ()
        throws java.lang.Exception {
        if (behaviourSpec == null  &&  definedCode.get(getNodeName()) == this) {
            upgradeToRTTAnalysis (this);
            return null;
        }
        if (behaviourSpec == null)
            System.out.println ("Howcome behaviourSpec is null: " + getNodeName());
        assert ((behaviourSpec == null)  ||  (behaviourSpec instanceof CodeBlob));
        //assert (behaviourSpec != null  &&
        //        behaviourSpec.accuracy != AnalysisLevel.Null);
        // if (pcb.accuracy != AnalysisLevel.Null)
            upgradeToRTTAnalysis (behaviourSpec);
        return this;
        /* String bspec = //(behaviourSpec instanceof CodeBlob  ?
                   ((CodeBlob)behaviourSpec).getNodeName();// : (String) behaviourSpec);
        String rest  = bspec.substring (bspec.lastIndexOf("."));
        String clsNm = getContainerType().name();
        for (ClassInfo ci = null;  clsNm != null;
             clsNm = ((ci = (ClassInfo)classes.get(clsNm)) == null) ?
              null : ci.parentType) {
            CodeBlob pcb = (CodeBlob) definedCode.get (clsNm + rest);
            if (pcb != null) {
                if (pcb.getNodeName().equals (getNodeName())) {
                    setRightCallerNdCalledNodes (getNodeName(), pcb);
                    pcb.calledCode .putAll (calledCode);
                    pcb.callingCode.putAll (callingCode);
                    if (pcb.accuracy != AnalysisLevel.RunTimeType)
                        pcb.upgradeToRTTAnalysis (pcb);
                    return pcb;
                } else {
                }
            }
        }
        return null;*/
    };

    public void setRightCallerNdCalledNodes (String name, CodeBlob node) {
        for (Iterator i = calledCode.entrySet().iterator();  i.hasNext();  ) {
            Map.Entry j = (Map.Entry) i.next();
            if (((CodeBlob)j.getValue()).getNodeName().equals (name))
                j.setValue (node);
        }
        for (Iterator i = callingCode.entrySet().iterator();  i.hasNext();  ) {
            Map.Entry j = (Map.Entry) i.next();
            if (((CodeBlob)j.getValue()).getNodeName().equals (name))
                j.setValue (node);
        }
    };

    public static int rttAnalysisCount = 0, cttAnalysisCount = 0;
    abstract public void upgradeToRTTAnalysis (CodeBlob spec)
        throws java.lang.Exception;
    abstract public void upgradeToCTTAnalysis (CodeBlob spec)
        throws java.lang.Exception;
    public void augmentReachableSCode (Set rv, Set workArea,
                                String firstLock, String callStack) 
        throws java.lang.Exception  {
        if (accuracy != AnalysisLevel.RunTimeType)
            upgradeToRTTAnalysis();
        if (calledCode == null)
            return;
        int ti = 2;
        for (Iterator j = calledCode.entrySet().iterator();  j.hasNext();  ti = 2)  {
            Map.Entry a = (Map.Entry) j.next();
            CodeBlob  b = (CodeBlob)  a.getValue();
            if (b == null)  continue;
            if (b.accuracy != AnalysisLevel.RunTimeType)  {
                CodeBlob c = b.upgradeToRTTAnalysis();
                if (c != null  &&  b != c)
                    a.setValue (b = c);
            }
            if (firstLock != null  &&  firstLock.length() == 0) {
                if (workArea.add (b.getNodeName()))
                    b.augmentReachableSCode (rv, workArea,
                        b.lockType == null ? firstLock : b.lockType,
                        callStack +"-"+ b.getReportableName((String)a.getKey()));           
            } else if (firstLock != null) {
                if (b.lockType != null  &&  !b.lockType.equals(firstLock)) {
                    if (ti > 0)  ti--;  else  continue;
                } else if (workArea.add (b.getNodeName()))
                    b.augmentReachableSCode (rv, workArea,
                        (b.lockType!=null && b.lockType.equals(firstLock)) ?  null : firstLock,
                        callStack +"-"+ b.getReportableName((String)a.getKey()));
            }  else  {
                if (b.lockType != null  &&  !b.lockType.equals ("java.lang.Object"))
                    rv.add (callStack +"-"+ b.lockType +"-"+ b.getReportableName(null));
                else if (workArea.add (b.getNodeName()))
                    b.augmentReachableSCode (rv, workArea, null, callStack +"-"+
                        b.getReportableName((String)a.getKey()));
            }
        }
    }
    public String getReportableName(String key) {
        return (getLockType() != null ? ("<" + getLockType() + ">") : "") +
               getNodeName() + (rtReturnTypes == null ? "" : "=M=") +
               (key!=null && !getNodeName().equals(key) && behaviourSpec!=null &&
                key.equals(behaviourSpec.getNodeName()) ? ("=THROandVarientOf="+key) : 
                (((key==null || getNodeName().equals(key)) ? "" : ("=THRO=" + key)) +
                 ((behaviourSpec == null) ? "" : ("=VarientOf=" + behaviourSpec.getNodeName()))));
    };
    
    public static String getVarientCbName (String mSign,
        String oType, String[] args) {
        int   colon = mSign.indexOf   (':');    assert (colon >= 0);
        String fsmN = mSign.substring (0, colon);
        String  prt = mSign.substring (colon + 1);
        String   mN = fsmN .substring (fsmN.lastIndexOf('.') + 1);
        String retT = prt  .substring (prt.lastIndexOf (')') + 1);
        String retV = oType + "." + mN + ":(";
        for (int i = 0; i < args.length; i++)
            retV += args[i];
        return retV + ")" + retT;
    };
    public static boolean isOkToCreate (String mSign) {
        return definedCode.get(mSign)==null   && !notDefinedCode.contains(mSign) &&
               tobeDefinedCode.get(mSign)==null;
    };
    public static void nativeOrAbstract (String ms, boolean isNat) {
        if ((isNat && notDefinedCode.contains(ms))  ||
            tobeDefinedCode.get(ms) != null)
            return;
        if (isNat)     notDefinedCode.add (ms);
        else       {  tobeDefinedCode.put (ms, new ConcurrentHashMap());
            System.out.println ("Inserted into tobeDefinedCode: " + ms); }
        CodeBlob rmv = (CodeBlob) definedCode.remove (ms);
        if (rmv != null)  {
            for (Iterator i = rmv.callingCode.values().iterator();  i.hasNext();  )  {
                 CodeBlob caller = (CodeBlob) i.next();
                 if (i != null  &&  definedCode.get(caller.getNodeName()) != null)
                    caller.calledCode.remove(ms);
            }
            rmv.callingCode.clear();
        }
    };

    public static boolean ensureField (String cn, String fn, boolean isStatic, String ctt) {
        if (ctt.length() == 1  &&  TypeSignature.basicEncoded.contains (ctt))
            return false;
        assert ((ctt.startsWith("L") || ctt.startsWith("["))  &&  ctt.indexOf('.') < 0);
        ClassInfo objT = (ClassInfo) classes.get (cn);
        if (objT == null)
            classes.put (cn, objT = new ClassInfo(cn, null));
        String attT = (String) (isStatic  ?  objT.classAttributes.get(fn)
                                          :  objT.instanceAttributes.get(fn));
        if (attT == null)
            if (isStatic)   objT.classAttributes.put(fn, ctt);
            else            objT.instanceAttributes.put(fn, ctt);
        return true;
    };
    public static void collectStartPoints () // BUP
        throws java.lang.Exception {
        for (Iterator i = sCode.values().iterator();  i.hasNext();  )  {
            CodeBlob n = (CodeBlob) i.next();
            // if (!n.isPublic  ||  !n.isConcrete())
                n.concPublicInvokers = n.collectPublicInvokers(fanout);
        }
    };
    public static List getInterfacesExtendedBy (String iNm) {
        List rv = new java.util.ArrayList ();
        rv.add (iNm);
        for (int i = 0;  i < rv.size();  i++)  {
            String   ii = (String) rv.get(i);
            String[] ie = null;
            if ((ie = (String[]) CodeBlob.interfaces.get(ii)) != null)
                for (int j = 0;  j < ie.length;  j++)
                    rv.add (ie[j]);
        }            
        return rv;
    };

    public static void countStartPoints ()  throws java.lang.Exception {
        int retval = 0;
        for (Iterator i = definedCode.values().iterator();  i.hasNext();  )  {
            CodeBlob n = (CodeBlob) i.next();
            if (n != null  &&  (n.isPublic  &&  n.isConcrete()))
                retval ++;
        }
        System.out.println ("Public Concrete StartPoints = " + retval);
    };
    public static void reportSubclassingNdImplements ()  throws java.lang.Exception {
        HashSet cS = null, iS = null;
        String  cN = null, iN = null;
        int subClassingIncidence = 0, interfaceImplementationIncidence = 0;
        for (Iterator i = classes.keySet().iterator();  i.hasNext();  )  {
            cN = (String) i.next();
            cS = new HashSet(); cS.add (cN);
            iS = new HashSet();
            for (ClassInfo ci = null;  cN != null;
                 cN = ((ci = (ClassInfo)classes.get(cN)) == null) ?
                  null : ci.parentType)  {
                if (cN != null) cS.add (cN);
                if (ci != null) iS.addAll (ci.getInterfacesImplemented ());
            }
            for (Iterator k = iS.iterator();  k.hasNext();  ) {
                iN = (String) k.next();
                iS.addAll (getInterfacesExtendedBy (iN));
            }
            subClassingIncidence += (cS.size()-1);
            interfaceImplementationIncidence += iS.size();
        }
        System.out.println ("Incidence of Subclassing: " + subClassingIncidence);
        System.out.println ("Incidence of Interface Implementation: " + interfaceImplementationIncidence);
    };

    //public static void collectStartPointsTDN ()  throws java.lang.Exception {
    //    for (Iterator i = definedCode.values().iterator();  i.hasNext();  )  {
    //        CodeBlob n = (CodeBlob) i.next();
    //        if (n != null  &&  (n.isPublic  ||  n.isConcrete()))
    //            retval ++;
    //        if (!n.isPublic  ||  !n.isConcrete())
    //            n.concPublicInvokers = n.collectPublicInvokers(fanout);
    //    }
    //};
    //static public void interpretSynchNdVarients ()
    //    throws java.lang.Exception {
    //    for (Iterator i = sCode.entrySet().iterator();  i.hasNext();  )  {
    //        Map.Entry sMe = (Map.Entry)  i.next();
    //        CodeBlob  sM  = (CodeBlob) sMe.getValue();
    //        assert (sM != null  &&  sM.getLockType() != null);
    //        if (sM.nodeName.signature().indexOf('#') > 0)
    //            continue;
    //        if (sM.accuracy != AnalysisLevel.RunTimeType) {
    //            CodeBlob mSm = sM.upgradeToRTTAnalysis ();
    //            if (mSm != null  &&  mSm != sM)
    //                sMe.setValue (mSm);
    //        }
    //        if (sM.varients == null  ||  sM.varients.size() == 0)
    //            continue;
    //        for (Iterator j = sM.varients.entrySet().iterator();  j.hasNext();  )  {
    //            Map.Entry sVp = (Map.Entry) j.next();
    //            CodeBlob sV = (CodeBlob) sVp.getValue();
    //            assert (sV.behaviourSpec != null);
    //            System.out.println ("Requesting Upgrade:\nBase NodeName: " +
    //                sM.getNodeName() + "\nVarient  Name: " + sV.getNodeName());
    //            CodeBlob cV = sV.upgradeToRTTAnalysis ();
    //            if (cV != sV  &&  cV != null)
    //                sVp.setValue (cV);
    //        }
    //    }
    //}

    //static public void interpretInterfaceVarients ()  throws java.lang.Exception {
    //    for (Iterator i = tobeDefinedCode.entrySet().iterator();  i.hasNext();  )  {
    //        Map.Entry        iiM  = (Map.Entry)         i.next();
    //        ConcurrentHashMap iM  = (ConcurrentHashMap) iiM.getValue();
    //        assert (iM != null);
    //        if (iM.size() == 0)    continue;
    //        System.out.println ("Interface: " + ((String)iiM.getKey()));
    //        for (Iterator j = iM.entrySet().iterator();  j.hasNext();  )  {
    //            Map.Entry impe = (Map.Entry) j.next();
    //            CodeBlob imp = (CodeBlob) impe.getValue();
    //            assert (imp != null  &&  imp.behaviourSpec != null);
    //            System.out.println ("Requesting Interpretation:\nEntry-Key: " +
    //                impe.getKey() + "\nRT-Method: " + imp.getNodeName());
    //            CodeBlob cV = imp.upgradeToRTTAnalysis ();
    //            if (cV != imp  &&  cV != null)
    //                System.out.println ("Creating disconnect");
    //                // XXX: this is not completely done, yet??
    //        }
    //
    //    }
    //}
    public static void sCodeReachedFromSCode () 
        throws java.lang.Exception {
        int rv = 0, rv1 = 0, rv2 = 0;
        HashSet avoidRepeats = new HashSet();
        for (Iterator i = sCode.values().iterator();  i.hasNext();  )  {
            CodeBlob mB = (CodeBlob) i.next();
            assert (mB != null);
            mB.concPublicInvokers = mB.collectPublicInvokers(fanout);
            if (mB.concPublicInvokers != null) {
                rv++;
                // rv1 += mB.concPublicInvokers.size();
                for (Iterator j = mB.concPublicInvokers.iterator();  j.hasNext();  )  {
                    CodeBlob spcB = (CodeBlob) j.next();
                    if (! avoidRepeats.add (spcB.getReportableName(null)))
                        continue;
                    rv1 ++;
                    HashSet workArea = new HashSet();
                    workArea.add (spcB.getNodeName());
                    spcB.augmentReachableSCode (mB.sEdges = new HashSet(),
                           workArea, mB.getLockType(), spcB.getReportableName(null));
                    rv2 += mB.sEdges.size();
                }
            }
        }
        System.out.println ("Total number of Synchronized methods investigated = " + rv);
        System.out.println ("Total Concurrent public invokers investigated = " + rv1);
        System.out.println ("Total sEdges discovered  = " + rv2);
    }
    public static void computeTransitiveClosureReachableFromDefinedPublicConcreteEntries ()
                  throws java.lang.Exception {
      for (Iterator i = definedCode.values().iterator();  i.hasNext();  )  {
            CodeBlob cb = (CodeBlob) i.next();
            if (cb != null   &&  cb.accuracy == AnalysisLevel.CompileTimeType  &&
                cb.isPublic  &&  cb.isConcrete())
                cb.upgradeToRTTAnalysis();
            if (cb.calledCode != null)
                cb.recursiveDescentUpgradation();
      }
    };
    public void recursiveDescentUpgradation () throws java.lang.Exception {
        for (Iterator j = calledCode.entrySet().iterator();  j.hasNext();  )  {
            Map.Entry a = (Map.Entry) j.next();
            CodeBlob  b = (CodeBlob)  a.getValue();
            if (b != null  &&  b.accuracy != AnalysisLevel.RunTimeType)  {
                b.upgradeToRTTAnalysis();
                b.recursiveDescentUpgradation();
            }
        }
    }

    public static int abstrctIntrfceVarient () { //returns count
        int retval = 0;
        for (Iterator i = tobeDefinedCode.values().iterator();  i.hasNext();  )  {
            ConcurrentHashMap hm = (ConcurrentHashMap) i.next();
            if (hm != null)
                retval += hm.size();
        }
        return retval;
    }
    public static int synchVarientCount () {
        int retval = 0;
        for (Iterator i = varients.values().iterator();  i.hasNext();  )  {
            CodeBlob cb = (CodeBlob) i.next();
            if (cb != null  &&  cb.lockType != null)
                retval ++;
        }
        return retval;
    }
    
}