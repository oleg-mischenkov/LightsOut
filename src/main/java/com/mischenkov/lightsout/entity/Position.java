package com.mischenkov.lightsout.entity;

public record Position(int x, int y) {

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
