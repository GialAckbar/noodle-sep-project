package client;

import shared.KategorieDatei;
import shared.LVKategorie;
import shared.LVKategorieElement;
import shared.Lehrveranstaltung;

public class Response<T>  {

    public final int statusCode;

    public final T element;

    public Response(int statusCode, T element){
        this.statusCode = statusCode;
        this.element = element;
    }

    public Response(int statusCode){
        this.statusCode = statusCode;
        this.element = null;
    }

    public int getStatusCode() {
        return statusCode;
    }
    public T getElement(){
        return element;
    }

}
