package com.juuu.lab4;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    /**
     * 读取 Excel 并转换为对象列表（表头在第一行，后续行是对象数据）
     * @param excelFilePath 类路径下的 Excel 文件路径（如 "学生信息.xlsx"）
     * @param clazz 对象的类型（如 Student.class）
     * @param <T> 对象类型泛型
     * @return 对象列表
     */
    public static <T> List<T> readExcelToObjects(String excelFilePath, Class<T> clazz) {
        List<T> objectList = new ArrayList<>();

        try (InputStream is = ExcelReader.class.getClassLoader().getResourceAsStream(excelFilePath);
             Workbook workbook = createWorkbook(is, excelFilePath)) {

            // 校验文件是否存在
            if (is == null) {
                throw new IOException("文件未找到：" + excelFilePath);
            }

            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.out.println("Excel 中无有效工作表");
                return objectList;
            }

            // 读取表头（第一行）
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                System.out.println("表头行不存在（第一行无数据）");
                return objectList;
            }
            List<String> headers = getHeaders(headerRow);

            // 遍历数据行（从第二行开始）
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row dataRow = sheet.getRow(rowNum);
                if (dataRow == null) continue; // 跳过空行

                // 将数据行转换为对象
                T object = rowToObject(dataRow, headers, clazz);
                if (object != null) {
                    objectList.add(object);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return objectList;
    }

    /**
     * 根据文件后缀创建 Workbook（支持 .xlsx 和 .xls）
     */
    private static Workbook createWorkbook(InputStream is, String filePath) throws IOException {
        if (filePath.endsWith(".xlsx")) {
            return new XSSFWorkbook(is);
        } else if (filePath.endsWith(".xls")) {
            return new HSSFWorkbook(is);
        } else {
            throw new IOException("不支持的文件格式，仅支持 .xlsx 和 .xls");
        }
    }

    /**
     * 读取表头（第一行）
     */
    private static List<String> getHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell).trim();
            headers.add(header);
        }
        return headers;
    }

    /**
     * 将数据行转换为对象（通过反射设置字段值）
     */
    private static <T> T rowToObject(Row dataRow, List<String> headers, Class<T> clazz) {
        try {
            T object = clazz.getDeclaredConstructor().newInstance();
            DataFormatter formatter = new DataFormatter();

            // 遍历每个列（表头对应对象的字段）
            for (int colNum = 0; colNum < headers.size(); colNum++) {
                String fieldName = headers.get(colNum); // 表头对应对象的字段名
                String cellValue = formatter.formatCellValue(dataRow.getCell(colNum));

                // 通过反射设置对象字段值
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                setFieldValue(object, field, cellValue);
            }
            return object;
        } catch (Exception e) {
            System.out.println("行 " + dataRow.getRowNum() + " 转换失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 根据字段类型设置值（支持 String/int/double 等常见类型）
     */
    private static void setFieldValue(Object object, Field field, String value) throws Exception {
        Class<?> fieldType = field.getType();
        Object convertedValue;

        if (value == null || value.isEmpty()) {
            convertedValue = null; // 空值处理
        } else if (fieldType == String.class) {
            convertedValue = value;
        } else if (fieldType == int.class || fieldType == Integer.class) {
            convertedValue = Integer.parseInt(value);
        } else if (fieldType == double.class || fieldType == Double.class) {
            convertedValue = Double.parseDouble(value);
        } else if (fieldType == long.class || fieldType == Long.class) {
            convertedValue = Long.parseLong(value);
        } else {
            throw new IllegalArgumentException("不支持的字段类型: " + fieldType);
        }

        field.set(object, convertedValue);
    }

    public static void main(String[] args) {
        // 假设类路径下有 "学生信息.xlsx" 文件，表头为 "姓名 年龄 身高"
        List<Student> students = readExcelToObjects("data.xlsx", Student.class);

        // 打印结果
        System.out.println("读取到的学生数据：");
        for (Student student : students) {
            System.out.println(student);
        }
    }
}
    