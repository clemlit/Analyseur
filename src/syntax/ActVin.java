package syntax;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;

import utils.*;
import lex.*;

/**
* La classe ActVin met en oeuvre les actions de l'automate d'analyse syntaxique de l'application Vin
*  des fiches de livraison de vin
* 
* @author Trinome 5, Groupe 3 : MESSAGER Clément, MOUTALABI Adebayo Habib, LEPORT Etel
* 
* janvier 2025
*/
public class ActVin extends AutoVin {

    /** table des actions */
    private final int[][] ACTION =
    {/* Etat        BJ    BG   IDENT  NBENT VIRG PTVIRG BARRE AUTRES  */
	/* 0 */      { 10,   10,    1,    10,   10,   10,    9,   10   },
	/* 1 */      {  3,    2,    4,     8,   10,   10,   10,   10   },
	/* 2 */      {  3,    2,    4,    10,   10,   10,   10,   10   },
	/* 3 */      { 10,   10,    4,    10,   10,   10,   10,   10   },
	/* 4 */      { 10,   10,   10,     5,   10,   10,   10,   10   },
	/* 5 */      { 10,   10,    5,    10,    7,    6,   10,   10   },
	/* 6 */      {  3,    2,    4,     8,   10,   10,   10,   10   },
	/*!!! TODO !!!*/
	/* Rappel conventions :  action -1 = action vide, pas de ligne pour etatFinal */
    } ;	       
    
    /** constructeur classe ActVin
	 * @param flot : donnee a analyser
	 */
    public ActVin(InputStream flot) {
    	super(flot);
    }
    
    /** definition de la methode abstraite getAction de Automate 
   	 * 
   	 * @param etat : code de l'etat courant
   	 * @param itemLex : code de l'item lexical courant
   	 * @return code de l'action suivante
   	 **/
   	public int getAction(int etat, int itemLex) {
   		return ACTION[etat][itemLex];
   	}
   	
   	/**
   	 * definition methode abstraite initAction de Automate
   	 */
   	public void initAction() {
   		// Correspond a l'action 0 a effectuer a l'init
   		initialisations();
   	}

   	/** definition de la methode abstraite faireAction de Automate 
   	 * 
   	 * @param etat : code de l'etat courant
   	 * @param itemLex : code de l'item lexical courant
   	 * @return code de l'etat suivant
   	 **/
   	public void faireAction(int etat, int itemLex) {
   		executer(ACTION[etat][itemLex]);
   	}

    /** types d'erreurs detectees */
	private static final int FATALE = 0, NONFATALE = 1;
	
	/** gestion des erreurs 
	 * @param tErr type de l'erreur (FATALE ou NONFATALE)
	 * @param messErr message associe a l'erreur
	 */
	private void erreur(int tErr, String messErr) {
		Lecture.attenteSurLecture(messErr);
		switch (tErr) {
		case FATALE:
			errFatale = true;
			break;
		case NONFATALE:
			etatCourant = etatErreur;
			break;
		default:
			Lecture.attenteSurLecture("parametre incorrect pour erreur");
		}
	}
	
	/**
	 * acces a un attribut lexical 
	 * cast pour preciser que analyseurLexical est ici de type LexVin
	 * @return valEnt associe a l'unite lexicale NBENTIER
	 */
	private int valEnt() {
		return ((LexVin)analyseurLexical).getValEnt();
	}
	/**
	 * acces a un attribut lexical 
	 * cast pour preciser que analyseurLexical est de type LexVin
	 * @return numId associe a l'unite lexicale IDENT
	 */
	private int numIdCourant() {
		return ((LexVin)analyseurLexical).getNumIdCourant();
	}
	
	/** taille d'une colonne pour affichage ecran */
	private static final int MAXLGID = 20;
	/** nombre maximum de chauffeurs */
	private static final int MAXCHAUF = 10;
	/** tableau des chauffeurs et resume des livraison de chacun */
	private Chauffeur[] tabChauf = new Chauffeur[MAXCHAUF];
	
	
	/** utilitaire d'affichage a l'ecran 
	 * @param ch est une chaine de longueur quelconque
	 * @return chaine ch cadree a gauche sur MAXLGID caracteres
	 * */
	private String chaineCadrageGauche(String ch) {
		int lgch = Math.min(MAXLGID, ch.length());
		String chres = ch.substring(0, lgch);
		for (int k = lgch; k < MAXLGID; k++)
			chres = chres + " ";
		return chres;
	} 
	
