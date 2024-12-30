package com.codingitcsolutions.dcwsample

import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.internal.toLongOrDefault
import org.json.JSONObject
import java.io.IOException
import java.util.*


data class HeaderReceipt(
    /** Ragione sociale*/
    var name:String="",
    /** Indirizzo*/
    var address:String="",
    /** Comune*/
    var city:String="",
    /** Codice postale*/
    var zip:String="",
    /** Sigla Provincia*/
    var country:String="",
    /** Sigla nazione*/
    var nation:String="",
    /** Numero civico*/
    var housenumber:String="",
    /** Partita IVA*/
    var vatnumber:String="",
    /** Codice Fiscale*/
    var taxcode:String=""
)

data class BodyReceipt(
    /** Data dello scontrino in formato DD/MM/YYYY*/
    var date:String="",
    /** Ammontare non pagato*/
    var notpaid:Float=0f,
    /** Ammontare dello sconto*/
    var discount:Number=0f,
    /** Beni dello scontrino*/
    var goods: ArrayList<Good> = ArrayList<Good>(),
    /** Ammontare del pagamento effettuato in contanti*/
    var payment_cash:Float=0f,
    /** Ammontare del pagamento effettuato con carte di pagamento*/
    var payment_card:Float=0f
)

data class Good(
    /** Descrizione del bene o del servizio*/
    var description:String="",
    /** Quantità del bene o del servizio*/
    var quantity:Float=0f,
    /** Prezzo unitario al netto dell'IVA*/
    var priceunit:Float=0f,
    /** Percentuale di sconto*/
    var discount:Float=0f,
    /** Codice IVA (22,10,5,4,N1,N2,N3,N4,N5,N6,N7) */
    var tax:String="",
    /** Omaggio*/
    var gratis:Boolean=false
)

data class Receipt(
    /** Dati intestatario dello scontrino*/
    var header:HeaderReceipt= HeaderReceipt(),
    /** Dettaglio dello scontrino*/
    var receipt:BodyReceipt= BodyReceipt()
)


class DCWService {
    private val client = OkHttpClient()
    val JSON = "application/json; charset=utf-8".toMediaType()

    /**codice fiscale dell'utente ADE*/
    var CF="";
    /**password dell'utente ADE*/
    var Password="";
    /**Identificativo fiscale su cui si sta operando sull'ADE*/
    var PIVA="";
    /**PIN dell'utente ADE*/
    var PIN="";
    /**incaricato o mestesso*/
    var Utenza="";
    /**FOL = FiscoOnLine ENT= Entratel*/
    var Tipo="FOL";
    /**chiave API del servizio DCW*/
    var apikey="";

    /**Scontrino corrente*/
    lateinit var data:Receipt


    constructor(CF:String="",Password:String="",PIN:String="",PIVA:String="",Utenza:String="",apikey:String=""){
        this.CF=CF;
        this.Password=Password;
        this.PIN=PIN;
        this.PIVA=PIVA;
        this.Utenza=Utenza;
        this.apikey=apikey;

    }

    /**
     * Crea un nuovo scontrino
     */
    fun newReceipt(){
        val now= Date();
        this.data= Receipt();
        this.data.receipt.date=now.day.toString()+"/"+now.month.toString()+"/"+now.year.toString();
    }

    /**
     * Aggiunge i dati di intestatario dello scontrino
     * @param name Ragione sociale
     * @param address Indirizzo
     * @param housenumber Numero civico
     * @param city Comune
     * @param zip Codice postale
     * @param nation Sigla della nazione (ex IT)
     * @param vatnumber Partita IVA
     * @param taxcode Codice fiscale
     **/
    fun addHeader(name:String,address:String,housenumber:String,city:String,zip:String,nation:String,vatnumber:String,taxcode:String){
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
     * @param description Nome del prodotto/servizio
     * @param quantity Quantità del prodotto/servizio
     * @param priceunit Prezzo di vendita unitario al netto dell'IVA
     * @param discount Percentuale di sconto
     * @param tax Codice IVA (22,10,5,4,N1,N2,N3,N4,N5,N6,N7)
     **/
    fun addGood(description:String,quantity:Float,priceunit:Float,discount:Float,tax:String) {
        var good = Good(description, quantity, priceunit, discount, tax);
        this.data.receipt.goods.add(good);
    }


    /**
     * Ottiene la lista dei beni presenti nello scontrino
     */
    fun getGoods(): ArrayList<Good> {
        return this.data.receipt.goods
    }

    /**
     * Inserisce il pagamento in contanti nello scontrino
     * @param amount Valore del pagamento
     **/
    fun addPaymentCash(amount:Float){
        this.data.receipt.payment_cash=amount;
    }

    /**
     * Inserisce il pagamento elettronico nello scontrino
     * @param amount Valore del pagamento
     **/
    fun addPaymentCard(amount:Float){
        this.data.receipt.payment_card=amount;
    }

    /**
     * Calcola il totale lordo dell'intero scontrino
     * @return totale lordo dello scontrino
     **/
    fun calculateTotal():Float{
        var total=0f;
        for(g in this.data.receipt.goods){

            total=total.plus(this.calculateTotalRow(g.priceunit,g.quantity,g.tax));
        }

        return total;
    }

    /**
     * Calcola il totale lordo di una riga dello scontrino
     * @param priceunit Prezzo di vendita unitario al netto dell'IVA
     * @param quantity Quantità del prodotto/servizio
     * @param tax Codice IVA (22,10,5,4,N1,N2,N3,N4,N5,N6,N7)
     * @return totale lordo del bene dello scontrino
     **/
    fun calculateTotalRow(priceunit:Float,quantity:Float,tax:String):Float{
        var taxpercentual=0;
        try{
            taxpercentual= tax.toLongOrDefault(0).toInt()
        }catch(e:java.lang.Exception){
            taxpercentual=0;
        }


        return priceunit*quantity*(1+taxpercentual/100);

    }


    /**
     * Invia una richiesta POST al servizio DCW
     * @param endpoint
     * @param args array di parametri da passare nella querystring
     * @param data payload del body della richiesta POST
     * @param callback funzione di ritorno con i dati di risposta della richiesta
     **/
    fun send(endpoint:String, args:ArrayList<String>?, data: String="", callback:(result:JSONObject)->Unit){
        var querystring="";
        //if(args.size>0) querystring=args.join("&");
        val url="http://dcw.codingictsolution.it:8585/${endpoint}?CF=${this.CF}&PIVA=${this.PIVA}&PIN=${this.PIN}&Password=${this.Password}&Utenza=${this.Utenza}&Tipo=${this.Tipo}&apikey=${this.apikey}&${querystring}";
        var request: Request? =null
        if(data!="") {
            var payload: RequestBody = RequestBody.create(JSON, data);
            request=Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(payload)
                .build()
        }else{
            request=Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .build()
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                if (!response.isSuccessful) throw IOException("Unexpected code $response")


                val jsonData: String = response.body?.string() ?: ""
                val Jobject = JSONObject(jsonData)


                callback(Jobject);
            }

        })

    }

    /**
     * Invia lo scontrino corrente al servizio DCW
     * @param callback funzione di ritorno con i dati di risposta della richiesta
     * */
    fun sendReceipt(callback:(result:JSONObject)->Unit){

        val jsonString = Gson().toJson(this.data)

        this.send("receipt",null,jsonString,callback);

    }


    /**
     * Effettua l'accesso all'ADE e restiuisce la risposta in callback
     * @param callback funzione di risposta dal server
     **/
    fun login(callback:(result:JSONObject)->Unit){
        this.send("login",null,"",callback);
    }


}