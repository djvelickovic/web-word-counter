package com.crx.raf.kids.d1.util;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Util {

    public static Map<String, Integer> generateCleanMap(Set<String> keywords) {
        return keywords.stream().collect(Collectors.toMap(key -> key, key -> 0));
    }

    public static Map<String, Integer> addMaps(Map<String, Integer> map1, Map<String, Integer> map2, Set<String> keywords) {
        return keywords.stream().collect(Collectors.toMap(key -> key, key -> map1.getOrDefault(key, 0) + map2.getOrDefault(key, 0)));
    }

//    public static Map<String, Integer> addMaps(Map<String, Integer> map1, Map<String, Integer> map2) {
//        return map1.keySet().stream().collect(Collectors.toMap(key -> key, key -> map1.getOrDefault(key, 0) + map2.getOrDefault(key, 0)));
//    }
}
