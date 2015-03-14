//Experiment while trying to work out the TestSessionParser
import java.util.regex.*;
s = "@Notes\nNOTE_1\nNOTE_2\n@Notes\nNOTE_3\n@notes\nNOTE_4\n@bug\nBUG\n@notes\nLAST_NOTE";
p = Pattern.compile("@notes\n(.*?)(?=\n@|\\z)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
m = p.matcher(s);
int i = 0;
while(m.find()) {
    i++
    System.out.println(i + "(1):" + m.group(1));
    System.out.println(m.groupCount());
}
System.out.println("done");