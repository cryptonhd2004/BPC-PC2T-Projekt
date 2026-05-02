package projekt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class Zamestnanec implements Serializable {

    private static final long serialVersionUID = 1L;

    protected int id;
    protected String jmeno;
    protected String prijmeni;
    protected int rokNarozeni;
    protected Map<Integer, UrovenSpoluprace> seznamSpolupracovniku;

    public Zamestnanec(int id, String jmeno, String prijmeni, int rokNarozeni) {
        this.id = id;
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.rokNarozeni = rokNarozeni;
        this.seznamSpolupracovniku = new HashMap<>();
    }

    public abstract void spustDovednost(Map<Integer, Zamestnanec> vsiZamestnanci);

    public void pridejSpolupracovnika(int idKolegy, UrovenSpoluprace uroven) {
        seznamSpolupracovniku.put(idKolegy, uroven);
    }

    public void odeberSpolupracovnika(int idKolegy) {
        seznamSpolupracovniku.remove(idKolegy);
    }

    public int getId() {
        return id;
    }

    public String getJmeno() {
        return jmeno;
    }

    public String getPrijmeni() {
        return prijmeni;
    }

    public int getRokNarozeni() {
        return rokNarozeni;
    }

    public Map<Integer, UrovenSpoluprace> getSeznamSpolupracovniku() {
        return seznamSpolupracovniku;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s %s (nar. %d) - Vazeb: %d",
                id, jmeno, prijmeni, rokNarozeni, seznamSpolupracovniku.size());
    }
}