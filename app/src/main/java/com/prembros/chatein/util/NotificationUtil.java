package com.prembros.chatein.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NotificationUtil {
    private static Map<Character, Integer> map = new HashMap<>();
    public static void init() {
        if (map.isEmpty()) {
            map.put('a', 1);
            map.put('b', 2);
            map.put('c', 3);
            map.put('d', 4);
            map.put('e', 5);
            map.put('f', 6);
            map.put('g', 7);
            map.put('h', 8);
            map.put('i', 9);
            map.put('j', 10);
            map.put('k', 11);
            map.put('l', 12);
            map.put('m', 13);
            map.put('n', 14);
            map.put('o', 15);
            map.put('p', 16);
            map.put('q', 17);
            map.put('r', 18);
            map.put('s', 19);
            map.put('t', 20);
            map.put('u', 21);
            map.put('v', 22);
            map.put('w', 23);
            map.put('x', 24);
            map.put('y', 25);
            map.put('z', 26);
            map.put('A', 1);
            map.put('B', 2);
            map.put('C', 3);
            map.put('D', 4);
            map.put('E', 5);
            map.put('F', 6);
            map.put('G', 7);
            map.put('H', 8);
            map.put('I', 9);
            map.put('J', 10);
            map.put('K', 11);
            map.put('L', 12);
            map.put('M', 13);
            map.put('N', 14);
            map.put('O', 15);
            map.put('P', 16);
            map.put('Q', 17);
            map.put('R', 18);
            map.put('S', 19);
            map.put('T', 20);
            map.put('U', 21);
            map.put('V', 22);
            map.put('W', 23);
            map.put('X', 24);
            map.put('Y', 25);
            map.put('Z', 26);
            map.put('-', 1);
            map.put('_', 1);
        }
    }

    @Contract(pure = true) public Map<Character, Integer> get() {
        return map;
    }

    public static int getId(@NotNull String str) {
        init();
        int id = 0;
        for(char c : str.toCharArray()) {
            try {
                int val = map.get(c);
                id = id + val;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return id;
    }
}