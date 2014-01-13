/* CodeBlob.java -- A Node in the Concurrency-Conflict Graph
 *
 * author: Vivek K. Shanbhag, Philips Research, India, Bangalore.
 */

import java.util.*;
import java.io.*;

public class CodeBlob {
    String  lockType;
           // Stores the type of object that is synchronised upon:
           //  Null ==> Object represents an 'un-Synchronised' code-blob
           //  Add a ".class" postfix for static methods after package-name.class-name
           //  Necessariely maps into the locks hashMap and updates it whenever set/reset.

    String  fullName;   // "package-name/class-name.method-name:signature"
    boolean isPublic;   // Is this a publicly accessable method ?
    int  inCount;       // Counts the number of references to this node, through the code-base.

    static int natives = 0;     // counts native methods noticed.

    HashSet calledCodeBlobs;    // references "called" code-blobs.
    HashSet alternateCodeBlobs; // references "alternate" code-blobs that 'could' handle the call.

    static HashSet          interfaces  = new HashSet (); // lists all interfaces seen.
    static HashMap               locks  = new HashMap (); // Maps locks->codeBlobs that block on it
    static HashMap      inheritanceMap  = new HashMap ();
    static HashMap   exploredCodeBlobs  = new HashMap (); // Code-blobs seen.
    static HashMap unexploredCodeBlobs  = new HashMap (); // Code-blobs refered in those seen.
    static Set         abstractClasses  = new HashSet (); // Collects names of Abstract classes
    static Set     ccgNodes             = new HashSet (); // The Concurrency-conflict-graph.
           Set     ccgEdges;

    CodeBlob (String fsClassName, String methodName,
        String signature, boolean isPub, boolean isStatic, boolean isSynch, boolean isNative)  {
        fullName = fsClassName + "." + methodName + ":" + signature;
        setLockType (isSynch ? (fsClassName + (isStatic ? ".class" : "")) : null);
        isPublic = isPub;    calledCodeBlobs    = null;
        ccgEdges = null;     alternateCodeBlobs = null;        inCount = 0;
        natives += (isNative ? 1 : 0);
        // System.out.println ("New xplored Node: " + fullName);
    }
    public static CodeBlob newCodeBlob (String fsClassNm, String mNm,
        String prtEncoding, boolean isPub, boolean isStatic, boolean isSynch, boolean isNative)  {
        CodeBlob rv = (CodeBlob) unexploredCodeBlobs.get(fsClassNm +"."+ mNm +":"+ prtEncoding);
        if (rv == null)
            rv = new CodeBlob(fsClassNm, mNm, prtEncoding, isPub, isStatic, isSynch, isNative);
        else  {
            rv.setLockType (isSynch ? (fsClassNm + (isStatic ? ".class" : "")) : null);
            rv.isPublic = isPub;
            natives += (isNative ? 1 : 0);
            unexploredCodeBlobs.remove (rv.fullName);
        }
        exploredCodeBlobs.put (rv.fullName, rv);
        if (rv.getLockType() != null)
            ccgNodes.add (rv);
        return rv;
    }
    public CodeBlob noteCallTo (String fsMethodSignature)  {
        if (fsMethodSignature != null  &&  fullName.equals(fsMethodSignature))
            return null;
        if (fsMethodSignature == null)
            fsMethodSignature = getUniqueName();
        CodeBlob rv = (CodeBlob) exploredCodeBlobs.get (fsMethodSignature);
        if (rv==null  &&  (rv = (CodeBlob) unexploredCodeBlobs.get (fsMethodSignature))==null)
            unexploredCodeBlobs.put (fsMethodSignature, rv = new CodeBlob(fsMethodSignature));
        if (calledCodeBlobs == null)
            calledCodeBlobs = new HashSet ();
        calledCodeBlobs.add (rv);
        return rv;
    }
    protected CodeBlob (String fullName)  {
        this.fullName      = fullName;    setLockType(null);  isPublic = false;
        calledCodeBlobs    = null;        ccgEdges = null;    alternateCodeBlobs = null;
        // System.out.println ("New un-xplored Node: " + fullName);
    };
    static int j = 0;
    protected static String getUniqueName ()  {  return "temp" + j++;  }
    public void setLockType (String lock)  {
        setLockType (lock, false);
    }
    public String getLockType ()  {    return lockType;       }
    public void setLockType (String lType, boolean isStatic)  {
        String old    = getLockType();
        lockType = lType==null ? null : (lType + (isStatic ? ".class" : ""));
        if (old != null)
            ((HashSet)locks.get(old)).remove (this);
        if (getLockType() != null)
             if (locks.containsKey(getLockType()))
                 ((HashSet)locks.get(getLockType())).add(this);
             else  {
                 HashSet why = new HashSet();
                 why.add (this);
                 locks.put(getLockType(), why);
             }
             if (old == null  &&  getLockType() != null)  ccgNodes.add (this);
        else if (old != null  &&  getLockType() == null)  ccgNodes.remove (this);
    }
    public static void defineReferencedInheritedFunctions ()  {
        HashSet removables = new HashSet ();
        for (Iterator i = unexploredCodeBlobs.keySet().iterator();  i.hasNext();  )  {
            String sign, clNm, rest;
            sign = (String) i.next();
            if (sign == null  ||  sign.indexOf(".") < 0  ||  sign.indexOf(":") < 0)
                continue;

            clNm = sign.substring (0, sign.indexOf("."));
            rest = sign.substring (sign.indexOf("."));
            // System.out.println ("clNm = " + clNm + "\nrest = " + rest);
            for (String p = (String) inheritanceMap.get(clNm);  p!=null;
                        p = (String) inheritanceMap.get(p))  {
                CodeBlob j, k;
                if ((j = (CodeBlob) exploredCodeBlobs.get (p + rest)) != null)  {
                    if ((k = (CodeBlob) unexploredCodeBlobs.get(sign)) != null)  {
                        k.inheritDefinition (j);
                        removables.add (sign);
                        exploredCodeBlobs.put (k.fullName, k);
                    }  else
                        System.out.println ("Found <" +sign+ ", null> in unexploredCodeBlobs!");
                    break;
                }
            }
        }
        if (! removables.isEmpty())
            for (Iterator i = removables.iterator();  i.hasNext();  )
                unexploredCodeBlobs.remove ((String) i.next());
        System.out.println ("Upgraded " + removables.size() + " nodes into explored-CodeBlobs.");
    }

