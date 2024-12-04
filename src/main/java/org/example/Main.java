package org.example;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    /**
     * Функция объединяет два множества, если они содержат хотя бы одно одинаковое значение
     *
     * @param set1 - первое множество
     * @param set2 - второе множество
     * @return множество, состоящее из элементов первого и второго множества, без повторяющихся значений
     */
    private static ArrayList<Integer> union(ArrayList<Integer> set1, ArrayList<Integer> set2) {
        boolean isGroup = false;
        for (Integer value : set1) {
            if (set2.contains(value)) {
                isGroup = true;
                break;
            }
        }
        if (isGroup) {
            Set<Integer> group = new HashSet<>();
            group.addAll(set1);
            group.addAll(set2);
            return new ArrayList<>(group);
        }
        return null;
    }

    /**
     * Функция проверяет значение в колонке и формирует ключ "значение&номер столбца"
     *
     * @param column - значение в колонке
     * @param index  - индекс колонки
     * @return ключ
     */
    private static String getKey(String column, int index) {
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
        if (column.length() < 2) return false;
        String value = column.substring(1, column.length() - 1);
        if (value.contains("\"")) return false;
        return true;
    }

    /**
     * Функция объединяет множества индексов строк в группы и выводит полученные группы в файл
     *
     * @param map - HashMap, где key - "значение&номер столбца", value - множество индексов строк
     *            повторяющихся значений в стобце
     * @param list - список строк
     */
    private static void unionToGroup(HashMap<String, ArrayList<Integer>> map, ArrayList<ArrayList<String>> list) {
        // собираем все списки индексов строк, в которых есть одинаковые столбцы в каких либо колонках, в один массив
        ArrayList<Integer>[] arr = new ArrayList[map.size()];
        int k = 0;
        for (String key : map.keySet()) {
            arr[k++] = map.get(key);
        }
        // объединяем множества в массиве arr
        for (int i = 0; i < arr.length; i++) {
            // если множество уже было добавлено в группу, то пропускаем
            if (arr[i] == null) continue;
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j] == null) continue;
                // если есть одинаковые значения объединяем два множества
                ArrayList<Integer> result = union(arr[i], arr[j]);
                if (result == null) continue;
                // в массив с индексом i записываем получившееся объединенное множество,
                // в массив с индексов j записываем null
                arr[i] = result;
                arr[j] = null;
            }
        }
        List<ArrayList<Integer>> groups = Arrays.stream(arr)
                .filter(Objects::nonNull)
                .sorted((group1, group2) -> group2.size() - group1.size())
                .toList();

        int n = 1;
        try(FileWriter writer = new FileWriter("groups.txt")){
            writer.write(groups.size() + "\n");
            for (ArrayList<Integer> group : groups) {
                writer.write("Группа " + n++ + "\n");
                for (Integer index : group) {
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
            ArrayList<ArrayList<String>> inputList = new ArrayList<>();
            loop:
            while ((inputLine = reader.readLine()) != null) {
                ArrayList<String> line = new ArrayList<>(Arrays.asList(inputLine.split(";")));
                for (String column : line) {
                    if (!checkValue(column)) continue loop;
                }
                inputList.add(line);
            }
            //удаляем дубликаты строк
            HashSet<ArrayList<String>> set = new HashSet<>();
            ArrayList<ArrayList<String>> list = new ArrayList<>();
            for (ArrayList<String> value : inputList) {
                if (set.add(value)) {
                    list.add(value);
                }
            }
            //создаем HashMap, где key - "значение&номер столбца", value - количество повторяющихся значений в стобце
            HashMap<String, Integer> countMap = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                ArrayList<String> line = list.get(i);
                for (int j = 0; j < line.size(); j++) {
                    String column = line.get(j);
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
                ArrayList<String> line = list.get(i);
                for (int j = 0; j < line.size(); j++) {
                    String column = line.get(j);
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