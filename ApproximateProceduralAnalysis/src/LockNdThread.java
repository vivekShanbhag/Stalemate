import java.io.*;
import java.util.*;
public class LockNdThread {
    static BufferedReader textIn  = null;
    static String assemblyName, fileName, nameSpace;
    static char   conLevelAt = 'a';
    static String[] t = null; // Temporary used in many places.
    static int ltPconLvlEntities = 0, assemblies = 0;
    static int cumNspaces = 0, aNspaces = 0;
    static int cumClasses = 0, aClasses = 0, nsClasses = 0;
    static int cumMethods = 0, aMethods = 0, nsMethods = 0, cMethods = 0;
    static int cumUses = 0, aUses = 0, nsUses = 0, cUses = 0, mUses = 0;
    static int cumLocks = 0, aLocks = 0, nsLocks = 0, cLocks = 0, mLocks = 0, lopens = 0;
    
    static public BufferedReader
    populateTextReader (String[] params)  {
        try {
            String arg = "";
            for (String s : params)
                if (s.charAt(0) != '-')  arg += " " + s;
            Process child = Runtime.getRuntime().exec ("/usr/local/bin/monodis " + arg);
            InputStream disOut = child.getInputStream();
            InputStreamReader r = new InputStreamReader (disOut);
            return new BufferedReader (r);
            // while ((line = textIn.readLine()) != null)
        } catch (Exception e) {
            System.out.println (e.toString());
        }
        return null;
    };
    static String readAssembly (String line)
        throws IOException {
        while (line!=null && line.indexOf(".assembly '")<0)
            line = textIn.readLine();
        if (line==null || ((t = line.split(" ")).length < 2)) {
            System.out.println ("Input `assemblyName' Missing!");
            return null;
        }
        assemblyName = t[t.length - 1];
        // System.out.println("Read assemblyName: " + assemblyName);
        
        while (line!=null && line.indexOf(".module ")!=0)
            line = textIn.readLine();
        if (line != null) line = line.substring (0, line.indexOf(" //"));
        while (line!=null && line.indexOf(".module ")<0)
            line = textIn.readLine();
        if (line==null || ((t = line.split(" ")).length < 2)) {
            System.out.println ("Input `fileName' Missing!");
            return null;
        }
        fileName = t[t.length - 1];
        // System.out.println("Read fileName: " + fileName);
        
        do {
            line = textIn.readLine();
            if (line != null  &&  line.indexOf(".namespace") >= 0)
                readNameSpace(line);
            if (line == null  ||  line.indexOf(".assembly") >= 0) {
                if (conLevelAt == 'a'  &&  (aLocks>0 || aUses>0))  {
                    ltPconLvlEntities++;
                    System.out.println (fileName + ":");
                    System.out.println ("    " + aLocks + " lock() calls, " + aUses +
                        " `Thread.*' calls in " + aMethods + " Methods in " + aClasses +
                        " Classes in " + aNspaces + " Namespaces.");
                }
                assemblies++;  aUses = 0;  aLocks = 0;  aMethods = 0;  aClasses = 0;  aNspaces = 0;
                
                return line;
            }
        } while (line != null);
        return null;
    }
    static void readNameSpace (String line)
        throws IOException {
        while (line!=null && line.indexOf(".namespace ")<0)
            line = textIn.readLine();
        if (line==null || ((t = line.split(" ")).length < 2)) {
            System.out.println ("Input `nameSpace' Missing!");
            return ;
        }
        nameSpace = t[t.length - 1];
        // System.out.println("Read nameSpace: " + nameSpace);
        
        while ((line=textIn.readLine()) != null  &&  line.indexOf("}") != 0)
            if (line.indexOf("  .class ") == 0)
                ReadClass (line, nameSpace);
        if (conLevelAt == 'n'  &&  (nsLocks>0 || nsUses>0))  {
            ltPconLvlEntities++;
            System.out.println (nameSpace + "(" + fileName + "):");
            System.out.println ("    " + nsLocks + " lock() calls, " + nsUses +
                " `Thread.*' calls in " + nsMethods + " Methods in " + nsClasses + " Classes.");
        }  else if (conLevelAt == 'a')  {
            aUses  += nsUses;       aLocks += nsLocks;
            aMethods += nsMethods;  aClasses += nsClasses;  aNspaces++;
        }
        cumNspaces++;  nsUses = 0;  nsLocks = 0;  nsMethods = 0;  nsClasses = 0;
        
    }
    static public void ReadClass (String line, String pref)
        throws IOException {
        String className = null, methodName = null, parentName = null;
        while (line!=null && line.indexOf("  .class ")<0)
            line = textIn.readLine();
        if (line==null || ((t = line.split(" ")).length < 2)) {
            System.out.println ("Input `className' Missing!");
            return ;
        }
        className = t[t.length - 1];
        // System.out.println("Read className: " + className);
        
        while ((line=textIn.readLine()) != null  &&  line.indexOf("  }") != 0)  {
            if (line.indexOf("  .class ") == 0)
                ReadClass (line, pref + "." + className);
            else if (line.indexOf("    .method ") == 0) {
                while (line!=null && line.indexOf(".method") < 0)
                    line = textIn.readLine();
                if (line != null)  line += textIn.readLine();
                // System.out.println("Reading ... methodName: " + line);
                if (line.indexOf(" (") >= 0)
                    line = line.substring (0, line.indexOf(" ("));
                while (line!=null && line.indexOf(".method ")<0)
                    line = textIn.readLine();
                if (line==null || ((t = line.split(" ")).length < 2)) {
                    System.out.println ("Input `methodName' Missing!");
                    return ;
                }
                methodName = t[t.length - 1];
                // System.out.println("Read methodName: " + methodName);
                
                while ((line=textIn.readLine()) != null  &&  line.indexOf("    }") != 0)  {
                    if (line.indexOf("call ") > 0  &&
                        line.indexOf("System.Threading") > 0) {
                        int closeS = line.indexOf("]"), openP = line.indexOf("(");
                        // System.out.println ("Line: " + line + ":" + closeS + ":" + openP + ".");
                        String called = openP>closeS ? line.substring(closeS+1, openP) : null;
                        if (called != null) {
                            mUses++;
                            if (called.indexOf("System.Threading.Monitor::Enter") == 0) {
                                lopens++; mUses--;
                            } else if (lopens > 0  &&
                                called.indexOf("System.Threading.Monitor::Exit")==0) {
                                lopens--; mLocks++; mUses--;
                            }
                        }
                    }
                    // System.out.println ("Processed: " + line);
                }
                mUses += lopens;  lopens = 0;
                if (conLevelAt == 'm'  &&  (mLocks>0 || mUses>0))  {
                    ltPconLvlEntities++;
                    System.out.println (methodName + "." + className + "(" + nameSpace + "(" + fileName + ")):");
                    System.out.println ("    " + mLocks + " lock() calls, " + mUses + " `Thread.*' calls.");
                }  else if (conLevelAt == 'c'  ||  conLevelAt == 'n'  ||  conLevelAt == 'a')  {
                    cUses += mUses;  cLocks += mLocks;  cMethods++;
                }
                cumUses += mUses;  cumLocks += mLocks;  cumMethods++;  mUses = 0;  mLocks = 0;
                
                
            }
        }
        if (conLevelAt == 'c'  &&  (cLocks>0 || cUses>0))  {
            ltPconLvlEntities++;
            System.out.println (className + "(" + nameSpace + "(" + fileName + ")):");
            System.out.println ("    " + cLocks + " lock() calls, " + cUses +
                " `Thread.*' calls in " + cMethods + " Methods.");
        }  else if (conLevelAt == 'n'  ||  conLevelAt == 'a')  {
            nsUses += cUses;  nsLocks += cLocks;  nsMethods += cMethods;  nsClasses++;
        }
        cumClasses++;  cUses = 0;  cLocks = 0;  cMethods = 0;
        
    }
    public static void main (String[] args) {
        try {
            if (args[0].charAt(0) == '-')  {
                conLevelAt = args[0].charAt(1);
                if (conLevelAt != 'm'  &&  conLevelAt != 'c'  &&
                    conLevelAt != 'n'  &&  conLevelAt != 'a')  {
                    System.out.println ("Usage: java LockNdThread -[mcna] assembly-name-list");
                    return;
                }
            }
            
            textIn = populateTextReader (args);
            String line = textIn.readLine();
            while (line != null)
                line = readAssembly (line);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        System.out.println ("Total:");
        System.out.print ("    " + cumLocks + " lock() calls, " + cumUses + " `Thread.*' calls in ");
        if (conLevelAt == 'm')
            System.out.println (ltPconLvlEntities + "/" + cumMethods + " Methods.");
        else {
            System.out.print   (cumMethods + " Methods, in ");
            if (conLevelAt == 'c')
                System.out.println (ltPconLvlEntities + "/" + cumClasses + " Classes.");
            else {
                System.out.print   (cumClasses + " Classes, in ");
                if (conLevelAt == 'n')
                    System.out.println (ltPconLvlEntities + "/" + cumNspaces + " NameSpaces.");
                else  {
                    System.out.print   (cumNspaces + " NameSpaces, in ");
                    System.out.println (ltPconLvlEntities + "/" + assemblies + " Assemblies.");
                }
            }
        }
    }
}