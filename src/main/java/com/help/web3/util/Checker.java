package com.help.web3.util;

import lombok.experimental.UtilityClass;
@UtilityClass
public class Checker {

    public boolean isOnGraph(double x, double y, double r) {
        return (x <= 0 && y <= 0 && 2*y + x >= -r) ||  //triangle
        (x >= 0 && x <= r && y >= 0 && y <= r/2) ||  //rectangle
        (x >= 0 && y <= 0 && Math.pow(x, 2) + Math.pow(y, 2) <= Math.pow(r / 2, 2)); //circle
    }
}
