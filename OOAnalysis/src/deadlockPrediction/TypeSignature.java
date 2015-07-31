/* TypeSignature.java -- Lock Acquisition Graph Construction, Cycle detection / reporting
 *
 * author: Vivek K. Shanbhag, IIIT-B, Bangalore, India.
 */

package deadlockPrediction;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class TypeSignature {
    String type = "";
    public TypeSignature (String type, boolean decode) {
        if (type == null)  type = "";
        if (decode) {
            for (this.type = "";  type.startsWith("[");  type = type.substring(1))
                this.type += "[]";
            if      ("B".equals(type))   this.type = "byte"    + this.type;
            else if ("C".equals(type))   this.type = "char"    + this.type;
            else if ("D".equals(type))   this.type = "double"  + this.type;
            else if ("F".equals(type))   this.type = "float"   + this.type;
            else if ("I".equals(type))   this.type = "int"     + this.type;
            else if ("J".equals(type))   this.type = "long"    + this.type;
            else if ("S".equals(type))   this.type = "short"   + this.type;
            else if ("Z".equals(type))   this.type = "boolean" + this.type;
            else if ("V".equals(type)  ||
                      "".equals(type))   this.type = "void"    + this.type;
            else  {
                if (type.startsWith("L")  &&  type.endsWith(";"))
                    this.type = type.substring(1, type.length()-1).replace ('/', '.') + this.type;
                else if (type.startsWith("c1") ||  type.startsWith("c2"))
                    this.type = type + this.type;
                else
                    assert (false);
            }
        } else
            this.type = type.equals("") ? "void" : type;
        if (this.type.startsWith("L") &&  this.type.endsWith(";")) {
            System.out.println("Offending Type in: "+ type);
            assert (false);
        }
    };
    
    static Map basicTypes = new HashMap ();
        static {
            basicTypes.put ("byte",    "B");
            basicTypes.put ("char",    "C");
            basicTypes.put ("double",  "D");
            basicTypes.put ("float",   "F");
            basicTypes.put ("int",     "I");
            basicTypes.put ("long",    "J");
            basicTypes.put ("short",   "S");
            basicTypes.put ("void",    "V");
            basicTypes.put ("",        "V");
            basicTypes.put ("boolean", "Z");
        };
        public static String basicEncoded = "BCDFIJSVZ";
    
    public String signature () { 
        String rv = "", t = type;
        while (t.length() > 0  &&  t.endsWith("[]"))  {
            rv += "[";
            t = t.substring (0, t.length()-2);
        }
        if (basicTypes.keySet().contains (t))
            rv += (String) basicTypes.get (t);
        else if (t.length()>0)
            rv += "L" + t.replace ('.', '/') + ";";
        return rv;
    };
    public String name () { return type; };
    
}