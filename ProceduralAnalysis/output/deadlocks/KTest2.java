import sun.security.krb5.internal.rcache.*;
import sun.security.krb5.internal.*;
import java.util.*;

public class KTest2 {
  public static void main(String [] a) throws Exception{
    final CacheTable ct = new CacheTable();
    final long time = System.currentTimeMillis();
    ct.put("TheOnlyOne", new AuthTime( time - Krb5.DEFAULT_ALLOWABLE_CLOCKSKEW * 1000L, 0), time);
    final ReplayCache rc = (ReplayCache) ct.get("TheOnlyOne");
    new Thread() {
      public void run() {
        rc.put(new AuthTime( time - Krb5.DEFAULT_ALLOWABLE_CLOCKSKEW * 1000L, 0), time + 300*1000);
      }
    }.start();
    ct.put("TheOnlyOne", new AuthTime( time - Krb5.DEFAULT_ALLOWABLE_CLOCKSKEW * 1000L, 0), time);
  }
}
