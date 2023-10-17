package org.mario.btle_1;

public class ObjetoDeDosEnteros {
    private int ID;
    private int VALOR;

    public ObjetoDeDosEnteros(int id, int valor) {
        this.ID = id;
        this.VALOR = valor;
    }

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public int getVALOR() {
        return VALOR;
    }

    public void setVALOR(int valor) {
        this.VALOR = valor;
    }

    @Override
    public String toString() {
        return "ObjetoDeDosEnteros[ID=" + ID + ", VALOR=" + VALOR + "]";
    }
}

