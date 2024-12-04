package org.example;

import java.io.*;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    /**
     * Функция объединяет два множества
     *
     * @param set1 - первое множество
     * @param set2 - второе множество
     * @return множество, состоящее из элементов первого и второго множества, без повторяющихся значений
     */
    private static ArrayList<Integer> union(ArrayList<Integer> set1, ArrayList<Integer> set2) {
        Set<Integer> group = new HashSet<>();
        group.addAll(set1);
        group.addAll(set2);
        return new ArrayList<>(group);
    }

    /**
     * Функция проверяет значение в колонке и формирует ключ "значение&номер столбца"
     *
     * @param column - значение в колонке
     * @param index  - индекс колонки
     * @return ключ
     */
    private static String getKey(String column, int index) {
        if (column.isEmpty()) return null;
        if (column.length() <= 2) return null;
        String value = column.substring(1, column.length() - 1);
        return value + "&" + index;
    }

    /**
     * Функция проверяет значение в колонке
     *
     * @param column - проверяемое значение
     * @return true - если значение корректно, иначе false
     */
    private static boolean checkValue(String column) {
        if (column.isEmpty()) return true;
        if (column.length() < 2) return false;
        String value = column.substring(1, column.length() - 1);
        if (value.contains("\"")) return false;
        return true;
    }

    public static int find(int i, int[] parent) {
        while (i != parent[i]) {
            i = parent[i];
        }
        return i;
    }

    /**
     * Функция объединяет множества индексов строк в группы и выводит полученные группы в файл
     *
     * @param map  - HashMap, где key - "значение&номер столбца", value - множество индексов строк
     *             повторяющихся значений в стобце
     * @param list - список строк
     */
    private static void unionToGroup(HashMap<String, ArrayList<Integer>> map, List<String[]> list) {
        // собираем все списки индексов строк, в которых есть одинаковые столбцы в каких либо колонках, в один массив
        ArrayList<Integer>[] arr = new ArrayList[map.size()];
        int k = 0;
        for (String key : map.keySet()) {
            arr[k++] = map.get(key);
        }
        int m = map.size();
        int n = list.size();
        int[] rows = new int[n];
        Arrays.fill(rows, Integer.MAX_VALUE);
        // создаем массив для хранения родительской группы
        int[] parent = new int[m];
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }

        /**
         * Для для каждой строки из множества обновляем в массиве rows значение текущей группы.
         * Например для массива arr = [[4,6], [0,1], [0,2,5], [2,3,4], [0,1,5]]
         * устанавливаем в rows для строк 4, 6 - id первого множества [4,6]: rows[4] = 0, rows[6] = 0
         * т.е. строки 4 и 6 являются группой с id = 0, и все остальные множества, если включают эти строки,
         * тоже принадлежат множеству с id = 0. Аналогично для [0, 1]: rows[0] = 1, rows[1] = 1.
         * Для [0, 2, 5] так как rows[0] = 1, то множество должно быть объединено с множеством c id = 1,
         * для этого в parent[2] = 1, и в остальных строках 2 и 5 устанавливаем принадлежность к множеству с id = 1:
         * rows[2] = 1, rows[5] = 1.
         * Для [2, 3, 5] rows[2] = 1, rows[4] = 0, из двух групп (1, 0) выбираем группу с минимальным id:
         * т.е. множество должно быть объединено с  множеством с id = 0, для этого parent[3] = 0, и для группы
         * с id = 1 так же заменяем parent[1] = 0 (так как множества 0, 1, 3 теперь должны быть в одной группе).
         * По аналогии множество [0, 1, 5] принадлежит 1 группе.
         */
        for (int i = 0; i < m; i++) {
            ArrayList<Integer> set = arr[i];
            int min = Integer.MAX_VALUE;
            for (int j = 0; j < set.size(); j++) {
                int group = rows[set.get(j)];
                min = Math.min(min, group);
            }

            if (min == Integer.MAX_VALUE) {
                for (int j = 0; j < set.size(); j++) {
                    rows[set.get(j)] = i;
                }
            } else {
                for (int j = 0; j < set.size(); j++) {
                    int ind = set.get(j);
                    int group = rows[ind];
                    if (group != min && group != Integer.MAX_VALUE) {
                        int oldGroup = rows[ind];
                        parent[oldGroup] = min;
                        rows[ind] = min;
                    }
                    rows[ind] = min;
                }
                parent[i] = min;
            }
        }
        // Проходим по массиву parent и объединяем множества в одну группу в завасимости от значения в массиве parent
        HashMap<Integer, ArrayList<Integer>> hashMap = new HashMap<>();
        for (int i = 0; i < parent.length; i++) {
            int p = find(i, parent);
            ArrayList<Integer> set;
            if (hashMap.containsKey(p)) {
                set = hashMap.get(p);
            } else {
                set = new ArrayList<>();
            }
            ArrayList<Integer> res = union(set, arr[i]);
            hashMap.put(p, res);
        }
        List<Map.Entry<Integer, ArrayList<Integer>>> groups = hashMap.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().size() - entry1.getValue().size())
                .toList();
        int l = 1;
        try (FileWriter writer = new FileWriter("groups2.txt")) {
            writer.write(groups.size() + "\n");
            for (Map.Entry<Integer, ArrayList<Integer>> group : groups) {
                writer.write("Группа " + l++ + "\n");
                for (Integer index : group.getValue()) {
                    String line = String.join(";", list.get(index));
                    writer.write(line + "\n");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String fileName = args[0];
        if (fileName == null || fileName.isEmpty()) {
            System.out.println("Не введен обязательный параметр: наименование файла");
            return;
        }
        long start = System.nanoTime();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String inputLine;
            List<String[]> list = new ArrayList<>();
            HashSet<String> set = new HashSet<>();
            loop:
            while ((inputLine = reader.readLine()) != null) {
                if (set.add(inputLine)) {
                    String[] line = inputLine.split(";");
                    for (String column : line) {
                        if (!checkValue(column)) continue loop;
                    }
                    list.add(line);
                }
            }
            //создаем HashMap, где key - "значение&номер столбца", value - количество повторяющихся значений в стобце
            HashMap<String, Integer> countMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                String[] line = list.get(i);
                for (int j = 0; j < line.length; j++) {
                    String column = line[j];
                    String key = getKey(column, j);
                    if (key == null) continue;
                    int count = 1;
                    if (countMap.containsKey(key)) {
                        count = countMap.get(key) + 1;
                    }
                    countMap.put(key, count);
                }
            }
            //создаем HashMap, где key - "значение&номер столбца", value - множество индексов строк повторяющихся значений в стобце
            HashMap<String, ArrayList<Integer>> rowsMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                String[] line = list.get(i);
                for (int j = 0; j < line.length; j++) {
                    String column = line[j];
                    String key = getKey(column, j);
                    if (key == null) continue;
                    // пропускаем значения, которые встречаются только в одной строке
                    if (countMap.get(key) == 1) continue;
                    ArrayList<Integer> arr;
                    if (rowsMap.containsKey(key)) {
                        arr = rowsMap.get(key);
                    } else {
                        arr = new ArrayList<>();
                    }
                    arr.add(i);
                    rowsMap.put(key, arr);
                }
            }
            unionToGroup(rowsMap, list);
            long end = System.nanoTime();
            System.out.println(end - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}