    public void inheritDefinition (CodeBlob pM) {
        // System.out.println ("Creating " + fullName + " from " + pM.fullName);
        setLockType (pM.getLockType());
        isPublic = pM.isPublic;
        calledCodeBlobs = pM.calledCodeBlobs == null  ?  null  :  new HashSet (pM.calledCodeBlobs);
        alternateCodeBlobs = pM.alternateCodeBlobs == null  ?  null  
                                                            :  new HashSet (pM.alternateCodeBlobs);
    }
    public static void SynchCodeReachedFromSynchCode ()  {
        for (Iterator i = ccgNodes.iterator();  i.hasNext();  )  {
            CodeBlob n = (CodeBlob) i.next(); 
            if (n.calledCodeBlobs != null)  {
                Set rv = new HashSet ();
                n.augmentReachableSynchNodeSet (rv, new HashSet(),
                                                n.getLockType() + " " + n.fullName);
                n.ccgEdges = rv;
            }
        }
    }

    void augmentReachableSynchNodeSet (Set rv, Set workArea, String callStack)  {
        if (calledCodeBlobs != null)
            for (Iterator j = calledCodeBlobs.iterator();  j.hasNext();  )  {
                CodeBlob m = (CodeBlob) j.next();
                if (m.getLockType() != null)
                    rv.add ((callStack + " " + m.fullName + " " + m.getLockType()).trim());
                else if (workArea.add (m))
                    m.augmentReachableSynchNodeSet (rv, workArea, callStack + " " + m.fullName);
                if (m.alternateCodeBlobs != null)
                    for (Iterator k = m.alternateCodeBlobs.iterator();  k.hasNext();  )  {
                        CodeBlob n = (CodeBlob) j.next();
                        if (n.getLockType() != null)
                            rv.add ((callStack + " " + n.fullName + " " + n.getLockType()).trim());
                        else if (workArea.add (n))
                            n.augmentReachableSynchNodeSet (rv, workArea,
                                                            callStack + " " + n.fullName);
                    }
            }
    }
    public static void dropUnsynchronizedCodeBlobs ()  {
        System.out.println ("Native method count (definitions not seen): " + natives + ".\n" +
            "Non-Synch method count: " + (exploredCodeBlobs.size() - ccgNodes.size()) + ".\n" +
            "Synch-Code-blobs count: " + ccgNodes.size() + ".");
        exploredCodeBlobs = null;
        System.out.println ("The following are undefined functions:");
        Object[] undefineds = unexploredCodeBlobs.values().toArray();
        int j = 0;
        for (int i = undefineds.length;  --i >= 0;  )
            if (! ((CodeBlob)undefineds[i]).fullName.startsWith("temp"))  {
                int    iDot = ((CodeBlob)undefineds[i]).fullName.indexOf(".");
                if (iDot < 0)  continue;
                String clNm = ((CodeBlob)undefineds[i]).fullName.substring (0, iDot);
                if (abstractClasses.contains (clNm))  continue;
                System.out.println ("undefined: " + ((CodeBlob)undefineds[i]).fullName);
                j++;
            }
        System.out.println ("Undefined method count: " + j + ".\n");
        unexploredCodeBlobs = null;
        System.gc();
    };
    public boolean canLockBeTheSame (CodeBlob x)  {
        if (x == null  ||  x.getLockType() == null  ||  getLockType() == null)
            return false;
        else if (x.getLockType().endsWith(".class")  &&  getLockType().endsWith(".class")  &&
                 getLockType().equals(x.getLockType()))
                 return true;
        else  {
            String a = getLockType(), b = x.getLockType(), t;
            if (a.endsWith(".class"))  a = a.substring(0, a.lastIndexOf("."));
            if (b.endsWith(".class"))  b = b.substring(0, b.lastIndexOf("."));
            for (t = a;  t != null;  t = (String) inheritanceMap.get(t))
                if (b.equals(t))   return true;
            for (t = b;  t != null;  t = (String) inheritanceMap.get(t))
                if (a.equals(t))   return true;
            return false;
        }
    };
    static TreeSet t12hc = new TreeSet(), lhcs = new TreeSet();
    public static void report2LockDeadlockingThreads ()  {
        HashSet donePairs = new HashSet();
        int    lhc;                   // lock  Hash-Code, where l1 is less than l2, always.
        TreeSet t1hc = new TreeSet(),
                t2hc = new TreeSet(); // Thread-1, and Thread-2 Hash-Codes, respectively.
        for (Iterator li = locks.keySet().iterator();  li.hasNext();  )  {
            String    l1 = (String)  li.next();
            HashSet cbl1 = (HashSet) locks.get(l1);
            for (Iterator lri = locks.keySet().iterator();  lri.hasNext();  )  {
                String    l2 = (String)  lri.next();
                HashSet cbl2 = (HashSet) locks.get(l2);
                if (l1.equals(l2)  ||  donePairs.contains (l1 +"-"+ l2)  ||
                    !reachableFromTo (l1, l2)  ||  !reachableFromTo (l2, l1))
                    continue;
                System.out.println ("<2-Cycle " + l1 +" "+ l2 + ">");
                lhc   =   l1.compareTo(l2) <= 0  ?  (l1 +"-"+ l2).hashCode()
                                                 :  (l2 +"-"+ l1).hashCode(); 
                for (Iterator cbi = cbl1.iterator();  cbi.hasNext();  )  {
                    CodeBlob cb = (CodeBlob) cbi.next();
                    if (cb.ccgEdges == null)  continue;
                    for (Iterator ccgEi = cb.ccgEdges.iterator();  ccgEi.hasNext();  )  {
                        String ccgE = (String) ccgEi.next();
                        int lb = ccgE.lastIndexOf (" ");
                        if (lb >= 0  &&  ccgE.substring(lb+1).equals(l2))  {
                            System.out.println ("    <Thread-1 Option>");
                            System.out.println (
                                ccgE.substring(ccgE.indexOf(" ")+1, lb).replace(' ', '\n'));
                            System.out.println ("    </Thread-1 Option>\n");
                            t1hc.add(new Integer(ccgE.substring(ccgE.indexOf(" ")+1, lb).hashCode()));
                        }
                    }
                }
                for (Iterator cbj = cbl2.iterator();  cbj.hasNext();  )  {
                    CodeBlob cb = (CodeBlob) cbj.next();
                    if (cb.ccgEdges == null)  continue;
                    for (Iterator ccgEj = cb.ccgEdges.iterator();  ccgEj.hasNext();  )  {
                        String ccgE = (String) ccgEj.next();
                        int lb = ccgE.lastIndexOf (" ");
                        if (lb >= 0  &&  ccgE.substring(lb+1).equals(l1))  {
                            System.out.println ("    <Thread-2 Option>");
                            System.out.println (
                                ccgE.substring(ccgE.indexOf(" ")+1, lb).replace(' ', '\n'));
                            System.out.println ("    </Thread-2 Option>\n");
                            t2hc.add(new Integer(ccgE.substring(ccgE.indexOf(" ")+1, lb).hashCode()));
                        }
                    }
                }
                System.out.println ("</2-Cycle " + l1 +" "+ l2 + ">\n\n");
                String t1s = "", t2s = "";
                for (Iterator i = t1hc.iterator();  i.hasNext();  )   t1s += "=" + i.next();
                for (Iterator j = t2hc.iterator();  j.hasNext();  )   t2s += "=" + j.next();
                System.out.println ("HC:" + lhc + "<--" + ((l1.compareTo(l2) <= 0) ? t1s : t2s));
                System.out.println ("HC:" + lhc + ">--" + ((l1.compareTo(l2) <= 0) ? t2s : t1s));
                lhcs.add (new Integer(lhc));
                t12hc.addAll (t1hc);  t12hc.addAll (t2hc);
                lhc = 0;  t1hc.clear();  t2hc.clear();  t1s = "";  t2s = "";
                donePairs.add (l1 +"-"+ l2);
                donePairs.add (l2 +"-"+ l1);
            }
        }
    }
    public static boolean reachableFromTo (String l1, String l2)  {
        HashSet cbl1 = (HashSet) locks.get(l1);
        for (Iterator cbi = cbl1.iterator();  cbi.hasNext();  )  {
            CodeBlob cb = (CodeBlob) cbi.next();
            if (cb.ccgEdges == null)  continue;
            for (Iterator ccgEi = cb.ccgEdges.iterator();  ccgEi.hasNext();  )  {
                 String ccgE = (String) ccgEi.next();
                 int lb = ccgE.lastIndexOf (" ");
                 if (lb >= 0  &&  ccgE.substring(lb+1).equals(l2)) return true;
            }
        }
        return false;
    }
    public static void findCycles (CodeBlob[] lNodes, Set oEdges, int cLen)  {
        if (lNodes == null)
            for (int i = 2;  i <= cLen;  i++)  {
                lNodes = new CodeBlob[i+1]; 
                for (Iterator ccgi = ccgNodes.iterator();  ccgi.hasNext();  )  {
                    lNodes[i] = (CodeBlob) ccgi.next();
                    if (lNodes[i].fullName.startsWith("temp"))    continue;
                    findCycles (lNodes, lNodes[i].ccgEdges, i-1);
                }
            }
        else if (oEdges != null) 
            for (Iterator ei = oEdges.iterator();  ei.hasNext();  )  {
                CodeBlob cb = (CodeBlob) ei.next();
                boolean embeddedCycle = false;
                for (int i = lNodes.length-1;  cLen>0 && i>cLen;  --i)
                     embeddedCycle = embeddedCycle || lNodes[i].canLockBeTheSame (cb);
                if (embeddedCycle)
                    continue;
                lNodes[cLen] = cb;
                if (cLen != 0)
                    findCycles (lNodes, cb.ccgEdges, cLen-1);
                else if (lNodes[lNodes.length-1].canLockBeTheSame (cb))
                    PrintCycle (lNodes);
            }
    };
    public static void PrintCycle (CodeBlob[] lNodes)  {
        int n = lNodes.length;
        System.out.println ("<" + (n-1) + "-cycle>");
        for (int t = n;  --t >= 0;  )
            System.out.println ("    " + lNodes[t].fullName + "\n\tlocks " +
                                         lNodes[t].getLockType() + " and calls ");
        System.out.println ("</" + (n-1) + "-cycle>\n\n");
    }
}