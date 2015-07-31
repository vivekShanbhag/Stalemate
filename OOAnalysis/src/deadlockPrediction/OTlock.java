/* OTlock.java -- Lock Acquisition Graph Construction, Cycle detection / reporting
 *
 * author: Vivek K. Shanbhag, IIIT-B, Bangalore, India.
 */

package deadlockPrediction;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class OTlock {
    static HashMap  locks = new HashMap ();
    
    String     lockName = null;
    String     version     = "1.7";
    HashMap    tlogEdges   = new HashMap();
    HashSet    lContenders = new HashSet();
    public OTlock (String name) {
        lockName = name; // Surprise: this attribute is never used.
        if (!name.endsWith (".class"))
            name = name + ".class";
        version  = getVersion (name);
    };
    private String getVersion (String name) {
        String retval = "1.7";
        try {
            Process c = Runtime.getRuntime().exec ("./getVersion " + name);
            BufferedReader in = new BufferedReader (
                                new InputStreamReader (c.getInputStream()));
            retval = in.readLine();
            if (c.waitFor() != 0)
                System.out.println ( "getVerion Failed: " + c.exitValue());
        } catch (Exception e) {
            System.out.println ("Exceotion in getVersion: " + e);
        }
        return retval;
    };
    
    static public void computeTLOGedges () {
      int rv = 0, nrv = 0;
      for (Iterator i = locks.keySet().iterator();  i.hasNext();  )  {
        String lN  = (String) i.next();                    // lock-Name
        Set    lCS = ((OTlock)locks.get(lN)).lContenders;  // lock-Contender-Set
        if (lCS != null)
            nrv += lCS.size();
        if (lN.charAt(0) == '[')  continue;
        for (Iterator j = lCS.iterator();  j.hasNext();  )  {
          CodeBlob lC = (CodeBlob) j.next();   // lock-Contender
          Set    eSlC = lC.sEdges; // edges-Starting from lock-Conteder
          if (eSlC == null) continue;
          for (Iterator k = eSlC.iterator();  k.hasNext();  )  {
            String eAnnot   = (String) k.next();
            int lastHyphen  = eAnnot.lastIndexOf('-');
            if (lastHyphen >= 0)  {
              // System.err.println ("Before eAnnot = " + eAnnot);
              String tN = eAnnot.substring(lastHyphen+1);  //last method
                  assert (tN.startsWith("<"));
                  tN = tN.substring (tN.indexOf(">")+1);
                  if (tN.indexOf("=") > 0)
                      tN = tN.substring (0, tN.indexOf("="));
                  assert (CodeBlob.sCode.get(tN) != null);
              String tLockType = ((CodeBlob)CodeBlob.sCode.get(tN)).getLockType();
              // System.err.println ("After tN = " + tN);
              assert (tLockType != null);
              OTlock  l = (OTlock) OTlock.locks.get(lN);
              HashSet tempM = (HashSet)((HashMap)l.tlogEdges).get(tLockType);
              // Create entry for tLockType into lN.tlogEdges, if not present
              //  the value for this new entry is an object of type HashSet
              if (tempM == null)
                l.tlogEdges.put (tLockType, tempM=new HashSet());
              tempM.add (eAnnot);  // Into this hashSet insert the edgeAnnotation.
              rv++;
            }
          }
        }
      }
      System.out.println ("Pottential TLOG-edges were   = " + nrv);
      System.out.println ("Total TLOG-edges discovered  = " + rv);
    }
    static public void reportTLOGcycles (int maxLen) {
        assert (maxLen >= 2);
        String  [] cycle      = new String  [maxLen]; // cycle[0]==cycle[n] ==> print
        Iterator[] populator  = new Iterator[maxLen]; // iterative method, not recursive.
        for (int len = 2;  len <= maxLen;  len++)  {  // start looking for len-cycles
            populator[0] = locks.keySet().iterator(); // create iterator to fill cycle[]
            boolean doSelect = true;
            for (int j = 0;  j < len;  j++)  {
                if (doSelect  &&  j==0  &&  !populator[j].hasNext())
                    break;
                doSelect = true;
                while (populator[j].hasNext() && doSelect)  { // select 'legal' next lock
                    cycle[j] = (String) populator[j].next();
                    if (cycle[j].equals ("java.lang.Object"))
                        continue;
                    doSelect = false;
                    doSelect |= ((j>0) && cycle[j].compareTo(cycle[j-1]) <= 0);
                    //for (int k = 0;  k < j;  k++) // If the just populated lock is already in
                    //    doSelect |= (cycle[j].compareTo(cycle[k]) == 0); // the cycle, select another one.
                    doSelect |= (j > 0)  &&       // Not a legal selection, so, select again.
                        !((OTlock)locks.get(cycle[j-1])).tlogEdges.keySet().contains(cycle[j]);
                    doSelect |= (j == len-1)  &&     // Not a cycle, too bad, select again.
                        !((OTlock)locks.get(cycle[j])).tlogEdges.keySet().contains(cycle[0]);
                }
                if (doSelect) {
                    if (j>0 && !populator[j].hasNext())  { // No legal next node; Backtrack
                        j--; j--;      // Equivalent to the recursive backtrack;
                    }                  //  '-2' above pre-corrects for the 'j++'
                } else {
                    if (j < len-1)     // If lock-j lock isnt the last lock then
                        populator[j+1] = locks.keySet().iterator(); // reset next iterator
                    if (j == len-1)
                        if (((OTlock)locks.get(cycle[j])).tlogEdges.keySet().contains(cycle[0])) {
                            printCycle (len, cycle);  // having selected all locks in the cycle
                            for (int k = j;  k>=0;  k--)
                                if (populator[k].hasNext())  {
                                    j = k-1; // '-1' precorrects for j++;
                                    break;   // print the cycle and continue looking
                                }
                        }
                }
            }
        }
        System.out.println ("Total Cycles from all Predictions: " + totalCycles);
    }

    static public void reportLockUsage ()  {
        System.out.println ("Number of locks used in the code-base: " + locks.size());
        int clam = 0, clab = 0; // cumulative lock acquring methods/blocks counters
        for (Iterator i = locks.keySet().iterator();  i.hasNext();  )  {
            int lam = 0, lab = 0; // lock acquring methods/blocks counters
            String k = (String) i.next();
            for (Iterator j = ((OTlock)locks.get(k)).lContenders.iterator();  j.hasNext();  )
                if (((CodeBlob)j.next()).getNodeName().indexOf('#') < 0)  lam++;
                else                                                      lab++;
            System.out.println ("YYY: " + k + ": " + lam + ":" + lab);
            clam += lam;  clab += lab;
        }
        System.out.println ("Cumulative clam = " + clam + ", clab = " + clab +
                                    ", Total = " + (clam+clab) + ".");
    }
    static private Set alreadyPrintedCumCycles = new HashSet();
    static private Set alreadyPrintedMultCycles = new HashSet();
    static public int  totalCycles = 0;
    static public void printCycle (int len, String[] cycle) {
        String lockNameList = "";
        int cycles = 1;
        
        java.math.BigInteger cumulativeHashValue = java.math.BigInteger.ZERO.abs();
        java.math.BigInteger multiplicativeHashValue = java.math.BigInteger.ONE.abs();
        String ver = ((deadlockPrediction.OTlock)locks.get(cycle[0])).version;
        boolean sameVer = true;
        for (int i = 1;  sameVer  &&  i < len;  i++)
            sameVer = ver.equals(((deadlockPrediction.OTlock)locks.get(cycle[i])).version);
        for (int i = 0;  i < len;  i++) {
            lockNameList += (" " + cycle[i]);
            cumulativeHashValue = cumulativeHashValue.add (java.math.BigInteger.valueOf ((long)cycle[i].hashCode()));
            multiplicativeHashValue = multiplicativeHashValue.multiply(
                                      java.math.BigInteger.valueOf ((long)cycle[i].hashCode()));
            if (! sameVer)
                lockNameList += "(" + ((deadlockPrediction.OTlock)locks.get(cycle[i])).version + ")";
        }
        lockNameList += "(" + (sameVer ? ver : "Update") + ")";
        //long cumulativeHashValue = lockNameList.hashCode();
        //if (! alreadyPrintedCumCycles.add(cumulativeHashValue.toString()) &&
        //    ! alreadyPrintedMultCycles.add(multiplicativeHashValue.toString()))
        //    return;
        
        System.err.println ("<Cycle-" +len+ " locks=\""+lockNameList +"\">");

        for (int i = 0;  i < len;  i++)  {
            HashSet stacks = ((HashSet)
                              ((deadlockPrediction.OTlock)locks.get(cycle[i])
                              ).tlogEdges.get(cycle[(i+1)%len]));
            assert (stacks.size() > 0);
            cycles *= stacks.size();
            for (Iterator j = stacks.iterator();  j.hasNext();  ) {
                System.err.println ("<Thread-" + (i+1) + ">");
                System.err.println (((String)j.next()).replace('-', '\n').
                                    replaceAll ("\"<init>\"", "\"init\""));
                System.err.println ("</Thread-" + (i+1) + ">");
            }
        }
        System.err.println ("</Cycle-" + len + ">");
        totalCycles += cycles;
    }
    
}