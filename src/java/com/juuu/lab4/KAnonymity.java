package com.juuu.lab4;

import java.util.*;
import java.util.stream.Collectors;

public class KAnonymity {
    private static final int K = 5;
    private static final List<String> AGE_INTERVALS = Arrays.asList("60-69", "70-79", "80-89", "90-100");

    public static void main(String[] args) {
        // 假设已读取学生列表（需移除最后一行"平均成绩"）
        List<Student> students = ExcelReader.readExcelToObjects("data.xlsx", Student.class);
        students.removeIf(s -> "平均成绩".equals(s.getName())); // 移除汇总行

        // 1. 按性别和原始平均成绩分组，统计各分组大小
        Map<String, List<Student>> initialGroups = groupByGenderAndAvg(students);

        // 2. 校验并合并区间，直至满足k-匿名
        Map<String, List<Student>> anonymizedGroups = mergeIntervalsToMeetK(initialGroups);

        // 3. 生成匿名化结果（打印或保存）
        printAnonymizedData(anonymizedGroups);
    }

    /**
     * 按性别和平均成绩区间分组（初始区间）
     */
    private static Map<String, List<Student>> groupByGenderAndAvg(List<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(s -> 
                    s.getGender() + "_" + getAvgInterval(Double.parseDouble(s.getAvg()))
                ));
    }

    /**
     * 获取平均成绩对应的初始区间
     */
    private static String getAvgInterval(double avg) {
        if (avg >= 60 && avg < 70) return "60-69";
        if (avg >= 70 && avg < 80) return "70-79";
        if (avg >= 80 && avg < 90) return "80-89";
        return "90-100"; // 包含90-100
    }

    /**
     * 合并区间直至每个分组大小≥K
     */
    private static Map<String, List<Student>> mergeIntervalsToMeetK(Map<String, List<Student>> groups) {
        Map<String, List<Student>> result = new LinkedHashMap<>(groups); // 保留分组顺序

        // 遍历所有分组，处理不满足k的组
        List<String> keysToProcess = new ArrayList<>(result.keySet());
        for (String key : keysToProcess) {
            List<Student> group = result.get(key);
            if (group.size() >= K) continue; // 满足条件，跳过

            // 解析性别和区间（格式："男_70-79"）
            String[] parts = key.split("_");
            String gender = parts[0];
            String originalInterval = parts[1];
            int originalIndex = AGE_INTERVALS.indexOf(originalInterval);

            // 尝试合并左侧区间（如"70-79"合并"60-69"）
            if (originalIndex > 0) {
                String mergedKey = gender + "_" + (AGE_INTERVALS.get(originalIndex - 1) + "-" + originalInterval);
                List<Student> mergedGroup = new ArrayList<>(result.getOrDefault(gender + "_" + AGE_INTERVALS.get(originalIndex - 1), new ArrayList<>()));
                mergedGroup.addAll(group);
                if (mergedGroup.size() >= K) {
                    result.put(mergedKey, mergedGroup);
                    result.remove(gender + "_" + AGE_INTERVALS.get(originalIndex - 1)); // 删除原左侧组
                    result.remove(key); // 删除当前组
                    return mergeIntervalsToMeetK(result); // 递归重新校验
                }
            }

            // 尝试合并右侧区间（如"70-79"合并"80-89"）
            if (originalIndex < AGE_INTERVALS.size() - 1) {
                String mergedKey = gender + "_" + (originalInterval + "-" + AGE_INTERVALS.get(originalIndex + 1));
                List<Student> mergedGroup = new ArrayList<>(group);
                mergedGroup.addAll(result.getOrDefault(gender + "_" + AGE_INTERVALS.get(originalIndex + 1), new ArrayList<>()));
                if (mergedGroup.size() >= K) {
                    result.put(mergedKey, mergedGroup);
                    result.remove(gender + "_" + AGE_INTERVALS.get(originalIndex + 1)); // 删除原右侧组
                    result.remove(key); // 删除当前组
                    return mergeIntervalsToMeetK(result); // 递归重新校验
                }
            }

            // 若无法合并（极端情况，如数据总量不足），抛出异常或扩大区间
            throw new IllegalStateException("无法满足5-匿名，需扩大泛化区间！");
        }
        return result;
    }

    /**
     * 打印匿名化结果（泛化姓名和成绩）
     */
    private static void printAnonymizedData(Map<String, List<Student>> groups) {
        groups.forEach((groupKey, students) -> {
            String[] parts = groupKey.split("_");
            String gender = parts[0];
            String avgRange = parts[1];

            // 生成匿名化姓名（如"女_组1"）
            String groupId = groupKey.replaceAll("[^_]", ""); // 提取"_组"部分
            int count = 1;
            for (Student s : students) {
                s.setName(gender + "_组" + groupId + "_" + count++); // 唯一分组内编号
                s.setChinese(avgRange); // 泛化成绩为区间（示例，可扩展到其他成绩）
                s.setMath(avgRange);
                s.setEnglish(avgRange);
                s.setAvg(avgRange);
            }

            // 打印分组信息
            System.out.println("分组：" + groupKey + "（人数：" + students.size() + "）");
            students.forEach(System.out::println);
            System.out.println();
        });
    }
}