package com.company;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

//2^k >= k + m + 1 (2^k > k + m)
//k >= log2(k+m+1)
//k = ceil(log2(k+m+1))
//=====================
//пусть k = m, 2^m > 2m => k < m + 1 для всех m > 2
//k = ceil(log2(k+m+1)) <= ceil(log2(2(m+1)) = ceil(log2(m+1) + 1)
//ceil(log2(m+1)) <= k <= ceil(log2(m+1) + 1)
public class Hamming {
    public static void encode(String in) throws IOException {
        int len = in.length();
        int m = len;
        int k = 0;
        // Находим количество инфо бит
        while (m > 0) {
            m >>= 1;
            k++;
        }
        // ceil(log2(m+1)) <= k <= ceil(log2(m+1) + 1)
        if (1 << k <= k + len) {
            k++;
        }

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("Hamming/encoded.txt"));

        m = 3;
        // Ставим два первых контрольных символа
        out.write('0');
        out.write('0');
        // Заводим массив контрольных бит
        boolean[] a = new boolean[k];
        // Проходим по файлу
        for (char c : in.toCharArray()) {
            // Если элемент является степенью двойки, тогда это контрольный (мы его скипаем записывая 0)
            if ((m & (m - 1)) == 0) {
                out.write('0');
                m++;
            }
            // Потом записываем сам э лемент
            out.write(c);
            // Если элемент 1 тогда смотрим с какими контрольными битами его xor(им) это имеет смысл только для 1
            if (c == '1') {
                for (byte j = 0; j < k; j++) {
                    // Если на j месте стоит единица мы инвертируем биты в boolean массиве
                    if ((m & (1 << j)) != 0) {
                        a[j] = !a[j];
                    }
                }
            }
            m++;
        }
        out.close();
        RandomAccessFile raf = new RandomAccessFile(new File("Hamming/encoded.txt"), "rw");
        // На месте контрольного бита при 1 нашего элемента в a массива ставим 1 на место контрольного
        for (byte j = 0; j < k; j++) {
            if (a[j]) {
                raf.seek((1L << j) - 1);
                raf.write('1');
            }
        }
        raf.close();
    }

    public static void decode(String in) throws IOException {
        int m = in.length() - 1;
        byte k = 0;
        // Находим количество инфо бит
        // k = ceil(log2(k+m+1))
        while (m > 0) {
            m >>= 1;
            k++;
        }
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("./Hamming/decoded.txt"));

        m = 1;
        boolean[] a = new boolean[k];
        for (char c : in.toCharArray()) {
            // Если элемент не является степенью двойки, тогда мы его просто записываем
            if ((m & (m - 1)) != 0) {
                out.write(c);
            }
            // Если элемент 1 тогда смотрим с какими контрольными битами его xor(им) это имеет смысл только для 1
            if (c == '1') {
                for (byte j = 0; j < k; j++) {
                    // Если на j месте стоит единица мы инвертируем биты в boolean массиве
                    if ((m & (1 << j)) != 0) {
                        a[j] = !a[j];
                    }
                }
            }
            m++;
        }
        out.close();
        m = 0;
        int max = 0;
        // Загнать все биты в обратном порядке для получения
        for (byte j = (byte) (k - 1); j >= 0; j--) {
            m <<= 1;
            if (a[j]) {
                m += 1;
                // Запоминаем последний разряд
                if (max == 0) {
                    max = j;
                }
            }
        }
        int c;
        if (m != 0) {
            System.out.println("Найдена ошибка в " + m + " разряде");
            // Если на m месте не степень двойки (информативный)
            if ((m & (m - 1)) != 0) {
                m -= max + 2; // +2 так как булеан массив с 0 и считывание с файла с 0
                // Меняем ошибочный бит
                RandomAccessFile raf = new RandomAccessFile(new File("Hamming/decoded.txt"), "rw");
                raf.seek(m);
                c = raf.read();
                raf.seek(m);
                raf.write(c == '1' ? '0' : '1');
                raf.close();
            }
        } else {
            System.out.println("Ошибок не найдено");
        }
    }

    public static void main(String[] args) {
        try {
            Hamming.encode(Files.readString(Path.of("Hamming/test.txt")));
            Hamming.decode(Files.readString(Path.of("Hamming/encoded.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
