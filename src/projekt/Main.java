package projekt;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SpravceZamestnancu spravce = new SpravceZamestnancu();

        boolean bezi = true;

        while (bezi) {
            System.out.println("\n===== DATABAZOVY SYSTEM ZAMESTNANCU =====");
            System.out.println("1 - Pridani zamestnance");
            System.out.println("2 - Pridani spoluprace");
            System.out.println("3 - Odebrani zamestnance");
            System.out.println("4 - Vyhledani zamestnance dle ID");
            System.out.println("5 - Spusteni dovednosti zamestnance");
            System.out.println("6 - Abecedni vypis zamestnancu");
            System.out.println("7 - Statistiky spoluprace");
            System.out.println("8 - Vypis poctu zamestnancu");
            System.out.println("9 - Ulozit data do textoveho souboru");
            System.out.println("10 - Nacist data z textoveho souboru");
            System.out.println("0 - Ukoncit program");
            System.out.print("Vase volba: ");

            int volba = sc.nextInt();
            sc.nextLine();

            switch (volba) {
                case 1:
                    System.out.println("Vyberte skupinu: 1 - Datovy analytik, 2 - Bezpecnostni specialista");
                    int typ = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Jmeno: ");
                    String jmeno = sc.nextLine();

                    System.out.print("Prijmeni: ");
                    String prijmeni = sc.nextLine();

                    System.out.print("Rok narozeni: ");
                    int rok = sc.nextInt();

                    spravce.pridejZamestnance(typ, jmeno, prijmeni, rok);
                    break;

                case 2:
                    System.out.print("Zadejte ID zamestnance: ");
                    int idZam = sc.nextInt();

                    System.out.print("Zadejte ID kolegy: ");
                    int idKol = sc.nextInt();

                    System.out.println("Uroven spoluprace: 1 - SPATNA, 2 - PRUMERNA, 3 - DOBRA");
                    int urovenVolba = sc.nextInt();

                    UrovenSpoluprace uroven = UrovenSpoluprace.PRUMERNA;
                    if (urovenVolba == 1) {
                        uroven = UrovenSpoluprace.SPATNA;
                    } else if (urovenVolba == 3) {
                        uroven = UrovenSpoluprace.DOBRA;
                    }

                    spravce.pridejSpolupraci(idZam, idKol, uroven);
                    break;

                case 3:
                    System.out.print("Zadejte ID zamestnance pro smazani: ");
                    spravce.odeberZamestnance(sc.nextInt());
                    break;

                case 4:
                    System.out.print("Zadejte ID zamestnance: ");
                    Zamestnanec z = spravce.najdiZamestnance(sc.nextInt());
                    if (z != null) {
                        System.out.println(z);
                    } else {
                        System.out.println("Zamestnanec nenalezen.");
                    }
                    break;

                case 5:
                    System.out.print("Zadejte ID zamestnance: ");
                    Zamestnanec dovZam = spravce.najdiZamestnance(sc.nextInt());
                    if (dovZam != null) {
                        dovZam.spustDovednost(spravce.getDatabaze());
                    } else {
                        System.out.println("Zamestnanec nenalezen.");
                    }
                    break;

                case 6:
                    spravce.vypisAbecednePodleSkupin();
                    break;

                case 7:
                    spravce.vypisStatistiky();
                    break;

                case 8:
                    spravce.vypisPocetVeSkupinach();
                    break;

                case 9:
                    System.out.print("Zadejte nazev txt souboru: ");
                    String nazevUloz = sc.nextLine();
                    DatabazeUloziste.ulozDoSouboru(spravce, nazevUloz);
                    break;

                case 10:
                    System.out.print("Zadejte nazev txt souboru: ");
                    String nazevNacti = sc.nextLine();
                    spravce = DatabazeUloziste.nactiZeSouboru(nazevNacti);
                    break;

                case 0:
                    System.out.println("Program ukoncen. Na shledanou!");
                    System.out.println("==============================");
                    System.out.println("Adam Solovic & David Sindelar");
                    System.out.println("========BPC-PC2T 2026========");
                    bezi = false;
                    break;

                default:
                    System.out.println("Neplatna volba, zkuste to znovu.");
            }
        }

        sc.close();
    }
}