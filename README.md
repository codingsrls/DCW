## _**Cos'è?**_

_**DCW eXpress**_ è il servizio API che permette di **consultare ed emettere scontrini** direttamente nella sezione "_Documento Commerciale online_" dell'_Agenzia delle Entrate - Fatture e Corrispettivi_.

_**DCW eXpress**_ **è un velocizzatore** perchè automatizza tutte le operazione che bisognerebbe effettuare manualmente sul sito dell'_Agenzia delle Entrate (ADE)_, come l'inserimento dei dati di accesso, la selezione dell'utenza di lavoro, la selezione della società su cui lavorare, l'accettazione delle condizioni di utilizzo, la compilazione dei dati dello scontrino e il suo invio. Tutte queste operazioni sono effettuate in pochissime chiamate API.

Per usare il servizio, basta registrarsi sul sito [https://dcw.codingictsolution.it](https://dcw.codingictsolution.it) ed ottenere la chiave API da inserire nelle chiamate di accesso.

## Come funziona?

Per poter usare le chiamate API bisogna possedere le credenziali Fisconline/Entratel rilasciate dall’Agenzia delle Entrate ([come posso ottenere le credenziali?](https://telematici.agenziaentrate.gov.it/Abilitazione/Fisconline.jsp)) e che vengono utilizzate per accedere al servizio.

<img src="https://content.pstmn.io/be9c9e8b-6419-4cb1-925e-6de1cc6a15dc/aW1hZ2UucG5n" alt="Pagina%20di%20accesso%20al%20servizio%20Fatture%20e%20Corrispettivi" width="100%">

Le credenziali dovranno essere passate come argomenti della richiesta nei seguenti campi:

- _CF_ = Codice fiscale dell'utenza ADE
    
- _PIN_ = Codice PIN dell'utenza ADE
    
- _Password_ = Password dell'utenza ADE
    
- _PIVA_ = Identificativo fiscale dell'utenza su cui si vuole operare
    
- _Tipo_ = valori ammessi "incaricato" o "mestesso"
    
- _Utenza_ = "FOL" -> accesso tramite FiscoOnLine, "ENT" -> accesso tramite Entratel
    

Il campo _PIVA_ deve possedere il valore che è riportato nella selezione dell'attività su cui si vuole operare dal sito dell'ADE

<img src="https://content.pstmn.io/eaf27b9a-cf00-40a1-b030-f717fa5bc977/U2NoZXJtYXRhIDIwMjQtMTItMjkgYWxsZSAxOS4zNC41OC5wbmc=" alt="Indicazione%20del%20valore%20da%20riportare%20nel%20campo%20PIVA" width="100%">

Di seguito un esempio di richiesta:

GET [https://dcw.codingictsolution.it:8585/login?CF=XXXXXX00X00X000X&amp;PIN=123456789&amp;Password=password1&amp;Tipo=incaricato&amp;Utenza=FOL&amp;apikey=xxxxxxxxxxxxxxx](https://dcw.codingictsolution.it:8585/login?CF=XXXXXX00X00X000X&PIN=123456789&Password=password1&Tipo=incaricato&Utenza=FOL&apikey=xxxxxxxxxxxxxxx)

Le chiamate generano risposte JSON del tipo:

{"result":true, "test":false}

## Posso fare delle prove?

Si, registrati gratuitamente sul h[ttps://dcw.codingictsolution.it](https://dcw.codingictsolution.it) per ottenere la chiave API test. Con questa chiave potrai:

- consultare gli scontrini presenti sul sito dell'ADE, ricercandoli per periodo (l'ADE limita l'arco temporale delle ricerche per max 30 giorni)
    
- effettuare un invio fittizio dello scontrino all'ADE simulando tutto il processo
    

L'API gratuita ha una limitazione temporale sulle chiamate ripetitive (tra due chiamate consecutive vi deve essere una pausa almeno di 10 secondi)
