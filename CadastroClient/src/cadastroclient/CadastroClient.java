package cadastroclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import model.Produto;

public class CadastroClient {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", 4321);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("------------------------------");
        System.out.print("Login: ");
        String login = reader.readLine();
        System.out.print("Senha: ");
        String senha = reader.readLine();

        out.writeObject(login);
        out.writeObject(senha);
        out.flush();

        String mensagem = (String) in.readObject();
        System.out.println("mensagem recebida do servidor=" + mensagem);

        if( "NAO AUTORIZADO".equals(mensagem) ) return;
                
        System.out.print("Comando desejado: ");
        String comando = reader.readLine();    
        out.writeObject(comando);
        out.flush();

        List<Produto> listaProdutos = (List<Produto>) in.readObject();
        
        listaProdutos.stream().forEach( produto -> System.out.println( produto.getNome() ));

        socket.close();
    }

}
