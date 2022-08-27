package com.cho.fly.controller;

import com.alibaba.fastjson.JSONArray;
import com.cho.fly.entity.Params4Rotating;
import com.cho.fly.service.MatrixToCoordinatesService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
public class WebController {

    @Autowired
    MatrixToCoordinatesService service;

    @RequestMapping(value = "/cho/bb", method = RequestMethod.POST)
    @ResponseBody
    public Object bb(MultipartFile file) {


        List<Object> matrix = getObjects(file);
        Object result = JSONArray.toJSON(matrix);
        List coordinates = service.matrixToCoordinates(matrix);

        // 20220812
        String filename = service.writeResultToFile(coordinates,"mtc");
        System.out.println(coordinates);
        List<Object> origin = new ArrayList<>();
        origin.add(0.0);
        origin.add(0.0);
        coordinates.add(origin);
        Map<Object, Object> resultMap = new HashMap<>();
        resultMap.put("matrix", matrix);
        resultMap.put("coordinates", coordinates);
        resultMap.put("filename", filename);
        System.out.println(coordinates);
        System.out.println(result);
        return resultMap;
    }

    @RequestMapping(value = "/cho/cc", method = RequestMethod.POST)
    @ResponseBody
    public Object cc(MultipartFile file) {
        List<Object> coordinates = getObjects(file);
        List matrix = service.coordinatesToMatrix(coordinates);
        String filename = service.writeResultToFile(matrix,"ctm");
        Map<Object, Object> resultMap = new HashMap<>();
        resultMap.put("matrix", matrix);
        resultMap.put("filename", filename);
        System.out.println(coordinates);
        System.out.println(matrix);
        return resultMap;
    }

    @RequestMapping(value = "/cho/dd", method = RequestMethod.POST)
    @ResponseBody
    public Object dd(@RequestBody Params4Rotating params) {
        List coordinates = params.getCoordinates();
        int rotateDegree = params.getRotateDegree();
        List result = service.rotateCoordinates(coordinates,rotateDegree);
        Map<Object, Object> resultMap = new HashMap<>();
        resultMap.put("coordinates", result);
        return resultMap;
    }

    private List<Object> getObjects(MultipartFile file) {
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
                        String s = sheet.getRow(row).getCell(rol).toString();
                        // 转换成double
                        double sDou = Double.parseDouble(s);
                        tmpList.add(sDou);
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
                        String s = sheet.getRow(row).getCell(rol).toString();
                        // 转换成double
                        double sDou = Double.parseDouble(s);
                        tmpList.add(sDou);
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
                        List<Object> tmpList = new ArrayList<>();
                        for (String s : strings) {
                            double sDou = Double.parseDouble(s);
                            tmpList.add(sDou);
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
                    s = s.trim();
                    String[] row;
                    if (s.contains(",")) {
                        row = s.split(",");

                    } else {
                        row = s.split(" ");
                    }
                    List<Object> tmpList = new ArrayList<>();
                    for (String str : row) {
                        double sDou = Double.parseDouble(str);
                        tmpList.add(sDou);
                    }
                    matrix.add(tmpList);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    private File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException {
        File file = new File("D:\\flymesky\\src\\main\\resources\\static\\temp.txt");
        multipart.transferTo(file);
        return file;
    }

    @RequestMapping("/cho/downloadfile/{filename}")
    @ResponseBody
    public void downloadFile(@PathVariable String filename, HttpServletResponse response, HttpServletRequest request) {
        String filepath = "D:" + File.separator + "flymesky" + File.separator + "src" + File.separator + "main" + File.separator
                + "resources" + File.separator + "static" + File.separator + filename;
        boolean is = myDownLoad(filepath, response);
        if (is)
            System.out.println("成功");
        else
            System.out.println("失败");
    }

    private boolean myDownLoad(String filepath, HttpServletResponse response) {
        File f = new File(filepath);
        if (!f.exists()) {
            try {
                response.sendError(404, "File not found!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        response.setContentType("application/force-download;charset=UTF-8");

        InputStream in = null;
        OutputStream out = null;
        try {

            //获取要下载的文件输入流
            in = new FileInputStream(filepath);
            int len = 0;
            //创建数据缓冲区
            byte[] buffer = new byte[1024];
            //通过response对象获取outputStream流
            out = response.getOutputStream();
            //将FileInputStream流写入到buffer缓冲区
            while ((len = in.read(buffer)) > 0) {
                //使用OutputStream将缓冲区的数据输出到浏览器
                out.write(buffer, 0, len);
            }
            //这一步走完，将文件传入OutputStream中后，页面就会弹出下载框

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
