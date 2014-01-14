/* CodeBlob.java -- Lock Acquisition Graph Construction, Cycle detection / reporting
 *
 * author: Vivek K. Shanbhag, IIIT-B, Bangalore, India.
 */

package deadlockPrediction;

import java.util.*;
import java.io.*;

public abstract class CodeBlob {
    
    protected static HashMap   exploredCode = new HashMap(); // Methods-bodies seen
    protected static HashMap unexploredCode = new HashMap(); // unseen called methods
              static HashMap          sCode = new HashMap(); // Synchronized methods
    
    public static Set   nativeMethods = new HashSet(); // List  native methods
    public static Set abstractClasses = new HashSet(); // List abstract Classes
    public static Set abstractMethods = new HashSet(); // Map-Key: abstract mNm
    //public static Map      interfaces = new HashMap(); // Key: Interface name
    
    static int     synchronizedCBs = 0;  // count        synchronized statements
    static int staticSynchronizeds = 0;  // count static-synchronized methods
    static protected HashMap classes = new HashMap();
    /* The CodeBlob Class has this class-attribute that stores the
     *  ClassInfo object corresponding to every className encountered
     *  during parsing of the input.
     *  mapping of parent-child relationships. Every entry into this
     *  map is a <key, value> pair, where the key is the name of the
     *  sub-class, and the value is the name of the super-class. The
     *  two applications that sub-class from the CodeBlob class must
     *  populate this data, using calls like:
     *  class.put (subClass.intern(), new ClassInfo (superClass.intern()));
     */
    
    private String  lockType = null;
        // Stores the type of object that is synchronised upon:
        //  Null implies an 'un-Synchronised' code-blob
        //  A ".class" postfix for static methods after package-name/class-name
        //  TODO: Necessariely maps into the locks hashMap and updates it whenever set/reset.
        //  Map-Key: String "package-name/class-name.method-name:prtEncoding"
    public boolean isPublic = false;   // Is this a public method ?
    public boolean isStatic = false;   // Is this a static method ?
    protected HashMap calledCode  = new HashMap(),
                      callingCode = new HashMap();
    HashSet sEdges = null;
    
    public CodeBlob (String mSignature) {
        assert (  exploredCode.get(mSignature)==null  &&
                unexploredCode.get(mSignature)==null);
        assert (!  nativeMethods.contains(mSignature)  &&
                !abstractMethods.contains(mSignature));
        
        assert (mSignature.substring (0, mSignature.indexOf(':')).
                           indexOf ('/') <= 0);
        if (mSignature.indexOf ('#') > 0)
           System.err.println ("Hash mSignature: " + mSignature);
        assert (mSignature.indexOf ('#') < 0);
        // assert (mSignature.indexOf ("\"<init>\"") > 0   &&
        //         mSignature.endsWith(")V"));
        unexploredCode.put (mSignature, this);
    };
    public CodeBlob (String fsClassNm, String mNm,
        String prtEnc, boolean isStat, boolean isSynch, boolean isPub)  {
        String key  = (fsClassNm +"."+ mNm +":"+ prtEnc).intern();
        assert (fsClassNm.indexOf ('/') <= 0);
        assert (  exploredCode.get(key)==null  &&
                unexploredCode.get(key)==null);
        assert (!  nativeMethods.contains(key)  &&
                !abstractMethods.contains(key));
        
        isPublic = isPub;
        isStatic = isStat;
        if (isSynch)  {
            setLockType (key, fsClassNm + (isStatic ? ".class" : ""));
            sCode.put (key, this);
            if (isStatic)  staticSynchronizeds++;
        }
        
        exploredCode.put (key, this);
    }
    
