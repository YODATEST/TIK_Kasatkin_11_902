package com.company;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class Huffman {
    // Создаю дерево char c леыым, правым эллементом и приоритетом
    private static class Node {
        public Node l, r;
        public int priority;
        public char ch;

        public Node(char ch) {
            this.ch = ch;
        }

        public Node(Node l, Node r, int priority) {
            this.l = l;
            this.r = r;
            this.priority = priority;
        }
    }

    public static void encode(String in) {
        char[] chars = in.toCharArray();

        HashMap<Character, Node> map = new HashMap<>();
        // Первым проходом добавляю все char и выставляю им приоритет
        for (char ch : chars) {
            map.computeIfAbsent(ch, Node::new).priority++;
        }
        // Добавляю очередь исходя из поля приоритет в Node
        PriorityQueue<Node> queue = new PriorityQueue<>(map.size(), Comparator.comparingInt(a -> a.priority));
        queue.addAll(map.values());
        // Строим дерево
        while (queue.size() > 1) {
            Node l = queue.poll();
            Node r = queue.poll();
            queue.add(new Node(l, r, l.priority + r.priority));
        }

        HashMap<Character, String> codes = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        // Заполняем код
        new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                if (node.l != null) {
                    sb.append('0');
                    this.accept(node.l);
                    sb.deleteCharAt(sb.length() - 1);
                }
                if (node.r != null) {
                    sb.append('1');
                    this.accept(node.r);
                    sb.deleteCharAt(sb.length() - 1);
                }
                if (node.ch != 0) {
                    codes.put(node.ch, sb.toString());
                }
            }
        }.accept(queue.poll());
        // Строим таблицу
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Huffman/table.txt"))) {
            for (Map.Entry<Character, String> entry : codes.entrySet()) {
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Записываем код
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Huffman/encoded.txt"))) {
            for (char ch : chars) {
                writer.write(codes.get(ch));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decode(String in, String table) {
        Scanner sc = new Scanner(table);
        // Считываем таблицу кодирования
        HashMap<String, Character> map = new HashMap<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.length() == 0 && sc.hasNextLine()) {
                line = '\n' + sc.nextLine();
            }
            map.put(line.substring(2), line.charAt(0));
        }
        // Ищем соответствия кода с символом
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Huffman/decoded.txt"))) {
            StringBuilder sb = new StringBuilder();
            for (char ch : in.toCharArray()) {
                sb.append(ch);
                Character code = map.get(sb.toString());
                if (code != null) {
                    writer.write(code);
                    sb.setLength(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        try {
            Huffman.encode(Files.readString(Path.of("Huffman/test.txt")));
            Huffman.decode(Files.readString(Path.of("Huffman/encoded.txt")),
                    Files.readString(Path.of("Huffman/table.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
