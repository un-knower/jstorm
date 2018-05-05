import java.util.ArrayList;
import java.util.List;

/**
 * Created by qianxm on 2017/7/10.
 */
public class ClassTest {

    public static void main(String[] args) {
        String[] str = {"1","2","3","4","5","6"};
        int le = str.length;
        List<String> list =  java.util.Arrays.asList(str);
//        list.remove(le-3);
//        list.remove(le-2);
//        list.remove(list.size()-1);

//        list.forEach(l-> System.out.println(l));

        System.out.println(list.toString());
        List<String> temp = new ArrayList<>(le-3);
        for(int i=0;i<le-3;i++){
            temp.add(str[i]);
        }
        System.out.println(String.join("_",temp).replaceAll("\\[","").replaceAll("\\]",""));
        System.out.println(String.join("_",temp));;

        List<Integer> l = new ArrayList<>(16);
        System.out.println(l.size());

        try{
//            String[] s=null;
//            String ss = s[1];
            try {
                String[] s1=null;
                String s2 = s1[1];
            }catch (Exception r) {
                System.out.println("r");
            }

            System.out.println("finally");
        } catch (Exception e) {
            System.out.println("e");
        }
        System.out.println("end");

        List<String> l1 = new ArrayList<>();
        l1.add("1");
        l1.add("2");
        l1.add("3");
        System.out.println("l1" + l1.toString());
        List<String> l2 = new ArrayList<>();
        l2.addAll(l1);

        List<String> l3 = l1;
        l1.clear();
        System.out.println("l1" + l1.toString());
        System.out.println("l2" + l2.toString());
        System.out.println("l3" + l3.toString());

        String r = "0160207102361773712649";
        System.out.println(r.substring(10,12));
    }


}
