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

    public abstract String getNazevSkupiny();

    public void pridejSpolupracovnika(int idKolegy, UrovenSpoluprace uroven) {
        seznamSpolupracovniku.put(idKolegy, uroven);
    }

    public void odeberSpolupracovnika(int idKolegy) {
        seznamSpolupracovniku.remove(idKolegy);
    }

    public int getPocetSpatnychSpolupraci() {
        int pocet = 0;

        for (UrovenSpoluprace uroven : seznamSpolupracovniku.values()) {
            if (uroven == UrovenSpoluprace.SPATNA) {
                pocet++;
            }
        }

        return pocet;
    }

    public int getPocetPrumernychSpolupraci() {
        int pocet = 0;

        for (UrovenSpoluprace uroven : seznamSpolupracovniku.values()) {
            if (uroven == UrovenSpoluprace.PRUMERNA) {
                pocet++;
            }
        }

        return pocet;
    }

    public int getPocetDobrychSpolupraci() {
        int pocet = 0;

        for (UrovenSpoluprace uroven : seznamSpolupracovniku.values()) {
            if (uroven == UrovenSpoluprace.DOBRA) {
                pocet++;
            }
        }

        return pocet;
    }

    public String getPrevladajiciKvalitaSpoluprace() {
        int spatna = getPocetSpatnychSpolupraci();
        int prumerna = getPocetPrumernychSpolupraci();
        int dobra = getPocetDobrychSpolupraci();

        if (seznamSpolupracovniku.isEmpty()) {
            return "zadna";
        }

        if (spatna >= prumerna && spatna >= dobra) {
            return "spatna";
        }

        if (prumerna >= spatna && prumerna >= dobra) {
            return "prumerna";
        }

        return "dobra";
    }

    public String getNazevUrovne(UrovenSpoluprace uroven) {
        switch (uroven) {
            case SPATNA:
                return "spatna";
            case PRUMERNA:
                return "prumerna";
            case DOBRA:
                return "dobra";
            default:
                return "neznamy";
        }
    }

    public String getDetailniVypis() {
        StringBuilder sb = new StringBuilder();

        sb.append("ID: ").append(id).append("\n");
        sb.append("Skupina: ").append(getNazevSkupiny()).append("\n");
        sb.append("Jmeno: ").append(jmeno).append("\n");
        sb.append("Prijmeni: ").append(prijmeni).append("\n");
        sb.append("Rok narozeni: ").append(rokNarozeni).append("\n");
        sb.append("Pocet spolupracovniku: ").append(seznamSpolupracovniku.size()).append("\n");
        sb.append("Spatna spoluprace: ").append(getPocetSpatnychSpolupraci()).append("\n");
        sb.append("Prumerna spoluprace: ").append(getPocetPrumernychSpolupraci()).append("\n");
        sb.append("Dobra spoluprace: ").append(getPocetDobrychSpolupraci()).append("\n");
        sb.append("Prevladajici kvalita: ").append(getPrevladajiciKvalitaSpoluprace()).append("\n");

        sb.append("Spolupracovnici:");
        if (seznamSpolupracovniku.isEmpty()) {
            sb.append(" zadni");
        } else {
            for (Map.Entry<Integer, UrovenSpoluprace> entry : seznamSpolupracovniku.entrySet()) {
                sb.append("\n - ID ")
                  .append(entry.getKey())
                  .append(" (")
                  .append(getNazevUrovne(entry.getValue()))
                  .append(")");
            }
        }

        return sb.toString();
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