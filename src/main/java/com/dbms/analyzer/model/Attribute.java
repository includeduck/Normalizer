package com.dbms.analyzer.model;

public class Attribute {
    private String name;
    private boolean isPrime;

    public Attribute(String name) {
        this.name = name;
        this.isPrime = false;
    }

    public Attribute(String name, boolean isPrime) {
        this.name = name;
        this.isPrime = isPrime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrime() {
        return isPrime;
    }

    public void setPrime(boolean prime) {
        isPrime = prime;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return name.equals(attribute.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
