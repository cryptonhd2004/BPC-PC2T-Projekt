package projekt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

public class DatabazeUloziste {

    public static void ulozDoSouboru(SpravceZamestnancu spravce, String nazevSouboru) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nazevSouboru))) {

            for (Object objekt : spravce.getDatabaze().values()) {
                Zamestnanec z = (Zamestnanec) objekt;

                String typ = "SPECIALISTA";
                if (z instanceof DatovyAnalytik) {
                    typ = "ANALYTIK";
                }

                writer.write("ZAMESTNANEC;" + z.getId() + ";" + typ + ";" + z.getJmeno() + ";" + z.getPrijmeni() + ";" + z.getRokNarozeni());
                writer.newLine();

                for (Object vazbaObj : z.getSeznamSpolupracovniku().entrySet()) {
                    Map.Entry vazba = (Map.Entry) vazbaObj;
                    writer.write("SPOLUPRACE;" + z.getId() + ";" + vazba.getKey() + ";" + vazba.getValue());
                    writer.newLine();
                }
            }

            System.out.println("Data uspesne ulozena do textoveho souboru.");
        } catch (Exception e) {
            System.out.println("Chyba pri ukladani do souboru: " + e.getMessage());
        }
    }

    public static SpravceZamestnancu nactiZeSouboru(String nazevSouboru) {
        SpravceZamestnancu spravce = new SpravceZamestnancu();
        Map docasnaDatabaze = new TreeMap();

        try (BufferedReader reader = new BufferedReader(new FileReader(nazevSouboru))) {
            String radek;

            while ((radek = reader.readLine()) != null) {
                String[] casti = radek.split(";");

                if (casti[0].equals("ZAMESTNANEC")) {
                    int id = Integer.parseInt(casti[1]);
                    String typ = casti[2];
                    String jmeno = casti[3];
                    String prijmeni = casti[4];
                    int rokNarozeni = Integer.parseInt(casti[5]);

                    Zamestnanec novy;
                    if (typ.equals("ANALYTIK")) {
                        novy = new DatovyAnalytik(id, jmeno, prijmeni, rokNarozeni);
                    } else {
                        novy = new BezpecnostniSpecialista(id, jmeno, prijmeni, rokNarozeni);
                    }

                    docasnaDatabaze.put(id, novy);
                } else if (casti[0].equals("SPOLUPRACE")) {
                    int idZamestnance = Integer.parseInt(casti[1]);
                    int idKolegy = Integer.parseInt(casti[2]);
                    UrovenSpoluprace uroven = UrovenSpoluprace.valueOf(casti[3]);

                    Zamestnanec z = (Zamestnanec) docasnaDatabaze.get(idZamestnance);
                    if (z != null) {
                        z.pridejSpolupracovnika(idKolegy, uroven);
                    }
                }
            }

            spravce.setDatabaze(docasnaDatabaze);
            System.out.println("Data uspesne nactena z textoveho souboru.");
        } catch (Exception e) {
            System.out.println("Chyba pri nacitani ze souboru: " + e.getMessage());
        }

        return spravce;
    }
}