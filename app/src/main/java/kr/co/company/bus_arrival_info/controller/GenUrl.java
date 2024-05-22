package kr.co.company.bus_arrival_info.controller;
import com.google.cloud.audit.AuthenticationInfo;

import java.io.IOException;
import java.net.URLEncoder;


import java.net.URL;
import java.net.URLEncoder;

public class GenUrl {
    private static String EncodingKey = String.valueOf("h5k2pHVV7fUZGtbtOyrAVXLHJ1Smbn9kC1zxcdrW71mzAFFRcepjKydbU2G1gREraWXsUZ9wmrNGxPhjkyPTzg%3D%3D");
    private static String DecodingKey = String.valueOf("h5k2pHVV7fUZGtbtOyrAVXLHJ1Smbn9kC1zxcdrW71mzAFFRcepjKydbU2G1gREraWXsUZ9wmrNGxPhjkyPTzg==");

    // 정류장 조회 URL Generate
    public static URL generate(String stSrch) throws IOException
    {
        StringBuilder urlBuilder = new StringBuilder("http://ws.bus.go.kr/api/rest/stationinfo/getStationByName"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + EncodingKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("stSrch","UTF-8") + "=" + URLEncoder.encode(stSrch, "UTF-8")); /*정류소명 검색어*/
        urlBuilder.append("&" + URLEncoder.encode("resultType","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
        URL url = new URL(urlBuilder.toString());
        return url;
    }

    public static URL generate(String arsId, String busNum) throws IOException
    {
        StringBuilder urlBuilder = new StringBuilder("http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + EncodingKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("arsId","UTF-8") + "=" + URLEncoder.encode(arsId, "UTF-8")); /*정류소명 검색어*/
        urlBuilder.append("&" + URLEncoder.encode("resultType","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8"));
        URL url = new URL(urlBuilder.toString());
        return url;
    }
}
