package syntax;

import java.io.InputStream;
import java.util.TreeSet;
import lex.*;
import utils.*;

/**
* La classe ActVin met en oeuvre les actions de l'automate d'analyse syntaxique de l'application Vin
*  des fiches de livraison de vin
* 
* @author "Trinome MESSAGER_Clément_LEPORT_Etel_MOUTALABI_Habib"
* 
* janvier 2025
*/
public class ActVin extends AutoVin {

    /** table des actions */
    private final int[][] ACTION =
    {/* Etat        BJ    BG   IDENT  NBENT VIRG PTVIRG BARRE AUTRES  */
	/* 0 */      { 10,   10,    1,     10,   10,   10,   9,   10   },
	/* 1 */      {  3,    4,    5,     2,    10,   10,   10,   10   },
	/* 2 */      {  3,    4,    5,     10,   10,   10,   10,   10  },
	/* 3 */      { 10,   10,    5,     10,   10,   10,   10,   10   },
	/* 4 */      { 10,   10,   10,     6,   10,   10,   10,   10   },
	/* 5 */      { 10,   10,   5,     10,   7,    8,   10,   10   },
	/* 6 */      { -1,   -1,   -1,    -1,   -1,   10,   9,   -1   },
	
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
		String titre = "CHAUFFEUR                   BJ        BG       ORD     NBMAG     CUMUL\n"
				+ "---------                   --        --       ---     -----     ------";
		Ecriture.ecrireStringln(titre);
		for (int i = 0; i <= indChauf; i++) {
			String idChaufCourant = ((LexVin)analyseurLexical).chaineIdent(tabChauf[i].numChauf);
			Ecriture.ecrireString(chaineCadrageGauche(idChaufCourant));
			Ecriture.ecrireInt(tabChauf[i].bj, 10);
			Ecriture.ecrireInt(tabChauf[i].bg, 10);
			Ecriture.ecrireInt(tabChauf[i].ordin, 10);
			Ecriture.ecrireInt(tabChauf[i].magDif.size(), 10);
			Ecriture.ecrireInt(tabChauf[i].ordin+tabChauf[i].bg+tabChauf[i].bj, 10);

			Ecriture.ecrireStringln("");
		}
	} 
	
	
	/** indice courant du nombre de chauffeurs dans le tableau tabChauf */
	private int indChauf ;
	
	private Chauffeur chaufActuel;
	private int volumeActuel;
	private int qualiteActuel;
	// 0 : BEAUJOLAIS
	// 1 : BOURGOGNE
	// 2 : ORDINAIRE
	
	private int volCiterne;
	private int nbFicheActuel;
	private int nbDeFicheCorrecte = 0 ; 
	

	
	/**
	 * initialisations a effectuer avant les actions
	 */
	private void initialisations() {
		indChauf = -1;
		chaufActuel = null;
		qualiteActuel = 2;
		nbFicheActuel = 0;
		volCiterne = 100;
	} 
	
	public void executer(int numAct) {

		switch (numAct) {
		case -1:	// action vide
			if(etatCourant == etatErreur) {
				break;
			}
			break;
		case 1: // Lecture du chauffeur
			nbFicheActuel++;
			
			boolean trouve = false;
			for( int i = 0; i<= indChauf ; i++) {
				Chauffeur chauf = tabChauf[i];
				if(chauf.numChauf == numIdCourant()) {
					chaufActuel = chauf;
					trouve = true;
					break;
				}
			}
			
			if(!trouve && indChauf < MAXCHAUF) {
				Chauffeur chauf = new Chauffeur(numIdCourant(),0,0,0, new TreeSet<Integer>());
				tabChauf[indChauf + 1] = chauf;
				chaufActuel = chauf;
				indChauf++;
			}
			break;
			
		case 2: // Lecture du Volume de la citerne
			volCiterne = valEnt();
			break;
		
		case 3: // Qualité = BEAUJOLAIS
			qualiteActuel = 0;
			break;
		
		case 4: //Qualité = BOURGOGNE
			qualiteActuel = 1;
			break;
		
		case 5: // Lecture du  magasin
			if (!chaufActuel.magDif.contains(numIdCourant())) {
		        chaufActuel.magDif.add(numIdCourant());
		    }
		    break;
		    
		case 6: // Lecture de la quantité
			volumeActuel = chaufActuel.bj + chaufActuel.bg + chaufActuel.ordin + valEnt();
			if(volumeActuel <= volCiterne) {
				switch(qualiteActuel) {
					case 0 :
						chaufActuel.bj += valEnt();
					break;
					case 1 :
						chaufActuel.bg += valEnt();
						break;
					case 2 :
						chaufActuel.ordin += valEnt();
						break;
			
					default :
				break;
				}
			}else {
				erreur(NONFATALE,"Le volume de la citerne va être dépasser");
				while(numAct != 8 || numAct != 9);
			}
		break;
			
		case 7: // Lecture de la ','
			qualiteActuel = 2;
			break;
		
		case 8: // Lecture du ';'
			Ecriture.ecrireStringln("Fin de la fiche " + nbFicheActuel);
			afficherChauf();
			chaufActuel = null;
			qualiteActuel = 2;
			nbDeFicheCorrecte++;
			volCiterne = 100;
			volumeActuel = 0;
			break;
			
		case 9: // Lecture de la '/'
			Ecriture.ecrireStringln("Fin de l'analyse ");
			Chauffeur chaufMax = new Chauffeur(numIdCourant(),0,0,0, new TreeSet<Integer>());
			String idChaufCourant = "";
			for (int i = 0; i <= indChauf; i++) {
				if(tabChauf[i].magDif.size()> chaufMax.magDif.size()) {
					System.out.println(tabChauf[i]);
					chaufMax = tabChauf[i];
					idChaufCourant = ((LexVin)analyseurLexical).chaineIdent(chaufMax.numChauf);
				}
			}			
			break;
			
		default:
			erreur(NONFATALE,"action " + numAct + " non prevue");
			while(numAct != 8 || numAct != 9);
			
		}
	} 

} 