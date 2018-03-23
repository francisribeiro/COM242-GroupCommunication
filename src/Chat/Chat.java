package Chat;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;
import java.io.*;
import java.net.InetAddress;
import java.util.List;
import java.util.LinkedList;
import org.jgroups.Address;
import org.jgroups.util.Buffer;
import org.jgroups.util.ByteArrayDataOutputStream;

public class Chat extends ReceiverAdapter {

    private JChannel channel;
    private String historico;
    private String fileName;
    private Buffer buffer;
    private Grupos grupo;
    private final Controller ctr;
    private final String user_name = System.getProperty("user.name", "n/a");
    private final List<String> state = new LinkedList<>();

    public Chat(Controller ctr) {
        this.ctr = ctr;
        this.historico = "";
    }

    public void start(String grupoNome) throws Exception {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        String localhost = InetAddress.getLocalHost().toString().split("/", 2)[1];
        System.setProperty("jgroups.bind_addr" , localhost);
        channel = new JChannel("tcp.xml");
        channel.setReceiver(this);
        channel.connect(grupoNome);
        grupo = ctr.getGrupos(grupoNome);
        
        File dir = new File("Anexos/"+grupoNome);
        dir.mkdir();
        
        View vi = channel.getView();
        List<Address> end = vi.getMembers();
        for (int i = 0; i < end.size(); i++) {
            //grupo.addMembros(end.get(i), channel.getAddressAsString());
        }
        for (String g : grupo.getMembros()) {
            System.out.println("Membros do grupo: " + g);
        }
        channel.getState(null, 10000);
    }

    public void enviarMsg(String msgCrua) throws Exception {
        String msgCompleta = "[" + user_name + "] :" + msgCrua + "\n"; //Adicionando nome do remetente à mensagem
        Message msg = new Message(null, null, msgCompleta);
        channel.send(msg);
    }

    public void enviarAnexo(File anexo) throws Exception {
        fileName = anexo.getName();
        String msgCompleta = "[" + user_name + "] : Enviando anexo :" + anexo.getName() + "\n"; //Adicionando nome do remetente à mensagem
        Message msg = new Message(null, null, msgCompleta);
        channel.send(msg);
        buffer = lerArquivo(anexo);
        try {
            msg = new Message(null, buffer.getBuf(), 0, buffer.getLength());
            channel.send(msg);
            msgCompleta = "[" + user_name + "] : Anexo " + anexo.getName() + " recebido com sucesso\n"; //Adicionando nome do remetente à mensagem
            msg = new Message(null, null, msgCompleta);
            channel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Buffer lerArquivo(File anexo) throws Exception {
        int size = (int) anexo.length();
        FileInputStream input = new FileInputStream(anexo);
        ByteArrayDataOutputStream out = new ByteArrayDataOutputStream(size);
        byte[] read_buf = new byte[1024];
        int bytes;
        while ((bytes = input.read(read_buf)) != -1) {
            out.write(read_buf, 0, bytes);
        }
        input.close();
        return out.getBuffer();
    }

    @Override
    public void receive(Message msg) {
        try {
            String mensagem = msg.getSrc() + ": " + msg.getObject();
            String[] nomeArquivo = mensagem.split(":");
            if (nomeArquivo.length == 4) {
                fileName = nomeArquivo[3];
            }
            historico += mensagem + "\n";
            ctr.getTextArea().appendText(mensagem + "\n");

            synchronized (state) {
                state.add(mensagem);
            }
        } catch (Exception ex) {
            try {
                System.out.println(grupo.getNome()+"/"+fileName);
                File temp = new File(fileName);
                FileOutputStream out = new FileOutputStream(temp);
                out.write(msg.getBuffer(), msg.getOffset(), msg.getLength());
                out.close();
            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }

    //Função chamada quando uma nova pessoa entra no grupo
    @Override
    public void viewAccepted(View new_view) {
        System.out.println("Membros: " + new_view);
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setState(InputStream input) throws Exception {
        List<String> list = (List<String>) Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println("received state (" + list.size() + " messages in chat history):");
        for (String str : list) {
            System.out.println(str);
        }
        ctr.getTextArea().appendText(historico);
    }

    public void desconectar() {
        View vi = channel.getView();
        if (vi.size() == 1) {
            channel.disconnect();
            fechaCanal();
        } else {
            channel.disconnect();
        }
    }

    public void fechaCanal() {
        if (channel.isOpen()) {
            state.clear();
            historico = "";
            channel.close();
        }
    }

    public JChannel getChannel() {
        return channel;
    }

    public String getHistorico() {
        return historico;
    }
}
