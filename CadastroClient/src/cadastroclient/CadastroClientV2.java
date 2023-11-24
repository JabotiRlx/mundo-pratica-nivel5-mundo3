package cadastroclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Scanner;

public class CadastroClientV2 {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket = new Socket("localhost", 4321);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Scanner scan = new Scanner(System.in);
 
        System.out.println("------------------------------");
        System.out.print("Login: ");
        String login = reader.readLine();
        System.out.print("Senha: ");
        String senha = reader.readLine();

        out.writeObject(login);
        out.writeObject(senha);
        out.flush();

        ThreadClient threadClient = new ThreadClient(in);
        threadClient.start();
		
         while(true)   {
            System.out.print("L – Listar |  X – Finalizar | E– Entrada | S – Saida ");
            String comando = reader.readLine();    
            out.writeObject(comando);
            out.flush();
            
            switch( comando.toUpperCase() ) {
                case "E","S" ->{
                    System.out.print("ID da Pessoa:");
                    int idPessoa = scan.nextInt();

                    System.out.print("Id do Produto:");
                    int idProduto = scan.nextInt();

                    System.out.print("Quantidade:");
                    int quantidade = scan.nextInt();

                    System.out.print("Valor Unitario:");
                    long valorUnitario = scan.nextLong();

                    out.writeObject(idPessoa);
                    out.writeObject(idProduto);
                    out.writeObject(quantidade);
                    out.writeObject(valorUnitario);
                }
                case "X" -> {
                    System.out.print("PROGRAMA FINALIZADO!");
                    socket.close();
                    System.exit(0);
                }
            }
         }    

    }

}
