class DCWService{
	
	CF=""; //codice fiscale dell'utente ADE 
	Password=""; //password dell'utente ADE
	PIVA=""; //Identificativo fiscale su cui si sta operando sull'ADE
	PIN=""; //PIN dell'utente ADE
	Utenza=""; // incaricato o mestesso
	Tipo="FOL"; //FOL = FiscoOnLine ENT= Entratel
	apikey=""; //chiave API del servizio DCW

	/* template della struttura dello scontrino per il servizio DCW */
	data={
		"header":{
			"name":"",
			"address":"",
			"city":"",
			"zip":"",
			"country":"",
			"nation":"",
			"housenumber":"",
			"vatnumber":"",
			"taxcode":""
		},
		"receipt":{
	        "date":"",
	        "notpaid":0,
	        "discount":0,
	        "goods":[],
	        "payment_cash":0,
	        "payment_card":0
	    }
	};


	constructor(CF,Password,PIVA,PIN,Utenza,apikey){
		this.CF=CF;
		this.Password=Password;
		this.PIN=PIN;
		this.PIVA=PIVA;
		this.Utenza=Utenza;
		this.apikey=apikey;
		const now=new Date();
		this.data.receipt.date=now.getDate()+"/"+now.getMonth()+"/"+now.getFullYear();
	}

	/**
	 * Effettua l'accesso all'ADE e restiuisce la risposta in callback
	 * @param {function} callback funzione di risposta dal server
	 **/
	login(callback){
		this.send("login",[],null,callback);
	}

	/**
	 * Aggiunge i dati di intestatario dello scontrino
	 * @param {string} name Ragione sociale
	 * @param {string} address Indirizzo
	 * @param {string} housenumber Numero civico
	 * @param {string} city Comune
	 * @param {string} zip Codice postale
	 * @param {string} nation Sigla della nazione (ex IT)
	 * @param {string} vatnumber Partita IVA
	 * @param {string} taxcode Codice fiscale
	 **/
	addHeader(name,address,housenumber,city,zip,nation,vatnumber,taxcode){
		this.data.header.name=name;
		this.data.header.address=address;
		this.data.header.housenumber=housenumber;
		this.data.header.city=city;
		this.data.header.zip=zip;
		this.data.header.nation=nation;
		this.data.header.vatnumber=vatnumber;
		this.data.header.taxcode=taxcode;
			
	}

	/**
	 * Aggiunge un bene all'elenco dello scontrino
	 * @param {string} description Nome del prodotto/servizio
	 * @param {number} quantity Quantità del prodotto/servizio
	 * @param {number} priceunit Prezzo di vendita unitario al netto dell'IVA
	 * @param {number} discount ammontare netto dello sconto
	 * @param {string} tax Codice IVA (22,10,5,4,N1,N2,N3,N4,N5,N6,N7)
	 **/
	addGood(description,quantity,priceunit,discount,tax,gratis=false){

		const good={
                "description":description,
                "quantity":parseInt(quantity),
                "priceunit":priceunit,
                "discount":discount,
                "tax":tax,
                "gratis":gratis

            }

		this.data.receipt.goods.push(good);
	}

	/**
	 * Ottiene la lista dei beni presenti nello scontrino
	 **/
	getGoods(){
		return this.data.receipt.goods;
	}

	/**
	 * Inserisce il pagamento in contanti nello scontrino
	 * @param {number} amount Valore del pagamento
	 **/
	addPaymentCash(amount){
		this.data.receipt.payment_cash=amount;
	}

	/**
	 * Inserisce il pagamento elettronico nello scontrino
	 * @param {number} amount Valore del pagamento
	 **/
	addPaymentCard(amount){
		this.data.receipt.payment_card=amount;
	}

	/**
	 * Calcola il totale lordo dell'intero scontrino
	 * @return {number} totale lordo dello scontrino
	 **/
	calculateTotal(){
		var total=0;
		for(let g of this.data.receipt.goods){
			
			
			total=total+this.calculateTotalRow(g.priceunit,g.quantity,g.tax,g.discount);
		}

		return total;
	}

	/**
	 * Calcola il totale lordo di una riga dello scontrino
	 * @param {number} priceunit Prezzo di vendita unitario al netto dell'IVA
	 * @param {number} quantity Quantità del prodotto/servizio
	 * @param {string} tax Codice IVA (22,10,5,4,N1,N2,N3,N4,N5,N6,N7)
	 * @param {number} discount ammontare netto dello sconto
	 * @return {number} totale lordo del bene dello scontrino
	 **/
	calculateTotalRow(priceunit,quantity,tax,discount){
		let taxpercentual=0;
		try{
			taxpercentual=parseInt(tax);
		}catch{
			taxpercentual=0;
		}
		return priceunit*quantity*(1+taxpercentual/100)-discount*(1+taxpercentual/100);

	}

	/**
	 * Invia una richiesta POST al servizio DCW
	 * @param {string} endpoint
	 * @param {array<string>} args array di parametri da passare nella querystring
	 * @param {json} data payload del body della richiesta POST 
	 * @param {function} callback funzione di ritorno con i dati di risposta della richiesta
	 **/
	send(endpoint,args=[],data=null,callback=null){
		var querystring="";
		if(args.lenght>0) querystring=args.join("&");
		const url=`https://dcw.codingictsolution.it:8585/${endpoint}?CF=${this.CF}&PIVA=${this.PIVA}&PIN=${this.PIN}&Password=${this.Password}&Utenza=${this.Utenza}&Tipo=${this.Tipo}&apikey=${this.apikey}&${querystring}`;
		
		var xhr = new XMLHttpRequest();
		xhr.open("POST", url, true);
		xhr.setRequestHeader('Content-Type', 'application/json');
		if(data)
			xhr.send(JSON.stringify(data));
		else
			xhr.send();
		xhr.onload = function() {
		  var res = JSON.parse(this.responseText);
		  if(callback)
		  	callback(res);
		}

	}	

	/** 
	 * Invia lo scontrino corrente al servizio DCW 
	 * @param {function} callback funzione di ritorno con i dati di risposta della richiesta
	 * */
	sendReceipt(callback){
		
		 
		this.send("receipt",[],this.data,callback);

	}



}