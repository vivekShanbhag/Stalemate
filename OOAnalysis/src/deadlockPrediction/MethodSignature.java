/* MethodSignature.java -- Lock Acquisition Graph Construction, Cycle detection / reporting
 *
 * author: Vivek K. Shanbhag, IIIT-B, Bangalore, India.
 */

package deadlockPrediction;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

public class MethodSignature {
    String ms;
    TypeSignature[] params = null;
    public static boolean isFullySpecified (String ms) {
        return ms.substring(0, ms.indexOf(':')).indexOf('.') > 0;
    };
    public MethodSignature (String ms) {
        assert (ms.indexOf (' ') < 0  &&  ms.indexOf (':') > 0);
        assert (ms.indexOf ('(') > 0  &&  ms.indexOf (')') > 0);
        this.ms = ms.intern();
        if (fsmName().contains("/"))
           this.ms = fsmName().replace ('/', '.') + ":" + prtEncoding();
        if (ms.indexOf("()") < 0)
            decodeParamEncoding ();
        //String clsNm = fsmName();  clsNm = clsNm.substring (0, clsNm.lastIndexOf('.'));
        //if (clsNm.startsWith("\"")  &&  clsNm.endsWith("\"")) {
        //    clsNm = clsNm.substring (1, clsNm.length()-1);
        //    this.ms = clsNm + dropClassNmGetRest();
        //};
    };
    public MethodSignature (String cN, String ms) {
        String fsmn = ms.substring (0, ms.indexOf(':')).replace('/', '.');
        // assert (! fsmn.startsWith(cN));
        this.ms =  fsmn.indexOf('.') < 0  ?  (cN + "." + ms)
                                          :  fsmn + ms.substring (ms.indexOf(':'));
        //this ((this.ms = ms.substring (0, ms.indexOf(':')).replace('/', '.')).indexOf('.') < 0
        //      ? (cN + "." + ms) : this.ms + ms.substring (ms.indexOf(':')));
        if (ms.indexOf("()") < 0)
            decodeParamEncoding ();
        //String clsNm = fsmName();  clsNm = clsNm.substring (0, clsNm.lastIndexOf('.'));
        //if (clsNm.startsWith("\"")  &&  clsNm.endsWith("\"")) {
        //    clsNm = clsNm.substring (1, clsNm.length()-1);
        //    this.ms = clsNm + dropClassNmGetRest();
        //};
    };

    public MethodSignature (String cN, String mN, String pp, TypeSignature rt) {
        pp = pp.substring(1, pp.length()-1);
        String ppp = "";
        for (int i = 0, j = 0;  i < pp.length();  i++)  {
            if (pp.charAt(i) == '<')           j++;
            if (j == 0)                        ppp += pp.charAt(i);
            if (j>0  &&  pp.charAt(i) == '>')  j--;
        }
        String[] pTypes = ppp.split(",");
        ms = cN + '.' + mN + ":(";
        if (pp.equals(""))
            params = null;
        else {
            params = new TypeSignature [pTypes.length];
            for (int i = 0;  i < pTypes.length;  i++) {
                //if (pTypes[i].trim().endsWith(">"))
                //    pTypes[i] = pTypes[i].substring (0, pTypes[i].indexOf("<"));
                params[i] = new TypeSignature (pTypes[i].trim(), false);
                ms += params[i].signature();
            }
        }
        ms += ")" + rt.signature();
    };

    private void decodeParamEncoding () {
        String pp = parenParams();
        assert (pp.startsWith ("(")  &&  pp.endsWith (")"));
        LinkedList l = new LinkedList();
        int rv = 0; String temp = "";
        for (int i = 1;  i < pp.length()-1;  rv++) {// post-incr i when consumed.
            while (pp.charAt(i) == '[') {
                temp += '[';  i++;
            }
            if ("BCFISZDJ".indexOf(pp.charAt(i)) >= 0) {
                l.add (rv, temp + pp.charAt (i++));
                temp = "";
            } else if (pp.charAt(i) == 'L') {
                // System.out.println ("pp(i...):" + pp.substring(i));
                l.add (rv, temp + pp.substring (i, pp.indexOf(';', i) + 1));
                i = pp.indexOf (';', i) + 1;
                temp = "";
            } else {
                System.out.println ("Offending type-symbol: " + pp + ":"+i+ "?");
                assert (false);
            }
        }
        if (rv > 0) {
            params = new TypeSignature [rv];
            for (int i = 0;  i < rv;  i++)
                params[i] = new TypeSignature ((String)l.get(i), true);
        }
        l.clear();
    };
    
    public TypeSignature className() { 
        return new TypeSignature (ms.substring (0, fsmName().lastIndexOf('.')), false);
    };

    public String dropClassNmGetRest () {
        return ms.substring (fsmName().lastIndexOf('.')+1);
    };

    public String signature ()   { return ms;                                 };
    public String fsmName ()     { return ms.substring (0, ms.indexOf(':'));  };
    public String prtEncoding () { return ms.substring (ms.indexOf(':') + 1); };
    public String methodName ()  {
        return fsmName().substring (ms.lastIndexOf('.') + 1);
    };

    public TypeSignature retType () {
        return new TypeSignature (ms.substring (ms.lastIndexOf (')') + 1), true);
    };

    public String parenParams () {
        return ms.substring (ms.indexOf(':')+1, ms.lastIndexOf(')')+1);
    }; 

    public int paramCount ()     { return (params == null  ?  0  :  params.length);  };
    public TypeSignature[] paramList () { return params;  };
    
}