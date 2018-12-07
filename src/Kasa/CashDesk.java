package Kasa;

import java.util.Random;

public class CashDesk {
    int cashdeskNumber;
    int privilegedQueue;
    int nonPrivilegedQueue;
    int suma;
    int serviceTime;
    Boolean isOpen;
    static Random rm = new Random();
    public static int ID = 0;
    public CashDesk() {
        cashdeskNumber = ++ID;
        privilegedQueue = 0;
        nonPrivilegedQueue = 0;
        suma = 0;
        isOpen = true;
        //serviceTime = rm.nextInt(20)+10;
        serviceTime = 100;
    }

    public int getCashdeskNumber() {
        return cashdeskNumber;
    }

    public void setCashdeskNumber(int cashdeskNumber) {
        this.cashdeskNumber = cashdeskNumber;
    }

    public void addPrivilegedQueue() {
        System.out.println("Dodanie uprzywilejowanego klienta do kasy " + cashdeskNumber);
        privilegedQueue++;
        sumaIncrement();
    }

    public void deletePrivilegedQueue() {
        System.out.println("Uprzywilejowany klient obsluzony w kasie " + cashdeskNumber);
        privilegedQueue--;
        sumaDecrement();
        serviceTime = rm.nextInt(10)+10;
}

    public void addNonPrivilegedQueue() {
        System.out.println("Dodanie zwykłego klienta do kasy " + cashdeskNumber);
        nonPrivilegedQueue++;
        sumaIncrement();
    }

    public void deleteNonPrivilegedQueue() {
        System.out.println("Zwykły klient obsluzony w kasie " + cashdeskNumber);
        nonPrivilegedQueue--;
        sumaDecrement();
        serviceTime = rm.nextInt(10)+10;
    }

    public void decrementServiceTime(){
        if(serviceTime==0){
            if(privilegedQueue>0)
                deletePrivilegedQueue();
            else if(nonPrivilegedQueue>0)
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
