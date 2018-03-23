package Chat;

import java.io.Serializable;
import java.util.ArrayList;
import org.jgroups.Address;

public class Grupos implements Serializable{

    private final String nome;
    private final String senha;

    public Grupos(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public String getSenha() {
        return senha;
    }
}
