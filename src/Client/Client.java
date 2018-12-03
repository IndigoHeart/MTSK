package Client;

public class Client {
    Boolean privileged;
    int time_of_shopping;

    public Client(Boolean privi, int tos){
        privileged = privi;
        time_of_shopping = tos;
        System.out.println("generated");
    }
}
