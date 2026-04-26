package projekt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SpravceZamestnancu implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map databaze;
    private int dalsiId;

    public SpravceZamestnancu() {
        this.databaze = new TreeMap<>();
        this.dalsiId = 1;
    }

    public void pridejZamestnance(int typSkupiny, String jmeno, String prijmeni, int rokNarozeni) {
        Zamestnanec novy = null;

        if (typSkupiny == 1) {
            novy = new DatovyAnalytik(dalsiId, jmeno, prijmeni, rokNarozeni);
        } else if (typSkupiny == 2) {
            novy = new BezpecnostniSpecialista(dalsiId, jmeno, prijmeni, rokNarozeni);
        }

        if (novy != null) {
            databaze.put(dalsiId, novy);
            System.out.println("Zamestnanec uspesne pridan. Pridelene ID: " + dalsiId);
            dalsiId++;
        } else {
            System.out.println("Neznamy typ skupiny.");
        }
    }

    public void pridejSpolupraci(int idZamestnance, int idKolegy, UrovenSpoluprace uroven) {
        if (databaze.containsKey(idZamestnance) && databaze.containsKey(idKolegy)) {
            if (idZamestnance == idKolegy) {
                System.out.println("Chyba: Zamestnanec nemuze spolupracovat sam se sebou.");
                return;
            }

            Zamestnanec zamestnanec = (Zamestnanec) databaze.get(idZamestnance);
            zamestnanec.pridejSpolupracovnika(idKolegy, uroven);
            System.out.println("Spoluprace uspesne zaevidovana.");
        } else {
            System.out.println("Chyba: Jedno nebo obe zadana ID neexistuji.");
        }
    }

    public void odeberZamestnance(int id) {
        if (databaze.remove(id) != null) {
            for (Object objekt : databaze.values()) {
                Zamestnanec z = (Zamestnanec) objekt;
                z.odeberSpolupracovnika(id);
            }
            System.out.println("Zamestnanec a vsechny jeho vazby byly uspesne odstraneny.");
        } else {
            System.out.println("Chyba: Zamestnanec s timto ID nebyl nalezen.");
        }
    }

    public Zamestnanec najdiZamestnance(int id) {
        return (Zamestnanec) databaze.get(id);
    }

    public void vypisAbecednePodleSkupin() {
        List analytici = new ArrayList();
        List specialisti = new ArrayList();

        for (Object objekt : databaze.values()) {
            Zamestnanec z = (Zamestnanec) objekt;
            if (z instanceof DatovyAnalytik) {
                analytici.add(z);
            } else if (z instanceof BezpecnostniSpecialista) {
                specialisti.add(z);
            }
        }

        Comparator podlePrijmeni = Comparator.comparing(o -> ((Zamestnanec) o).getPrijmeni());
        analytici.sort(podlePrijmeni);
        specialisti.sort(podlePrijmeni);

        System.out.println("\n--- DATOVI ANALYTICI ---");
        for (Object objekt : analytici) {
            Zamestnanec z = (Zamestnanec) objekt;
            System.out.println(z.getPrijmeni() + " " + z.getJmeno() + " (ID: " + z.getId() + ")");
        }

        System.out.println("\n--- BEZPECNOSTNI SPECIALISTE ---");
        for (Object objekt : specialisti) {
            Zamestnanec z = (Zamestnanec) objekt;
            System.out.println(z.getPrijmeni() + " " + z.getJmeno() + " (ID: " + z.getId() + ")");
        }
    }

    public void vypisStatistiky() {
        if (databaze.isEmpty()) {
            System.out.println("Databaze je prazdna, nelze generovat statistiky.");
            return;
        }

        int spatna = 0;
        int prumerna = 0;
        int dobra = 0;

        Zamestnanec nejviceVazeb = null;
        int maxVazeb = -1;

        for (Object objekt : databaze.values()) {
            Zamestnanec z = (Zamestnanec) objekt;

            for (Object urovenObj : z.getSeznamSpolupracovniku().values()) {
                UrovenSpoluprace u = (UrovenSpoluprace) urovenObj;
                if (u == UrovenSpoluprace.SPATNA) {
                    spatna++;
                } else if (u == UrovenSpoluprace.PRUMERNA) {
                    prumerna++;
                } else if (u == UrovenSpoluprace.DOBRA) {
                    dobra++;
                }
            }

            int pocetVazeb = z.getSeznamSpolupracovniku().size();
            if (pocetVazeb > maxVazeb) {
                maxVazeb = pocetVazeb;
                nejviceVazeb = z;
            }
        }

        System.out.println("\n--- STATISTIKY SPOLUPRACE ---");
        System.out.println("Spatnych: " + spatna);
        System.out.println("Prumernych: " + prumerna);
        System.out.println("Dobrych: " + dobra);

        if (nejviceVazeb != null) {
            System.out.println("Nejvice vazeb ma: " + nejviceVazeb.getJmeno() + " "
                    + nejviceVazeb.getPrijmeni() + " (" + maxVazeb + ")");
        }
    }

    public void vypisPocetVeSkupinach() {
        int analytici = 0;
        int specialisti = 0;

        for (Object objekt : databaze.values()) {
            Zamestnanec z = (Zamestnanec) objekt;
            if (z instanceof DatovyAnalytik) {
                analytici++;
            } else if (z instanceof BezpecnostniSpecialista) {
                specialisti++;
            }
        }

        System.out.println("\n--- STAV ZAMESTNANCU ---");
        System.out.println("Datovi analytici: " + analytici);
        System.out.println("Bezpecnostni specialistove: " + specialisti);
        System.out.println("Celkem ve firme: " + (analytici + specialisti));
    }

    public Map getDatabaze() {
        return databaze;
    }
}