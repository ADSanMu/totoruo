import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

  public static void main(String[] args) {
//    //language=RegExp
//    String a = "(?<!\\d)(\\d{3})(?!\\d)";
//    String content = "1231da999as1234";
//    Pattern compile = Pattern.compile(a);
//    Matcher matcher = compile.matcher(content);
//    matcher.find();
//    System.out.println(matcher.group(1));
//    //language=RegExp


    /**
     *  http/gvcms53.4502/content/ssmp/hk/en/prop/etfs/products/asd.html?a=b
     *
     */
    String zz = "";
    String reg = "(http|https)/\\w{0,10}\\.(4502|4503)(/content/ssmp)?/\\w+/\\w+/\\w+/etfs/products/\\w.+\\.html(\\?)?.*";//language=RegExp  ((?!/editor\.html).)+
    String reg1 = "https?/[^/]+?\\.450[23](?<path>(?:/content/ssmp)?/[^/]+/[^/]+/[^/]+/etfs/products/[^/]+?)\\.html(\\?.+)?";
    String path = "http/gvcms53.4502/editor.html/content/ssmp/hk/en/prop/etfs/products/asd.html?a=b(?#sd)";
    Matcher matcher = Pattern.compile(reg1).matcher(path);
    String r = "/content/ssmpproducts.dynamic-select.html${path}";
    StringBuffer buf = new StringBuffer();
    if (matcher.find()) {
      matcher.appendReplacement(buf, r);
      matcher.appendTail(buf);
    }
    System.out.println(buf);
  }
}
