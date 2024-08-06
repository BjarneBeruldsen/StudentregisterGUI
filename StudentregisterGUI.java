/* Oblig 2. Studentregister 
 * Programmet oppretter db studenter og legger inn data 
 * fra tekstfil "studentregister.txt".
 * Deretter får bruker opp en meny med ulike valg for 
 * endringer og visning av innhold i tabellen Student.
 * Utvikler: Bjarne Hovd Beruldsen 
 */ 
import static java.lang.System.*;
import static javax.swing.JOptionPane.*;
import static java.lang.Integer.*;
import static java.lang.Double.*; 
import static java.lang.Math.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.sql.*; 

//importerer klasser til GUI 
import javafx.application.Application; 
import javafx.stage.Stage; 
import javafx.scene.Scene; 
import javafx.scene.layout.*; 
import javafx.scene.text.*;
import javafx.scene.control.*; 
import javafx.event.*; 
import javafx.geometry.*; 
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;

public class StudentregisterGUI extends Application {
	//deklarerer stms og conn utenfor main slik at de kan brukes i alle metoder. 
	private static Connection conn = null; 
	private static Statement stmt = null; 

	//deklarerer elementer i GUI som skal gjennbrukes
	private TextField studNrFelt, navnFelt, mailFelt, 
			  		  tlfFelt, vervFelt, redigerFelt, endringFelt, 
			  		  filnavnFelt; 
	private Button leggTilKnapp, slettKnapp, redigerKnapp, 
		   		   opprettBknapp, hentBknapp, blankkUt, 
		 		   leggTilKnapp2, leggTilBlankUt, jaKnapp, 
		 		   neiKnapp, lukkKnapp, utførKnapp, angreKnapp, 
		 		   lukkKnapp2, utførKnapp2, angreKnapp2, oppdaterKnapp;

	private TextArea tekstOmråde; 
	private ComboBox<String> sorterListe, vervListe, kolonneListe;
	//tabell for valg av sortering  
	private String[] sorterValg = {"Velg sortering", "Navn A->Å", "Mobilnummer",
						   "Verv"}; 



	private String[] vervValg = {"Ingen", "Nestleder", "Kasserer", "Kodesjef", 
						  "Linjeleder", "PRansvarlig", "Styremedlem"}; 

	private String[] kolonneValg = {"Velg", "StudNr", "Navn", 
									"Epost", "Tlf", "Verv"}; 
	
	//Til logo 
	private Image usnLogo; 
	private ImageView usnLogoVis; 

	private String filnavn = "studentregister"; 

	//Stage for å legge til student 
	private Stage leggTilStage; 

	//Stage for sletting av student 
	private Stage slettStudent; 

	//Stage for redigering av student 
	private Stage redigerStudent; 


