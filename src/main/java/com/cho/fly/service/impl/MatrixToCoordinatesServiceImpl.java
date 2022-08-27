package com.cho.fly.service.impl;

import com.cho.fly.service.MatrixToCoordinatesService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MatrixToCoordinatesServiceImpl implements MatrixToCoordinatesService {
    @Override
    public List matrixToCoordinates(List matrix) {
        /* 距离矩阵转换坐标的思路是：
         转换成数学问题：已知三角形的三边长（距离矩阵中包含的信息），两个点的坐标，求另一个点的坐标；
         每次取矩阵中的三阶矩阵，迭代求未知点坐标；
         初始点坐标假设为(0,0)，第二个点坐标假设为(matrix12,0)
         三角形三个点分别为A，B，C
         */

        // 三角形上边长
        double sideTop;
        // 三角形下边长
        double sideBot;
        // 三角形对边长
        double sideOpp;
        // 三角形角A的角度
        double angleCAB;
        // 边AC与X轴的夹角
        double angleCAX;
        // 边AB与X轴的夹角
        double angleX;
        // A，B横坐标差Δx
        double deltaX;
        // A，B纵坐标差Δy
        double deltaY;
        // Π
        final double PI = Math.PI;
        // 返回的坐标List
        List<Object> coordinates = new ArrayList<>();


        for (int i = 0; i < matrix.size() - 2; i++) {
            List tempRow1 = (List) matrix.get(i);
            List tempRow2 = (List) matrix.get(i + 1);
            sideTop = (double) tempRow1.get(i + 1);
            sideBot = (double) tempRow1.get(i + 2);
            sideOpp = (double) tempRow2.get(i + 2);
            angleCAB = Math.acos((Math.pow(sideTop, 2) + Math.pow(sideBot, 2) - Math.pow(sideOpp, 2)) / (2 * sideTop * sideBot));

            List<Double> coordinateA = new ArrayList<>();
            List<Double> coordinateB = new ArrayList<>();


            if (i == 0) {
                coordinateA.add(0.0);
                coordinateA.add(0.0);
                coordinateB.add(sideTop);
                coordinateB.add(0.0);
                coordinates.add(coordinateA);
                coordinates.add(coordinateB);
            } else {
                coordinateA = (List<Double>) coordinates.get(i);
                coordinateB = (List<Double>) coordinates.get(i + 1);
            }

            double AX = coordinateA.get(0);
            double BX = coordinateB.get(0);
            double AY = coordinateA.get(1);
            double BY = coordinateB.get(1);

            deltaX = BX - AX;
            deltaY = BY - AY;
            if (deltaY == 0) {
                deltaX = 1e-20;
            }

            angleX = PI - (PI / 2) * sgn(deltaY) - Math.atan(deltaX / deltaY);

            // 第一种可能：点C在AB上方
            angleCAX = angleX + angleCAB;
            if (angleCAX >= 2 * PI) {
                angleCAX = angleCAX - 2 * PI;
            }
            List<Double> coordinateCTmp = getCoordinateC(sideBot, angleCAX, AX, AY);

            // 第二种可能：点C在AB下方
            angleCAX = angleX - angleCAB;
            List<Double> coordinateCTmp2 = getCoordinateC(sideBot, angleCAX, AX, AY);

            if (i == 0) {
                coordinateCTmp.set(0, (double) Math.round(coordinateCTmp.get(0) * 1000)/1000);
                coordinateCTmp.set(1, (double) Math.round(coordinateCTmp.get(1) * 1000)/1000);
                coordinates.add(coordinateCTmp);
            } else {
                List<Double> coordinateC = checkCoordinateC(coordinateCTmp, coordinateCTmp2, coordinates, matrix, i);
                // 坐标保留三位小数
                coordinateC.set(0, (double) Math.round(coordinateC.get(0) * 1000)/1000);
                coordinateC.set(1, (double) Math.round(coordinateC.get(1) * 1000)/1000);
                coordinates.add(coordinateC);
            }
        }
        return coordinates;
    }

    @Override
    public String writeResultToFile(List result, String type) {

        try {

            String filePrefix = "";
            if ("mtc".equals(type)) {
                filePrefix = "coordinates";
            } else if ("ctm".equals(type)) {
                filePrefix = "matrix";
            }
            String filename = filePrefix + generateRandomFilename();
            String pathname = "D:"+ File.separator+"flymesky"+File.separator+"src"+ File.separator+ "main" +File.separator
                    +"resources"+File.separator + "static"+File.separator + filename;
            File file = new File(pathname);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (int i = 0; i < result.size(); i++) {
                List tempRow = (List) result.get(i);
                for (Object s: tempRow) {
                    double tmp = (double) s;
                    bufferedWriter.write(tmp + " ");
                }
//                String s = tempRow.get(0) + " " + tempRow.get(1);
//                bufferedWriter.write(s);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            return filename;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List coordinatesToMatrix(List coordinates) {
        List<Object> result = new ArrayList<>();
        int dimension = coordinates.size();
        for (int i = 0; i < dimension; i++) {
            List<Object> tmpRow = new ArrayList<>();
            List tmpCoordinate1 = (List) coordinates.get(i);
            for (int j = 0; j < dimension; j++) {
                double tmpElement = 0;
                if (j < i) {
                    List rowPre = (List) result.get(j);
                    tmpElement = (double) rowPre.get(i);
                }else if (j == i) {
                    tmpElement = 0;
                }else {
                    List tmpCoordinate2 = (List) coordinates.get(j);
                    tmpElement = calDistance(tmpCoordinate1, tmpCoordinate2);
                }

                tmpRow.add(tmpElement);
            }
            result.add(tmpRow);
        }
        return result;
    }

    @Override
    public List rotateCoordinates(List coordinates, int degreeRotate) {
        List<Object> result = new ArrayList<>();
        double rotate = (degreeRotate/180.0)*Math.PI;
        for (Object tmpCo:coordinates) {
            List coor = (List) tmpCo;
            double x = Double.parseDouble(String.valueOf(coor.get(0)));
            double y = Double.parseDouble(String.valueOf(coor.get(1)));
            List<Object> coordinateEach = new ArrayList<>();
            if (x == 0 && y == 0) {
                coordinateEach.add(0);
                coordinateEach.add(0);
                result.add(coordinateEach);
                continue;
            }
            double degreeX;
            if (x>0) {
                double degreeTmp = Math.atan(y/x);
                if (degreeTmp < 0) {
                    degreeX = degreeTmp + 2*Math.PI;
                }else {
                    degreeX = degreeTmp;
                }
            } else if (x < 0) {
                degreeX = Math.atan(y/x) + Math.PI;
            } else {
                if (y > 0) {
                    degreeX = Math.PI/2;
                }else {
                    degreeX = 3*Math.PI/2;
                }
            }
            double degreeResult = degreeX + rotate;
            double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            double xResult = distance * Math.cos(degreeResult);
            double yResult = distance * Math.sin(degreeResult);
            coordinateEach.add(xResult);
            coordinateEach.add(yResult);
            result.add(coordinateEach);
        }
        return result;
    }

    private double calDistance(List tmpCoordinate1, List tmpCoordinate2) {
        double x1 = (double) tmpCoordinate1.get(0);
        double x2 = (double) tmpCoordinate2.get(0);
        double y1 = (double) tmpCoordinate1.get(1);
        double y2 = (double) tmpCoordinate2.get(1);
        double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        double ret = (double) Math.round(distance * 1000)/1000;
        return ret;
    }

    private List<Double> checkCoordinateC(List<Double> coordinateCTmp, List<Double> coordinateCTmp2, List<Object> coordinates, List matrix, int i) {
        for (int j = 0; j < i; j++) {
            List tempRow = (List) matrix.get(j);
            double tempLen = (double) tempRow.get(i + 2);
            List<Double> tempCoordinate = (List<Double>) coordinates.get(j);

            Double tempX = tempCoordinate.get(0);
            Double tempY = tempCoordinate.get(1);

            double s0 = Math.pow(Math.abs(coordinateCTmp.get(0) - tempX),2) + Math.pow(Math.abs(coordinateCTmp.get(1) - tempY),2);
            double s1 = Math.pow(Math.abs(coordinateCTmp2.get(0) - tempX),2) + Math.pow(Math.abs(coordinateCTmp2.get(1) - tempY),2);

            double absS0 = Math.abs(Math.pow(tempLen,2) - s0);
            double absS1 = Math.abs(Math.pow(tempLen,2) - s1);

            if (absS0 < absS1) {
                return coordinateCTmp;
            }else if (absS0 > absS1) {
                return coordinateCTmp2;
            }
        }
        return coordinateCTmp;
    }

    private List<Double> getCoordinateC(double sideBot, double angleCAX, double AX, double AY) {
        List<Double> coordinateC = new ArrayList<>();
        double CX;
        double CY;
        CX = AX + sideBot * Math.cos(angleCAX);
        CY = AY + sideBot * Math.sin(angleCAX);
        coordinateC.add(CX);
        coordinateC.add(CY);
        return coordinateC;
    }

    private int sgn(double deltaY) {
        if (deltaY >= 0) {
            return 1;
        } else {
            return -1;
        }
    }

    //生成随机文件名
    public String generateRandomFilename(){
        String uuid = UUID.randomUUID().toString().replaceAll("-","").substring(0,6);
        String fileName = uuid + ".txt";
        return fileName;
    }
}
