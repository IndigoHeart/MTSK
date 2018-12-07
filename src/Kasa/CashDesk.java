package Kasa;

import java.util.Random;

public class CashDesk {
    int cashdeskNumber;
    int privilegedQueue;
    int nonPrivilegedQueue;
    int suma;
    int serviceTime;
    Boolean isOpen;
    Random rm;
    public static int ID = 0;

    public CashDesk() {
        cashdeskNumber = ++ID;
        privilegedQueue = 0;
        nonPrivilegedQueue = 0;
        suma = 0;
        isOpen = true;
        serviceTime = rm.nextInt(10)+10;
    }

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
        serviceTime = rm.nextInt(10)+10;
}

    public void addNonPrivilegedQueue() {
        nonPrivilegedQueue++;
        sumaIncrement();
    }

    public void deleteNonPrivilegedQueue() {
        nonPrivilegedQueue--;
        sumaDecrement();
        serviceTime = rm.nextInt(10)+10;
    }

    public void decrementServiceTime(){
        if(serviceTime==0){
            if(privilegedQueue>0)
                deletePrivilegedQueue();
            else
                deleteNonPrivilegedQueue();
        }else
            serviceTime--;
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
        suma--;
    }

    public int getPrivilegedQueue() {
        return privilegedQueue;
    }

    public int getNonPrivilegedQueue() {
        return nonPrivilegedQueue;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
    }

    public Boolean getOpen() {
        return isOpen;
    }
}
