package com.juuu.lab4;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

// 示例对象类（需与Excel表头字段名匹配）
@ToString
@NoArgsConstructor
@Data
public class Student {
    private String name;
    private String gender;
    private String chinese;
    private String math;
    private String english;
    private String avg;
}