package com.company;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

class BigFloat {
    public final static int DIGITS = 80;
    public final static int BIGFLOAT_PRECISION = 18;
    public byte[] value = new byte[DIGITS];

    public BigFloat() {
    }

    public BigFloat(double value) {
        for (int i = 0; i < BIGFLOAT_PRECISION; i++) {
            value *= 10;
            this.value[i] = (byte) value;
            value = value % 1;
        }
    }

    public void add(BigFloat a) {
        byte over = 0;
        for (int i = DIGITS - 1; i >= 0; i--) {
            byte temp = (byte) (value[i] + a.value[i] + over);
            over = (byte) (temp / 10);
            value[i] = (byte) (temp % 10);
        }
    }

    private BigFloat multByDig(byte a, int shift) {
        byte over = 0;
        byte[] res = new byte[DIGITS];
        for (int i = DIGITS - 1; i >= 0; i--) {
            byte temp = (byte) (value[i] * a + over);
            over = (byte) (temp / 10);
            if (i + shift < DIGITS) {
                res[i + shift] = (byte) (temp % 10);
            }
        }
        for (int i = shift - 1; i >= 0; i--) {
            byte temp = over;
            over = (byte) (temp / 10);
            res[i] = (byte) (temp % 10);
        }
        BigFloat bf = new BigFloat();
        bf.value = res;
        return bf;
    }

    public BigFloat multiply(BigFloat a) {
        BigFloat result = new BigFloat();
        for (int i = 0; i < DIGITS; i++) {
            result.add(this.multByDig(a.value[i], i + 1));
        }
        return result;
    }

    public void minus(BigFloat a) {
        byte over = 0;
        for (int i = DIGITS - 1; i >= 0; i--) {
            byte temp = (byte) (value[i] - a.value[i] - over);
            if (temp < 0) {
                temp += 10;
                over = 1;
            } else {
                over = 0;
            }
            value[i] = (byte) (temp % 10);
        }
    }

    public void minus(BigFloat a, int shift) {
        byte over = 0;
        for (int i = DIGITS - 1; i >= shift; i--) {
            byte temp = (byte) (value[i] - a.value[i - shift] - over);
            if (temp < 0) {
                temp += 10;
                over = 1;
            } else {
                over = 0;
            }
            value[i] = (byte) (temp % 10);
        }
        if (over == 1) {
            value[shift - 1]--;
        }
    }

    public boolean isLess(BigFloat a, int shift) {
        for (int i = 0; i < shift; i++) {
            if (value[i] != 0) {
                return false;
            }
        }
        for (int i = shift; i < DIGITS; i++) {
            if (value[i] != a.value[i - shift]) {
                return value[i] < a.value[i - shift];
            }
        }
        return false;
    }

    public BigFloat divide(BigFloat a) {
        BigFloat result = new BigFloat();
        for (int i = 1; i < DIGITS; i++) {
            while (!this.isLess(a, i)) {
                this.minus(a, i);
                result.value[i - 1]++;
            }
        }
        return result;
    }

    public double toDouble() {
        int k = Math.min(15, BIGFLOAT_PRECISION);
        StringBuilder sb = new StringBuilder("0.");
        for (int i = 0; i < k; i++) {
            sb.append(value[i]);
        }
        return Double.parseDouble(sb.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(DIGITS);
        int last = 0;
        for (int i = 0; i < DIGITS; i++) {
            sb.append(value[i]);
            if (value[i] != 0) {
                last = i;
            }
        }
        sb.setLength(last + 1);
        return sb.toString();
    }
}

public class Arithmetic {
    public static void encode(String in) {
        char[] chars = in.toCharArray();
        HashMap<Character, Double> lens = new HashMap<>();
        HashMap<Character, Double> starts = new HashMap<>();
        // Считаем частоту встречаемости букв и записываем в HashMap
        for (char ch : chars) {
            lens.compute(ch, (it, freq) -> freq == null ? 1 : freq + 1);
        }
        double sum = 0;
        // Считаем начало и длину записывую в таблицу
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Arithmetic/table.txt"))) {
            for (Map.Entry<Character, Double> entry : lens.entrySet()) {
                starts.put(entry.getKey(), sum);
                entry.setValue(entry.getValue() / in.length());
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
                sum += entry.getValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BigFloat len = null;
        BigFloat start = new BigFloat();

        for (char ch : chars) {
            // Каждый раз к новой позициии добовляю старт отрезка умноженный на длину предыдущего
            // Длина равна (длиина предыдущего *  длина текущего)
            if (len == null) {
                start.add(new BigFloat(starts.get(ch)));
                len = new BigFloat(lens.get(ch));
            } else {
                start.add(new BigFloat(starts.get(ch)).multiply(len));
                len = len.multiply(new BigFloat(lens.get(ch)));
            }
        }

        BigFloat half = new BigFloat();
        half.value[0] = 5;
        start.add(len.multiply(half));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Arithmetic/encoded.txt"))) {
            writer.write(chars.length + " " + start);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decode(String in, String table) throws IOException {
        Scanner sc = new Scanner(table);
        // Считываем таблицу частот попутно рассчитывая начало и длины, заполняя hashMap
        TreeMap<Double, Character> starts = new TreeMap<>();
        HashMap<Character, Double> lens = new HashMap<>();
        double sum = 0;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.length() == 0 && sc.hasNextLine()) {
                line = '\n' + sc.nextLine();
            }
            char at = line.charAt(0);
            starts.put(sum, at);
            double tmp = Double.parseDouble(line.substring(2));
            lens.put(at, tmp);
            sum += tmp;
        }

        sc = new Scanner(in);
        int k = sc.nextInt();
        BigFloat start = new BigFloat();
        char[] chars = sc.next().toCharArray();
        // Считываем код дробную делая перевод char в число вычитая 0
        for (int i = 0; i < chars.length; i++) {
            start.value[i] = (byte) (chars[i] - '0');
        }
        // Проходим
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Arithmetic/decoded.txt"))) {
            for (int i = k; i > 0; i--) {
                // находим старт меньше значения
                Map.Entry<Double, Character> entry = starts.floorEntry(start.toDouble());
                // Записываем символ
                writer.write(entry.getValue());
                // Вычитаем значения меньшего от старта
                start.minus(new BigFloat(entry.getKey()));
                // Новый старт делю на длину символа от которого начинается отрезок
                start = start.divide(new BigFloat(lens.get(entry.getValue())));
            }
        }
    }
    public static void main(String[] args) {
        try {
            Arithmetic.encode(Files.readString(Path.of("Arithmetic/test.txt")));
            Arithmetic.decode(Files.readString(Path.of("Arithmetic/encoded.txt")),
                    Files.readString(Path.of("Arithmetic/table.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
