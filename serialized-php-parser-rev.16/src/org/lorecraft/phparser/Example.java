package org.lorecraft.phparser;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author AKhusnutdinov
 */
public class Example {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String example = "a:4:{i:0;i:555;i:1;i:777;i:2;i:111;i:3;a:1:{s:3:\"qqq\";s:3:\"zzz\";}}";
        String example = "a:4:{i:0;a:3:{i:0;i:100010001804;i:1;s:3:\"www\";i:2;s:3:\"eee\";}i:1;a:3:{i:0;s:4:\"qqq1\";i:1;s:4:\"www1\";i:2;s:4:\"eee1\";}i:2;a:3:{i:0;s:4:\"qqq2\";i:1;s:4:\"www2\";i:2;s:4:\"eee2\";}i:3;a:3:{i:0;s:4:\"qqq3\";i:1;s:4:\"www3\";i:2;s:4:\"eee3\";}}";
//        String example = "a:4:{s:3:\"uid\";i:2131519277;s:4:\"text\";s:4:\"test\";s:7:\"rootmid\";s:16:\"3422740608561651\";s:5:\"rtmid\";i:3422865137422183;}";
        SerializedPhpParser serializedPhpParser = new SerializedPhpParser(example);
        Object result = serializedPhpParser.parse();
//        Map<Object, Object> qqq=(Map<Object, Object>)result;
//        Set<Object> www= qqq.keySet();
//        Object[]xxx=www.toArray();
//        for (Object tmp:xxx) {
////         System.out.println(tmp.toString());   
//        }
        System.out.println(result);
    }
}
