package projekt;

import java.util.Map;

public class BezpecnostniSpecialista extends Zamestnanec {

    private static final long serialVersionUID = 1L;

    public BezpecnostniSpecialista(int id, String jmeno, String prijmeni, int rokNarozeni) {
        super(id, jmeno, prijmeni, rokNarozeni);
    }

    @Override
    public void spustDovednost(Map<Integer, Zamestnanec> vsiZamestnanci) {
        System.out.println("--- Dovednost: Bezpecnostni specialista (" + jmeno + ") ---");

        if (seznamSpolupracovniku.isEmpty()) {
            System.out.println("Rizikove skore: 0");
            return;
        }

        double skore = 0;

        for (UrovenSpoluprace uroven : seznamSpolupracovniku.values()) {
            switch (uroven) {
                case SPATNA:
                    skore += 10;
                    break;
                case PRUMERNA:
                    skore += 2;
                    break;
                case DOBRA:
                    skore -= 5;
                    break;
            }
        }

        double vysledneRiziko = skore * seznamSpolupracovniku.size();
        System.out.println("Vypocitane rizikove skore spoluprace: " + vysledneRiziko);
    }

    @Override
    public String getNazevSkupiny() {
        return "Bezpecnostni specialista";
    }
}