	//Stager for innlesing av backup
	private Stage innlesing; 


	

@Override 
	public void start (Stage vindu) throws FileNotFoundException {

		//Oppretter database 
		opprettDatabase(); 

		//oppretter panel 
		BorderPane panel = new BorderPane(); 
		
		//oppretter panel øverst 
		FlowPane øverst = new FlowPane(); 
		//lager mellomrom mellom elelemnter og sentrerer dem 
		øverst.setHgap(10); 
		øverst.setVgap(10); 
		øverst.setAlignment(Pos.CENTER_LEFT); 

		//legger logo øverst til venstre 
		usnLogo = new Image(new FileInputStream("USN_logo_rgb.png")); 
		usnLogoVis = new ImageView(usnLogo); 

		//Justerer størrelse på logo 
		usnLogoVis.setFitWidth(250); 
	        usnLogoVis.setFitHeight(150); 
	      	//hindrer endring av proposisjoner 
	        usnLogoVis.setPreserveRatio(true); 

		//legger element til panelet
		øverst.getChildren().addAll(usnLogoVis); 
		panel.setTop(øverst);

		//Oppretter panel til midten 
		FlowPane midten = new FlowPane(); 
		//lager mellomrom mellom elelemnter og sentrerer dem 
		
		midten.setVgap(10); 
		midten.setAlignment(Pos.CENTER); 

		//Oppretter tekstfelt for visning av studentinfo 
		tekstOmråde = new TextArea(); 
		tekstOmråde.setPrefColumnCount(65); 
		tekstOmråde.setPrefRowCount(15); 
		tekstOmråde.setEditable(false);

		midten.getChildren().addAll(tekstOmråde); 

		//legger til panel 
		panel.setCenter(midten); 


		//Oppretter panel til bunnen 
		FlowPane bunn = new FlowPane(); 
		bunn.setVgap(5); 
		bunn.setHgap(5); 
		bunn.setAlignment(Pos.CENTER); 

		//legger til nedtrekksliste for valg av sortering 
		sorterListe = new ComboBox<String>(); 
		//legger verdier til nedtrekksliste 
		sorterListe.getItems().addAll(sorterValg); 
		//setter standarvalg 
		sorterListe.getSelectionModel().selectFirst(); 
		//kaller på metode 
		sorterListe.setOnAction(e -> behandleKlikk(e)); 

		//knapp for å legge til ny student
		leggTilKnapp = new Button("Legg til student"); 
		leggTilKnapp.setOnAction(e -> behandleKlikk(e)); 

		//Felt og label for redigering/sletting av student 
		Label redigerLabel = new Label("Rediger/Slett StudNr.: "); 
		redigerFelt = new TextField(); 
		redigerFelt.setPrefColumnCount(4); 

		//Knapp for redigering og sletting 
		redigerKnapp = new Button("Rediger");
		redigerKnapp.setOnAction(e -> behandleKlikk(e));  
		slettKnapp = new Button("Slett"); 
		slettKnapp.setOnAction(e -> behandleKlikk(e)); 


		//Knapper for oppretting og henting av backup
		opprettBknapp = new Button("Opprett Backup");
		opprettBknapp.setOnAction(e -> behandleKlikk(e));
		hentBknapp = new Button("Hent Backup"); 
		hentBknapp.setOnAction(e -> behandleKlikk(e)); 

		//Knapp for oppdatering av liste 
		oppdaterKnapp = new Button("Oppdater"); 
		oppdaterKnapp.setOnAction(e -> behandleKlikk(e));



		bunn.getChildren().addAll(sorterListe, leggTilKnapp, redigerLabel,
								  redigerFelt, redigerKnapp, slettKnapp, 
								  opprettBknapp, hentBknapp, oppdaterKnapp); 

		panel.setBottom(bunn); 


		//Oppretter scene og legger til elementer til stage 
		Scene scene = new Scene(panel, 800, 400); 
		vindu.setScene(scene); 
		vindu.setTitle("Studentregister"); 
		vindu.show();
		//gjør vinduet ujusterbart 
		vindu.setResizable(false); 

		//viser usortert liste ved start av programmet. 
		visUsortert(); 
	} 

