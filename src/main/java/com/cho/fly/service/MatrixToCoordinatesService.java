package com.cho.fly.service;

import java.util.List;

public interface MatrixToCoordinatesService {

    List matrixToCoordinates(List matrix);

    String writeResultToFile(List result, String type);

    List coordinatesToMatrix(List coordinates);
}
