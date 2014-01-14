/* OTlock.java -- Lock Acquisition Graph Construction, Cycle detection / reporting
 *
 * author: Vivek K. Shanbhag, IIIT-B, Bangalore, India.
 */

package deadlockPrediction;

import java.util.*;
import java.io.*;

public class OTlock {
    static HashMap  locks = new HashMap ();
    
    HashMap    lagEdges = new HashMap();
    HashSet    lContenders = new HashSet();
    
    // @<OTlock Constructors@>
    // @<OTlock Instance Methods@>
    static public void computeLAGedges () {
        for (Iterator i = locks.keySet().iterator();  i.hasNext();  )  {
            String lN  = (String) i.next();                    // lock-Name
            if (lN.charAt(0) == '[')  continue;
            Set    lCS = ((OTlock)locks.get(lN)).lContenders;  // lock-Contender-Set
            for (Iterator j = lCS.iterator();  j.hasNext();  )  {
                String lCN   = (String) j.next();   // lock-Contender-Name
                Set    eSlCN = ((CodeBlob)CodeBlob.sCode.get(lCN)).sEdges;
                                                     // edges-Starting from lock-Conteder-Named above
                for (Iterator k = eSlCN.iterator();  k.hasNext();  )  {
                    String eAnnot = (String) k.next();
                    int firstHyphen = eAnnot.indexOf('-'), lastHyphen = eAnnot.lastIndexOf('-');
                    if (firstHyphen < 0  ||  !lCN.equals (eAnnot.substring(0, firstHyphen))  ||
                        ! ((CodeBlob)CodeBlob.sCode.get(lCN)).getLockType().equals (lN))
                       System.out.println ("Assertion failure would have occurrred in computeLAGedges at LN: " + lN);
                       //   asserting that the first component is lCN and its lockType is lN

                    if (lastHyphen >= 0)  {
                        String tN = eAnnot.substring(lastHyphen+1);  //last method in edge-annotation
                        String tLockType   = ((CodeBlob)CodeBlob.sCode.get(tN)).getLockType();
                        assert (tLockType != null);
                        OTlock  l = (OTlock) OTlock.locks.get(lN);
                        HashSet tempM = (HashSet)((HashMap)l.lagEdges).get(tLockType);
                        // Create an entry for the tLockType into lN.lagEdges, if not already present
                        //  the value for this new entry is an object of type HashSet
                        if (tempM == null)
                            l.lagEdges.put (tLockType, tempM=new HashSet());
                        tempM.add (eAnnot);  // Into this hashSet insert the edgeAnnotation.
                    }
                }
            }
        }
    }
    static public void reportLAGcycles (int maxLen) {
        assert (maxLen >= 2);
        String  [] cycle      = new String  [maxLen]; // cycle[0] == cycle[n]  ==>  print-cycle
        Iterator[] populator  = new Iterator[maxLen]; // iterative method, not recursive.
        for (int len = 2;  len <= maxLen;  len++)  {  // start looking for len-cycles
            populator[0] = locks.keySet().iterator(); // initialise the iterator to fill cycle[]
            boolean doSelect = true;
            for (int j = 0;  j < len;  j++)  {
                if (doSelect  &&  j==0  &&  !populator[j].hasNext())
                    break;
                doSelect = true;
                while (populator[j].hasNext()  &&  doSelect)  { // selects a 'legal' next lock
                    cycle[j] = (String) populator[j].next();
                    doSelect = false;
                    for (int k = 0;  k < j;  k++)     // If the just populated lock is already in
                        doSelect |= (cycle[j] == cycle[k]);    //  the cycle, select another one.
                    doSelect |= (j > 0)  &&          // Not a legal selection, sorry, select again.
                                !((OTlock)locks.get(cycle[j-1])).lagEdges.keySet().contains(cycle[j]);
                    // bad idea!! doSelect |= (j > 0)  &&  (cycle[j].compareTo(cycle[j-1]) < 0);
                    // ordering to avoid multiple reporting!
                    doSelect |= (j == len-1)  &&     // Not a cycle, too bad, select again.
                                !((OTlock)locks.get(cycle[j])).lagEdges.keySet().contains(cycle[0]);
                }
                if (doSelect) {
                    if (j>0 && !populator[j].hasNext())  { // No legal next node; Backtrack
                        j--; j--;      // Equivalent to the recursive backtrack; '-2' above pre-corrects for the 'j++'
                    }
                } else {
                    if (j < len-1)     // If lock-j lock isnt the last lock
                        populator[j+1] = locks.keySet().iterator(); //  then initialise the next iterator
                    if (j == len-1)
                        if (((OTlock)locks.get(cycle[j])).lagEdges.keySet().contains(cycle[0])) {
                            printCycle (len, cycle);              // having selected all locks along the cycle
                            for (int k = j;  k>=0;  k--)
                                if (populator[k].hasNext())  {
                                    j = k-1; // '-1' precorrects for j++; having printed the cycle, look for the next one!
                                    break;
                                }
                        }
                }
            }
        }
    }

