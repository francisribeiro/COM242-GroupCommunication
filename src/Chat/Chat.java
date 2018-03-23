package Chat;

import org.jgroups.*;
import org.jgroups.protocols.TP;
import org.jgroups.util.Util;
import java.io.*;
import java.net.InetAddress;
import java.util.List;
import java.util.LinkedList;

import org.jgroups.util.Buffer;
import org.jgroups.util.ByteArrayDataOutputStream;

public class Chat extends ReceiverAdapter {

    private JChannel channel;
    private String historico;
    private File fileAnexo;
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
        channel = new JChannel("udp.xml");
        channel.setReceiver(this);
        channel.connect(grupoNome);
        grupo = ctr.getGrupo(grupoNome);
        
        File dir = new File("Anexos/"+grupoNome);
        dir.mkdir();
        
        View vi = channel.getView();
        List<Address> end = vi.getMembers();
        for (Address a : end) {
            System.out.println("Membros do grupo: " + a.toString());
        }
        channel.getState(null, 10000);
    }

    public void enviarMsg(String msgCrua) throws Exception {
        String msgCompleta = "[" + user_name + "] :" + msgCrua; //Adicionando nome do remetente à mensagem
        Message msg = new Message(null, null, msgCompleta);
        channel.send(msg);
    }

    public void enviarAnexo(File anexo) throws Exception {
        fileAnexo = anexo;
        String msgCompleta = "[" + user_name + "] : Enviando anexo :" + anexo.getName(); //Adicionando nome do remetente à mensagem
        Message msg = new Message(null, null, msgCompleta);
        channel.send(msg);
        buffer = lerArquivo(anexo);
        try {
            msg = new Message(null, buffer.getBuf(), 0, buffer.getLength());
            msg.setFlag(Message.Flag.INTERNAL);
            channel.send(msg);
            msgCompleta = "[" + user_name + "] : Anexo " + anexo.getName() + " recebido com sucesso "; //Adicionando nome do remetente à mensagem
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
        byte[] read_buf = new byte[8096];
        int bytes;
        while ((bytes = input.read(read_buf)) != -1) {
            out.write(read_buf, 0, bytes);
        }
        input.close();
        return out.getBuffer();
    }

    @Override
    public void receive(Message msg) {
        if(!msg.isFlagSet(Message.Flag.INTERNAL)) {
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
        } else {
            try {
                System.out.println("Anexos"+File.separator+grupo.getNome()+File.separator+fileName);
                File temp = new File("Anexos"+File.separator+grupo.getNome()+File.separator+fileName);
                temp.getParentFile().mkdirs();
                temp.createNewFile();
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
            historico += str + "\n";
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