	/** affichage de tout le tableau de chauffeurs a l'ecran 
	 * */
	private void afficherChauf() {
		Ecriture.ecrireStringln("");
		String titre = "CHAUFFEUR                   BJ        BG       ORD     NBMAG\n"
				+ "---------                   --        --       ---     -----";
		Ecriture.ecrireStringln(titre);
		for (int i = 0; i <= indChauf; i++) {
	        if (tabChauf[i] != null) {  // Vérifie que l'élément est non nul avant d'accéder à ses propriétés
	            String idChaufCourant = ((LexVin)analyseurLexical).chaineIdent(tabChauf[i].numChauf);
	            Ecriture.ecrireString(chaineCadrageGauche(idChaufCourant));
	            Ecriture.ecrireInt(tabChauf[i].bj, 10);
	            Ecriture.ecrireInt(tabChauf[i].bg, 10);
	            Ecriture.ecrireInt(tabChauf[i].ordin, 10);
	            Ecriture.ecrireInt(tabChauf[i].magDif.size(), 10);
	            Ecriture.ecrireStringln("");
	        } else {
	            Ecriture.ecrireStringln("Chauffeur à l'indice " + i + " non initialisé.");
	        }
	    }
	} 
	
	
	/** indice courant du nombre de chauffeurs dans le tableau tabChauf */
	private int indChauf ;
	private int indMagasin;
	private int bj;
	private int bg;
	private int ordin;
	private int volume;
	private String itemLivré;
	private int sommeq;
	
	/**
	 * initialisations a effectuer avant les actions
	 */
	private void initialisations() {
		indChauf = -1;
		indMagasin = -1;
		bj = 0;
		bg = 0;
		ordin = 0;
		volume =100;	
		itemLivré = "";
		sommeq = 0;
	} 
	

	/**
	 * execution d'une action
	 * @param numact numero de l'action a executer
	 */
	public void executer(int numAct) {
	    switch (numAct) {
	        case -1: // Action vide
	            break;
	        
	        case 1: // Associer l'identifiant du chauffeur courant
	        	indChauf = numIdCourant();
	            // Vérifie si l'indice est valide
	            if (indChauf >= 0 && indChauf < MAXCHAUF) {
	                if (tabChauf[indChauf] == null) {
	                    tabChauf[indChauf] = new Chauffeur(indChauf, 0, 0, 0, new TreeSet<>());
	                }  
	            }
	            break;
	        
	        case 2: // Mise à jour de la quantité livrée en fonction du type de vin
	            if (numIdCourant() == 0) {
	                itemLivré = "BEAUJOLAIS";
	            } else {
	                itemLivré = "ORDINAIRE";
	            }
	            break;
	        
	        case 3: // Mise à jour du Beaujolais livré
	        	if (numIdCourant() == 1) {
	                itemLivré = "BOURGOGNE";
	            } else {
	                itemLivré = "ORDINAIRE";
	            }
	            break;
	        
	        case 4: // Associer l'identifiant du magasin courant
	            indMagasin = numIdCourant();
	            break;
	        
	        case 5: // Lire la quantité à livrer
	        	int quantite = valEnt();
	        	sommeq += quantite;
                
                if (quantite == 0) {
                    erreur(NONFATALE, "Erreur : volume livré nul");
                    while (numIdCourant() != 5 || numIdCourant() != 6);
                } else if (quantite > volume) {
                    erreur(NONFATALE, "Erreur : volume livré dépasse la capacité de la citerne");
                    while (numIdCourant() != 5 || numIdCourant() != 6);
                }
                /*else if (sommeq > volume) {
                    erreur(NONFATALE, "Erreur : volume total des livraison dépasse la capacité de la citerne");
                    sommeq = 0;
                    while (numIdCourant() != 5 || numIdCourant() != 6);
                }*/
                else if(itemLivré.equals( "BEAUJOLAIS")) {
                    bj += quantite;
                } 
                else if(itemLivré.equals("BOURGOGNE")) {
                    bg += quantite;
                } 
                 else {
                    ordin += quantite;
                }
                break;

	        
	        case 6: // Stocker les informations dans tabChauf[] uniquement si la fiche est valide
	            if (indChauf >= 0 && indChauf < MAXCHAUF) {
	                tabChauf[indChauf].bj += bj;
	                tabChauf[indChauf].bg += bg;
	                tabChauf[indChauf].ordin += ordin;
	                tabChauf[indChauf].magDif.add(indMagasin);
	                // Réinitialisation après validation
	                bj = 0;
	                bg = 0;
	                ordin = 0;
	                sommeq = 0;
	            } else {
	                erreur(FATALE, "Indice de chauffeur hors limites");
	            }
	            break;
	        
	        case 7: // Incrémentation du nombre de magasins livrés
	            break;
	        
	        case 8: // Vérification et stockage du volume
	            int volumeLu = valEnt();
	            if (volumeLu >= 100 && volumeLu <= 200) {
	                volume = volumeLu;
	            } else {
	                erreur(NONFATALE, "Le volume de la citerne doit être entre 100 et 200");
	                quantite = -1; // Réinitialisation des variables intermédiaires
	                indMagasin = -1;
	                // Synchronisation après erreur : consommer jusqu'à ';'
	                while (numIdCourant() != 5 || numIdCourant() != 6);
	            }
	            break;
	        
	        case 9: // Affichage des informations des chauffeurs
	            afficherChauf();
	            break;
	        
	        default:
	            erreur(NONFATALE, "Action " + numAct + " non prévue");
	            // Synchronisation après erreur : consommer jusqu'à ';'
	            while (numIdCourant() != 5 || numIdCourant() != 6);
	            break;
	    }
	} 
	

} 