    static public void reportLockUsage ()  {
        System.out.println ("Number of locks used in the code-base: " + locks.size());
        int clam = 0, clab = 0; // cumulative lock acquring methods/blocks counters
        for (Iterator i = locks.keySet().iterator();  i.hasNext();  )  {
            int lam = 0, lab = 0; // lock acquring methods/blocks counters
            String k = (String) i.next();
            for (Iterator j = ((OTlock)locks.get(k)).lContenders.iterator();  j.hasNext();  )
                if (((String)j.next()).indexOf('#') < 0)  lam++;
                else                                      lab++;
            System.out.println (k + ": " + lam + ":" + lab);
            clam += lam;  clab += lab;
        }
        System.out.println ("Cumulative clam = " + clam + ", clab = " + clab +
                                    ", Total = " + (clam+clab) + ".");
    }
    static private Set alreadyPrintedCycles = new HashSet();
    static public void printCycle (int len, String[] cycle) {
        String lockNameList = "";
        int cumulativeHashValue = 0;
        for (int i = 0;  i < len;  i++) {
            lockNameList += (" " + cycle[i]);
            cumulativeHashValue += cycle[i].hashCode();
        }
        if (! alreadyPrintedCycles.add(cumulativeHashValue))
            return;
        
        System.out.println ("<Cycle-" +len+ " locks=\""+lockNameList +"\">");
        
        for (int i = 0;  i < len;  i++)  {
            if (cycle[i].endsWith(".class"))
                continue;
            if (((ClassInfo)CodeBlob.classes.get(cycle[i])).instanceAttributes.size() > 0)  {
                System.out.println ("<instance-Attributes-of--" +cycle[i]+ ">");
                for (Iterator j = ((ClassInfo)CodeBlob.classes.get(cycle[i])).
                              instanceAttributes.iterator();  j.hasNext();  )
                    System.out.println ("    " + (String)j.next());
                System.out.println ("</instance-Attributes-of--" +cycle[i]+ ">");
            }
            
            if (((ClassInfo)CodeBlob.classes.get(cycle[i])).classAttributes.size() > 0)  {
                System.out.println ("<class-Attributes-of--" +cycle[i]+ ">");
                for (Iterator j = ((ClassInfo)CodeBlob.classes.get(cycle[i])).
                              classAttributes.iterator();  j.hasNext();  )
                    System.out.println ("    " + (String)j.next());
                System.out.println ("</class-Attributes-of--" +cycle[i]+ ">");
            }
            
        }
         
        for (int i = 0;  i < len;  i++)  {
            for (Iterator j = ((HashSet)((deadlockPrediction.OTlock)locks.
                                         get(cycle[i])
                                        ).lagEdges.get(cycle[(i+1)%len])
                              ).iterator();  j.hasNext();  ) {
                System.out.println ("<Thread-" + (i+1) + ">");
                System.out.println (((String)j.next()).replace('-', '\n').
                                    replaceAll ("\"<init>\"", "\"init\""));
                System.out.println ("</Thread-" + (i+1) + ">");
            }
        }
        System.out.println ("</Cycle-" + len + ">");
    }
    
    // @<Report statistics of the size of the analysis@>
}