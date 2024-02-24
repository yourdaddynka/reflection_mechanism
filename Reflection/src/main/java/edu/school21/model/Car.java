package edu.school21.model;

import java.util.Objects;
import java.util.StringJoiner;

public class Car {
    private String make;
    private String model;
    private int year;

    public Car() {
        this.make = "Default make";
        this.model = "Default model";
        this.year = 0;
    }

    public Car(String make, String model, int year) {
        this.make = make;
        this.model = model;
        this.year = year;
    }

    public void drive() {
        System.out.println("The car is being driven.");
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Car.class.getSimpleName() + "[", "]")
                .add("make='" + make + "'")
                .add("model='" + model + "'")
                .add("year=" + year)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return year == car.year && Objects.equals(make, car.make) && Objects.equals(model, car.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(make, model, year);
    }
}