    public void upgradeCB (String key,
        boolean isStat, boolean isSynch, boolean isPub)  {
        isPublic = isPub;
        isStatic = isStat;
        String fsClassNm = key.substring (0, key.indexOf(":")).
                               substring (0, key.lastIndexOf("."));
        assert (fsClassNm.indexOf ('/') <= 0);
        if (isSynch)  {
            setLockType (key, fsClassNm + (isStatic ? ".class" : ""));
            sCode.put (key, this);
            if (isStatic)  staticSynchronizeds++;
        }
        
        exploredCode.put (key, this);
    }
    public void noteCallTo (String caller,
                                                           String called, CodeBlob calledBlob)  {
        called = called.intern();
        if (calledCode.keySet().contains(called) || // Already recorded.
                  nativeMethods.contains(called))   // No point recording this.
            return;
        if (! abstractMethods.contains(called))
            calledBlob.callingCode.put (caller.intern(), this);
        calledCode.put (called, calledBlob);
    }
    public CodeBlob (String p, String c, String lT)  {
        System.out.println ("Creating: " + c); //Debug comment
        setLockType (c, lT);
        noteCallTo (p, c, this);
        sCode.put (c, this);
        synchronizedCBs++;
    }
    /* A public method to set the value for a private attribute
     *  Below is defined 'setLockType' that asserts that the lockType attribute
     *  value is null, and that the value to be assigned to it is non-null.
     *  This assertion enforces the programming discipline that the value
     *  of the private attribute can be set at-most once. The global
     *  initialiser already sets it to null. Only for nodes where it is
     *  discovered later as being 'synchronized', can this method be
     *  invoked, that too only once.
     */
    public void setLockType (String myMn, String lock)  {
        assert (lockType == null  &&  lock != null);
        assert (!lock.equals ("com")  &&  !lock.equals("java")  &&
                !lock.equals ("sun")  &&  !lock.equals("javax"));
        lockType = lock;
        OTlock rv = (OTlock)OTlock.locks.get(lock);
        if (rv == null)
            OTlock.locks.put(lock, rv = new OTlock());
        rv.lContenders.add (myMn);
    }
    public String getLockType () {
        return lockType;
    }
    public abstract void processMethodBody (String codeBlobName)
                    throws java.lang.Exception;

    public void removeFromUnexploredCode (String key) {
        for (Iterator i = callingCode.keySet().iterator();  i.hasNext();  )  {
             String caller = (String) i.next();
             if (exploredCode.get(caller) != null)
                ((CodeBlob)exploredCode.get(caller)).calledCode.put (key, null);
        }
        callingCode.clear();
    }

    
    public static void reportOverridingIncidence ()  {
        Set overridden = new HashSet ();
        Set overriding = new HashSet ();
        int synchOverridden = 0, overridingSynch = 0, synchOverridesSynch = 0;
        for (Iterator i = exploredCode.keySet().iterator();  i.hasNext();  )  {
            String clNm, rest, sign = (String) i.next();
            if (sign == null  ||  sign.indexOf(".") < 0  ||  sign.indexOf(":") < 0)
                continue;
            clNm = sign.substring (0, sign.lastIndexOf("."));
            rest = sign.substring (sign.lastIndexOf("."));
            if (rest.startsWith("\"<init>\""))
                continue;
            for (ClassInfo ci  = (ClassInfo) classes.get(clNm);
                           ci != null && ci.parentName != null; // ci.parentName is parent
                           ci  = (ClassInfo) classes.get(ci.parentName))  {
                CodeBlob pc, cc;   // pc: parent code; cc: child code
                if ((pc = (CodeBlob) exploredCode.get (ci.parentName + rest)) != null)  {
                    overridden.add (ci.parentName+rest);
                    overriding.add (sign);
                    if (pc.getLockType() != null) synchOverridden++;
                    if ((cc = (CodeBlob) exploredCode.get(sign)) != null  &&
                        cc.getLockType() != null) overridingSynch++;
                    if (pc.getLockType() != null  &&
                        cc.getLockType() != null) synchOverridesSynch++;
                    break;
                }
            }
        }
        System.out.println (overriding.size() + " methods, of which " + overridingSynch + " are synchronized,");
        System.out.println ("\toverride or redefine");
        System.out.println (overridden.size() + " methods, of which " + synchOverridden + " are synchronized,");
        System.out.println (synchOverridesSynch + " synchronized methods, override synchronized methods.");
        overriding.clear();
        overridden.clear();
    }
    public static void inheritNecessaryParentMethods ()  {
        reportOverridingIncidence ();
        Set inheritedMethods = new HashSet ();
        for (Iterator i = unexploredCode.keySet().iterator();  i.hasNext();  )  {
            String clNm, rest, sign = (String) i.next();
            if (sign == null  ||  sign.indexOf(".") < 0  ||  sign.indexOf(":") < 0)
                continue;

            clNm = sign.substring (0, sign.lastIndexOf("."));
            rest = sign.substring (sign.lastIndexOf("."));
            // System.out.println ("clNm = " + clNm + "\nrest = " + rest);
            for (ClassInfo ci  = (ClassInfo) classes.get(clNm);
                           ci != null && ci.parentName != null; // ci.parentName is parent
                           ci  = (ClassInfo) classes.get(ci.parentName))  {
                CodeBlob pc, cc;   // pc: parent code; cc: child code
                if ((pc = (CodeBlob) exploredCode.get (ci.parentName + rest)) != null)  {
                    inheritParentMethod (sign, ci.parentName+rest, pc);
                    inheritedMethods.add (sign);
                    break;
                }
            }
        }
        System.out.println ("Inherited " + inheritedMethods.size() + " parent-methods.");
        for (Iterator j = inheritedMethods.iterator();  j.hasNext();  )
            unexploredCode.remove ((String) j.next());
        inheritedMethods.clear();
    }
    static public void
    inheritParentMethod (String cMnm, String pMnm, CodeBlob pM) {
        // System.out.println ("Creating " + cMnm + " from " + pMnm);
        CodeBlob cM = (CodeBlob) unexploredCode.get (cMnm);
        assert (cM != null);
        if (pM.lockType != null)  {
            cM.isPublic  = pM.isPublic;
            cM.isStatic  = pM.isStatic;
            cM.setLockType (cMnm, (cMnm.substring(0, cMnm.lastIndexOf('.')) +
                (pM.lockType.endsWith(".class")  ?  ".class"  :  "")).intern());
            cM.calledCode.putAll (pM.calledCode);
            exploredCode.put (cMnm, cM);
            sCode.put (cMnm, cM);
        }  else  {
            for (Iterator i = cM.callingCode.keySet().iterator();  i.hasNext();  )  {
                String caller = (String) i.next();
                if (exploredCode.get(caller) != null)
                    ((CodeBlob)exploredCode.get(caller)).calledCode.put (cMnm, pM);
            }
            // pM.callingCode.putAll (cM.callingCode); Unnecessary, isnt it!
            exploredCode.put (cMnm, pM);
        }
    }
    public static void sCodeReachedFromSCode ()  {
        System.out.println ("  Defined-method count: \t" +   exploredCode.size());
        System.out.println ("Undefined-method count: \t" + unexploredCode.size());
        System.out.println ("Native method count (definitions not seen): " +
                            nativeMethods.size() + "\nNon-Synch method count: " +
                            (exploredCode.size() - sCode.size() - synchronizedCBs));
        System.out.println ("Abstract-method count: \t" +  abstractMethods.size());
        System.out.println ("         Class  count: \t" + classes.keySet().size());
        System.out.println ("Abstract-Class  count: \t" +  abstractClasses.size());
        System.out.println ("\nNon-Synch method count: " +
                            (exploredCode.size() - sCode.size() - synchronizedCBs));
        System.out.println ("Synch-method count: \t" +
                            (sCode.size()-synchronizedCBs-staticSynchronizeds));
        System.out.println ("Static-Synch-method count: \t" + staticSynchronizeds);
        System.out.println ("Synch-Code-Blobs count: \t" + synchronizedCBs);
        System.out.println ("Total Synch Code count: \t" + sCode.size());
        for (Iterator i = sCode.keySet().iterator();  i.hasNext();  )  {
            String mNm  = (String) i.next();
            CodeBlob mB = (CodeBlob) sCode.get(mNm);
            if (mB.calledCode != null)  {
                HashSet workArea = new HashSet();
                workArea.add (mNm);
                mB.augmentReachableSCode (mB.sEdges = new HashSet(), workArea, mNm);
            }
        }
    }

