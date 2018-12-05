package Client;

public class Client {
    private Boolean privileged;
    private int timeOfShopping;

    public Boolean getPrivileged() {
        return privileged;
    }

    public void setPrivileged(Boolean privileged) {
        this.privileged = privileged;
    }

    public int getTimeOfShopping() {
        return timeOfShopping;
    }

    public void setTimeOfShopping(int timeOfShopping) {
        this.timeOfShopping = timeOfShopping;
    }

    public Client(Boolean privi, int timeOfShopping){
        privileged = privi;

        this.timeOfShopping = timeOfShopping;
        System.out.println("generated");


    }
}
