import java.io.*;
import java.util.ArrayList;

public class Graph {
    int nbSommets;
    int nbArcs;
    private Sommet sommets[];
    private Arc arcs[];
    private boolean sat = false;
    private String modele;

    public Graph(String[] args) {
        nbSommets = Integer.parseInt(args[0]);
        nbArcs = Integer.parseInt(args[1]);

        sommets = new Sommet[nbSommets];
        arcs = new Arc[nbArcs];

        for (int i = 0; i < nbSommets; i++)
            sommets[i] = new Sommet(i+1);

        for (int i = 0; i < nbArcs; i++)
            arcs[i] = new Arc(
                    sommets[Integer.parseInt(args[i*2+2])-1],
                    sommets[Integer.parseInt(args[i*2+3])-1]);


        boolean lastsat = false;
        String lastSolution = "INSAT";
        int iter = 0;
        ArrayList<String> newClause = new ArrayList<>();

        dimacs(iter);

        do {
            startMinisat();
            analyseOutput();
            if(sat) {
                lastsat = true;
                lastSolution = modele;
                addSolution(++iter, lastSolution, newClause);
            }
        } while (sat);


        if(lastsat) {
            System.out.println("SAT en " + iter);
            colorGraph(lastSolution);
            affiche();
        } else {
            System.out.println("INSAT");
        }
    }

    private void colorGraph(String lastSolution) {
        for (String retval: lastSolution.split(" ")) {
            int i = Integer.parseInt(retval);
            if (i > 0)
                sommets[i-1].sendNoyau();
        }
    }

    private void addSolution(int iter, String lastSolution, ArrayList<String> newClause) {
        dimacs(iter);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("kernel.cnf"), true));

            for (String clause : newClause) {
                writer.write(clause);
            }

            String c = "";
            for (String retval: lastSolution.split(" ")) {
                int i = Integer.parseInt(retval);
                if (i > 0) {
                    c += "-" + i + " ";
                }
                else if (i < 0) {
                    c += (i*-1) + " ";
                }
            }

            c += "0\n";
            newClause.add(c);
            writer.write(c);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dimacs(int iter) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("kernel.cnf")));

            writer.write("p cnf " + (nbArcs + nbSommets + iter) +" " + (nbSommets) + "\n");

            for (int i = 0; i < nbArcs; i++) {
                writer.write("-" + (arcs[i].getNameSommet1()) + " -" + (arcs[i].getNameSommet2()) + " 0\n");
            }

            for (int i = 0; i < nbSommets; i++) {
                writer.write("-" + (sommets[i].getName()) + " ");
                for (int j = 0; j < nbArcs; j++) {
                    if(arcs[j].getNameSommet2() == sommets[i].getName())
                    writer.write("-" + (arcs[j].getNameSommet1()) + " ");
                }
                writer.write("0\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMinisat() {
        try {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("./minisat kernel.cnf output");
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void analyseOutput() {
        String ligne = "";
        BufferedReader ficTexte;
        try {
            ficTexte = new BufferedReader(new FileReader(new File("output")));
            if (ficTexte == null) {
                throw new FileNotFoundException("Fichier non trouvé: output");
            }

            ligne = ficTexte.readLine();
            if (ligne != null) {
                if(ligne.equals("SAT")) {
                    sat = true;
                } else {
                    sat = false;
                }
            }

            if(sat && ficTexte != null) {
                ligne = ficTexte.readLine();
                if (ligne == null) {
                    System.out.println("Erreur de récupération du modèle");
                    ficTexte.close();
                    System.exit(1);
                }
                modele = ligne;
            }

            ficTexte.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void affiche() {
        for (int i = 0; i < nbSommets; i++)
            System.out.println(sommets[i].toString());
    }

    public static void main(String[] args) {
        if(args.length < 3) {
            System.out.println("Aucune entrée détecté");
            System.exit(1);
        }

        if(args.length != Integer.parseInt(args[1])*2+2) {
            System.out.println("Entrée incorecte");
            System.exit(1);
        }

        Graph graph = new Graph(args);
    }
}