	//metode som oppretter database visst den ikke allerede eksisterer
	public void opprettDatabase() {
		String ut = "Oppretting av database vellykket!"; 
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:studenter.db"); //Oppretter database
			stmt = conn.createStatement(); //Oppretter objekt for å håndtere spørringer 
			//Oppretter tabell 
			String sql = "create table if not exists Student(StudNr integer, Navn varchar(30)," + 
												            " Epost varchar(50), Tlf integer,  Verv varchar(30));";
			stmt.executeUpdate(sql); 

			//leser inn data fra studenregister.txt
			sql = "select count(*) from Student;"; 
	    	ResultSet rs = stmt.executeQuery(sql);

	    	//leser inn data visst tabellen student er tom
	    	int antStudenter = rs.getInt(1); 
	    	
	    	if(antStudenter == 0) {
				lesInnData("studentregister"); 
			}
		} 
		catch(Exception e) {
			ut = "Feil med oppretting av database!"; 
		}
		out.println(ut); 
	
	} 

	//Metode som leser inn valg og returnerer valget til main. 
	private void behandleKlikk(ActionEvent e) {
		if(e.getSource() == sorterListe) {
			sorterListe(); 
		}
		if(e.getSource() == leggTilKnapp) {
			åpneVinduLeggTil();  
		}
		if(e.getSource() == leggTilKnapp2) {
			leggTilStudent(); 
			leggTilStage.close(); 
		}
		if(e.getSource() == leggTilBlankUt) {
			studNrFelt.setText(""); 
			navnFelt.setText(""); 
			mailFelt.setText(""); 
			tlfFelt.setText(""); 
			vervListe.getSelectionModel().selectFirst(); 
		}
		if(e.getSource() == slettKnapp) {
			åpneSlettVindu(); 
		}
		if(e.getSource() == jaKnapp) {
			slettStudent('J'); 
			slettStudent.close(); 
		}
		if(e.getSource() == neiKnapp) {
			slettStudent('N'); 
			slettStudent.close(); 
		}
		if(e.getSource() == lukkKnapp) {
			slettStudent.close(); 
		}
		if(e.getSource() == lukkKnapp2 || e.getSource() == angreKnapp) {
			redigerStudent.close(); 
		}
		if(e.getSource() == redigerKnapp) {
			åpneRedigerVindu();
		}
		if(e.getSource() == utførKnapp) {
			redigerStudent(); 
			redigerStudent.close(); 
		}
		if(e.getSource() == opprettBknapp) {
			opprettBackup(); 
		}
		if(e.getSource() == hentBknapp) {
			åpneLesInnVindu(); 
		}
		if(e.getSource() == utførKnapp2) {
			lesInnbackup(); 
			innlesing.close(); 
		}
		if(e.getSource() == angreKnapp2) {
			innlesing.close(); 
		}
		if(e.getSource() == oppdaterKnapp) {
			visUsortert();
		}
	}

	private void sorterListe() {
		//henter valg
		String valg = sorterListe.getValue(); 
		String ut = ""; 
		if(valg.equals(sorterValg[1])) {
			sorterNavn(); 
		}
		else if(valg.equals(sorterValg[2])) {
			sorterMobil(); 
		}
		else if(valg.equals(sorterValg[3])) {
			sorterVerv(); 
		}
	}

	//Metode som skriver usortert liste i tekstområde
	private void visUsortert() {
		String ut = ""; 
		try {
			String sql = "select * from Student;";
			ResultSet rs = stmt.executeQuery(sql); 
			while(rs.next()) {
				int studNr = rs.getInt("StudNr"); 
				String navn = rs.getString("Navn"); 
				String epost = rs.getString("Epost"); 
				int tlf = rs.getInt("Tlf"); 
				String verv = rs.getString("Verv"); 
				ut += (studNr+";"+navn+";"+epost+";"+tlf+";"+verv) + '\n'; 
			}
		}
		catch(Exception e) {
			ut = "Feil med vising av student liste!"; 
		}
		tekstOmråde.setText(ut); 
		sorterListe.getSelectionModel().selectFirst(); 
		redigerFelt.setText(""); 
	}


	//Metode som sorterer liste etter navn 
	private void sorterNavn() {
		String ut = ""; 
		try {	
				String sql = "select * from Student order by Navn;"; 
				ResultSet rs = stmt.executeQuery(sql); 
				ut = "Studenter sortert på navn:" + '\n';  
				while(rs.next()) {
					int studNr = rs.getInt("StudNr"); 
					String navn = rs.getString("Navn"); 
					String epost = rs.getString("Epost"); 
					int tlf = rs.getInt("Tlf"); 
					String verv = rs.getString("Verv"); 
					ut += (studNr+";"+navn+";"+epost+";"+tlf+";"+verv) + '\n'; 
			}
		}
		catch(Exception e) {
			out.println("Kunne ikke sortere!"); 
			ut = "Feil med sortering på navn!"; 

		}
			tekstOmråde.setText(ut); 
	}


	//Metode som sorterer på mobilnummer 
	private void sorterMobil() {
		String ut = ""; 
		try {
			String sql = "select * from Student where Tlf not like '0' order by Tlf;"; 
			ResultSet rs = stmt.executeQuery(sql);
			ut = "Studenter sortert på mobilnummer: " + '\n'; 
			while(rs.next()) {
				int studNr = rs.getInt("StudNr"); 
				String navn = rs.getString("Navn"); 
				String epost = rs.getString("Epost"); 
				int tlf = rs.getInt("Tlf"); 
				String verv = rs.getString("Verv"); 
				ut += (studNr+";"+navn+";"+epost+";"+tlf+";"+verv) + '\n'; 
			}

		}
		catch(Exception e) {
			ut = "Feil med sortering av mobilnummer!"; 
			out.println("Feil med sortering av mobilnummer!"); 
		}
			tekstOmråde.setText(ut); 
	}
	//Metode som sorterer på verv
	private void sorterVerv() {
		String ut = ""; 
		try {
			//sql spørring som sorterer hierarkisk etter verv
			String sql = "select * from Student where Verv not like 'ingen' "
						 + "order by case verv when 'Nestleder' then 1 "
						 + "when 'Kasserer' then 2 " 
						 + "when 'Kodesjef' then 3 "
						 + "when 'Linjeleder' then 4 "
						 + "when 'PRansvarlig' then 5 "
						 + "when 'Styremedlem' then 6 " 
						 + "else 7 end;"; 
			ResultSet rs = stmt.executeQuery(sql);
			ut = "Studenter sortert på verv: " + '\n'; 
			while(rs.next()) {
				int studNr = rs.getInt("StudNr"); 
				String navn = rs.getString("Navn"); 
				String epost = rs.getString("Epost"); 
				int tlf = rs.getInt("Tlf"); 
				String verv = rs.getString("Verv"); 
				ut += (studNr+";"+navn+";"+epost+";"+tlf+";"+verv) + '\n'; 
			}

		}
		catch(Exception e) {
			ut = "Feil med sortering på verv!"; 
			out.println(ut); 
		}
			tekstOmråde.setText(ut); 
	}


	//Åpner vindu for å legge til student
    	private void åpneVinduLeggTil() {
	 	//GUI for ny student 
	 	leggTilStage = new Stage(); 
	 	FlowPane leggTilPanel = new FlowPane(); 
	 	leggTilPanel.setVgap(5); 
	 	leggTilPanel.setHgap(5); 
	 	leggTilPanel.setAlignment(Pos.CENTER); 

	 	//Felt for studentnummer 
	 	Label studNrLabel = new Label("StudNr:");
	 	studNrFelt = new TextField(); 
	 	studNrFelt.setPrefColumnCount(6); 

	 	//Felt for Navn 
	 	Label navnLabel = new Label("Navn:"); 
	 	navnFelt = new TextField(); 
	 	navnFelt.setPrefColumnCount(14); 

	 	//Felt for mail 
	 	Label mailLabel = new Label("Mail:"); 
	 	mailFelt = new TextField(); 
	 	mailFelt.setPrefColumnCount(18); 

	 	//Felt for tlf 
	 	Label tlfLabel = new Label("Tlf:"); 
	 	tlfFelt = new TextField(); 
	 	tlfFelt.setPrefColumnCount(8);  


	 	//Nedtrekksliste for verv
	 	Label vervLabel = new Label("Verv:"); 
	 	vervListe = new ComboBox<String>(); 
	 	vervListe.getItems().addAll(vervValg);
	 	vervListe.getSelectionModel().selectFirst(); 

	 	//knapper for utføring og nullstilling 
	 	leggTilKnapp2 = new Button("Legg til"); 
	 	leggTilKnapp2.setOnAction(e -> behandleKlikk(e)); 

	 	leggTilBlankUt = new Button("Nullstill");  
	 	leggTilBlankUt.setOnAction(e -> behandleKlikk(e));

	 	//legger elementer til panel 
	 	leggTilPanel.getChildren().addAll(studNrLabel, studNrFelt, 
	 											navnLabel, navnFelt, 
	 											mailLabel, mailFelt, 
	 											tlfLabel, tlfFelt, 
	 											vervLabel, vervListe,
	 											leggTilKnapp2, leggTilBlankUt); 
	 	Scene leggTilScene = new Scene(leggTilPanel, 900, 100); 
	 	leggTilStage.setScene(leggTilScene); 
	 	leggTilStage.setTitle("Legg til student"); 
	 	leggTilStage.show(); 
	}
 	

 	public void leggTilStudent() {

 		String nyStudent = ""; 
 		String ut = ""; 

 		try {

			int studNr = parseInt(studNrFelt.getText());  
			String navn = navnFelt.getText();  
			String epost = mailFelt.getText(); 
			int tlf = parseInt(tlfFelt.getText()); 
			String verv = vervListe.getValue();      
			nyStudent = studNr+";"+navn+";"+epost+";"+tlf+";"+verv;   
		 	String sql = "insert into Student values("+studNr+", '"+navn+"', '"+epost+"', "+tlf+", '"+verv+"');"; 
		 	stmt.executeUpdate(sql); 
		 	int antStudenter = antStudenter(); 
		 	ut = "Registrering vellykket! Ny student reg.:" + nyStudent + '\n' + 
		 						   "Antall studenter: " + antStudenter; 
		 	tekstOmråde.setText(ut); 
 		} 
 		catch(Exception e) {
 				ut = "Ny student ikke registrert!";
 		}
 		tekstOmråde.setText(ut); 
 		sorterListe.getSelectionModel().selectFirst(); 
 		out.println(ut); 
 	}

 	//Åpne slett student vindu 
 	public void åpneSlettVindu() {
 		try {
 			if(!redigerFelt.getText().trim().equals("")) {
		 		int studNr = parseInt(redigerFelt.getText()); 
			 	String student = hentStudent(studNr); 

			 	//Oppretter panel 
			 	FlowPane slettPanel = new FlowPane(); 
			 	slettPanel.setHgap(10); 
			 	slettPanel.setVgap(10); 
			 	slettPanel.setAlignment(Pos.CENTER); 

			 	//Sikkerhetsjekk 
			 	Label sikkerTxt = new Label("Sikker på at du vil slette student: " + '\n' + 
			 									student + "?"); 
			 	//knapp for ja 
			 	jaKnapp = new Button("Ja"); 
			 	jaKnapp.setOnAction(e -> behandleKlikk(e)); 

			 	//knapp for nei 
			 	neiKnapp = new Button ("Avbryt"); 
			 	neiKnapp.setOnAction(e -> behandleKlikk(e)); 

			 	//Legger elementer til panel 
			 	if(!student.equals("")) {
			 		slettPanel.getChildren().addAll(sikkerTxt, jaKnapp, neiKnapp); 
			 	}
			 	else {
			 		sikkerTxt = new Label("Studenten eksisterer ikke!"); 
			 		lukkKnapp = new Button("Lukk"); 
			 		lukkKnapp.setOnAction(e -> behandleKlikk(e)); 
			 		slettPanel.getChildren().addAll(sikkerTxt, lukkKnapp); 
			 		redigerFelt.setText("");
			 	}


			 	//Oppretter vindu og scene 
			 	slettStudent = new Stage(); 
			 	Scene slettScene = new Scene(slettPanel, 600, 200); 
				slettStudent.setScene(slettScene); 
			 	slettStudent.show(); 
		 	}
			else {
				tekstOmråde.setText("Vennligst skriv inn studentnummer i tekstfelt!"); 
			}
		}
		catch(Exception e) {
			tekstOmråde.setText("Feil med sletting av student!"); 
		}
 	}

 	//metode som sletter student ved gitt studentnummer. 
 	private void slettStudent(char sikker) {
 		String ut = ""; 
 		try {
		 	int studNr = parseInt(redigerFelt.getText()); 
		 	String student = hentStudent(studNr); 
		 	String sql = "Delete from Student where StudNr = " + studNr; 

		 	if(sikker == 'J') {
		 		stmt.executeUpdate(sql);
		 		ut = "Student: " + student + ". Slettet!"; 
		 		slettStudent.close(); 
		 	}
		 	else {
		 		ut = "Student: " + student + ". Ble ikke slettet!"; 
		 		slettStudent.close(); 
	 		}
 		}
 		catch (Exception e) {
 			ut = "Feil med sletting av student!"; 
 		}
 		int antStudenter = antStudenter(); 
 		out.println(ut + " Antall studenter: " + '\n' + 
 					antStudenter); 
 		tekstOmråde.setText(ut); 
 		sorterListe.getSelectionModel().selectFirst(); 
 		redigerFelt.setText(""); 
 	}


 	//Åpner vindu for redigering av student 
 	private void åpneRedigerVindu() {
 		try {
 			if(!redigerFelt.getText().trim().equals("")) {
		 		//Panel til redigering av student 
		 		FlowPane redigerPanel = new FlowPane(); 
		 		redigerPanel.setHgap(5); 
		 		redigerPanel.setVgap(5); 
		 		redigerPanel.setAlignment(Pos.CENTER); 

		 		//Henter student info
		 		int studNr = parseInt(redigerFelt.getText()); 
			 	String student = hentStudent(studNr);


			 	//Label for kolonnevalg 
			 	Label studentLabel = new Label("Student: " + student); 
			 	Label velgKolonneLabel = new Label("Velg kolonne:"); 
			 	//nedtrekksliste for valg av kolonne 
			 	kolonneListe = new ComboBox<String>(); 
			 	kolonneListe.getItems().addAll(kolonneValg);
				kolonneListe.getSelectionModel().selectFirst(); 
			 	Label endringLabel = new Label("Endring:");
			 	//Felt for endring 
			 	endringFelt = new TextField(); 
			 	endringFelt.setPrefColumnCount(15); 


			 	//Knapper for utførelse eller angring 
			 	utførKnapp = new Button("Utfør"); 
			 	utførKnapp.setOnAction(e -> behandleKlikk(e)); 
			 	angreKnapp = new Button("Angre");  
			 	angreKnapp.setOnAction(e -> behandleKlikk(e)); 


				//Legger elementer til panel 
			 	if(!student.equals("")) {
			 		redigerPanel.getChildren().addAll(studentLabel, velgKolonneLabel, 
			 										  kolonneListe, endringLabel, 
			 										  endringFelt, utførKnapp, 
			 										  angreKnapp); 
			 	}
			 	else {
			 		studentLabel = new Label("Studenten eksisterer ikke!"); 
			 		lukkKnapp2 = new Button("Lukk"); 
			 		lukkKnapp2.setOnAction(e -> behandleKlikk(e)); 
			 		redigerPanel.getChildren().addAll(studentLabel, lukkKnapp2); 
			 		redigerFelt.setText("");
			 	}

			 	redigerStudent = new Stage(); 
			 	Scene redigerScene = new Scene(redigerPanel, 400, 100); 
			 	redigerStudent.setScene(redigerScene); 
			 	redigerStudent.setTitle("Rediger student"); 
			 	redigerStudent.show(); 
			 }
			 else {
				tekstOmråde.setText("Vennligst skriv inn studentnummer i tekstfelt!"); 
			 }
		}
		catch(Exception e) {
			tekstOmråde.setText("Feil med redigering av student!"); 
		}
	}

 	//Metode som redigerer valgt data om student. 
 	private void redigerStudent() {
 		String ut = ""; 
 		try {
	 		int studNr = parseInt(redigerFelt.getText()); 
	 		String kolonne = kolonneListe.getValue();
	 		String verdi = endringFelt.getText();  
	 		String før = hentStudent(studNr); 
	 		String sql = "update student set " + kolonne + " = '"+verdi+"' where StudNr = " + studNr; 
	 		stmt.executeUpdate(sql); 
	 		//Visst studentnummer endres 
	 		if(kolonne.equals("StudNr")) {
	 			studNr = parseInt(verdi); 
	 		}
	 		String etter = hentStudent(studNr); 
	 		ut = "Endring velykket!" + '\n' + 
	 			 "Før: " + før + '\n' + 
	 			 "Etter: " + etter; 
	 	}
	 	catch(Exception e) {
	 		ut = "Feil med redigering av student!"; 
	 	}
	 	out.println(ut); 
	 	tekstOmråde.setText(ut); 
	 	sorterListe.getSelectionModel().selectFirst();
	 	redigerFelt.setText(""); 
 	}
 	//Henter student ved hjelp av studentNr
 	private static String hentStudent(int studNr) { 
 		String ut = ""; 
 		try {
	 		ut = ""; 
	 		String sql = "select * from Student where StudNr = " + studNr; 
		 	ResultSet rs = stmt.executeQuery(sql); 
		 	studNr = rs.getInt("StudNr");
		 	String navn = rs.getString("Navn");
		 	String epost = rs.getString("Epost"); 
		 	int tlf = rs.getInt("Tlf"); 
		 	String verv = rs.getString("Verv");
		 	ut = (studNr+";"+navn+";"+epost+";"+tlf+";"+verv); 
		 }
		 catch(Exception e) {
		 	out.println("Feil med henting av student!"); 
		 }
		 return ut; 
 	}

 	//Teller Antall studenter registrert i databasen
 	private static int antStudenter() {
 		int antStudenter = 0; 
 		try {
			String sql = "select count(*) from Student;"; 
	    	ResultSet rs = stmt.executeQuery(sql);
	    	antStudenter = rs.getInt(1); 		
    	}
    	catch (Exception e){
    		showMessageDialog(null, "Feil med antStudenter!"); 
    	}
    	return antStudenter; 
 	}

 	//Oppretter backup av databse ved inkrementer nummer i filnavnet 
 	private void opprettBackup() {
		String ut = ""; 
		//henter dagens dato 
		LocalDateTime nå = LocalDateTime.now();
		// Lager format 
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
	        String dato = nå.format(formatter);

		try {	
				String sql = "select * from Student;"; 
				ResultSet rs = stmt.executeQuery(sql); 
				String filnavn = "studentregister_Backup_" + dato + ".txt"; 
				PrintWriter skriver = new PrintWriter(filnavn); 
				while(rs.next()) {
					int studNr = rs.getInt("StudNr"); 
					String navn = rs.getString("Navn"); 
					String epost = rs.getString("Epost"); 
					int tlf = rs.getInt("Tlf"); 
					String verv = rs.getString("Verv"); 
					skriver.println(studNr+";"+navn+";"+epost+";"+tlf+";"+verv); 
			}
			skriver.close(); 
			ut = "Oppretting av backup vellykket!" + '\n' + 
				 "Antall studenter: " + antStudenter() + '\n' + 
				 "Filnavn: " + filnavn; 
			out.println(ut); 
		}
		catch(Exception e) {
			showMessageDialog(null, "Feil med oppretting av backup!"); 
		} 
		tekstOmråde.setText(ut); 	
 	}

 	private void åpneLesInnVindu() {
 		//Oppretter panel 
 		FlowPane lesInnPanel = new FlowPane(); 
 		lesInnPanel.setVgap(5); 
 		lesInnPanel.setHgap(5); 
 		lesInnPanel.setAlignment(Pos.CENTER);

 		//Label for innskriving av filnavn 
 		Label filnavnLabel = new Label("Gi filnavn: (uten" +'"' +".txt" + '"'+")"); 

 		//Felt for innskrivning av filnavn ved henting av backuo
		filnavnFelt = new TextField(); 
		filnavnFelt.setPrefColumnCount(10); 

		//knapp for utførelse eller angring
		utførKnapp2 = new Button("Utfør");
		utførKnapp2.setOnAction(e -> behandleKlikk(e));  
		angreKnapp2 = new Button("Angre");
		angreKnapp2.setOnAction(e -> behandleKlikk(e)); 

		//legger elementer til panel 
		lesInnPanel.getChildren().addAll(filnavnLabel, filnavnFelt, 
										 utførKnapp2, angreKnapp2);

		innlesing = new Stage();
		Scene innlesingScene = new Scene(lesInnPanel, 250, 100); 
		innlesing.setScene(innlesingScene); 
		innlesing.setTitle("Innlesing av backup"); 
		innlesing.show(); 
 	}


 	//metode som sletter database og leser inn data fra backup tekstfil. 
 	private void lesInnbackup() {
 		String nyPerson = ""; 
 		String rad = ""; 
 		String ut = ""; 
 	    try {
		 	String filnavn = filnavnFelt.getText(); 
		 	Scanner leser = new Scanner(new File(filnavn + ".txt")); 
		    String sql = "drop table if exists Student;" + 
		    			 "create table if not exists Student(StudNr integer, Navn varchar(30)," +
		 			     " Epost varchar(50), Tlf integer,  Verv varchar(30));";
		 		stmt.executeUpdate(sql); 
 		
		 		while(leser.hasNextLine()) {
					rad = leser.nextLine(); 
					String data[] = rad.split(";"); 
					int studNr = parseInt(data[0]); 
					String navn = data[1]; 
					String epost = data[2]; 
					int tlf; 
					if(data[3].equals("#")) {
						tlf = 0; 
					}
					else {
						tlf = parseInt(data[3]); 
					}

					String verv;
					if(data[4].equals("#")) {
						verv = "Ingen"; 
					}
					else {
						verv = data[4]; 
					}
					sql = "insert into Student values("+studNr+", '"+navn+"', '"+epost+"', "+tlf+", '"+verv+"');"; 
					stmt.executeUpdate(sql); 
			}
			leser.close(); 
		 		ut = "Les inn backup vellykket!" + '\n' + 
		 					"Antall studenter: " + antStudenter(); 
		 		out.println(ut); 
 		} 
 		catch(Exception e) {
 				ut = "Feil med henting av backup!"; 
 		}
 		tekstOmråde.setText(ut);
 		sorterListe.getSelectionModel().selectFirst(); 
 	}

 	//metode som leser inn data dra tekstfil
	private static void lesInnData(String filnavn) {
		String rad = ""; 
		String sql = ""; 
		try {
			File fil = new File(filnavn + ".txt"); 
			Scanner leser = new Scanner(fil); 

			while(leser.hasNextLine()) {
				rad = leser.nextLine(); 
				String data[] = rad.split(";"); 
				int studNr = parseInt(data[0]); 
				String navn = data[1]; 
				String epost = data[2]; 
				int tlf; 
				if(data[3].equals("#")) {
					tlf = 0; 
				}
				else {
					tlf = parseInt(data[3]); 
				}

				String verv;
				if(data[4].equals("#")) {
					verv = "Ingen"; 
				}
				else {
					verv = data[4]; 
				}
				sql = "insert into Student values("+studNr+", '"+navn+"', '"+epost+"', "+tlf+", '"+verv+"');"; 
				stmt.executeUpdate(sql); 
			}
			leser.close(); 
    		int antStudenter = antStudenter();  
			System.out.println("Overføring av data fra fil vellykket!" + '\n' + 
							   "Antall studenter registrert: " + antStudenter); 
		}
		catch(Exception e){
			out.println("Kunne ikke lese inn data!"); 
		}
	}
}	
