package com.company;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class LZW {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz .,!?;";
    private static final int DICT_SIZE = ALPHABET.length() * 2;

    public static void encode(String in) {
        char[] chars = in.toCharArray();
        HashMap<String, Integer> codeDict = new HashMap<>();
        // Складываю буквы с алфавита в словарь
        for (int i = 0; i < ALPHABET.length(); i++) {
            codeDict.put(ALPHABET.charAt(i) + "", i);
        }
        StringBuilder sb = new StringBuilder();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("LZW/encoded.txt"));) {
            String curCode;
            int prevCode = 0;
            for (char ch : chars) {
                sb.append(ch);
                curCode = sb.toString();
                // Достаю индех от string в dict
                Integer code = codeDict.get(curCode);
                // Если string код есть, то инициализирую предыдущий код
                if (code != null) {
                    prevCode = code;
                // Если нет, то добавляю
                } else {
                    // Записываю новую комбинацию ch в словарь
                    if (codeDict.size() < DICT_SIZE) {
                        codeDict.put(curCode, codeDict.size());
                    }
                    try {
                        // записываю предыдущий код в файл
                        writer.write(prevCode);
                        System.out.println(prevCode);
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    }
                    // обнуляю комбинацию ch и добавляю текущий ch для начала новой комбинации
                    sb.setLength(0);
                    sb.append(ch);
                    // Добавляю предыдущий код
                    prevCode = codeDict.get(ch + "");
                }
            }
            // Дозаписываем остатки символов
            writer.write(codeDict.get(sb.toString()));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void decode(String in) {
        char[] chars = in.toCharArray();
        HashMap<Integer, String> codeDict = new HashMap<>();
        // Складываю буквы с алфавита в словарь. Только наоборот код, текст
        for (int i = 0; i < ALPHABET.length(); i++) {
            codeDict.put(i, ALPHABET.charAt(i) + "");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("LZW/decoded.txt"))) {
            String prevCode = null;
            String curCode;
            for (int ch : chars) {
                // Достаю string из кода
                curCode = codeDict.get(ch);
                // Записываю в декодеровщик
                writer.write(curCode);
                // Пропускаю первый символ и начинаю записывать комбинации кодов
                if (prevCode != null) {
                    codeDict.put(codeDict.size(), prevCode + curCode.charAt(0));
                }
                prevCode = curCode;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public static void main(String[] args) {
        try {
            LZW.encode(Files.readString(Path.of("LZW/test.txt")));
            LZW.decode(Files.readString(Path.of("LZW/encoded.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
