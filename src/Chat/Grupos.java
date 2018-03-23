/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.Serializable;
import java.util.ArrayList;
import org.jgroups.Address;

/**
 *
 * @author vinic
 */
public class Grupos implements Serializable{

    private final String nome;
    private final ArrayList<String> membros;
    private final ArrayList<Address> address;
    private final String senha;

    public Grupos(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
        this.membros = new ArrayList<>();
        this.address = new ArrayList<>();
    }
    
//    public void addMembros(Address address, String nome) {
//        membros.add(nome);
//        this.address.add(address);
//    }
    
//    public void remover(Address address, String name) {
//        for(int i = 0; i < address.size(); i++) {
//            if(this.address.get(i).equals(address)) {
//                this.address.remove(i);
//            }
//        }
//        for(int i = 0; i < membros.size(); i++) {
//            if(this.membros.get(i).equals(name)) {
//                this.membros.remove(i);
//            }
//        }
//    }

    public ArrayList<Address> getAddress() {
        return address;
    }

    public ArrayList<String> getMembros() {
        return membros;
    }

    public String getNome() {
        return nome;
    }

    public String getSenha() {
        return senha;
    }
}
