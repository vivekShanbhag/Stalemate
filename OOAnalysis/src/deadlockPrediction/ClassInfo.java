/* ClassInfo.java -- Lock Acquisition Graph Construction, Cycle detection / reporting
 *
 * author: Vivek K. Shanbhag, IIIT-B, Bangalore, India.
 */

package deadlockPrediction;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class ClassInfo {
    public String  typeName   = null;
    public String  parentType = null;
    public boolean isAbstract = false;
    public Set asInstanceAttributes = new HashSet();
    public Set     asClassAttributes = new HashSet();
    public Map    instanceAttributes = new HashMap();
    public Map       classAttributes = new HashMap();
    public String[] interfacesImplemented = null;
    
    public ClassInfo (String myNm, String pNm) {
        typeName = myNm;    parentType = pNm;
    };
    
    public void setAsInstanceAttribute (String nm) {
        asInstanceAttributes.add (nm);
    };

    public void setAsClassAttribute (String nm) {
        if (asInstanceAttributes.remove (nm))
            asClassAttributes.add (nm);
    };
    public void setInstanceAttribute (String nm, String type) {
        instanceAttributes.put (nm, type);
    };

    public void setClassAttribute (String nm, String type) {
        if (instanceAttributes.remove (nm) != null)
            classAttributes.put (nm, type);
    };

    public void noteInterfaces (String[] implemented) {
        interfacesImplemented = implemented;
    }
    public List getInterfacesImplemented () {
        List rv = new java.util.ArrayList ();
        for (ClassInfo c = this;  c != null;
             c = c.parentType!=null ? ((ClassInfo)CodeBlob.classes.get(c.parentType)) : null)
            if (c.interfacesImplemented != null)
                for (int j = 0;  j < c.interfacesImplemented.length;  j++)
                    rv.add (c.interfacesImplemented[j]);
        for (int i = 0;  i < rv.size();  i++)  {
            String   ii = (String) rv.get(i);
            String[] ie = null;
            if ((ie = (String[]) CodeBlob.interfaces.get(ii)) != null)
                for (int j = 0;  j < ie.length;  j++)
                    rv.add (ie[j]);
        }            
        return rv;
    }
    
}