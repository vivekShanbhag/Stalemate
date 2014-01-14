/* ClassInfo.java -- Lock Acquisition Graph Construction, Cycle detection / reporting
 *
 * author: Vivek K. Shanbhag, IIIT-B, Bangalore, India.
 */

package deadlockPrediction;

import java.util.*;
import java.io.*;

public class ClassInfo {
    public String parentName = null;
    public Set instanceAttributes = new HashSet();
    public Set classAttributes  = new HashSet();
    
    public ClassInfo (String pNm) {
        parentName = pNm;
    };
    
    public void setInstanceAttribute (String desc) {
        instanceAttributes.add (desc);
    };

    public void setClassAttribute (String desc) {
        if (instanceAttributes.remove (desc))
            classAttributes.add (desc);
    };
    
}