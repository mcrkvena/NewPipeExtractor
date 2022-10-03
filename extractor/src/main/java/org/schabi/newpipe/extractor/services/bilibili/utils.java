package org.schabi.newpipe.extractor.services.bilibili;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class utils {
    int[] s = {11, 10, 3, 8, 4, 6};
    public  int xor = 177451812;
    public long add = 8728348608L;
    public String table = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF";
    public Map<Character, Integer> map =new HashMap<Character, Integer>();
    public utils(){
        for(int i=0;i<58;i++){
            map.put(table.charAt(i), i);
        }
    }
    public Long bv2av(String bv){
        long r = 0;
        for(int i=0;i<6;i++){
            r += map.get(bv.charAt(s[i]))*Math.pow(58, i);
        }
        return (r - add) ^ xor;
    }
    public String av2bv(Long x){
        x = (x^xor) + add;
        String[] r = "BV1  4 1 7  ".split("");
        for(int i=0;i<6;i++){
            r[s[i]] = String.valueOf(table.charAt((int) ((x / Math.pow(58, i)) % 58)));
        }
        String result = "";
        for(String i: r){
            result += i;
        }
        return result;
    }
    public static String getUrl(String url, String id){
        String p = "1";
        if(url.contains("p=")){
            p = url.split("p=")[1].split("&")[0];
        }
        return "https://api.bilibili.com/x/web-interface/view?bvid="+ id+ "&p="+ p;
    }
    public static String getPureBV(String id){
        return id.split("\\?")[0];
    }
    public static String getChannelApiUrl(String url, String id){
        String pn = "1";
        if(url.contains("pn=")){
            pn = url.split("pn=")[1].split("&")[0];
        }
        return "https://api.bilibili.com/x/space/arc/search?pn="+ pn +"&ps=10&mid=" + id;
    }
    public static String getRecordApiUrl(String url){
        String pn = "1", sid, mid;
        if(url.contains("pn=")){
            pn = url.split("pn=")[1].split("&")[0];
        }
        mid = Optional.of(url.split("space.bilibili.com/")[1].split("/")[0]).orElse(url.split("space.bilibili.com/")[1]);
        sid = url.split("sid=")[1];
        return String.format("https://api.bilibili.com/x/series/archives?mid=%s&series_id=%s&only_normal=true&sort=desc&pn=%s&ps=30",mid, sid, pn);
    }
    public static String getMidFromRecordUrl(String url){
        return url.split("space.bilibili.com/")[1].split("/")[0];
    }
    public static String getMidFromRecordApiUrl(String url){
        return url.split("mid=")[1].split("&")[0];
    }
}