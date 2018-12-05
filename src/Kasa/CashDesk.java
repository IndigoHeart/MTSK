package Kasa;

public class CashDesk {
    int cashdeskNumber;
    int privilegedQueue;
    int nonPrivilegedQueue;
    int suma;

    public int getCashdeskNumber() {
        return cashdeskNumber;
    }

    public void setCashdeskNumber(int cashdeskNumber) {
        this.cashdeskNumber = cashdeskNumber;
    }

    public void addPrivilegedQueue() {
        privilegedQueue++;
        sumaIncrement();
    }

    public void deletePrivilegedQueue() {
        privilegedQueue--;
        sumaDecrement();
    }

    public void addNonPrivilegedQueue() {
        nonPrivilegedQueue++;
        sumaIncrement();
    }

    public void deleteNonPrivilegedQueue() {
        nonPrivilegedQueue--;
        sumaDecrement();
    }

    public int getSuma() {
        return suma;
    }

    public void setSuma(int suma) {
        this.suma = suma;
    }

    public void sumaIncrement(){
        suma++;
    }

    public void sumaDecrement(){
        suma++;
    }

    public int getPrivilegedQueue() {
        return privilegedQueue;
    }

    public int getNonPrivilegedQueue() {
        return nonPrivilegedQueue;
    }

}