    void augmentReachableSCode (Set rv, Set workArea, String callStack)  {
        if (calledCode != null)
            for (Iterator j = calledCode.keySet().iterator();  j.hasNext();  )  {
                String m   = (String) j.next();
                CodeBlob b = (CodeBlob) calledCode.get(m);
                boolean addedLock = false;
                if (b == null)  continue;
                if (b.lockType != null)  {
                    rv.add (callStack + "-" + m);
                    // addedLock = workArea.add (b.lockType);
                }
                if (workArea.add (m))  {  // an 'else' here gets us edges of the lag
                    b.augmentReachableSCode (rv, workArea, callStack + "-" + m);
                    // workArea.remove (m);
                    // if (addedLock)
                    //    workArea.remove (b.lockType);
                }  // Removing the 'else' above prints paths, from the lag
            }
    }
    public static void dropUnsynchronizedCodeBlobs ()  {
        exploredCode.clear();        exploredCode = null;
        System.out.println ("Undefined methods (" +unexploredCode.size()+"):" );
        Object[] undefineds = unexploredCode.keySet().toArray();
        for (int i = undefineds.length;  --i >= 0;  )
            System.out.println ("undefined: " + ((String)undefineds[i]));
        unexploredCode.clear();    unexploredCode = null;
        System.gc();
    }
    
    // @<Collect and Report (size) statistics of the analysis@>
}