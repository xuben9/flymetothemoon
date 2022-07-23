package com.cho.fly.controller;

import com.alibaba.fastjson.JSONArray;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@Controller
public class WebController {

    @RequestMapping(value = "/cho/bb", method = RequestMethod.POST)
    @ResponseBody
    public Object bb(MultipartFile file) {


        final String XLSX = ".xlsx";
        final String XLS = ".xls";
        final String CSV = ".csv";
        final String TXT = ".txt";

        List<Object> matrix = new ArrayList<>();
        try {
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            if (fileName.endsWith(XLSX)) {
                XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
                //获取最后一行的num，即总行数。此处从0开始
                int maxRow = sheet.getLastRowNum();
                for (int row = 0; row <= maxRow; row++) {
                    //获取最后单元格num，即总单元格数 ***注意：此处从1开始计数***
                    int maxRol = sheet.getRow(row).getLastCellNum();
                    System.out.println("--------第" + row + "行的数据如下--------");
                    ArrayList<Object> tmpList = new ArrayList<>();
                    for (int rol = 0; rol < maxRol; rol++) {
                        tmpList.add(sheet.getRow(row).getCell(rol).toString());
                        System.out.print(sheet.getRow(row).getCell(rol).toString() + "  ");
                    }
                    matrix.add(tmpList);
                    System.out.println();
                }
            }
            if (fileName.endsWith(XLS)) {
                HSSFWorkbook hssfWorkbook = new HSSFWorkbook(inputStream);
                HSSFSheet sheet = hssfWorkbook.getSheetAt(0);
                //获取最后一行的num，即总行数。此处从0开始
                int maxRow = sheet.getLastRowNum();
                for (int row = 0; row <= maxRow; row++) {
                    //获取最后单元格num，即总单元格数 ***注意：此处从1开始计数***
                    int maxRol = sheet.getRow(row).getLastCellNum();
                    System.out.println("--------第" + row + "行的数据如下--------");
                    ArrayList<Object> tmpList = new ArrayList<>();
                    for (int rol = 0; rol < maxRol; rol++) {
                        tmpList.add(sheet.getRow(row).getCell(rol).toString());
                        System.out.print(sheet.getRow(row).getCell(rol).toString() + "  ");
                    }
                    matrix.add(tmpList);
                    System.out.println();
                }
            }
            if (fileName.endsWith(CSV)) {
                CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
                try {
                    while (true) {
                        String[] strings = csvReader.readNext();
                        if (null == strings) {
                            break;
                        }
                        ArrayList<Object> tmpList = new ArrayList<>();
                        for (String s : strings) {
                            tmpList.add(s);
                            System.out.print(s + " ");
                        }
                        matrix.add(tmpList);
                        System.out.println();
                    }
                } catch (CsvValidationException e) {
                    e.printStackTrace();
                }
            }
            if (fileName.endsWith(TXT)) {
                File txtFile = multipartToFile(file);
                BufferedReader bufferedReader = new BufferedReader(new FileReader(txtFile));
                while (true) {
                    String s = bufferedReader.readLine();
                    if (null == s) {
                        bufferedReader.close();
                        break;
                    }
                    String[] row;
                    if (s.contains(",")) {
                        row = s.split(",");
                    }else {
                        row = s.split(" ");
                    }
                    matrix.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object result = JSONArray.toJSON(matrix);
        System.out.println(result);
        return result;
    }

    private File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException
    {
        File file = new File("D:\\flymesky\\src\\main\\resources\\static\\temp.txt");
        multipart.transferTo(file);
        return file;
    }
}
