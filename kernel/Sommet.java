public class Sommet {
    private int name;
    private boolean inNoyau = false;

    public Sommet(int name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Sommet " + (name) + ", Noyau : " + inNoyau;
    }

    public int getName() {
        return name;
    }

    public void sendNoyau() {
        inNoyau = true;
    }

}
